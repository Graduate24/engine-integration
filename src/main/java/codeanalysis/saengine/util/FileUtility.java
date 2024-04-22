package codeanalysis.saengine.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_256;

/**
 * @author Ran Zhang
 * @since 2024/3/13
 */
public class FileUtility {

    /**
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public static void extractJar(String zipFilePath, String destDirectory) throws IOException {
        java.util.jar.JarFile jarfile = new java.util.jar.JarFile(new java.io.File(zipFilePath));
        java.util.Enumeration<java.util.jar.JarEntry> enu = jarfile.entries();
        while (enu.hasMoreElements()) {
            java.util.jar.JarEntry je = enu.nextElement();
            java.io.File fl = new java.io.File(destDirectory, je.getName());
            if (!fl.exists()) {
                fl.getParentFile().mkdirs();
                fl = new java.io.File(destDirectory, je.getName());
            }
            if (je.isDirectory()) {
                continue;
            }
            java.io.InputStream is = jarfile.getInputStream(je);
            java.io.FileOutputStream fo = new java.io.FileOutputStream(fl);
            while (is.available() > 0) {
                fo.write(is.read());
            }
            fo.close();
            is.close();
        }

    }

    public static String createTempdir() {
        return Files.createTempDir().getAbsolutePath();
    }

    public static File download(String tmpdir, String url, String fileName) throws IOException {

        File dest = new File(tmpdir, fileName);
        FileUtils.copyURLToFile(new URL(url), dest);
        return dest;
    }

    public static List<String> filterFile(String baseDir, String[] patterns) {
        DirectoryScanner scanner = new DirectoryScanner();
        //new String[]{"**/*.java"}
        scanner.setIncludes(patterns);
        scanner.setBasedir(baseDir);
        scanner.setCaseSensitive(false);
        scanner.scan();
        return Arrays.asList(scanner.getIncludedFiles());
    }

    public static void deteleTempdir(String path) {
        String tmpDirsLocation = System.getProperty("java.io.tmpdir");
        if (path.startsWith(tmpDirsLocation)) {
            try {
                FileUtils.deleteDirectory(new File(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteFile(String path) {
        File fileToDelete = FileUtils.getFile(path);
        return FileUtils.deleteQuietly(fileToDelete);
    }

    public static void zip(String targetPath, String destinationFilePath, String password) {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.MEDIUM_FAST);

            if (password != null && password.length() > 0) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
                parameters.setAesKeyStrength(KEY_STRENGTH_256);
            }

            ZipFile zipFile = new ZipFile(destinationFilePath);

            File targetFile = new File(targetPath);
            if (targetFile.isFile()) {
                zipFile.addFile(targetFile, parameters);
            } else if (targetFile.isDirectory()) {
                zipFile.addFolder(targetFile, parameters);
            } else {
                //neither file nor directory
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String targetZipFilePath, String destinationFolderPath, String password) {
        try {
            ZipFile zipFile = new ZipFile(targetZipFilePath);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll(destinationFolderPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String objectKey(String md5, String fileName) {
        return "codedb" + "/" + md5 + "/" + fileName;
    }

    public static String md5(String filePath) throws IOException {
        File file = new File(filePath);
        HashCode hashCode = Files.asByteSource(file).hash(Hashing.md5());
        return hashCode.toString();
    }

    public static long size(String path) throws IOException {
        Path imageFilePath = Paths.get(path);
        FileChannel fileChannel = FileChannel.open(imageFilePath);
        return fileChannel.size();
    }
}
