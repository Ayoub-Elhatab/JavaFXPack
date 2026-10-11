package com.ayoub.javafxpack.models;


import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Holds the detected paths and availability of the external build tools
 * (JDK, jpackage, jlink, WiX).
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
public class ToolchainInfo {

    private String javaHome;
    private String jpackagePath;
    private String jlinkPath;

    // e.g. "candle.exe"/"light.exe" directory, needed for --type exe
    private String wixBinPath;

    private String jdkVersion;
    private String wixVersion;

    private boolean jdkFound;
    private boolean wixFound;

}
