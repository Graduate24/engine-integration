package codeanalysis.saengine.mq;

import codeanalysis.saengine.entity.SaRequest;
import codeanalysis.saengine.entity.SaResponse;
import com.geneea.celery.Celery;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Ran Zhang
 * @since 2024/5/12
 */
@Slf4j
@Component
public abstract class MessageReceiver {
    protected Gson gson = new Gson();

    public abstract SaResponse handle(SaRequest request) throws Exception;

    public abstract void postHandle(SaResponse response);

    @RabbitHandler
    public void process(Object message) {
        try {
            if (!(message instanceof Message)) {
                return;
            }
            Map<String, String> msg = getBody((Message) message);
            log.info("receive message");
            SaResponse response = handle(gson.fromJson(gson.toJson(msg), SaRequest.class));
            postHandle(response);
        } catch (Exception e) {
            log.info("process message error!:{}", e.getMessage());
        }
    }

    private Map<String, String> getBody(Message message) {
        try {
            byte[] body = message.getBody();
            ArrayList msg = gson.fromJson(new String(body), ArrayList.class);
            return (Map<String, String>) ((ArrayList) msg.get(0)).get(0);
        } catch (Exception e) {
            log.info("getBody error：{}", e.getMessage());
            return null;
        }
    }

    protected void sendCeleryMq(Celery celery, String name, Object... args) {
        Object[] objects = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            objects[i] = gson.toJson(args[i]);
        }
        try {
            celery.submit(name, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
