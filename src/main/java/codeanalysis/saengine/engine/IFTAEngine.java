package codeanalysis.saengine.engine;

import codeanalysis.saengine.entity.InputStreamRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class IFTAEngine implements Engine<IFTAEngine.ExecuteParams, String> {
    protected Logger logger = LoggerFactory.getLogger(IFTAEngine.class);

    public static class ExecuteParams {
        String javaPath;
        String enginePath;
        String project;
        String jdk;
        String output;
        String config;
        String maxMemory;
        String waitTime;

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

    @Override
    public EngineType type() {
        return EngineType.IFTA;
    }

    @Override
    public String execute(ExecuteParams s) {
        //String project, String jdk, String output, configPath, Xmx
        // /home/ran/.jdks/graalvm-ce-17/bin/java -jar ta.jar -dc true -p "/WebGoat-5.0" -j "/jdk/rt.jar" -t true -w true -o result.json -cg SPARK -to 180
        String command = "{0} -Xms{1} -Xmx{2} -jar {3} -c {4} -p {5} -t true -w true -o {6} -cg SPARK";

        String cmd = MessageFormat.format(command,
                s.javaPath,
                s.maxMemory + "g", s.maxMemory + "g",
                s.enginePath, s.config, s.project, s.output);

        logger.info("ifta cmd: {}", cmd);
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
}
