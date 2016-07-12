package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.zip.ZipException;

public class Main {
	
	private static final String VERSION = "v0.2";
	
	public static void main(String[] args) throws ZipException, IOException{
		Scanner scanner = new Scanner(System.in);
		String path = scanner.next();
		
		Decompiler decompiler = new Decompiler();
		File f = new File(path);
		System.out.println("Decompiling file.");
		boolean success = decompiler.decompile(f);
		Checker checker = new Checker();
		if(success){
			System.out.println("Decompiled jar file!");
			checker.check(decompiler);
		}else{
			System.err.println("Unable to decompile jar file!!");
			System.exit(1);
		}
		System.out.println("-----------------------------------------------------");
		System.out.println("Jar name: " + f.getName());
		System.out.println();
		System.out.println("File checked with JarChecker " + VERSION + " by bwfcwalshy");
		System.out.println();
		//Today these will no longer show.
		System.out.println("Amount of malicious content: " + checker.getMaliciousCount());
		System.out.println("Amount of potentially malicious content: " + checker.getWarningCount());
		System.out.println();
		//Complete this message
		System.out.println("Plugin is " + checker.getWarningLevel() + "!");
		
		scanner.close();
	}
}
