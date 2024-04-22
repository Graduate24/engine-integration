package codeanalysis.saengine.entity;

import codeanalysis.saengine.service.LogService;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Ran Zhang
 * @since 2023/11/5
 */
@Slf4j
public class InputStreamRunnable extends Thread {
    BufferedReader bReader = null;
    String type = null;
    LogService logService;
    CompileLog clog;

    public InputStreamRunnable(InputStream is, String _type) {
        try {
            bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), StandardCharsets.UTF_8));
            type = _type;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public InputStreamRunnable(CompileLog clog, LogService logService, InputStream is, String _type) {
        try {
            this.clog = clog;
            this.logService = logService;
            bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), StandardCharsets.UTF_8));
            type = _type;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line;
        int lineNum = 0;
        try {
            while ((line = bReader.readLine()) != null) {
                lineNum++;
                log.info(type + ":" + line);
                if (logService != null && clog != null) {
                    logService.log(clog.log(line));
                }
            }
            bReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
