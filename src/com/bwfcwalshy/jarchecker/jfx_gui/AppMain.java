package com.bwfcwalshy.jarchecker.jfx_gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TEMP main class for the new GUI
 */
public class AppMain extends Application {

	// too lazy to pass it through constructors.
	private static AppMain instance;
	
	private Stage primaryStage;
	
	/**
	 * Default constructor
	 */
	public AppMain() {
		instance = this;
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		
		FXMLLoader loader = new FXMLLoader(AppMain.class.getResource("MainWindow.fxml"));
		BorderPane mainPane = loader.load();
		
		Scene scene = new Scene(mainPane);
		primaryStage.setScene(scene);
		
		primaryStage.show();
	}

	/**
	 * @return The primary stage
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	/**
	 * @return The main class instance
	 */
	public static AppMain getInstance() {
		return instance;
	}
	
	/**
	 * @param args The arguments passed to this program
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
