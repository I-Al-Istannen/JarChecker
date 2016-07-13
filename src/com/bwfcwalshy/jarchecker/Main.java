package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.zip.ZipException;

public class Main {
	
	private static final String VERSION = "v0.5";
	
	public static void main(String[] args) throws ZipException, IOException{
		Scanner scanner = new Scanner(System.in);
		String path = scanner.next();
		
		Decompiler decompiler = new Decompiler();
		File f = new File(path);
		File export = new File(f.getName().replace(".jar", "") + "-src");
		
		Logger.print("Decompiling file.");
		boolean success = decompiler.decompile(f, export);
		Checker checker = new Checker();
		if(success){
			Logger.print("Decompiled jar file!");
			checker.check(new File(export.getAbsolutePath() + File.separator + f.getName()));
		}else{
			Logger.error("Unable to decompile jar file!!");
			System.exit(1);
		}
		Logger.print("-----------------------------------------------------");
		Logger.printNoInfo("Jar name: " + f.getName());
		Logger.emptyLine();
		Logger.printNoInfo("File checked with JarChecker " + VERSION + " by bwfcwalshy");
		Logger.emptyLine();
		Logger.printNoInfo("Found: " + (checker.getFound().isEmpty() ? "Nothing!" : "\n" + checker.getFound()));
		Logger.printNoInfo("Plugin is " + checker.getWarningLevel() + "!");
		Logger.emptyLine();
		Logger.printNoInfo("JarChecker is not always 100% accurate there is still always a risk.");
		Logger.printNoInfo("If you would like someone to check the jar or you have any questions about JarChecker join the IRC channel #jarchecker on EsperNet.");
		
		scanner.close();
	}
}
