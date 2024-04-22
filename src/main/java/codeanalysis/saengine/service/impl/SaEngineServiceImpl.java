package codeanalysis.saengine.service.impl;

import codeanalysis.saengine.config.CompilerConfig;
import codeanalysis.saengine.engine.IFPCEngine;
import codeanalysis.saengine.engine.IFTAEngine;
import codeanalysis.saengine.entity.*;
import codeanalysis.saengine.service.LogService;
import codeanalysis.saengine.service.SaService;
import codeanalysis.saengine.util.FileUtility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static codeanalysis.saengine.util.FileUtility.filterFile;
import static codeanalysis.saengine.util.PathUtil.*;


/**
 * @author Ran Zhang
 * @since 2024/4/12
 */
@Service
@Slf4j
public class SaEngineServiceImpl implements SaService {

    @Autowired
    private CompilerConfig config;
    private final Type RESULT_TYPE = new TypeToken<InfoflowAnalysisResponse>() {
    }.getType();
    final private Gson gson = new Gson();
    @Autowired
    private LogService logService;

    private final IFTAEngine iftaEngine = new IFTAEngine();

    private final IFPCEngine ifpcEngine = new IFPCEngine();

    @Override
    public SaResponse doAnalysis(SaRequest request) {
        log.info("start analysis; taskId:{}, filename:{}", request.getTaskId(), request.getFilename());
        return analysis(request, request.getRules());
    }

    @Override
    public void analysisExec(String root, String outputDir, String rulePath, SaRequest request) {
        String command = "java -Xms{0} -Xmx{1} -XX:+UseG1GC -jar {2} -i {3} -o {4} -rp {5} -c infoflow";
        String cmd = MessageFormat.format(command, request.getMaxMemory() + "g", request.getMaxMemory() + "g",
                config.getExecpath(), root, outputDir, rulePath);
        log.info("cmd: {}", cmd);
        CompileLog clog = new CompileLog(request.getTaskId());
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            new InputStreamRunnable(clog, logService, process.getErrorStream(), "").start();
            new InputStreamRunnable(clog, logService, process.getInputStream(), "").start();
            process.waitFor(request.getTimeout(), TimeUnit.MINUTES);
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run by ifta-core.
     *
     * @param project
     * @param jdk
     * @param outputDir
     * @param rules
     * @param request
     */
    private void runIFTAExec(String project, String jdk, String outputDir, List<Rule> rules, SaRequest request) throws IOException, InterruptedException {
        // /home/ran/.jdks/graalvm-ce-17/bin/java -jar ta.jar -dc true -p "/WebGoat-5.0" -j "/jdk/rt.jar" -t true -w true -o result.json -cg SPARK -to 180
        String command = "{0} -Xms{1} -Xmx{2} -jar {3} -c {4} -p {5} -j {6} -t true -w true -o {7} -cg SPARK -to {8}";
        String configs = createConfig(project, jdk, rules);
        String configPath = Paths.get(project, "config.json").toString();
        String resultPath = Paths.get(outputDir, "result.json").toString();
        try {
            String json = gson.toJson(configs);
            Writer writer = new FileWriter(configPath);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String cmd = MessageFormat.format(command,
                config.getTajava(),
                request.getMaxMemory() + "g", request.getMaxMemory() + "g",
                config.getTapath(), configPath, project, config.getJdk(), resultPath, 180);

        log.info("ifta cmd: {}", cmd);
        CompileLog clog = new CompileLog(request.getTaskId());
        Process process = Runtime.getRuntime().exec(cmd);
        new InputStreamRunnable(clog, logService, process.getErrorStream(), "").start();
        new InputStreamRunnable(clog, logService, process.getInputStream(), "").start();
        process.waitFor(request.getTimeout(), TimeUnit.MINUTES);
        process.destroy();


    }

    private String createConfig(String project, String jdk, List<Rule> rules) {
        Gson g = new Gson();
        Map<String, Object> c = new HashMap<>();
        c.put("project", project);
        c.put("jdk", jdk);
        c.put("autoDetect", true);
        List<Map<String, Object>> ruleList = rules.stream().map(r -> {
            Map<String, Object> rm = new HashMap<>();
            rm.put("name", r.getName());
            rm.put("sources", r.getSource());
            rm.put("sinks", r.getSink());
            return rm;
        }).collect(Collectors.toList());
        c.put("rules", ruleList);

        return g.toJson(c);
    }

    private List<String> createRules(String project, List<Rule> rules) {
        List<String> ruleList = rules.stream().map(r -> {
            Map<String, Object> rm = new HashMap<>();
            rm.put("ruleName", r.getName());
            rm.put("ruleCwe", r.getCwe());
            rm.put("ruleLevel", r.getLevel());
            rm.put("sources", r.getSource());
            rm.put("sinks", r.getSink());
            String rulePath = Paths.get(project, "rule_" + r.getCwe() + ".json").toString();
            try {
                String json = gson.toJson(rm);
                Writer writer = new FileWriter(rulePath);
                writer.write(json);
                writer.close();
                return rulePath;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        return ruleList;
    }

    private Map<String, RuleDetectedResult> formatResult(String project, List<IftaRuleResult> iftaRuleResults) {

        /**
         * {
         *     "SQL 注入": {
         *       "ruleName": "SQL 注入",
         *       "ruleCwe": "89",
         *       "ruleLevel": 1,
         *       "detectedResults": [
         *         {
         *           "sourceSig": "\u003cjavax.servlet.http.HttpServletRequest: javax.servlet.http.HttpSession getSession()\u003e",
         *           "sinkSig": "\u003cjava.sql.Statement: java.sql.ResultSet executeQuery(java.lang.String)\u003e",
         *           "path": [
         *             {
         *               "file": "org/owasp/webgoat/lessons/DefaultLessonAction.java",
         *               "function": "\u003corg.owasp.webgoat.lessons.DefaultLessonAction: boolean isAuthorized(org.owasp.webgoat.session.WebSession,int,java.lang.String)\u003e",
         *               "jimpleStmt": "$r2 \u003d interfaceinvoke $r1.\u003cjavax.servlet.http.HttpServletRequest: javax.servlet.http.HttpSession getSession()\u003e()",
         *               "javaStmt": "$r2 \u003d interfaceinvoke $r1.\u003cjavax.servlet.http.HttpServletRequest: javax.servlet.http.HttpSession getSession()\u003e()",
         *               "jspStmt": "$r2 \u003d interfaceinvoke $r1.\u003cjavax.servlet.http.HttpServletRequest: javax.servlet.http.HttpSession getSession()\u003e()",
         *               "line": 245
         *             },
         *           ]
         *         },
         */
        Map<String, RuleDetectedResult> result = new HashMap<>();
        for (IftaRuleResult ruleResult : iftaRuleResults) {
            for (DetectedResult detectedResult : ruleResult.getResult()) {
                for (PathUnit unit : detectedResult.getPath()) {
                    if (!StringUtils.hasText(unit.getFile())) {
                        String file = locateSourceFile(project, unit.getJavaClass());
                        unit.setFile(file);
                    }
                    if (unit.getJavaClass().endsWith("_jsp") && unit.getLine() != -1) {
                        String classPath = packageToDirString(unit.getJavaClass()) + ".class";
                        List<String> sourceFile = filterFile(project, new String[]{"**/" + classPath});
                        if (sourceFile.isEmpty()) {
                            continue;
                        }
                        String sourceDebug = readSourceDebug(Paths.get(project, sourceFile.get(0)).toString());
                        if (sourceDebug == null) {
                            continue;
                        }
                        SmapInfo smapInfo = getSmapInfo(sourceDebug);
                        List<SmapInfo.LineInfo.LineMapping> jspLineMap = smapInfo.getMapping();
                        for (SmapInfo.LineInfo.LineMapping m : jspLineMap) {
                            if (m.getOutputBeginLine() <= unit.getLine() && unit.getLine() <= m.getOutputEndLine()) {
                                unit.setJspLine(m.getInputLine());
                            }
                        }
                        List<String> jspFile = filterFile(project, new String[]{"**/" + smapInfo.getSourceFilePath()});
                        if (!jspFile.isEmpty()) {
                            unit.setJspFile(jspFile.get(0));
                        }

                    }
                }
            }
        }

        for (IftaRuleResult r : iftaRuleResults) {
            RuleDetectedResult ruleDetectedResult = new RuleDetectedResult();
            ruleDetectedResult.setRuleName(r.getRuleName());
            ruleDetectedResult.setRuleCwe(r.getRuleCwe());
            ruleDetectedResult.setRuleLevel(r.getRuleLevel());
            ruleDetectedResult.setDetectedResults(r.getResult());
            result.put(r.getRuleName(), ruleDetectedResult);
        }
        //log.info("format Result: {}", result);
        return result;

    }


    private Map<String, RuleDetectedResult> runIFTA(SaRequest request, String root, List<Rule> rules) throws IOException {
        //analysisExec(root, root, rulePath, request);
        IFTAEngine.ExecuteParams params = new IFTAEngine.ExecuteParams();
        String configs = createConfig(root, config.getJdk(), rules);
        String configPath = Paths.get(root, "config.json").toString();
        // log.info("config.json:\n {}", configs);
        Writer writer = new FileWriter(configPath);
        writer.write(configs);
        writer.close();
        String resultPath = Paths.get(root, "result.json").toString();

        params.setConfig(configPath);
        params.setProject(root);
        params.setJdk(config.getJdk());
        params.setEnginePath(config.getTapath());
        params.setJavaPath(config.getTajava());
        params.setOutput(resultPath);
        params.setMaxMemory(request.getMaxMemory().toString());
        params.setWaitTime(request.getTimeout().toString());

        iftaEngine.execute(params);
        // runIFTAExec(root, config.getJdk(), root, rules, request);

        if (!new File(resultPath).exists()) {
            throw new RuntimeException("analysis result not found");
        }
        JsonReader reader = new JsonReader(new FileReader(resultPath));
        Type iftaType = new TypeToken<ArrayList<IftaRuleResult>>() {
        }.getType();
        List<IftaRuleResult> iftaRuleResults = gson.fromJson(reader, iftaType);
        return formatResult(root, iftaRuleResults);
    }


    private Map<String, RuleDetectedResult> runIFPC(SaRequest request, String root, List<Rule> rules) throws IOException {
        IFPCEngine.ExecuteParams params = new IFPCEngine.ExecuteParams();
        List<String> rulePaths = createRules(root, rules);
        params.setRules(rulePaths);
        params.setProject(root);
        params.setJdk(config.getJdk());
        //params.setEnginePath(config.getIfpcPath());
        params.setJavaPath(config.getTajava());
        Path output = Paths.get(root, "ifpcoutput");
        Files.createDirectories(output);
        params.setOutput(output.toString());
        params.setMaxMemory(request.getMaxMemory().toString());
        params.setWaitTime(request.getTimeout().toString());

        ifpcEngine.execute(params);
        String resultPath = Paths.get(params.getOutput(), "reports.json").toString();
        if (!new File(resultPath).exists()) {
            throw new RuntimeException("analysis result not found");
        }
        JsonReader reader = new JsonReader(new FileReader(resultPath));
        Type iftaType = new TypeToken<ArrayList<IftaRuleResult>>() {
        }.getType();
        List<IftaRuleResult> iftaRuleResults = gson.fromJson(reader, iftaType);
        return formatResult(root, iftaRuleResults);
    }

    private SaResponse analysis(SaRequest request, List<Rule> rules) {
        SaResponse response = new SaResponse();
        response.setRequestId(request.getTaskId());
        response.setTaskId(request.getTaskId());
        response.setWorkflowId(request.getWorkflowId());
        String tempdir = FileUtility.createTempdir();

        try {
            File target = FileUtility.download(tempdir, request.getDirectory(), request.getFilename());
            FileUtility.unzip(target.getPath(), tempdir, null);
            String root = tempdir + File.separator + FilenameUtils.removeExtension(request.getFilename());

            //analysisExec(root, root, rulePath, request);
            Map<String, RuleDetectedResult> result = runIFTA(request, root, rules);
//            Map<String, RuleDetectedResult> result = runIFPC(request, root, rules);

            response.setSuccess(true);
            response.setResult(result);
        } catch (Exception e) {
            log.error("run analysis fail.", e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return response;
        } finally {
            // Finished
            log.info("run analysis finished.delete tempdir");
            FileUtility.deteleTempdir(tempdir);
        }
        return response;
    }

}
