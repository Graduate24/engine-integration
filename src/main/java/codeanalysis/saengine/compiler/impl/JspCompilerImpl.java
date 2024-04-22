package codeanalysis.saengine.compiler.impl;

import codeanalysis.saengine.compiler.JspCompiler;
import codeanalysis.saengine.config.CompilerConfig;
import codeanalysis.saengine.entity.InputStreamRunnable;
import codeanalysis.saengine.util.FileUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Ran Zhang
 * @since 2024/4/13
 */
@Service
@Slf4j
public class JspCompilerImpl implements JspCompiler {

    @Autowired
    private CompilerConfig config;

    @Override
    public void compile(String uriRoot, String outputDir, boolean smapSuppressed) {
        JspC jspc = new JspC();
        jspc.setValidateXml(false);
        jspc.setUriroot(uriRoot);
        jspc.setOutputDir(outputDir);
        jspc.setCompile(true);
        jspc.setSmapDumped(false);
        jspc.setSmapSuppressed(smapSuppressed);
        try {
            jspc.execute();
        } catch (JasperException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<String> preCompile(String tempdir, String url, String filename) {
        try {
            // download war file
            File target = FileUtility.download(tempdir, url, filename);
            // extract
            String extractDest = tempdir + File.separator + FilenameUtils.removeExtension(filename);
            log.info("extract dest:{}", extractDest);
            FileUtility.extractJar(target.getPath(), extractDest);
            // compile
            // String outputDir = extractDest + File.separator + "WEB-INF" + File.separator + "classes";
            compileExec(extractDest, extractDest);
            //compile(extractDest, extractDest);
            // filter _jsp.class
            List<String> jspClass = FileUtility.filterFile(extractDest, new String[]{"**/*_jsp.class"});
            log.info("pre compile finished. found :{} Jsps", jspClass.size());
            return jspClass;
        } catch (IOException e) {
            log.error("pre compile fail.", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void compileExec(String uriRoot, String outputDir) {
        String command = "java -jar {0} -i {1} -o {2} -c jspc";
        String cmd = MessageFormat.format(command, config.getExecpath(), uriRoot, outputDir);
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            new InputStreamRunnable(process.getErrorStream(), "Error").start();
            new InputStreamRunnable(process.getInputStream(), "Info").start();
            process.waitFor(10, TimeUnit.MINUTES);
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
