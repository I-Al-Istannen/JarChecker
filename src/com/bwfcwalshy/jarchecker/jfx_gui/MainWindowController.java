package com.bwfcwalshy.jarchecker.jfx_gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.bwfcwalshy.jarchecker.Checker;
import com.bwfcwalshy.jarchecker.JarDecompiler;
import com.bwfcwalshy.jarchecker.Main;
import com.bwfcwalshy.jarchecker.jfx_gui.log.LogFilter;
import com.bwfcwalshy.jarchecker.jfx_gui.log.LogFilter.FilterType;
import com.bwfcwalshy.jarchecker.jfx_gui.suspicious_class.SuspiciousClassListController;
import com.bwfcwalshy.jarchecker.jfx_gui.log.LogPaneController;
import com.bwfcwalshy.jarchecker.jfx_gui.utils.import_file_creator.ImportFileCreatorController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * The controller for the main window
 */
public class MainWindowController {

	private LogPaneController logPane;

	@FXML
	private BorderPane borderPane;

	@FXML
	private MenuItem checkFileMenuItem;

	@FXML
	private MenuItem exitMenuItem;

	@FXML
	private MenuItem aboutMenuItem;

	private Map<String, String> suspiciousClasses;

	@FXML
	private Button showClasses;

	@FXML
	private void initialize() {
		{
			Image exitIcon = new Image(Main.class.getResource("/resources/exit icon.png").toString(), 20, 20, true,
					true);
			exitMenuItem.setGraphic(new ImageView(exitIcon));
		}

		{
			Image scanIcon = new Image(Main.class.getResource("/resources/scan icon 2.png").toString(), 20, 20, true,
					true);
			checkFileMenuItem.setGraphic(new ImageView(scanIcon));
		}

		{
			Image aboutIcon = new Image(Main.class.getResource("/resources/help icon color.png").toString(), 20, 20,
					true, true);
			aboutMenuItem.setGraphic(new ImageView(aboutIcon));
		}

		{
			try {
				FXMLLoader loader = new FXMLLoader(LogPaneController.class.getResource("LogPane.fxml"));
				BorderPane pane = loader.load();
				logPane = loader.getController();
				logPane.addFilter(new LogFilter(FilterType.AND, (level) -> level != Level.FINER, "Debug remover"));
				borderPane.setCenter(pane);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		{
			showClasses.setDisable(true);
		}
	}

	@FXML
	void onAbout(ActionEvent event) {
		AboutWindow.show(Main.getInstance().getPrimaryStage());
	}

	@FXML
	void onCheckFile(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("Jar-Files", "*.jar"));
		chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));
		showClasses.setDisable(true);

		File resultFile = chooser.showOpenDialog(Main.getInstance().getPrimaryStage());

		if (resultFile == null) {
			Logger.log(Level.INFO, "Scan cancelled!");
			return;
		}

		AtomicBoolean decompiled = new AtomicBoolean(false);

		new Thread(() -> {
			Optional<Path> decompiledPath = JarDecompiler.decompile(resultFile.toPath(),
					Main.getInstance().getSettings());
			decompiled.set(true);

			Logger.log(Level.INFO, "Started checking!");
			if (decompiledPath.isPresent()) {
				startCheck(decompiledPath.get().toFile());
			}
		}, "Decompiler").start();
	}

	@FXML
	private void onShowClasses() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(SuspiciousClassListController.class.getResource("SuspiciousClassList.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root1));
			stage.setTitle("Suspicious classes");
			stage.show();
		} catch (IOException e) {
			Logger.logException(Level.SEVERE, e);
		}
	}

	private void startCheck(File jarFile) {
		System.out.println("Checking: " + jarFile.getName() + " " + jarFile.getAbsolutePath());
		Checker checker = new Checker();
		checker.check(jarFile);
		Logger.log(Level.INFO, "-----------------------------------------------------");
		Logger.log(Level.INFO, "File name: " + jarFile.getName());
		Logger.log(Level.INFO, "");
		Logger.log(Level.INFO, "File checked with JarChecker " + Main.getInstance().getVersion() + " by bwfcwalshy");
		Logger.log(Level.INFO, "");
		Logger.log(Level.INFO, "Found: " + (checker.getFound().isEmpty() ? "Nothing!" : "\n" + checker.getFound()));
		Logger.log(Level.INFO, "Plugin is " + checker.getWarningLevel() + "!");
		Logger.log(Level.INFO, "");
		Logger.log(Level.INFO,
				"If you would like any support on what JarChecker is or maybe why your plugin was flagged join the IRC channel #JarChecker on irc.esper.net!");
		suspiciousClasses = checker.getSuspiciousClasses();
		Platform.runLater(() -> {
			showClasses.setDisable(false);
		});
	}

	/**
	 * @param progress
	 *            The progress, negative for indefinite, between 0 and 1 for
	 *            displaying
	 */
	public void setProgress(double progress) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> setProgress(progress));
			return;
		}
		this.logPane.getProgressBar().setProgress(progress);
	}

	@FXML
	void onExit(ActionEvent event) {
		System.exit(1);
	}

	@FXML
	void onImportFileCreator(ActionEvent event) {
		try {
			Stage importer = new Stage();
			FXMLLoader loader = new FXMLLoader(
					ImportFileCreatorController.class.getResource("ImportFileCreatorWindow.fxml"));
			GridPane pane = loader.load();

			importer.setScene(new Scene(pane));
			importer.initModality(Modality.APPLICATION_MODAL);
			importer.initOwner(Main.getInstance().getPrimaryStage());

			ImportFileCreatorController controller = loader.getController();
			controller.setThisStage(importer);

			importer.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return The log pane
	 */
	public LogPaneController getLogPane() {
		return logPane;
	}

	public Map<String, String> getSuspiciousClasses() {
		return suspiciousClasses;
	}
}
