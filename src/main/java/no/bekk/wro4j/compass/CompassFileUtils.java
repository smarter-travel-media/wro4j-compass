package no.bekk.wro4j.compass;

import java.io.File;

/**
 * Simple file utils used by various components of the project.
 */
public class CompassFileUtils {

    /**
     * Given a starting point backs out of the directory until a pom.xml file is found in the directory.
     * @param candidate file that serves as a starting point
     * @return {@link File} the directory containing a pom.xml
     * @throws RuntimeException when no pom file can be found.
     */
    public static File locateMavenBaseDir(File candidate) {

        while (candidate != null && candidate.isDirectory()) {
            File[] files = candidate.listFiles();
            for (File f : files) {
                if (f.getName().equals("pom.xml")) {
                    return candidate;
                }
            }
            candidate = candidate.getParentFile();
        }
        throw new RuntimeException("Cannot location maven base directory");
    }

    /**
     * Takes a base directory and a relative plath and returns an absolute path representing of the relative path
     * starting from the base directory.
     * @param projectBaseDir
     * @param relativePath
     * @return String
     */
    public static String computePath(File projectBaseDir, String relativePath) {

        if(relativePath == null) {
            return projectBaseDir.getAbsolutePath();
        }
        else {
            return new File(projectBaseDir, relativePath).getAbsolutePath();
        }
    }


}
