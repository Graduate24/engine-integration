package codeanalysis.saengine.entity;

import lombok.Data;
import lombok.ToString;

/**
 * @author zhangran
 * @since 2023-11-09
 **/
@Data
@ToString
public class CompileLog {
    private String taskId;
    private String content;
    private int index = 0;

    public CompileLog() {
    }

    public CompileLog(String taskId) {
        this.taskId = taskId;
    }

    public CompileLog log(String content) {
        this.content = content;
        this.index++;
        return this;
    }

}
