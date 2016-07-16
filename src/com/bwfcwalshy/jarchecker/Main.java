package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipException;

import com.bwfcwalshy.jarchecker.gui.MainWindow;

public class Main {

    private static boolean debug = false;
    private static final String VERSION = "v0.7.5";
    public static MainWindow mainWindow;
    public static boolean nogui = false;
    private static boolean nobar = false;

    public static void main(String[] args) throws ZipException, IOException {
	for(String s : args) {
	    if(s.equalsIgnoreCase("--debug")) {
		debug = true;
	    }
	    if(s.equalsIgnoreCase("--nobar")) {
		nobar = true;
	    }
	}
	if(args.length > 0 && !args[0].equalsIgnoreCase("--debug")) {
	    nogui = true;
	    String path;
	    Scanner scanner = null;
	    if(args[0].equalsIgnoreCase("nogui")) {
		scanner = new Scanner(System.in);
		path = scanner.next();
	    } else path = args[0];


	    decompilerStart(path);


	    if(scanner != null) scanner.close();
	} else {
	    // GUI goes here
	    MainWindow mw = new MainWindow();
	    mw.setVisible(true);
	    Main.mainWindow = mw;
	}
    }

    public static String getVersion() {
	return VERSION;
    }

    public static Map<String, String> decompilerStart(String path) {
	Decompiler decompiler = new Decompiler();
	File f = new File(path);
	File export = new File(f.getName().replace(".jar", "") + "-src");

	if(!f.exists()) {
	    Logger.error("The file " + f.getAbsolutePath() + " does not exist!");
	    return null;
	}
	Logger.debug("Starting check of: " + f.getAbsolutePath());
	boolean success = true;
	Checker checker = new Checker();
	try{Thread.sleep(100);}catch(Exception e){}
	if(path.endsWith(".jar")) {
	    Logger.print("Decompiling file.");
	    success = decompiler.decompile(f, export);
	    if(success){
		Logger.print("Decompiled jar file!");
		checker.check(new File(export.getAbsolutePath() + File.separator + f.getName()));
	    }else{
		Logger.error("Unable to decompile jar file!!");
		System.exit(1);
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

	return checker.getSuspiciusClasses();
    }

    public static boolean printDebug() {
	return debug;
    }

    public static void doDebug(boolean debug) {
	Main.debug = debug;
    }
    
    public static boolean printBar() {
	return !nobar;
    }
}
