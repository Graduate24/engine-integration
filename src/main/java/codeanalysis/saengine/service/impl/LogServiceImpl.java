package codeanalysis.saengine.service.impl;

import codeanalysis.saengine.entity.CompileLog;
import codeanalysis.saengine.service.LogService;
import com.geneea.celery.Celery;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author zhangran
 * @since 2024-11-09
 **/
@Service
@Slf4j
public class LogServiceImpl implements LogService {

    @Qualifier("logCeleryClient")
    @Autowired
    private Celery logger;

    protected Gson gson = new Gson();

    @Override
    public void log(CompileLog cl) {
        try {
            logger.submit("compile_log", new Object[]{gson.toJson(cl)});
        } catch (IOException e) {
            log.info("send compile log fail.");
        }
    }
}
