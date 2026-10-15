package com.ayoub.javafxpack.ui;


import com.ayoub.javafxpack.models.BuildResult;
import com.ayoub.javafxpack.models.ProjectConfig;
import com.ayoub.javafxpack.models.ToolchainInfo;
import com.ayoub.javafxpack.models.ValidationResult;
import com.ayoub.javafxpack.process.BuildTask;
import com.ayoub.javafxpack.services.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

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
    @FXML private Button openOutputFolderButton;

    @FXML private ProgressBar buildProgressBar;

    @FXML private TextArea buildLogArea;

    private final JavaFxModuleDetector javaFxModuleDetector = new JavaFxModuleDetector();
    private final ToolsLocatorService toolsLocatorService = new ToolsLocatorService();
    private final ValidationService validationService = new ValidationService();
    private final JlinkService jlinkService = new JlinkService();
    private final JpackageService jpackageService = new JpackageService();

    private final Map<String, CheckBox> moduleCheckBoxes = new LinkedHashMap<>();
    private ToolchainInfo toolchainInfo;
    private String lastOutputPath;

    @FXML
    public void initialize(){
        buildModuleCheckboxes();
        locateToolsAsync();
        openOutputFolderButton.setDisable(true);
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

        jdkStatusLabel.setText(info.isJdkFound() ? "JDK : " + info.getJdkVersion() : "JDK : not found");
        jdkStatusLabel.getStyleClass().removeAll("ok", "error");
        jdkStatusLabel.getStyleClass().add(info.isJdkFound() ? "ok" : "error");

        wixStatusLabel.setText(info.isWixFound() ? "WiX : " + info.getWixVersion() : "WiX : not found");
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

    /**
     * Opens a file chooser filtered to .ico files and puts the selected path in the icon field.
     */
    @FXML
    private void onBrowseIcon(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select main Icon");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Icon files", "*.ico"));
        File file = fileChooser.showOpenDialog(buildButton.getScene().getWindow());
        if (file != null) {
            iconPathField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Opens a folder chooser and puts the selected path in the output folder field.
     */
    @FXML
    private void onBrowseOutputDir() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select output folder");
        File file = dirChooser.showDialog(buildButton.getScene().getWindow());
        if (file != null) {
            outputDirField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Opens a folder chooser and puts the selected javafx-jmods path in the jmods field.
     */
    @FXML
    private void onBrowseJmods() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select javafx-jmods folder");
        File file = dirChooser.showDialog(buildButton.getScene().getWindow());
        if (file != null) {
            jmodsPathField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Validates the form, then runs the build on a background thread
     * while showing progress and live output.
     */
    @FXML
    private void onBuild() {
        ProjectConfig config = collectConfig();

        ValidationResult validation = validationService.validate(config, toolchainInfo);

        if (!validation.isValid()) {
            showAlert(Alert.AlertType.WARNING, "Cannot build", String.join("\n", validation.getErrors()));
            return;
        }

        buildLogArea.clear();
        buildButton.setDisable(true);
        buildProgressBar.setVisible(true);
        buildProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        BuildTask task = new BuildTask(config, toolchainInfo, jlinkService, jpackageService, this::appendLog);

        task.setOnSucceeded(e -> {
            BuildResult result = task.getValue();
            onBuildFinished(result, config);
            openOutputFolderButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            appendLog("Build failed: " + task.getException());
            buildButton.setDisable(false);
            buildProgressBar.setVisible(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Reads the form fields and ticked module checkboxes into a new ProjectConfig.
     * JVM options are split on whitespace.
     *
     * @return the project configuration built from the current form state
     */
    private ProjectConfig collectConfig() {
        ProjectConfig config = new ProjectConfig();

        config.setAppName(appNameField.getText());
        config.setAppVersion(appVersionField.getText());
        config.setVendor(vendorField.getText());
        config.setJarPath(jarPathField.getText());
        config.setMainClass(mainClassField.getText());
        config.setIconPath(iconPathField.getText());
        config.setOutputDir(outputDirField.getText());
        config.setJavafxJmodsPath(jmodsPathField.getText());

        List<String> jvmOptions = new ArrayList<>();
        String rawOptions = jvmOptionsField.getText();
        if(rawOptions != null && !rawOptions.isBlank()){
            jvmOptions.addAll(Arrays.asList(rawOptions.trim().split("\\s+")));
        }
        config.setJvmOptions(jvmOptions);

        List<String> modules = new ArrayList<>();
        moduleCheckBoxes.forEach((module, cb) -> {
            if(cb.isSelected()){
                modules.add(module);
            }
        });
        config.setJavafxModules(modules);

        return config;
    }

    /**
     * Restores the UI after a build and reports success or failure to the user.
     *
     * @param result the outcome returned by jpackage
     * @param config the project settings used for the build
     */
    private void onBuildFinished(BuildResult result, ProjectConfig config) {
        buildButton.setDisable(false);
        buildProgressBar.setVisible(false);

        if (result.isSuccess()) {
            lastOutputPath = result.getOutputExePath();
            appendLog("Build succeeded: " + result.getOutputExePath());
            showAlert(Alert.AlertType.INFORMATION, "Build succeeded",
                    "Your exe was created at:\n" + result.getOutputExePath());
        } else{
            lastOutputPath = config.getOutputDir();
            appendLog("Build failed with exit code " + result.getExitCode());
            showAlert(Alert.AlertType.ERROR, "Build failed",
                    "jpackage exited with code " + result.getExitCode() + ". See the log for details.");
        }
    }

    /**
     * Appends a line to the build log.
     *
     * @param line the text to append, ignored if null
     */
    private void appendLog(String line) {
        if (line == null) return;
        buildLogArea.appendText(line + System.lineSeparator());
    }

    /**
     * Shows a modal alert styled and iconified according to its type.
     *
     * @param type the alert type (error, warning, information)
     * @param title the dialog title
     * @param message the text shown in the dialog body
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.initOwner(buildButton.getScene().getWindow());

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/ayoub/javafxpack/icons/exe.png"))));

        String typeName = type.name().toLowerCase();

        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/ayoub/javafxpack/css/style.css")).toExternalForm());

        InputStream iconStream = getClass().getResourceAsStream("/com/ayoub/javafxpack/icons/alert-" + typeName + ".png");
        if (iconStream != null) {
            ImageView icon = new ImageView(new Image(iconStream));
            icon.setFitWidth(40);
            icon.setFitHeight(40);
            alert.setGraphic(icon);
        }

        alert.showAndWait();
    }

    /**
     * Opens the output folder in Windows Explorer, using the last build's output path
     * or the output folder field if no build has run yet.
     */
    @FXML
    private void onOpenOutputFolder() {
        String path = lastOutputPath != null ? lastOutputPath : outputDirField.getText();
        if (path == null || path.isBlank()) return;

        try{
            File file = new File(path);
            File dir = file.isDirectory() ? file : file.getParentFile();
            if(dir != null && dir.exists() && Desktop.isDesktopSupported()){
                Desktop.getDesktop().open(dir);
            }
        } catch (IOException ignored) {
        }
    }

}
