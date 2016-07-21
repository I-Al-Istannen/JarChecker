package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;

import com.bwfcwalshy.jarchecker.jfx_gui.Logger;
import com.bwfcwalshy.jarchecker.jfx_gui.MainWindowController;
import com.bwfcwalshy.jarchecker.jfx_gui.log.LogPaneController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TEMP main class for the new GUI
 */
public class Main extends Application {

	private final String VERSION = "v0.8.5";
	
	// too lazy to pass it through constructors.
	private static Main instance;
	
	private Stage primaryStage;
	
	private Settings settings;
		
	private MainWindowController mainWindowController;
	
	/**
	 * Default constructor
	 */
	public Main() {
		instance = this;
		settings = new Settings(false, Level.FINEST);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		if(getParameters().getNamed().containsKey("logLevel")) {
			String level = getParameters().getNamed().get("logLevel");
			try {
				settings.setMinLogLevel(Level.parse(level));
			} catch(IllegalArgumentException e) {
				System.out.println("Illegal named param: " + level + " for key 'logLevel'!");
			}
		}
		if(getParameters().getNamed().containsKey("jarFile")) {
			settings.setNoGui(true);
			String filePath = getParameters().getNamed().get("jarFile");
			File jarFile = new File(filePath);
			if(!jarFile.exists()) {
				System.out.println("File '" + filePath + "' doesn't exist.");
				System.exit(-1);
			}
			
			Optional<Path> decompiledPath = JarDecompiler.decompile(jarFile.toPath(), Main.getInstance().getSettings());
			if(!decompiledPath.isPresent()) {
				System.out.println("Error while decompiling.");
				System.exit(-1);
			}
			jarFile = decompiledPath.get().toFile();
			
			// TODO: Externalize this.
			System.out.println("Checking: " + jarFile.getName() + " " + jarFile.getAbsolutePath());
			Checker checker = new Checker();
			checker.check(jarFile);
			Logger.log(Level.INFO, "-----------------------------------------------------");
			Logger.log(Level.INFO, "File name: " + jarFile.getName());
			Logger.log(Level.INFO, "");
			Logger.log(Level.INFO, "File checked with JarChecker " + getVersion() + " by bwfcwalshy");
			Logger.log(Level.INFO, "");
			Logger.log(Level.INFO, "Found: " + (checker.getFound().isEmpty() ? "Nothing!" : "\n" + checker.getFound()));
			Logger.log(Level.INFO, "Plugin is " + checker.getWarningLevel() + "!");
			Logger.log(Level.INFO, "");
			Logger.log(Level.INFO, 
					"If you would like any support on what JarChecker is or maybe why your plugin was flagged join the IRC channel #jarchecker on irc.esper.net!");
			
			System.exit(0);
		}
		System.out.println("Minimum Log level set to: " + settings.getMinLogLevel() + " (" + settings.getMinLogLevel().intValue() + ")");
		this.primaryStage = primaryStage;
		
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("jfx_gui/MainWindow.fxml"));
		BorderPane mainPane = loader.load();
		mainWindowController = loader.getController();
		
		Scene scene = new Scene(mainPane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("JarChecker " + VERSION + " by bwfcwalshy");
		
		primaryStage.show();
	}

	/**
	 * @return The primary stage
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	/**
	 * @return The log pane controller
	 */
	public LogPaneController getLogPane() {
		return mainWindowController.getLogPane();
	}
	
	/**
	 * @return The controller for the main window
	 */
	public MainWindowController getMainWindowController() {
		return mainWindowController;
	}
	
	/**
	 * @return The {@link Settings}
	 */
	public Settings getSettings() {
		return settings;
	}
	
	/**
	 * @return The version.
	 */
	public String getVersion() {
		return VERSION;
	}
	
	/**
	 * @return The main class instance
	 */
	public static Main getInstance() {
		return instance;
	}
	
	/**
	 * @param args The arguments passed to this program
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
