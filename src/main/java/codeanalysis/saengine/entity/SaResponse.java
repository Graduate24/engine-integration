package codeanalysis.saengine.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author Ran Zhang
 * @since 2024/4/12
 */
@Data
@ToString
public class SaResponse {
    private boolean success;
    private String workflowId;
    private String taskId;
    private String requestId;
    private String message;
    private Integer statusCode;
    private String code;
    private Map<String, RuleDetectedResult> result;

    private List<String> jspClasses;
    private CompileResult compileResult;
    private List<String> uncompiled;
}
