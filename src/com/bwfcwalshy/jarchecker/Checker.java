package com.bwfcwalshy.jarchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Checker {

    private List<Checks> foundChecks = new ArrayList<>();
    private Map<String, String> foundClasses = new HashMap<>();

    private int maliciousCount;
    private int warningCount;

    public Checker() {
	maliciousCount = 0;
	warningCount = 0;
    }

    public void check(File jarFile) {
	ZipInputStream zipInput = null;
	ZipFile zipFile = null;
	if (jarFile.getName().endsWith(".java")) {
	    // Its a source file
	    try {
		String className = jarFile.getName().substring(0, jarFile.getName().lastIndexOf('.'));
		boolean suspicious = false;
		String clazz = "";
		int lineNumber = 0;
		for (String s : Files.readAllLines(jarFile.toPath())) {
		    Map<Checks, Integer> result = checkLine(s, ++lineNumber, className);
		    clazz += "\n";
		    if (result.size() > 0) {
			suspicious = true;
			clazz += " // Here found: ";
			int rc = 0;
			for (Entry<Checks, Integer> c : result.entrySet()) {
			    rc++;
			    clazz += c.getKey().toString();
			    if (rc < result.size())
				clazz += ", ";
			}
		    }
		    clazz += "\n";
		}
		if (suspicious) {
		    foundClasses.put(className, clazz);
		}
	    } catch (IOException e) {
		Logger.error(e);
	    }
	} else
	    try {
		// Its a Fernflower decompiled file
		zipFile = new ZipFile(jarFile);
		zipInput = new ZipInputStream(new FileInputStream(jarFile));
		ZipEntry entry;
		// Loop through entries
		while ((entry = zipInput.getNextEntry()) != null) {
		    // Get the full class name (domain.website.project...)
		    String className = entry.getName().replaceFirst("\\.java", "").replace('/', '.');
		    // Marks the file as suspicious so it can be added to the source map
		    boolean suspicious = false;
		    // Stores classes source
		    String clazz = "";
		    // Is it a class?
		    if (entry.getName().endsWith(".java")) {
			// Try-with-resource block, to save us the closes
			try (InputStream zin = zipFile.getInputStream(entry);
				InputStreamReader in = new InputStreamReader(zin);
				BufferedReader br = new BufferedReader(in);) {

			    
			    int lineNumber = 0;
			    // Stores the current line
			    String line;
			    while ((line = br.readLine()) != null) {
				// Adds the current line to the class
				clazz += line;
				// Finds all of the checks that are on this line
				Map<Checks, Integer> result = checkLine(line, ++lineNumber, className);
				// If it found anything
				if (result.size() > 0) {
				    // Mark the class as suspicious
				    suspicious = true;
				    // Mark what it found
				    clazz += " // Here found: ";
				    // Counts the amount of times the loop ran
				    int lc = 0;
				    for (Entry<Checks, Integer> c : result.entrySet()) {
					// Increments the run counter
					lc++;
					// Adds the found check
					clazz += c.getKey().toString();
					// If it is not the last one add a comma and a space
					if (lc < result.size())
					    clazz += ", ";
				    }
				}
				
				// Newline 
				clazz += "\n";
			    }

			}
		    }
		    // Stores the class in a foundClasses map
		    if (suspicious) {
			foundClasses.put(className, clazz);
		    }
		}
	    } catch (IOException e) {
		Logger.error(e);
	    } finally {
		// Closes everything
		try {
		    if (zipInput != null)
			zipInput.close();

		    if (zipFile != null)
			zipFile.close();
		} catch (IOException e) {
		    Logger.error(e);
		}
	    }
    }

    // Function improved upon a request by I Al Istannen
    public Map<Checks, Integer> checkLine(String line, int lineNumber, String className) {
	Map<Checks, Integer> founds = new HashMap<>();
	for (Checks check : Checks.values()) {
	    if (check.matches(line)) {
		found(check, lineNumber, line, className);
		founds.put(check, founds.containsKey(check) ? founds.get(check) + 1 : 1);
	    }
	}

	return founds;
    }

    public void found(Checks check, int lineNumber, String line, String path) {
	if (check.getType() == WarningType.MALICIOUS)
	    maliciousCount++;
	else
	    warningCount++;

	if (check == Checks.SET_OP)
	    setOp = true;
	else if (check == Checks.EQUALS_NAME)
	    hardCodedName = true;
	else if (check == Checks.OP_ME)
	    opMe = true;

	foundChecks.add(check);
	Logger.print(
		"Found " + check.toString() + " on line " + lineNumber + " in type " + path + "!! Type=" + check.getType());
	Logger.print("Line " + lineNumber + ": " + line.replace("\t", ""));
    }

    private boolean setOp;
    private boolean hardCodedName;
    private boolean opMe;

    public void extraChecks() {
	if (setOp && hardCodedName) {
	    warningCount -= 2;
	    maliciousCount++;
	}

	if (opMe && setOp) {
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
	Map<Checks, Integer> count = new HashMap<>();
	for (Checks check : foundChecks) {
	    count.put(check, count.containsKey(check) ? count.get(check) + 1 : 1);
	}
	int rc = 0;
	for (Entry<Checks, Integer> e : count.entrySet()) {
	    rc++;
	    sb.append(e.getKey().toString());
	    sb.append(" x" + e.getValue());
	    if (rc < count.size())
		sb.append("\n");
	}
	return sb.toString();
    }

    public Map<String, String> getSuspiciusClasses() {
	return foundClasses;
    }

    // Enum improved upon a request by I Al Istannen
    enum Checks {
	THREAD_SLEEP("Thread.sleep", WarningType.MALICIOUS), 
	WHILE_TRUE(Pattern.compile("while\\((\\s*)?true(\\s*)?\\)"), WarningType.MALICIOUS), 
	RUNTIME("Runtime.getRuntime(", WarningType.MALICIOUS), 
	ENDLESS_LOOP(Pattern.compile("for\\((\\s*)?;(\\s*)?;(\\s*)?;(\\s*)?\\)"), WarningType.MALICIOUS), 
	SET_OP(Pattern.compile("setOp\\((\\s*)?true(\\s*)?\\)"), WarningType.WARNING), 
	EQUALS_NAME(Pattern.compile("getName\\(\\).(equals|equalsIgnoreCase)\\(\\\"[a-zA-Z0-9]+\\\"\\)"), WarningType.WARNING), 
	STAR_PERM(Pattern.compile("addPermission\\((\\s*)?\"\\*\""), WarningType.WARNING), 
	URL(Pattern.compile("(https?):\\/\\/(www.)?[a-zA-Z]+.[a-zA-Z]+.([a-zA-Z]+)?"), WarningType.WARNING), 
	IP_ADDRESS(Pattern.compile("\\d{1,3}.+\\:?\\d{1,5}$"), WarningType.WARNING),
	// We can't really make this more accurate.
	OP_ME("opme", WarningType.MALICIOUS), 
	EXIT(".exit(", WarningType.MALICIOUS);

	private Predicate<String> predicate;
	private WarningType type;

	private Checks(String s, WarningType type) {
	    this((line) -> line.contains(s), type);
	}

	private Checks(Pattern p, WarningType type) {
	    this((line) -> p.matcher(line).find(), type);
	}

	private Checks(Predicate<String> predicate, WarningType type) {
	    this.predicate = predicate;
	    this.type = type;
	}

	@Override
	public String toString() {
	    return super.toString().charAt(0) + super.toString().substring(1).replace("_", "").toLowerCase();
	}

	public boolean matches(String input) {
	    return predicate.test(input);
	}

	public WarningType getType() {
	    return type;
	}
    }

    enum WarningType {
	WARNING, MALICIOUS;
    }

    public String getWarningLevel() {
	if (maliciousCount == 0 && warningCount == 0)
	    return "not malicious";
	else if ((maliciousCount >= 1 && maliciousCount < 3) && warningCount < 3)
	    return "likely malicious";
	else if (maliciousCount >= 1 && warningCount >= 3)
	    return "malicious";
	else if (maliciousCount == 0 && warningCount > 3)
	    return "possibily malicious";
	else if (maliciousCount == 0 && warningCount <= 3)
	    return "probably not malicious";
	else
	    return "Tell bwfcwalshy to add something here! " + maliciousCount + "," + warningCount;
    }

}
