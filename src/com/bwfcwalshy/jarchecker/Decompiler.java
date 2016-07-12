package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.io.IOException;

public class Decompiler {
	
	public boolean decompile(File f, File export){
		if(!export.exists())
			export.mkdir();
		
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "java -jar C:\\temp\\Tests\\fernflower.jar " + f.getAbsolutePath() + " " + export.getAbsolutePath());
		try {
			Process p = builder.start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
