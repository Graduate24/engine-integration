package codeanalysis.saengine.mq;

import codeanalysis.saengine.config.CompilerConfig;
import codeanalysis.saengine.entity.SaRequest;
import codeanalysis.saengine.entity.SaResponse;
import codeanalysis.saengine.service.SaService;
import com.geneea.celery.Celery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author Ran Zhang
 * @since 2021/6/7
 */
@RabbitListener(queues = "sa_engine")
@Component
@Slf4j
public class AnalysisTask extends MessageReceiver {

    @Autowired
    private SaService saService;

    @Qualifier("analysisCeleryClient")
    @Autowired
    private Celery celery;

    @Autowired
    private CompilerConfig config;


    @Override
    public SaResponse handle(SaRequest request) {
        log.info("sa_engine queue handler start...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<SaResponse> future = executor.submit(new AnalysisTaskCaller(saService, request));
        SaResponse response;
        int timeout = request.getTimeout() == null ? config.getTimeout() : request.getTimeout();
        if (timeout < 1 || timeout >= 30) {
            timeout = config.getTimeout();
        }
        if (timeout == 30) {
            timeout = 29;
        }
        int memory = request.getMaxMemory() == null ? config.getMaxmemory() : request.getMaxMemory();
        if (memory < 1 && memory >= 16) {
            memory = config.getMaxmemory();
        }
        request.setMaxMemory(memory);
        request.setTimeout(timeout);
        log.info("request timeout:{}, max memory:{}", request.getTimeout(), request.getMaxMemory());
        try {
            response = future.get(timeout, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.info("sa_engine queue handler Interrupted...");
            e.printStackTrace();
            response = new SaResponse();
            response.setSuccess(false);
            response.setMessage("InterruptedException");
            future.cancel(true);
        } catch (ExecutionException e) {
            response = new SaResponse();
            response.setSuccess(false);
            response.setMessage("ExecutionException");
            e.printStackTrace();
            future.cancel(true);
        } catch (TimeoutException e) {
            log.info("sa_engine queue handler TIMEOUT: {} minutes...", timeout);
            response = new SaResponse();
            response.setSuccess(false);
            response.setMessage("TimeoutException");
            future.cancel(true);
        } finally {
            executor.shutdownNow();
        }
        log.info("sa_engine queue handler end...");
        return response;
    }

    @Override
    public void postHandle(SaResponse response) {
        sendCeleryMq(celery, "sa_finish", response);
    }

    private static class AnalysisTaskCaller implements Callable<SaResponse> {
        private final SaService service;
        private final SaRequest request;

        public AnalysisTaskCaller(SaService service, SaRequest request) {
            this.service = service;
            this.request = request;
        }

        @Override
        public SaResponse call() {
            log.info("call do analysis...");
            return service.doAnalysis(request);
        }
    }
}
