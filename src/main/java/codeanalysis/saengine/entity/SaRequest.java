package codeanalysis.saengine.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ran Zhang
 * @since 2024/4/12
 */
@Data
@ToString
public class SaRequest {
    @SerializedName("workflowId")
    private String workflowId;
    @SerializedName("taskId")
    private String taskId;
    @SerializedName("source")
    private String directory;
    @SerializedName("filename")
    private String filename;
    @SerializedName("rules")
    private List<Rule> rules = new ArrayList<>();
    @SerializedName("maxMemory")
    private Integer maxMemory;
    @SerializedName("timeout")
    private Integer timeout;

    public SaRequest() {
    }

    public SaRequest(String directory, String filename, List<Rule> rules) {
        this.directory = directory;
        this.filename = filename;
        this.rules = rules;
    }

    public SaRequest(String directory) {
        this.directory = directory;
    }
}
