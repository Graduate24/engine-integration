package codeanalysis.saengine;

import codeanalysis.saengine.util.FileUtility;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileTest {

    @Test
    public void test() {
        FileUtility.zip("/work/codeanalysis/a", "/work/codeanalysis/a.zip", null);
    }

    @Test
    public void testUnzip() {
        FileUtility.unzip("/work/codeanalysis/thu/example/example.zip", "/work/codeanalysis/thu/example/", null);
    }

    @Test
    public void test2() throws IOException {
        String jar = "/work/codeanalysis/test/jsp-demo.war";
        File jarFile = new File(jar);
        // extract
        String name = FilenameUtils.removeExtension("jsp-demo.war");
        FileUtility.extractJar(jarFile.getPath(), "/work/codeanalysis/test/a" + File.separator + name);
    }

    @Test
    public void test3(){
        List<String> jspClasses = FileUtility.filterFile("/home/ran/download/jsp-demo (1)", new String[]{"**/WEB-INF/lib/*.jar"});
        System.out.println(jspClasses);
    }

    @Test
    public void test4(){
        List<String> file = FileUtility.filterFile("/home/ran/Downloads/WebGoat-5.0", new String[]{"**/"+"org/owasp/webgoat/session/LessonTracker.java"});
        System.out.println(file);
    }
}
