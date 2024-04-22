package codeanalysis.saengine.compiler;

import java.util.List;

/**
 * @author Ran Zhang
 * @since 2024/4/13
 */
public interface JspCompiler {

    default void compile(String uriRoot, String outputDir) {
        compile(uriRoot, outputDir, false);
    }

    void compile(String uriRoot, String outputDir, boolean smapSuppressed);

    List<String> preCompile(String tempdir, String url, String filename);

    void compileExec(String uriRoot, String outputDir);
}
