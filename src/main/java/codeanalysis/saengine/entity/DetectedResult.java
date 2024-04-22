package codeanalysis.saengine.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class DetectedResult {
    enum Level {
        LOW, MEDIUM, HIGH
    }
    String description;
    Level severity;
    Level confidence;
    String sourceSig;
    String sinkSig;
    List<PathUnit> path;
    private List<String> pathStm;

}
