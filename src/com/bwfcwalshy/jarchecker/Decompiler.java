package com.bwfcwalshy.jarchecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jd.core.DecompilerException;

public class Decompiler {
	
	private Map<String, List<String>> jarData;

	//TODO: Tidy this method up
	public boolean decompile(File f){
		jarData = new HashMap<>();
		jd.core.Decompiler decompiler = new jd.core.Decompiler();
		try {
			Map<String, String> decompiled = decompiler.decompile(f.getAbsolutePath());
			for(String s : decompiled.keySet()){
				if(s.endsWith(".java")){
					String[] lines = decompiled.get(s).split("\n");
					List<String> classLines = new ArrayList<>();
					for(String line : lines){
						classLines.add(line);
					}
					jarData.put(s, classLines);
				}
			}
			return true;
		} catch (DecompilerException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Map<String, List<String>> getData(){
		return this.jarData;
	}
}
