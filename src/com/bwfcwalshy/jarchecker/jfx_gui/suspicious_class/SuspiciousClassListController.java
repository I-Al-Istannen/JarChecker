package com.bwfcwalshy.jarchecker.jfx_gui.suspicious_class;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import com.bwfcwalshy.jarchecker.Main;
import com.bwfcwalshy.jarchecker.jfx_gui.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SuspiciousClassListController {

	@FXML
	private ListView<String> classList;

	@FXML
	private BorderPane borderPane;

	@FXML
	private void initialize() {
		classList.setEditable(false);
		for(String path : Main.getInstance().getMainWindowController().getSuspiciousClasses().keySet()){
			classList.getItems().add(path);
		}
	}
	
	@FXML
	private void onListClick() {
		Map<String, String> suspiciusClasses;
		if((suspiciusClasses = Main.getInstance().getMainWindowController().getSuspiciousClasses()) != null) {
			String clazz;
			if((clazz = suspiciusClasses.get(classList.getSelectionModel().getSelectedItem())) != null) {
				try {
					FXMLLoader fxmlLoader = new FXMLLoader(SuspiciousClassController.class.getResource("SuspiciousClass.fxml"));
					Parent root1 = (Parent) fxmlLoader.load();
					Stage stage = new Stage();
					stage.initModality(Modality.APPLICATION_MODAL);
					stage.setScene(new Scene(root1));
					stage.setTitle(classList.getSelectionModel().getSelectedItem());
					stage.show();
					SuspiciousClassController controller = fxmlLoader.getController();
					controller.setClass(clazz);
				} catch (IOException e) {
					Logger.logException(Level.SEVERE, e);
				}
			}
		}
	}
}
