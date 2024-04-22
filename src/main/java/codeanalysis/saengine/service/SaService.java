package codeanalysis.saengine.service;

import codeanalysis.saengine.entity.SaRequest;
import codeanalysis.saengine.entity.SaResponse;

/**
 * @author Ran Zhang
 * @since 2024/4/12
 */
public interface SaService {
    SaResponse doAnalysis(SaRequest request);

    void analysisExec(String root, String outputDir, String rulePath, SaRequest request);

}
