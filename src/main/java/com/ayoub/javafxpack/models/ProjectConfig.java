package com.ayoub.javafxpack.models;


import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Holds the settings of one packaging job
 * (jar, app metadata, JVM options, JavaFX modules, output folder).
 *
 * @author Ayoub Elhatab
 * LinkedIn: <a href="https://www.linkedin.com/in/ayoub-elhatab/">Ayoub Elhatab</a>
 */
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProjectConfig {

    private String appName;
    private String appVersion;
    private String vendor;
    private String jarPath;
    private String mainClass;
    private String iconPath;
    private String outputDir;
    private List<String> jvmOptions = new ArrayList<>();
    private List<String> javafxModules = new ArrayList<>();
    private String javafxJmodsPath;
}
