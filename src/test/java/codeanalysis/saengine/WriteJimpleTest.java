package codeanalysis.saengine;

import org.junit.Test;
import soot.PackManager;
import soot.Scene;
import soot.options.Options;

import java.io.File;
import java.util.Arrays;

public class WriteJimpleTest {
    @Test
    public void writeJimple() {
        soot.G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_output_dir("/home/ran/download/jsp-demo/org/apache/jsp");
        String libPath = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";
        String appPath = "/home/ran/download/jsp-demo/org/apache/jsp";
        Options.v().set_soot_classpath(libPath + File.pathSeparator + appPath);
        Options.v().set_process_dir(Arrays.asList(appPath));// all class files in appPath will be solved to jimple files
        Scene.v().loadNecessaryClasses();
//        String[] classes = {
//                "codeanalysis.saengine.WriteJimpleTest"
//        };
//        for (String className: classes) {
//            //write specific class to jimple
//            SootClass theClass = Scene.v().getSootClass(className);
//            theClass.setApplicationClass();
//        }
        PackManager.v().writeOutput();
    }
}
