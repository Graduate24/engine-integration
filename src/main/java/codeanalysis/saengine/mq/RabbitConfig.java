package codeanalysis.saengine.mq;

import com.geneea.celery.Celery;
import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ran Zhang
 * @since 2024/5/12
 */
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class RabbitConfig {

    private String host;
    private String port;
    private String username;
    private String password;
    private String virtualHost;

    public String getVirtualHost() {
        return virtualHost.equals("/") ? "%2F" : virtualHost;
    }

    //发送消息时如不配置序列化方法则按照java默认序列化机制，则会造成发送编码不符合
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public Queue sa() {
        return new Queue("sa_engine_complete", true);
    }

    @Bean
    public Queue saCompile() {
        return new Queue("sa_compile_complete", true);
    }

    @Bean
    public Queue compileLog() {
        return new Queue("sa_compile_log", true);
    }

    @Bean
    DirectExchange saExchange() {
        return new DirectExchange("sa");
    }

    @Bean
    Binding bindingDirect(Queue sa, DirectExchange saExchange) {
        return BindingBuilder.bind(sa).to(saExchange).with("sa_finish");
    }

    @Bean
    Binding bindingDirectCompileComplete(Queue saCompile, DirectExchange saExchange) {
        return BindingBuilder.bind(saCompile).to(saExchange).with("compile_finish");
    }

    @Bean
    Binding bindingDirectCompileLog(Queue compileLog, DirectExchange saExchange) {
        return BindingBuilder.bind(compileLog).to(saExchange).with("compile_log");
    }

    @Bean
    public Celery analysisCeleryClient() {
        String uri = String.format("amqp://%s:%s@%s:%s/%s", username, password, host, port, getVirtualHost());
        return Celery.builder()
                .brokerUri(uri)
                .queue("sa_engine_complete")
                .build();
    }

    @Bean
    public Celery compileCeleryClient() {
        String uri = String.format("amqp://%s:%s@%s:%s/%s", username, password, host, port, getVirtualHost());
        return Celery.builder()
                .brokerUri(uri)
                .queue("sa_compile_complete")
                .build();
    }

    @Bean
    public Celery logCeleryClient() {
        String uri = String.format("amqp://%s:%s@%s:%s/%s", username, password, host, port, getVirtualHost());
        return Celery.builder()
                .brokerUri(uri)
                .queue("sa_compile_log")
                .build();
    }
}
