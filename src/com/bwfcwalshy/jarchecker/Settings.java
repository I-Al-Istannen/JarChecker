package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * Holds some global settings
 */
public class Settings {

	private final Path WORKING_DIR;
	private final Path FERNFLOWER_FILE;
	
	private boolean noGui;
	
	private Level minLogLevel;
	
	/**
	 * @param noGui True if no Gui should be shown
	 * @param minLogLevel The minimum log level to show it. Only relevant for the console output, not for the Gui
	 */
	public Settings(boolean noGui, Level minLogLevel) {
		this.noGui = noGui;
		this.minLogLevel = minLogLevel;
	}

	// initialize the static constants
	{
		// Obtain the data folder
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("WIN")) {
			WORKING_DIR = Paths.get(System.getenv("APPDATA") + File.separator + ".JarChecker");
		} else if (OS.contains("MAC")) {
			WORKING_DIR = Paths.get(System.getProperty("user.home") + "/Library/Application " + "Support"
					+ File.separator + ".JarChecker");
		} else if (OS.contains("NUX")) {
			WORKING_DIR = Paths.get(System.getProperty("user.home") + File.separator + ".JarChecker");
		} else {
			WORKING_DIR = Paths.get(System.getProperty("user.home") + File.separator + ".JarChecker");
		}

		try {
			Files.createDirectories(WORKING_DIR);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FERNFLOWER_FILE = WORKING_DIR.resolve("fernflower.jar");
	}
	
	/**
	 * @return The working folder
	 */
	public Path getWorkingDir() {
		return WORKING_DIR;
	}
	
	/**
	 * @return The Path to the fernflower file
	 */
	public Path getFernflowerFile() {
		return FERNFLOWER_FILE;
	}
	
	/**
	 * @return True if no gui should be shown
	 */
	public boolean isNoGui() {
		return noGui;
	}
	
	/**
	 * @param noGui True of no gui should be shown
	 */
	public void setNoGui(boolean noGui) {
		this.noGui = noGui;
	}
	
	/**
	 * Only relevant for the console output, not for the Gui
	 * 
	 * @return The minimum log level.
	 */
	public Level getMinLogLevel() {
		return minLogLevel;
	}
	
	/**
	 * @param minLogLevel The new min log level. Only applied to console, not the Gui
	 */
	public void setMinLogLevel(Level minLogLevel) {
		this.minLogLevel = minLogLevel;
	}
	
	/**
	 * @return True if fernflower exists
	 */
	public boolean existsFernflower() {
		return Files.exists(getFernflowerFile());
	}
}
