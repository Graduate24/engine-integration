package codeanalysis.saengine.engine;

import codeanalysis.saengine.entity.InputStreamRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class JspcEngine implements Engine<JspcEngine.ExecuteParams, String> {
    protected Logger logger = LoggerFactory.getLogger(JspcEngine.class);

    @Override
    public EngineType type() {
        return EngineType.JSPC;
    }

    @Override
    public String execute(ExecuteParams s) {
        String command = "{0} -jar {1} -p {2} -o {3}";

        String cmd = MessageFormat.format(command,
                s.javaPath,
                s.enginePath, s.project, s.output);

        logger.info("jspc cmd: {}", cmd);
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
        String output;
        String config;
        String maxMemory;
        String waitTime = "5";


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

        public String getJavaPath() {
            return javaPath;
        }

        public void setJavaPath(String javaPath) {
            this.javaPath = javaPath;
        }
    }


}
