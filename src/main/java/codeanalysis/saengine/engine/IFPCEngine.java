package codeanalysis.saengine.engine;

import codeanalysis.saengine.entity.InputStreamRunnable;
import codeanalysis.saengine.util.PathUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class IFPCEngine implements Engine<IFPCEngine.ExecuteParams, String> {
    protected Logger logger = LoggerFactory.getLogger(IFPCEngine.class);

    @Override
    public EngineType type() {
        return EngineType.IFPC;
    }

    private String refineAppPath(String project) {
        // copy all .class to temporary directory.
        // filter all .class file
        List<String> classFiles = PathUtil.filterFile(project, new String[]{"**/*.class"});

        Set<String> copied = new HashSet<>();
        List<String> javaClasses = new ArrayList<>();
        try {
            for (String file : classFiles) {
                String absPath = Paths.get(project, file).toString();
                String md5 = PathUtil.md5(absPath);
                if (copied.contains(md5)) {
                    continue;
                }
                File o = new File(absPath);
                String packageName = PathUtil.classPackageName(absPath);
                String className = PathUtil.className(absPath);
                if (packageName == null) {
                    continue;
                }
                if (className != null) {
                    javaClasses.add(className);
                }
                Path d = Paths.get(project, PathUtil.packageToDirString(packageName), o.getName());
                logger.info("copy {} to {}", o.getPath(), d);
                if (!o.getCanonicalPath().equals(d.toFile().getCanonicalPath())) {
                    PathUtil.copy(o, d.toFile());
                }
                copied.add(md5);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return project;
    }

    /**
     * {
     * "appPath": "./WebContent/WEB-INF/classes",
     * "libPath": "../jdk/rt.jar:./",
     * "useTaintWrapper": true,
     * "verbose": true,
     * "outputDirectory": "ifpcOutput"
     * }
     *
     * @return
     */
    private String createIFPCConfig(ExecuteParams params) {
        String configPath = Paths.get(params.project, "ifpcconfig.json").toString();
        Map<String, Object> config = new HashMap<>();
        config.put("appPath", refineAppPath(params.project));
        config.put("libpath", params.jdk + ":" + params.project);
        config.put("useTaintWrapper", true);
        config.put("verbose", true);
        config.put("outputDirectory", params.output);
        try {
            Gson gson = new Gson();
            String json = gson.toJson(config);
            Writer writer = new FileWriter(configPath);
            writer.write(json);
            writer.close();
            return configPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String execute(ExecuteParams s) {
        // TODO: cmd line params.
        //  java -jar ifpc-core-0.1.jar --config ../ifpc-testcase/WebGoat-5.0/ifpc.json
        //  --extra-rule rule/path-traversal.json --default-entry -cg SPARK
        String command = "{0} -Xms{1} -Xmx{2} -jar {3} --config {4} -er {5} --default-entry -cg SPARK";

        String configPath = createIFPCConfig(s);
        String rules = String.join(" ", s.getRules());

        String cmd = MessageFormat.format(command,
                s.javaPath,
                1 + "g", s.maxMemory + "g",
                s.enginePath, configPath, rules);

        logger.info("ifpc cmd: {}", cmd);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            new InputStreamRunnable(process.getErrorStream(), "").start();
            new InputStreamRunnable(process.getInputStream(), "").start();
            process.waitFor(Integer.parseInt(s.waitTime), TimeUnit.MINUTES);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        process.destroy();
        return s.output;
    }

    public static class ExecuteParams {
        String javaPath;
        String enginePath;
        String project;
        String jdk;
        String output;
        String config;
        String maxMemory;
        String waitTime;

        List<String> rules;

        public List<String> getRules() {
            return rules;
        }

        public void setRules(List<String> rules) {
            this.rules = rules;
        }

        public String getJavaPath() {
            return javaPath;
        }

        public void setJavaPath(String javaPath) {
            this.javaPath = javaPath;
        }

        public String getEnginePath() {
            return enginePath;
        }

        public void setEnginePath(String enginePath) {
            this.enginePath = enginePath;
        }

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public String getJdk() {
            return jdk;
        }

        public void setJdk(String jdk) {
            this.jdk = jdk;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public String getMaxMemory() {
            return maxMemory;
        }

        public void setMaxMemory(String maxMemory) {
            this.maxMemory = maxMemory;
        }

        public String getWaitTime() {
            return waitTime;
        }

        public void setWaitTime(String waitTime) {
            this.waitTime = waitTime;
        }
    }
}
