package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.io.IOException;

public class Decompiler {
	
	public boolean decompile(File f, File export){
		if(!export.exists())
			export.mkdir();
		
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "java -jar fernflower.jar " + f.getAbsolutePath() + " " + export.getAbsolutePath());
		try {
			Process p = builder.start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			Logger.error(e);
			return false;
		}
		return true;
	}
}
