package com.ayoub.javafxpack.ui;


import com.ayoub.javafxpack.models.ToolchainInfo;
import com.ayoub.javafxpack.services.JavaFxModuleDetector;
import com.ayoub.javafxpack.services.ToolsLocatorService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MainController {

    @FXML private Label jdkStatusLabel;
    @FXML private Label wixStatusLabel;

    @FXML private TextField appNameField;
    @FXML private TextField appVersionField;
    @FXML private TextField vendorField;
    @FXML private TextField jarPathField;
    @FXML private TextField mainClassField;
    @FXML private TextField iconPathField;
    @FXML private TextField outputDirField;

    @FXML private TextField jvmOptionsField;

    @FXML private TextField jmodsPathField;

    @FXML private Label moduleDetectionLabel;
    @FXML private FlowPane modulesFlowPane;

    @FXML private Button buildButton;

    @FXML private ProgressBar buildProgressBar;

    @FXML private TextArea buildLogArea;

    private final JavaFxModuleDetector javaFxModuleDetector = new JavaFxModuleDetector();
    private final ToolsLocatorService toolsLocatorService = new ToolsLocatorService();

    private final Map<String, CheckBox> moduleCheckBoxes = new LinkedHashMap<>();
    private ToolchainInfo toolchainInfo;

    @FXML
    public void initialize(){
        buildModuleCheckboxes();
        locateToolsAsync();
    }

    /**
     * Creates one checkbox per known JavaFX module and adds it to the modules pane.
     * javafx.controls and javafx.fxml are ticked by default.
     */
    private void buildModuleCheckboxes(){
        Set<String> commonModules = javaFxModuleDetector.allKnownModules();
        for(String module : commonModules){
            CheckBox cb = new CheckBox(module);
            if(module.equals("javafx.controls") || module.equals("javafx.fxml") ){
                cb.setSelected(true);
            }
            moduleCheckBoxes.put(module, cb);
            modulesFlowPane.getChildren().add(cb);
        }
    }

    /**
     * Detects the JDK and WiX on a background thread,
     * then applies the result on the JavaFX thread.
     */
    private void locateToolsAsync(){
        Thread thread = new Thread(() -> {
            ToolchainInfo info = toolsLocatorService.detect();
            Platform.runLater(() -> applyToolchainInfo(info));
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Stores the detection result, updates the JDK/WiX status labels and
     * enables the Build button only when both tools are found.
     *
     * @param info the detected toolchain
     */
    private void applyToolchainInfo(ToolchainInfo info) {
        this.toolchainInfo = info;

        jdkStatusLabel.setText(info.isJdkFound() ? "JDK: " + info.getJdkVersion() : "JDK: not found");
        jdkStatusLabel.getStyleClass().removeAll("ok", "error");
        jdkStatusLabel.getStyleClass().add(info.isJdkFound() ? "ok" : "error");

        wixStatusLabel.setText(info.isWixFound() ? "WiX: " + info.getWixVersion() : "WiX: not found");
        wixStatusLabel.getStyleClass().removeAll("ok", "error");
        wixStatusLabel.getStyleClass().add(info.isWixFound() ? "ok" : "error");

        buildButton.setDisable(!info.isJdkFound() || !info.isWixFound());
    }

    /**
     * Opens a file chooser filtered to .jar files and puts the selected path in the jar field.
     */
    @FXML
    private void onBrowseJar(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select main Jar");
        fileChooser.getExtensionFilters().add(new  FileChooser.ExtensionFilter("Jar Files", "*.jar"));
        File file = fileChooser.showOpenDialog(buildButton.getScene().getWindow());
        if (file != null) {
            jarPathField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Scans the selected jar for JavaFX usage and ticks the matching module checkboxes.
     * Shows an error message in the label if no valid jar is selected.
     */
    @FXML
    private void onDetectModules() {
        String jarPath = jarPathField.getText();
        if(jarPath == null || jarPath.isBlank() || !new File(jarPath).isFile()){
            moduleDetectionLabel.setText("Select a valid jar first.");
            return;
        }
        Set<String> suggested = javaFxModuleDetector.suggestModules(jarPath);
        moduleCheckBoxes.forEach((module, cb) -> cb.setSelected(suggested.contains(module)));
        moduleDetectionLabel.setText("found : " + String.join(", ", suggested));
    }










}
