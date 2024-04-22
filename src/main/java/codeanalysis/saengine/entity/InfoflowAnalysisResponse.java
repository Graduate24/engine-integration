package codeanalysis.saengine.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author Ran Zhang
 * @since 2023/11/5
 */
@Data
@ToString
public class InfoflowAnalysisResponse {
    private boolean success;
    private Map<String, RuleDetectedResult> result;
    private CompileResult compileResult;
    private String errorMessage;
    private List<String> uncompiled;
}
