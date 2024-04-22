package codeanalysis.saengine;

import codeanalysis.saengine.compiler.JspCompiler;
import codeanalysis.saengine.entity.Rule;
import codeanalysis.saengine.service.SaService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author Ran Zhang
 * @since 2021/4/12
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SaEngineApplication.class)
@EnableAutoConfiguration
public class EngineTest {
    @Autowired
    SaService service;

    @Autowired
    JspCompiler jspCompiler;

    @Test
    public void test() throws Exception {
        //service.doAnalysis(new SaRequest("http://82.156.234.100:9010/codeanalysis/1620893032270/1351/jsp-demo.war?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minio%2F20210513%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20210513T080352Z&X-Amz-Expires=86400&X-Amz-SignedHeaders=host&X-Amz-Signature=029c60ca2101286666762f4c12f668afe9bd9f1d4c5909b07672043581794911", "jsp-demo.war"));
    }

    @Test
    public void testCompile() {
        jspCompiler.compile("/work/codeanalysis/sa-engine/compile", "/work/codeanalysis/sa-engine/compile");
    }

    @Test
    public void testReduce() {
        List<Rule> rules = new ArrayList<>();
        Rule r1 = new Rule();
        r1.setSink(Arrays.asList("sink1", "sink2", "sink3"));
        r1.setSource(Arrays.asList("source1", "source2", "source3"));
        Rule r2 = new Rule();
        r2.setSink(Arrays.asList("sink3", "sink4", "sink5"));
        r2.setSource(Arrays.asList("source3", "source4", "source5"));

        rules.add(r1);
        rules.add(r2);


        Rule reduceRule = rules.stream().filter(r -> !isEmpty(r.getSink()) && !isEmpty(r.getSource()))
                .reduce(Rule.emptyRule(), Rule::merge);
        System.out.println(reduceRule);
    }



}
