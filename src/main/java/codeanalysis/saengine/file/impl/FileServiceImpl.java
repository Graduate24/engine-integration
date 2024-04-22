package codeanalysis.saengine.file.impl;

import codeanalysis.saengine.config.MinioConfig;
import codeanalysis.saengine.file.FileOpResponse;
import codeanalysis.saengine.file.FileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ran Zhang
 * @since 2024/4/16
 */
@Service
public class FileServiceImpl implements FileService {

    protected Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig config;

    @Override
    public FileOpResponse put(String objectKey, String filePath) {
        FileOpResponse response;
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(objectKey)
                            .filename(filePath)
                            .build());
            response = new FileOpResponse(true, 200, "200",
                    "", "", "", objectKey);
        } catch (Exception e) {
            response = new FileOpResponse(false, 500, "500",
                    e.getMessage(), "", "", objectKey);
        }
        logger.info("{}", response);

        return response;
    }
}
