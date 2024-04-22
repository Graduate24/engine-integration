package codeanalysis.saengine;

import codeanalysis.saengine.entity.Rule;
import codeanalysis.saengine.entity.SaRequest;
import codeanalysis.saengine.entity.SaResponse;
import codeanalysis.saengine.mq.AnalysisTask;
import codeanalysis.saengine.service.SaService;
import codeanalysis.saengine.util.FileUtility;
import com.google.common.io.Files;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Ran Zhang
 * @since 2024/4/18
 */
public class CompilerTest {


    /**
     * SMAP
     * welcome_jsp.java
     * JSP
     * *S JSP
     * *F
     * + 0 welcome.jsp
     * welcome.jsp
     * *L
     * 1,14:140,0
     * 15,25:142
     * 39,3:167,0
     * 41:168
     * 42:169,0
     * 42:170
     * 43:199,14
     * 44:213,0
     * 44:238,8
     * 44:216,0
     * 45,8:174,0
     * *E
     */
    @Test
    public void test() throws JasperException {
        JspC jspc = new JspC();
			/*String[] arg0 = {"-uriroot", "E:/jspc", "-d", "d:/t",
					"temp/temp.jsp" };
			jspc.setArgs(arg0);*/
        jspc.setUriroot("/home/ran/download/jsp-demo");//web application root directory
        jspc.setOutputDir("/home/ran/download/jsp-demo");//.java file and .class file output directory
        //jspc.setJspFiles("welcome.jsp");//jsp to compile
        jspc.setCompile(true);// Whether to compile false or not specified, only generate .java files
        jspc.setSmapDumped(true);
        jspc.setValidateXml(false);
        jspc.setSmapSuppressed(false);
        jspc.execute();

    }

    @Test
    public void testTempdir() {
        String tmpdir = Files.createTempDir().getAbsolutePath();
        String tmpDirsLocation = System.getProperty("java.io.tmpdir");
        System.out.println(tmpdir);
        System.out.println(tmpDirsLocation);

    }

    @Test
    public void testDownload() throws IOException {
        String tmpdir = Files.createTempDir().getAbsolutePath();
        System.out.println(tmpdir);
        String url = "{source:http://82.156.234.100:9010/codeanalysis/1620893032270/1351/jsp-demo.war?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minio%2F20210513%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20210513T080352Z&X-Amz-Expires=86400&X-Amz-SignedHeaders=host&X-Amz-Signature=029c60ca2101286666762f4c12f668afe9bd9f1d4c5909b07672043581794911";
        FileUtils.copyURLToFile(new URL(url), new File(tmpdir, "jsp-demo.war"));
    }

    @Test
    public void testUnzip() throws IOException {
        FileUtility.extractJar("/tmp/1620899963743-0/jsp-demo.war", "/tmp/1620899963743-0/");
    }

    @Test
    public void testFilter() throws IOException {
        List<String> result = FileUtility.filterFile("/tmp/1620899963743-0", new String[]{"**/*_jsp.java"});
        System.out.println(result);
        for (String path : result) {
            File fileToDelete = FileUtils.getFile("/tmp/1620899963743-0" + File.separator + path);
            FileUtils.deleteQuietly(fileToDelete);
        }


    }

    @Test
    public void testGson() {
        String json = "{\"source\":\"http://127.0.0.1:9010/codeanalysis/1621503387678/4578/jsp-demo.war?X-Amz-Algorithm\\u003dAWS4-HMAC-SHA256\\u0026X-Amz-Credential\\u003dminio%2F20210520%2Fus-east-1%2Fs3%2Faws4_request\\u0026X-Amz-Date\\u003d20210520T093627Z\\u0026X-Amz-Expires\\u003d86400\\u0026X-Amz-SignedHeaders\\u003dhost\\u0026X-Amz-Signature\\u003d10c946656680fac36adaff08903898bc4c2ad7e42c4877f56595a2a23db0879e\",\"filename\":\"jsp-demo.war\",\"rules\":{\"id\":\"rule1\",\"source\":[\"javax.servlet.http.HttpServletRequest: java.lang.String getParameter(java.lang.String)\"],\"sink\":[\"java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])\"]}}";
        Gson gson = new Gson();
        SaRequest saRequest = gson.fromJson(json, SaRequest.class);
        System.out.println(saRequest);

    }

    @Test
    public void testTOJson() {
        List<Rule> rules = new ArrayList<>();
        Rule r1 = new Rule();
        r1.setSink(Arrays.asList("sink1", "sink2", "sink3"));
        r1.setSource(Arrays.asList("source1", "source2", "source3"));
        Rule r2 = new Rule();
        r2.setSink(Arrays.asList("sink3", "sink4", "sink5"));
        r2.setSource(Arrays.asList("source3", "source4", "source5"));

        rules.add(r1);
        rules.add(r2);
        SaRequest request = new SaRequest();
        request.setRules(rules);
        request.setDirectory("asdf");
        request.setFilename("fadfa");
        Gson gson = new Gson();
        System.out.println(gson.toJson(request));
    }

    @Test
    public void testExec() throws IOException, InterruptedException {
        String command = "java -jar /work/codeanalysis/thu/sa-compile/target/sa-compile.jar -i " +
                "/home/ran/download/jsp-demo -o /home/ran/download -c jspc,soot";
        Process child = Runtime.getRuntime().exec(command);
        System.out.println(111);
        // Get output stream to write from it
        OutputStream out = child.getOutputStream();

        out.flush();
        out.close();
    }

    @Test
    public void test3() throws IOException {
        // extract
        String tempdir = "/work/codeanalysis/thu/sa-engine/sootOutput";
        String filename = "jsp-demo.zip";
        File target = new File(tempdir, filename);
        String extractDest = tempdir + File.separator + FilenameUtils.removeExtension(filename);
        FileUtility.extractJar(target.getPath(), extractDest);
        // compile
        //compileExec(extractDest, extractDest);
    }

    @Test
    public void test4() {
        String extractDest = "/work/codeanalysis/thu/sa-engine/sootOutput/jsp-demo";
        compileExec(extractDest, extractDest);
    }

    @Test
    public void test5() {
        String tempdir = "/work/codeanalysis/thu/sa-engine/sootOutput";
        String name = FilenameUtils.removeExtension("jsp-demo.zip");
        String destPath = tempdir + File.separator + name + ".zip";
        if (new File(destPath).exists()) {
            FileUtility.deleteFile(destPath);
        }
        FileUtility.deleteFile(destPath);
        FileUtility.zip(tempdir + File.separator + name, destPath, null);
    }

    public void compileExec(String uriRoot, String outputDir) {
        String command = "java -jar {0} -i {1} -o {2} -c jspc,jimple";
        String cmd = MessageFormat.format(command, "/work/codeanalysis/thu/sa-compile/target/sa-compile.jar", uriRoot, outputDir);
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

    static class InputStreamRunnable extends Thread {
        BufferedReader bReader = null;
        String type = null;

        public InputStreamRunnable(InputStream is, String _type) {
            try {
                bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), StandardCharsets.UTF_8));
                type = _type;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void run() {
            String line;
            int lineNum = 0;
            try {
                while ((line = bReader.readLine()) != null) {
                    lineNum++;
                    System.out.println(type + ":" + line);
                }
                bReader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void test6() throws Exception {
        String zipFilePath = "/work/codeanalysis/thu/sa-engine/src/test/java/codeanalysis/saengine/jsp-demo1.zip";
        String dest = "/work/codeanalysis/thu/sa-engine/src/test/java/codeanalysis/saengine/jsp-demo1";
        FileUtility.extractJar(zipFilePath, dest);
        // compile


        FileUtility.unzip(zipFilePath, dest, null);


        compileExec(dest, dest);
    }

    @Test
    public void test7() throws Exception {
        String[] sources = (
                "<javax.servlet.http.HttpSession: java.lang.Object getAttribute(java.lang.String)>\n").split("\n");
        String[] sinks = (
                "<org.apache.axis.client.Call: java.lang.Object invoke(java.lang.Object[])>\n"
        ).split("\n");

//
//        InfoflowC infoflowC = new InfoflowC();
//        String tempdir = "/work/codeanalysis/thu";
//        String root = tempdir + File.separator + "cbfe";
//        List<String> jspClasses = FileUtility.filterFile(root, new String[]{"**/*_jsp.class"});
//        List<String> libPaths = FileUtility.filterFile(root, new String[]{"**/WEB-INF/lib/*.jar"});
//        GlobalConfig config = new GlobalConfig();
//        libPaths.forEach(l -> config.addLibPath(root + File.separator + l));
//        infoflowC.setGlobalConfig(config);
//        String classpath = root + File.separator + "WEB-INF" + File.separator + "classes";
//        Rule rule = new Rule();
//        rule.setSink(Arrays.asList(sinks));
//        rule.setSource(Arrays.asList(sources));
//        Map<String, RuleDetectedResult> resultMap = infoflowC.getResult(jspClasses, classpath, Collections.singletonList(rule), false);
//        System.out.println(resultMap);


    }

    @Test
    public void test8() {
        String tempdir = "/work/codeanalysis/thu";
        String root = tempdir + File.separator + "cbfe";
        String command = "java -Xms6g -Xmx6g -XX:+UseG1GC -jar {0} -i {1} -o {2} -rp /work/codeanalysis/thu/sa-compile/src/main/resources/defaultrule.txt -c infoflow";
        String compilerPath = "/work/codeanalysis/thu/sa-compile/target/sa-compile.jar";
        String cmd = MessageFormat.format(command, compilerPath, root, root);
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


    @Test
    public void test9() throws IOException {
        String[] sources = (
                "<javax.servlet.http.HttpSession: java.lang.Object getAttribute(java.lang.String)>\n").split("\n");
        String[] sinks = (
                "<org.apache.axis.client.Call: java.lang.Object invoke(java.lang.Object[])>\n"
        ).split("\n");

        Rule rule = new Rule();
        rule.setSink(Arrays.asList(sinks));
        rule.setSource(Arrays.asList(sources));
        rule.setId("123345");
        rule.setCategory("安全漏洞");
        rule.setCwe("78");
        rule.setLevel(2);
        rule.setName("命令注入");

        Rule rule1 = new Rule();
        rule1.setSink(Arrays.asList(sinks));
        rule1.setSource(Arrays.asList(sources));
        rule1.setId("123345567");
        rule1.setCategory("安全漏洞1");
        rule1.setCwe("76");
        rule1.setLevel(2);
        rule1.setName("fea");

        List<Rule> rules = Arrays.asList(rule, rule1);

        Gson gson = new Gson();
        String ret = gson.toJson(rules);
        FileWriter file = new FileWriter("ret.json");
        file.write(ret);
        file.close();
    }

    @Test
    public void test10() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Ts());
        try {
            String s = future.get(5, TimeUnit.SECONDS);
            System.out.println(s);
        } catch (TimeoutException e) {
            System.out.println("timeout");
            future.cancel(true);
        } finally {
            executor.shutdownNow();
        }
    }

    private static class Ts implements Callable<String> {


        @Override
        public String call() throws InterruptedException {
            Thread.sleep(10_000);
            return "fadsfa";
        }
    }
}
