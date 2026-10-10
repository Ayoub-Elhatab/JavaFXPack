package com.ayoub.javafxpack.models;


import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProjectConfig {

    private String appName = "MyApp";
    private String appVersion = "1.0.0";
    private String vendor = "";
    private String jarPath = "";
    private String mainClass = "";
    private String iconPath = "";
    private String outputDir = "";
    private List<String> jvmOptions = new ArrayList<>();
    private List<String> javafxModules = new ArrayList<>();
    private String javafxJmodsPath = "";
}
