package codeanalysis.saengine.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ran Zhang
 * @since 2023/11/4
 */
@Data
public class Rule {
    /**
     * rules = [{'id': str(r.id), 'name': r.name, 'cwe': r.cwe,
     * 'category': r.category.name, 'level': r.level, 'source': r.source, 'sink': r.sink} for r in rule]
     */
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("cwe")
    private String cwe;
    @SerializedName("category")
    private String category;
    @SerializedName("level")
    private Integer level;
    @SerializedName("source")
    private List<String> source;
    @SerializedName("sink")
    private List<String> sink;

    public Rule() {
    }

    public void addSource(String source) {
        this.source.add(source);
    }

    public void addSink(String sink) {
        this.sink.add(sink);
    }

    public Rule(List<String> source, List<String> sink) {
        this.source = source;
        this.sink = sink;
    }

    public static Rule emptyRule() {
        return new Rule(new ArrayList<>(), new ArrayList<>());
    }

    public Rule merge(Rule other) {
        this.sink = Lists.newArrayList(Iterables.concat(this.sink, other.sink));
        this.source = Lists.newArrayList(Iterables.concat(this.source, other.source));
        return this;
    }

    public List<String> distinctSource() {
        this.source = Lists.newArrayList(Sets.newHashSet(this.source));
        return this.source;
    }

    public List<String> distinctSink() {
        this.sink = Lists.newArrayList(Sets.newHashSet(this.sink));
        return this.sink;
    }
}
