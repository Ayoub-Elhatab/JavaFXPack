package com.ayoub.javafxpack.models;


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
public class ToolchainInfo {

    private String javaHome;
    private String jpackagePath;
    private String jlinkPath;

    // e.g. "candle.exe"/"light.exe" directory, needed for --type exe
    private String wixBinPath;

    private String jdkVersion;

    private boolean jdkFound;
    private boolean wixFound;

}
