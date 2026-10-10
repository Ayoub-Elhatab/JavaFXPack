package com.ayoub.javafxpack.process;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs external commands (jlink, jpackage)
 * and streams their output line by line to a callback.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
public class ProcessRunner {


    /**
     * Runs the given command and blocks until it exits.
     *
     * @param command full command + args
     * @param workingDir working directory
     * @param onLine called on the calling thread for every line of output
     * @return the process exit code
     */
    public int run(List<String> command, File workingDir, Consumer<String> onLine) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true); // Merges stderr into stdout

        if (workingDir != null) {
            processBuilder.directory(workingDir);
        }

        Process process = processBuilder.start();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while((line = reader.readLine()) != null ){
                onLine.accept(line);
            }
        }
        return process.waitFor();
    }



}
