package codeanalysis.saengine.mq;

import codeanalysis.saengine.compiler.JspCompiler;
import codeanalysis.saengine.config.CompilerConfig;
import codeanalysis.saengine.engine.JspcEngine;
import codeanalysis.saengine.entity.CompileResult;
import codeanalysis.saengine.entity.SaRequest;
import codeanalysis.saengine.entity.SaResponse;
import codeanalysis.saengine.file.FileService;
import codeanalysis.saengine.util.FileUtility;
import com.geneea.celery.Celery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ran Zhang
 * @since 2024/6/7
 */
@RabbitListener(queues = "sa_compile")
@Component
@Slf4j
public class CompileTask extends MessageReceiver {

    @Autowired
    private CompilerConfig config;

    private JspcEngine jspcEngine = new JspcEngine();

    @Qualifier("compileCeleryClient")
    @Autowired
    private Celery celery;

    @Autowired
    private FileService fileService;

    @Override
    public SaResponse handle(SaRequest request) {
        SaResponse response = new SaResponse();
        try {
            log.info("sa_compile queue handler start...");
            String tempdir = FileUtility.createTempdir();
            String project = prepareProject(tempdir, request.getDirectory(), request.getFilename());
            JspcEngine.ExecuteParams params = new JspcEngine.ExecuteParams();
            params.setJavaPath("java");
            params.setEnginePath(config.getExecpath());
            params.setProject(project);
            params.setOutput(project);
            jspcEngine.execute(params);
            // archive
            String name = FilenameUtils.removeExtension(request.getFilename());
            String destPath = project + ".zip";
            if (new File(destPath).exists()) {
                FileUtility.deleteFile(destPath);
            }
            FileUtility.zip(project, destPath, null);
            String md5 = FileUtility.md5(destPath);
            long size = FileUtility.size(destPath);
            String objectKey = FileUtility.objectKey(md5, name + ".zip");
            fileService.put(objectKey, destPath);
            FileUtility.deteleTempdir(tempdir);
            log.info("sa_compile queue handler end...");
            response.setWorkflowId(request.getWorkflowId());
            CompileResult cr = new CompileResult();
            cr.setName(name + ".zip");
            cr.setMd5(md5);
            cr.setObjectKey(objectKey);
            cr.setSize(size);
            response.setCompileResult(cr);
            response.setSuccess(true);
        } catch (Exception e) {
            response.setSuccess(false);
            e.printStackTrace();
        }
        return response;

    }

    private String prepareProject(String tempdir, String url, String filename) throws IOException {
        // download war file
        File target = FileUtility.download(tempdir, url, filename);
        // extract
        String extractDest = tempdir + File.separator + FilenameUtils.removeExtension(filename);
        log.info("extract dest:{}", extractDest);
        FileUtility.extractJar(target.getPath(), extractDest);
        return extractDest;

    }

    @Override
    public void postHandle(SaResponse response) {
        sendCeleryMq(celery, "compile_finish", response);
    }


}
