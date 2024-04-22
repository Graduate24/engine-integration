package codeanalysis.saengine.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Ran Zhang
 * @since 2024/3/29
 */
@Data
@ToString
public class RuleDetectedResult {
    private String ruleName;
    private String ruleCwe;
    private String ruleCategory;
    private Integer ruleLevel;
    private List<DetectedResult> detectedResults;

}
