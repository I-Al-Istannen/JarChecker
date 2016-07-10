package com.bwfcwalshy.jarchecker;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Checker {
	
	private int maliciousCount;
	private int warningCount;
	public Checker(){
		maliciousCount = 0;
		warningCount = 0;
	}

	public void check(Decompiler decompiler){
		Map<String, List<String>> data = decompiler.getData();
		for(String clazz : data.keySet()){
			for(String line : data.get(clazz)){
				checkLine(line);
			}
		}
	}
	
	public void checkLine(String line){
		for(Checks check : Checks.values()){
			if(!check.isPattern()){
				if(line.contains(check.getString())){
					found(check);
				}
			}else{
				Matcher matcher = check.getPattern().matcher(line);
				boolean found = matcher.find();
				if(found){
					found(check);
				}
			}
		}
		//System.out.println(line);
	}
	
	public void found(Checks check){
		if(check.getType() == WarningType.MALICIOUS)
			maliciousCount++;
		else
			warningCount++;
		
		if(check == Checks.SET_OP)
			setOp = true;
		else if(check == Checks.EQUALS_NAME)
			hardCodedName = true;
		
		System.out.println("Found " + check + "!! Type=" + check.getType());
	}
	
	private boolean setOp;
	private boolean hardCodedName;
	public void extraChecks(){
		if(setOp && hardCodedName){
			warningCount -= 2;
			maliciousCount++;
		}
	}
	
	public int getMaliciousCount() {
		return maliciousCount;
	}
	
	public int getWarningCount() {
		return warningCount;
	}
	
	enum Checks {
		THREAD_SLEEP("Thread.sleep", WarningType.MALICIOUS),
		WHILE_TRUE("while(true)", WarningType.MALICIOUS),
		RUNTIME("Runtime.getRuntime().", WarningType.MALICIOUS),
		ENDLESS_LOOP("for(;;;)", WarningType.MALICIOUS),
		SET_OP("setOp(true)", WarningType.WARNING),
		EQUALS_NAME(Pattern.compile("getName\\(\\).(equals|equalsIgnoreCase)\\(\\\"[a-zA-Z0-9]+\\\"\\)"), WarningType.WARNING),
		STAR_PERM("addPermission(\"*\"", WarningType.WARNING),
		URL(Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"), WarningType.WARNING);
		
		private String s;
		private Pattern p;
		private WarningType type;
		
		private Checks(String s, WarningType type){
			this.s = s;
			this.type = type;
		}
		
		private Checks(Pattern p, WarningType type){
			this.p = p;
			this.type = type;
		}
		
		public boolean isPattern(){
			return (p != null);
		}
		
		public String getString(){
			return s;
		}
		
		public Pattern getPattern(){
			return p;
		}
		
		public WarningType getType(){
			return type;
		}
	}
	
	enum WarningType {
		WARNING,
		MALICIOUS;
	}
}
