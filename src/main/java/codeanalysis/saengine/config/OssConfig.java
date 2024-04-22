package codeanalysis.saengine.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author Ran Zhang
 * @since 2024/4/16
 */
@Configuration
@Slf4j
public class OssConfig {

    @Bean
    public MinioClient minioClient(MinioConfig config) {

        MinioClient client = MinioClient.builder()
                .endpoint(config.getProtocol() + "://" + config.getHost() +
                        (!StringUtils.hasLength(config.getPort()) ? "" : ":" + config.getPort()))
                .credentials(config.getAccessKeyId(), config.getSecretAccessKey())
                .build();
        try {
            log.info("minio check bucket...");
            boolean found =
                    client.bucketExists(BucketExistsArgs.builder().bucket(config.getBucketName()).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(config.getBucketName()).build());
            }
        } catch (Exception ignored) {
            log.error("init minio fail", ignored);
        }

        return client;
    }
}
