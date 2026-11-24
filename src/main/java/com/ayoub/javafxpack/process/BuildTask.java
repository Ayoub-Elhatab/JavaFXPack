package com.ayoub.javafxpack.process;


import com.ayoub.javafxpack.models.BuildResult;
import com.ayoub.javafxpack.models.ProjectConfig;
import com.ayoub.javafxpack.models.ToolchainInfo;
import com.ayoub.javafxpack.services.InputStagingService;
import com.ayoub.javafxpack.services.JdepsService;
import com.ayoub.javafxpack.services.JlinkService;
import com.ayoub.javafxpack.services.JpackageService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs the build pipeline (input folder, jdeps, jlink, then jpackage)
 * on a background thread so the UI stays responsive.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
@RequiredArgsConstructor
public class BuildTask extends Task<BuildResult> {

    private final ProjectConfig config;
    private final ToolchainInfo toolchain;
    private final InputStagingService inputStagingService;
    private final JdepsService jdepsService;
    private final JlinkService jlinkService;
    private final JpackageService jpackageService;

    private final Consumer<String> logSink;


    /**
     * Prepares the input folder, detects the JDK modules with jdeps,
     * builds the runtime image with jlink, then creates the exe with jpackage.
     *
     * @return the result of the jpackage step
     * @throws Exception if the input folder or the jlink step fails
     */
    @Override
    protected BuildResult call() throws Exception {
        Consumer<String> log = line -> Platform.runLater(() -> logSink.accept(line));

        log.accept("Preparing input folder...");
        File inputDir = inputStagingService.stage(config, log);

        log.accept("Detecting JDK modules with jdeps...");
        List<String> jdkModules = jdepsService.detectJdkModules(toolchain, inputDir, config.getLibsDir(), log);

        log.accept("Building custom runtime with jlink...");
        String runtimeImagePath = jlinkService.buildRuntime(config, toolchain, jdkModules, log);

        log.accept("Running jpackage...");
        return jpackageService.run(config, toolchain, runtimeImagePath, inputDir, log);
    }
}
