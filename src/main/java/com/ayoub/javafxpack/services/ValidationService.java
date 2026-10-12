package com.ayoub.javafxpack.services;


import com.ayoub.javafxpack.models.ProjectConfig;
import com.ayoub.javafxpack.models.ToolchainInfo;
import com.ayoub.javafxpack.models.ValidationResult;
import java.io.File;

/**
 * Runs pre-flight checks on the project settings
 * and detected tools before a build starts.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public class ValidationService {

    /**
     * Checks the tools, paths and settings needed for a build and collects every problem found.
     *
     * @param config the project settings from the form
     * @param toolchain the detected JDK and WiX tools
     * @return a result listing all errors, valid if there are none
     */
    public ValidationResult validate(ProjectConfig config, ToolchainInfo toolchain){
        ValidationResult result = new ValidationResult();

        if (!toolchain.isJdkFound()) {
            result.addError("No JDK with jpackage/jlink found. Set JAVA_HOME or install a JDK 21+.");
        }
        if (!toolchain.isWixFound()) {
            result.addError("WiX Toolset v3 not found. It's required for --type exe. " +
                    "Install it from wixtoolset.org and restart JavaFXPack.");
        }

        if (isBlank(config.getAppName())) {
            result.addError("App name is required.");
        }
        if (isBlank(config.getJarPath())) {
            result.addError("Jar path is required.");
        } else if (!new File(config.getJarPath()).isFile()) {
            result.addError("Jar file not found: " + config.getJarPath());
        }

        if (isBlank(config.getMainClass())) {
            result.addError("Main class is required (e.g. com.ayoub.myapp.Main).");
        }

        if (!isBlank(config.getIconPath())) {
            File icon = new File(config.getIconPath());
            if (!icon.isFile()) {
                result.addError("Icon file not found: " + config.getIconPath());
            } else if (!config.getIconPath().toLowerCase().endsWith(".ico")) {
                result.addError("Icon must be a .ico file for a Windows exe.");
            }
        }

        if (isBlank(config.getOutputDir())) {
            result.addError("Output directory is required.");
        } else {
            File outDir = new File(config.getOutputDir());
            if (outDir.exists() && !outDir.canWrite()) {
                result.addError("Output directory is not writable: " + config.getOutputDir());
            }
        }

        if (isBlank(config.getJavafxJmodsPath())) {
            result.addError("javafx-jmods path is required (jlink needs it to build the runtime image).");
        } else if (!new File(config.getJavafxJmodsPath()).isDirectory()) {
            result.addError("javafx-jmods path is not a valid directory: " + config.getJavafxJmodsPath());
        }
        if (config.getJavafxModules().isEmpty()) {
            result.addError("Select at least one JavaFX module.");
        }

        return result;
    }

    /**
     * Checks whether a string is null or blank.
     *
     * @param s the string to check
     * @return true if the string is null or blank
     */
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
