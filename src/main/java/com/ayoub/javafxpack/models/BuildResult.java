package com.ayoub.javafxpack.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the outcome of a build: success flag, exit code,
 * output exe path and the full log.
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildResult {
    private boolean success;
    private int exitCode;
    private String outputExePath;
    private String rawLog;
}
