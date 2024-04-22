package codeanalysis.saengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Ran Zhang
 * @since 2024/4/16
 */
@Component
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {

    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
    private String zone;
    private String protocol = "http";
    private String host = "localhost";
    private String port = "9010";
    private String mask;


}
