package com.bwfcwalshy.jarchecker.jfx_gui;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.bwfcwalshy.jarchecker.gui.MainWindow;
import com.bwfcwalshy.jarchecker.jfx_gui.log.LogPaneController;
import com.bwfcwalshy.jarchecker.jfx_gui.utils.import_file_creator.ImportFileCreatorController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * The controller for the main window
 */
public class MainWindowController {

	private LogPaneController logPane;
	
	private ProgressIndicator progressIndicator;
	
    @FXML
    private BorderPane borderPane;
	
	@FXML
	private MenuItem checkFileMenuItem;

	@FXML
	private MenuItem exitMenuItem;

	@FXML
	private MenuItem aboutMenuItem;

	@FXML
	private void initialize() {
		{
			Image exitIcon = new Image(MainWindow.class.getResource("/resources/exit icon.png").toString(), 20, 20,
					true, true);
			exitMenuItem.setGraphic(new ImageView(exitIcon));
		}

		{
			Image scanIcon = new Image(MainWindow.class.getResource("/resources/scan icon 2.png").toString(), 20, 20,
					true, true);
			checkFileMenuItem.setGraphic(new ImageView(scanIcon));
		}

		{
			Image aboutIcon = new Image(MainWindow.class.getResource("/resources/help icon color.png").toString(), 20, 20,
					true, true);
			aboutMenuItem.setGraphic(new ImageView(aboutIcon));
		}
		
		{
			try {
				FXMLLoader loader = new FXMLLoader(LogPaneController.class.getResource("LogPane.fxml"));
				BorderPane pane = loader.load();
				logPane = loader.getController();
				borderPane.setCenter(pane);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	void onAbout(ActionEvent event) {
		AboutWindow.show(AppMain.getInstance().getPrimaryStage());
	}

	@FXML
	void onCheckFile(ActionEvent event) {
		Alert alert = new Alert(AlertType.NONE);
		alert.getButtonTypes().add(ButtonType.CLOSE);
		alert.setHeaderText("Decompiling");
		progressIndicator = new ProgressIndicator();
		progressIndicator.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		progressIndicator.setPrefSize(100, 100);
		alert.getDialogPane().setContent(progressIndicator);
		
		Timer timer = new Timer("test");
		timer.scheduleAtFixedRate(new TimerTask() {

			double counter = -0.5;
			@Override
			public void run() {
				if(counter > 1) {
					this.cancel();
					return;
				}
				counter += 0.01;
				setProgress(counter);
			}
		}, 0, 100);
		
		alert.showAndWait();
		progressIndicator = null;
	}
	
	/**
	 * @param progress The progress, negative for indefinite, between 0 and 1 for displaying
	 */
	public void setProgress(double progress) {
		if(this.progressIndicator != null) {
			if(!Platform.isFxApplicationThread()) {
				Platform.runLater(() -> setProgress(progress));
				return;
			}
			this.progressIndicator.setProgress(progress);
		}
	}
	
	@FXML
	void onExit(ActionEvent event) {
		System.exit(1);
	}
	

	@FXML
	void onImportFileCreator(ActionEvent event) {
		try {
			Stage importer = new Stage();
			FXMLLoader loader = new FXMLLoader(ImportFileCreatorController.class.getResource("ImportFileCreatorWindow.fxml"));
			GridPane pane = loader.load();
			
			importer.setScene(new Scene(pane));
			importer.initModality(Modality.APPLICATION_MODAL);
			importer.initOwner(AppMain.getInstance().getPrimaryStage());
			
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
}
