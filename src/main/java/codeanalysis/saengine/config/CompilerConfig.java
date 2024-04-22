package codeanalysis.saengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Ran Zhang
 * @since 2024/3/15
 */
@Component
@ConfigurationProperties(prefix = "compiler")
@Data
public class CompilerConfig {

    private String execpath;
    private Integer maxmemory = 2;
    private Integer timeout = 20;
    private String tapath;
    private String jdk;
    private String tajava;


}
