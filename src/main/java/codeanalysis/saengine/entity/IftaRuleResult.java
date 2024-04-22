package codeanalysis.saengine.entity;

import java.util.Collections;
import java.util.List;

public class IftaRuleResult {
    private String ruleName;

    private String ruleCwe;

    private Integer ruleLevel;

    private List<DetectedResult> result = Collections.emptyList();

    private int resultCount;

    public IftaRuleResult() {
    }

    public String getRuleCwe() {
        return ruleCwe;
    }

    public void setRuleCwe(String ruleCwe) {
        this.ruleCwe = ruleCwe;
    }

    public Integer getRuleLevel() {
        return ruleLevel;
    }

    public void setRuleLevel(Integer ruleLevel) {
        this.ruleLevel = ruleLevel;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    public IftaRuleResult(String ruleName, List<DetectedResult> result) {
        this.ruleName = ruleName;
        this.result = result;
        this.resultCount = result.size();
    }

    public int getResultCount() {
        return resultCount;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public List<DetectedResult> getResult() {
        return result;
    }

    public void setResult(List<DetectedResult> result) {
        this.result = result;
    }
}
