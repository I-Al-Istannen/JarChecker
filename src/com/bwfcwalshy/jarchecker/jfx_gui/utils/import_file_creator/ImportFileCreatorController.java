package com.bwfcwalshy.jarchecker.jfx_gui.utils.import_file_creator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import com.bwfcwalshy.jarchecker.jfx_gui.AppMain;
import com.bwfcwalshy.jarchecker.jfx_gui.Logger;
import com.bwfcwalshy.jarchecker.symbol_tables.ImportFileCreationUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * The import file creator
 */
public class ImportFileCreatorController {

	private File library, exportFile;
	
	private Stage thisStage;

	@FXML
	private Button pickFileButton;

	@FXML
	private Button pickFolderButton;

	@FXML
	private Label libraryLocationLabel;

	@FXML
	private Label outputFileLabel;

	@FXML
	void onPickFile(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("Jar-Files", "*.jar"));
		chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));

		chooser.setTitle("Choose a library");
		File file = chooser.showOpenDialog(AppMain.getInstance().getPrimaryStage());

		if (file == null) {
			return;
		}
		library = file;

		libraryLocationLabel.setText(library.getAbsolutePath());
	}

	@FXML
	void onPickFolder(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("Textfiles", "*.txt"));
		chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));

		chooser.setTitle("Choose a file to save to");

		File file = chooser.showSaveDialog(AppMain.getInstance().getPrimaryStage());

		if (file == null) {
			return;
		}

		exportFile = file;
		
		if(!exportFile.exists()) {
			try {
				exportFile.createNewFile();
			} catch (IOException e) {
				Logger.logException(Level.WARNING, e);
				e.printStackTrace();
			}
		}

		outputFileLabel.setText(exportFile.getAbsolutePath());
	}
	
	/**
	 * @param thisStage This stage
	 */
	public void setThisStage(Stage thisStage) {
		this.thisStage = thisStage;
	}

	@FXML
	void onClose(ActionEvent event) {
		thisStage.hide();
	}

	@FXML
	void onExport(ActionEvent event) {
		ImportFileCreationUtil.writeJarImportsToFile(library, exportFile.toPath());
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(thisStage);
		alert.setHeaderText("Imports saved.");
		alert.setContentText("If an error occured, you will see it in the log.");
		
		alert.show();
	}
}
