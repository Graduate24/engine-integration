package codeanalysis.saengine.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PathUnit {
    String hint;
    String file;
    String function;
    String jimpleStmt;
    String javaStmt;
    String jspStmt;
    int line;
    Integer jspLine;
    String javaClass;
    String jspFile;
}
