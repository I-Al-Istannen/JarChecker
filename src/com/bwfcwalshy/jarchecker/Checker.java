package com.bwfcwalshy.jarchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Checker {

    private List<Checks> foundChecks = new ArrayList<>();
    private Map<String, String> foundClasses = new HashMap<>();

    private int maliciousCount;
    private int warningCount;
    public Checker(){
	maliciousCount = 0;
	warningCount = 0;
    }

    public void check(File jar){
	ZipInputStream input = null;
	ZipFile zip = null;
	try{
	    zip = new ZipFile(jar);
	    input = new ZipInputStream(new FileInputStream(jar));
	    ZipEntry entry;
	    while((entry = input.getNextEntry()) != null){
		String className = entry.getName().replaceFirst("\\.java", "").replace('/', '.');
		boolean suspicious = false;
		String clazz = "";
		if(entry.getName().endsWith(".java")){
		    InputStream zin = zip.getInputStream(entry);
		    InputStreamReader in = new InputStreamReader(zin);
		    BufferedReader br = new BufferedReader(in);
		    int ln = 0;
		    String line;
		    while((line = br.readLine()) != null){
			clazz += line;
			List<Checks> result = checkLine(line, ++ln, className);
			if(result.size() > 0){
			    suspicious = true;
			    clazz += " // Here found: ";
			    for(Checks c : result) {
				clazz += c.toString() + " ";
			    }
			}

			clazz += "\n";
		    }
		    zin.close();
		    in.close();
		    br.close();
		}
		if(suspicious) {
		    foundClasses.put(className, clazz);
		}
	    }
	}catch(IOException e){
	    e.printStackTrace();
	}finally{
	    try {
		if(input != null)
		    input.close();

		if(zip != null)
		    zip.close();
	    }catch(IOException e){
		e.printStackTrace();
	    }
	}
    }

    public List<Checks> checkLine(String line, int ln, String className){
	List<Checks> founds = new ArrayList<>();
	for(Checks check : Checks.values()){
	    if(!check.isPattern()){
		if(line.contains(check.getString())) {
		    found(check, ln, line, className);
		    founds.add(check);
		}
	    }else{
		Matcher matcher = check.getPattern().matcher(line);
		boolean found = matcher.find();
		if(found) {
		    found(check, ln, line, className);
		    founds.add(check);
		}
	    }
	}

	return founds;
    }

    public void found(Checks check, int ln, String line, String path){
	if(check.getType() == WarningType.MALICIOUS)
	    maliciousCount++;
	else
	    warningCount++;

	if(check == Checks.SET_OP)
	    setOp = true;
	else if(check == Checks.EQUALS_NAME)
	    hardCodedName = true;
	else if(check == Checks.OP_ME)
	    opMe = true;

	foundChecks.add(check);
	Logger.print("Found " + check.toString() + " on line " + ln + " in type " + path + "!! Type=" + check.getType());
	Logger.print("Line " + ln + ": " + line.replace("\t", ""));
    }

    private boolean setOp;
    private boolean hardCodedName;
    private boolean opMe;
    public void extraChecks(){
	if(setOp && hardCodedName){
	    warningCount -= 2;
	    maliciousCount++;
	}

	if(opMe && setOp){
	    warningCount--;
	    maliciousCount++;
	}
    }

    public int getMaliciousCount() {
	return maliciousCount;
    }

    public int getWarningCount() {
	return warningCount;
    }

    public String getFound() {
	StringBuilder sb = new StringBuilder();
	for(Checks check : foundChecks){
	    sb.append(check.toString().charAt(0) + check.toString().replace("_", " ").substring(1).toLowerCase() + "\n");
	}
	return sb.toString();
    }

    public Map<String, String> getSuspiciusClasses(){
	return foundClasses;
    }

    enum Checks {
	THREAD_SLEEP("Thread.sleep", WarningType.MALICIOUS),
	WHILE_TRUE(Pattern.compile("while\\((\\s*)?true(\\s*)?\\)"), WarningType.MALICIOUS),
	RUNTIME("Runtime.getRuntime(", WarningType.MALICIOUS),
	ENDLESS_LOOP(Pattern.compile("for\\((\\s*)?;(\\s*)?;(\\s*)?;(\\s*)?\\)"), WarningType.MALICIOUS),
	SET_OP(Pattern.compile("setOp\\((\\s*)?true(\\s*)?\\)"), WarningType.WARNING),
	EQUALS_NAME(Pattern.compile("getName\\(\\).(equals|equalsIgnoreCase)\\(\\\"[a-zA-Z0-9]+\\\"\\)"), WarningType.WARNING),
	STAR_PERM("addPermission(\"*\"", WarningType.WARNING),
	URL(Pattern.compile("(https?):\\/\\/(www.)?[a-zA-Z]+.[a-zA-Z]+.([a-zA-Z]+)?"), WarningType.WARNING),
	IP_ADDRESS(Pattern.compile("\\d{1,3}.+\\:?\\d{1,5}$"), WarningType.WARNING),
	//We can't really make this more accurate.
	OP_ME("opme", WarningType.MALICIOUS),
	EXIT(".exit(", WarningType.MALICIOUS);

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

	@Override
	public String toString(){
	    return super.toString().charAt(0) + super.toString().substring(1).replace("_", "").toLowerCase();
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

    public String getWarningLevel() {
	if(maliciousCount == 0 && warningCount == 0)
	    return "not malicious";
	else if((maliciousCount >= 1 && maliciousCount < 3)&& warningCount < 3)
	    return "likely malicious";
	else if(maliciousCount >= 1 && warningCount >= 3)
	    return "malicious";
	else if(maliciousCount == 0 && warningCount > 3)
	    return "possibily malicious";
	else if(maliciousCount == 0 && warningCount <= 3)
	    return "probably not malicious";
	else
	    return "Tell bwfcwalshy to add something here! " + maliciousCount + "," + warningCount;
    }

}
