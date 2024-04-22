package codeanalysis.saengine.controller;

import codeanalysis.saengine.compiler.JspCompiler;
import codeanalysis.saengine.entity.SaRequest;
import codeanalysis.saengine.entity.SaResponse;
import codeanalysis.saengine.service.SaService;
import codeanalysis.saengine.util.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author Ran Zhang
 * @since 2024/4/12
 */
@RestController
public class SaController {
    Logger log = LoggerFactory.getLogger(SaController.class);

    @Resource
    private SaService saService;
    @Resource
    private JspCompiler jspCompiler;

    @GetMapping("/test")
    public HttpResponse<LocalDateTime> localTime() {
        log.info("time:{}", LocalDateTime.now());
        return HttpResponse.success(LocalDateTime.now());
    }

    @PostMapping("/analysis")
    public HttpResponse<SaResponse> doAnalysis(@RequestBody SaRequest request) {
        return HttpResponse.success();
    }

    @PostMapping("/jspc")
    public HttpResponse jspc(@RequestParam String input, @RequestParam String output) {
        log.info("jspc===========");
        // jspCompiler.compileExec(input, output);
        return HttpResponse.success();
    }

}
