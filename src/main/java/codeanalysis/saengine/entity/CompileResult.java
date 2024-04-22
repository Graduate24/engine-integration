package codeanalysis.saengine.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CompileResult {
    private String name;
    private long size;
    private String objectKey;
    private String md5;
}
