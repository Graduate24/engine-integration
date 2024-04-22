package codeanalysis.saengine.file;


/**
 * @author Ran Zhang
 * @since 2024/4/16
 */
public interface FileService {

    FileOpResponse put(String objectKey, String filePath) throws Exception;


}
