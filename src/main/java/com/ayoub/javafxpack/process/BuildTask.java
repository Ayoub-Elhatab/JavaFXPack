package com.ayoub.javafxpack.process;


import com.ayoub.javafxpack.models.BuildResult;
import com.ayoub.javafxpack.models.ProjectConfig;
import com.ayoub.javafxpack.models.ToolchainInfo;
import com.ayoub.javafxpack.services.JlinkService;
import com.ayoub.javafxpack.services.JpackageService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import java.util.function.Consumer;

/**
 * Runs the build pipeline (jlink, then jpackage)
 * on a background thread so the UI stays responsive.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
@RequiredArgsConstructor
public class BuildTask extends Task<BuildResult> {

    private final ProjectConfig config;
    private final ToolchainInfo toolchain;
    private final JlinkService jlinkService;
    private final JpackageService jpackageService;

    private final Consumer<String> logSink;


    /**
     * Builds the runtime image with jlink,
     * then creates the exe with jpackage.
     *
     * @return the result of the jpackage step
     * @throws Exception if the jlink step fails
     */
    @Override
    protected BuildResult call() throws Exception {
        Consumer<String> log = line -> Platform.runLater(() -> logSink.accept(line));

        log.accept("Building custom runtime with jlink...");
        String runtimeImagePath = jlinkService.buildRuntime(config, toolchain, log);

        log.accept("Running jpackage...");
        return jpackageService.run(config, toolchain, runtimeImagePath, log);
    }
}
