package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipException;

import com.bwfcwalshy.jarchecker.gui.MainWindow;
import com.bwfcwalshy.jarchecker.symbol_tables.ImportFileCreationUtil;

/**
 * The main class.
 */
public class Main {

	private static boolean debug = false;
	private static final String VERSION = "v0.8.2";
	/**
	 * The main window for the gui
	 */
	private static MainWindow mainWindow;
	public static File WORKING_DIR;
	public static File FERNFLOWER;
	private static boolean nogui = false;
	private static boolean nobar = false;
	private static List<String> keywords = new ArrayList<>();

	@SuppressWarnings("javadoc")
	public static void main(String[] args) throws ZipException, IOException {
		// Obtain the data folder
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("WIN"))
			WORKING_DIR = new File(System.getenv("APPDATA") + File.separator + ".JarChecker");
		else if (OS.contains("MAC"))
			WORKING_DIR = new File(System.getProperty("user.home") + "/Library/Application " + "Support"
					+ File.separator + ".JarChecker");
		else if (OS.contains("NUX"))
			WORKING_DIR = new File(System.getProperty("user.home") + File.separator + ".JarChecker");
		else
			WORKING_DIR = new File(System.getProperty("user.home") + File.separator + ".JarChecker");
		WORKING_DIR.mkdirs();
		FERNFLOWER = new File(WORKING_DIR, "fernflower.jar");
		
		keywords.add("--debug");
		keywords.add("--nobar");
		keywords.add("createimports");
		for (String s : args) {
			if (s.equalsIgnoreCase("--debug")) {
				debug = true;
			}
			if (s.equalsIgnoreCase("--nobar")) {
				nobar = true;
			}
		}
		if (args.length == 3 && args[0].equalsIgnoreCase("createImports")) {
			File from = new File(args[1]);
			File to = new File(args[2]);
			if (!to.exists()) {
				to.createNewFile();
			}
			ImportFileCreationUtil.writeJarImportsToFile(from, to.toPath());
		}
		if (args.length > 0) {
			if (!keywords.contains(args[0].toLowerCase())) {
				nogui = true;
				String path;
				Scanner scanner = null;
				if (args[0].equalsIgnoreCase("nogui")) {
					scanner = new Scanner(System.in);
					path = scanner.next();
				} else
					path = args[0];

				decompilerStart(path);

				if (scanner != null)
					scanner.close();
			} else if (args[0].equalsIgnoreCase("createImports")) {
				Logger.print("Usage:");
				Logger.print("createImports <library> <output>");
			}
		} else {
			// GUI goes here
			MainWindow mw = new MainWindow();
			mw.setVisible(true);
			Main.mainWindow = mw;
		}
		
		if(!FERNFLOWER.exists()) {
			Logger.print("Downloading fernflower!");
			final Path target = FERNFLOWER.toPath();
			try {
				final InputStream fromInternet = new URI("https://dl.dropboxusercontent.com/s/b9cna8hproe2smg/fernflower.jar?dl=0").toURL().openStream();
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Files.copy(fromInternet, target, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							Logger.error(e);
						}
						Logger.print("Fernflower downloaded!");
					}
					
				}, "Download").start();
				
			} catch (URISyntaxException | ConnectException e) {
				Logger.error(e);
				Logger.error("Failed!");
				
			}
			
		}
	}

	/**
	 * @return True if it should't show the gui
	 */
	public static boolean isNoGui() {
		return nogui;
	}

	/**
	 * @return The main window for the Gui or null if {@link #isNoGui()} is true
	 */
	public static MainWindow getMainWindow() {
		return mainWindow;
	}

	/**
	 * @return The version of this program
	 */
	public static String getVersion() {
		return VERSION;
	}

	/**
	 * Decompiles and checks the plugin. Returns the findings
	 * 
	 * @param path
	 *            The Path of the file to decompile
	 * @return A Map with all the suspicious classes.
	 */
	public static Map<String, String> decompilerStart(String path) {
		Decompiler decompiler = new Decompiler();
		File f = new File(path);
		File export = new File(WORKING_DIR, f.getName().replace(".jar", "") + "-src");

		if (!f.exists()) {
			Logger.error("The file " + f.getAbsolutePath() + " does not exist!");
			return null;
		}
		Logger.debug("Starting check of: " + f.getAbsolutePath());
		boolean success = true;
		Checker checker = new Checker();
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		if (path.endsWith(".jar")) {
			Logger.print("Decompiling file.");
			success = decompiler.decompile(f, export);
			if (success) {
				Logger.print("Decompiled jar file!");
				checker.check(new File(export.getAbsolutePath() + File.separator + f.getName()));
			} else {
				Logger.error("Unable to decompile jar file!!");
				if (nogui)
					System.exit(1);
				else return null;
			}
		} else {
			checker.check(new File(path));
		}

		Logger.print("-----------------------------------------------------");
		Logger.printNoInfo("File name: " + f.getName());
		Logger.emptyLine();
		Logger.printNoInfo("File checked with JarChecker " + VERSION + " by bwfcwalshy");
		Logger.emptyLine();
		Logger.printNoInfo("Found: " + (checker.getFound().isEmpty() ? "Nothing!" : "\n" + checker.getFound()));
		Logger.printNoInfo("Plugin is " + checker.getWarningLevel() + "!");
		Logger.emptyLine();
		Logger.printNoInfo(
				"If you would like any support on what JarChecker is or maybe why your plugin was flagged join the IRC channel #jarchecker on irc.esper.net!");

		return checker.getSuspiciusClasses();
	}

	/**
	 * @return True if debug information should be printed
	 */
	public static boolean isPrintDebug() {
		return debug;
	}

	/**
	 * @param debug
	 *            True if debug should be enabled
	 */
	public static void setDebug(boolean debug) {
		Main.debug = debug;
	}

	/**
	 * @return True if the progressbar should be printed
	 */
	public static boolean isPrintBar() {
		return !nobar;
	}
}
