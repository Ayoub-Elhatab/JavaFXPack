package com.ayoub.javafxpack.services;


import com.ayoub.javafxpack.models.ToolchainInfo;
import java.io.File;
import java.util.Optional;

/**
 * Locates the external tools a build depends on:
 * a JDK (jpackage, jlink) and the WiX Toolset.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public class ToolsLocatorService {

    /**
     * Detects the JDK (via JAVA_HOME, then common install folders) and the WiX Toolset.
     *
     * @return the detected tool paths and availability flags
     */
    public ToolchainInfo detect() {

        ToolchainInfo.ToolchainInfoBuilder builder = ToolchainInfo.builder();

        String javaHome = System.getenv("JAVA_HOME");
        boolean jdkFound = false;
        String jpackagePath = null;
        String jlinkPath = null;
        String jdkVersion = null;

        if (javaHome != null && !javaHome.isBlank()) {
            File jpackageExe = new File(javaHome, "bin\\jpackage.exe");
            File jlinkExe = new File(javaHome, "bin\\jlink.exe");

            if (jpackageExe.exists() && jlinkExe.exists()) {
                jdkFound = true;
                jpackagePath = jpackageExe.getAbsolutePath();
                jlinkPath = jlinkExe.getAbsolutePath();
                jdkVersion = readJdkVersion(javaHome);
            }

        }

        // Fallback: scan common install roots if JAVA_HOME didn't work out
        if (!jdkFound) {
            Optional<File> found = scanCommonJdkRoots();
            if (found.isPresent()) {
                javaHome = found.get().getAbsolutePath();
                jpackagePath = new File(found.get(), "bin\\jpackage.exe").getAbsolutePath();
                jlinkPath = new File(found.get(), "bin\\jlink.exe").getAbsolutePath();
                jdkFound = true;
                jdkVersion = readJdkVersion(javaHome);
            }
        }

        Optional<File> wixBin = findWixBin();
        boolean wixFound = wixBin.isPresent();
        String wixBinPath = wixBin.map(File::getAbsolutePath).orElse(null);
        String wixVersion = wixBin.map(this::readWixVersion).orElse(null);

        return builder
                .javaHome(javaHome)
                .jpackagePath(jpackagePath)
                .jlinkPath(jlinkPath)
                .jdkFound(jdkFound)
                .jdkVersion(jdkVersion)
                .wixFound(wixFound)
                .wixBinPath(wixBinPath)
                .wixVersion(wixVersion)
                .build();

    }

    /**
     * Scans the common JDK install folders for one containing jpackage.
     *
     * @return the first JDK home found, or empty if none
     */
    private Optional<File> scanCommonJdkRoots() {
        String[] roots = {"C:\\Program Files\\Java"};

        for (String root : roots) {
            File rootDir = new File(root);
            File[] children = rootDir.listFiles(File::isDirectory);
            if (children == null) continue;
            for (File child : children) {
                if (new File(child, "bin\\jpackage.exe").exists()) {
                    return Optional.of(child);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Derives a display version from the JDK folder name.
     *
     * @param javaHome path to the JDK home
     * @return the folder name, e.g. jdk-21.0.2
     */
    private String readJdkVersion(String javaHome) {
        return new File(javaHome).getName();
    }

    /**
     * Finds the WiX 3 bin folder via the WIX environment variable,
     * then by scanning Program Files.
     *
     * @return the bin folder containing candle.exe and light.exe, or empty if not found
     */
    private Optional<File> findWixBin() {
        String wixEnv = System.getenv("WIX");
        if (wixEnv != null) {
            File bin = new File(wixEnv, "bin");
            if (hasWixTools(bin)) return Optional.of(bin);
        }
        for (String root : new String[]{"C:\\Program Files (x86)", "C:\\Program Files"}) {
            File[] dirs = new File(root).listFiles(f -> f.isDirectory() && f.getName().startsWith("WiX Toolset v3"));
            if (dirs == null) continue;
            for (File dir : dirs) {
                File bin = new File(dir, "bin");
                if (hasWixTools(bin)) return Optional.of(bin);
            }
        }
        return Optional.empty();
    }

    /**
     * Checks that a folder contains the WiX compiler and linker.
     *
     * @param bin the folder to check
     * @return true if both candle.exe and light.exe exist
     */
    private boolean hasWixTools(File bin) {
        return new File(bin, "candle.exe").exists() && new File(bin, "light.exe").exists();
    }

    /**
     * Derives a display version from the WiX install folder name.
     *
     * @param wixBin the WiX bin folder
     * @return the version, e.g. v3.14
     */
    private String readWixVersion(File wixBin) {
        return wixBin.getParentFile().getName().replace("WiX Toolset ", "");
    }

}