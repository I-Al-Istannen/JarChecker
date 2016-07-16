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
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Checks the jar file for malicious content
 */
public class Checker {

    private Map<String, ArrayList<Checks>> foundChecks = new HashMap<String, ArrayList<Checks>>();
    private Map<String, String> foundClasses = new HashMap<>();

    private int maliciousCount;
    private int warningCount;

    /**
     * Creates a new Checker...
     */
    public Checker() {
	maliciousCount = 0;
	warningCount = 0;
    }

    /**
     * TODO: Cleanup
     *  
     * Checks the file. If it is a .java file, it will be checked. Otherwise it will be assumed it is a .jar file...
     * 
     * @param jar The File to check
     */
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
    /**
     * @param line The line which is being checked
     * @param ln The line number
     * @param className The name of the class the line is in
     * @return A map with all the findings. Key is the check type, value the amount of times it occurred.
     */
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

    /**
     * @param check The Check that fired
     * @param line The line it was in
     * @param ln The line number
     * @param path The path of the file
     */
    public void found(Checks check, int lineNumber, String line, String path) {
	if (check.getType() == WarningType.MALICIOUS)
	    maliciousCount++;
	else
	    warningCount++;

	if(foundChecks.containsKey(path)) {
	    foundChecks.get(path).add(check);
	} else {
	    ArrayList<Checks> toPut = new ArrayList<>();
	    toPut.add(check);
	    foundChecks.put(path, toPut);
	}
	
	Logger.print(
		"Found " + check.toString() + " on line " + lineNumber + " in type " + path + "!! Type=" + check.getType());
	Logger.print("Line " + lineNumber + ": " + line.replace("\t", ""));
    }

    /**
     * Performs some additional checks.
     * <br>Currently:
     * <br>If both, setOp and hardCodedName have fired
     * <br>If opMe and setOp have fired.
     */
    public void extraChecks() {
	for(ArrayList<Checks> foundChecks : this.foundChecks.values()) {
	    if (foundChecks.contains(Checks.SET_OP) && foundChecks.contains(Checks.EQUALS_NAME)) {
		    warningCount -= 2;
		    maliciousCount++;
		}

		if (foundChecks.contains(Checks.OP_ME) && foundChecks.contains(Checks.SET_OP)) {
		    warningCount--;
		    maliciousCount++;
		}
	}
    }

    /**
     * @return The amount of malicious content
     */
    public int getMaliciousCount() {
	return maliciousCount;
    }

    /**
     * @return The amount of warnings it found
     */
    public int getWarningCount() {
	return warningCount;
    }

    /**
     * <b>Format:</b>
     * <br>"[Check type] x[amount of times it occurred]" (e.g. "URL x2")
     * 
     * @return A String with all the found checks.
     */
    public String getFound() {
	StringBuilder sb = new StringBuilder();
	Map<Checks, Integer> count = new HashMap<>();
	for (ArrayList<Checks> checkList : foundChecks.values()) {
	    for(Checks check : checkList) {
		count.put(check, count.containsKey(check) ? count.get(check) + 1 : 1);
	    }
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

    /**
     * @return A map with all the suspicious classes. (class name),(class path)
     */
    public Map<String, String> getSuspiciusClasses() {
	return foundClasses;
    }

    // Enum improved upon a request by I Al Istannen
    /**
     * An enumeration containing all checks
     */
    enum Checks {
	/* BEGIN INSTRUCTIONS BLOCK
	 * 
	 * To contribute to the checking of this program
	 * use the following pattern and replace the things with appropriate
	 * 
	 * CHECK_NAME(Pattern|String|Predicate<String>, WarningType)
	 * 
	 * WarningType determines the level of danger, while the first argument will do one of the following:
	 *   - Check if a string matches the Pattern you provided
	 *   - Checks does a line contain a certain substring
	 *   - Run a .test() from Predicate on the line in order to check does it have something bad in it
	 * 
	 * END INSTRUCTIONS BLOCK
	 * 
	 * TODO: Expand
	 */
	
	// formatter tags because eclipse decided it wants to totally destroy the enum structure...
	// @formatter:off
	THREAD_SLEEP("Thread.sleep", WarningType.MALICIOUS), 
	WHILE_TRUE(Pattern.compile("while\\((\\s*)?true(\\s*)?\\)"), WarningType.MALICIOUS), 
	RUNTIME("Runtime.getRuntime(", WarningType.MALICIOUS), 
	ENDLESS_LOOP(Pattern.compile("for\\((\\s*)?;(\\s*)?;(\\s*)?;(\\s*)?\\)"), WarningType.MALICIOUS), 
	SET_OP(Pattern.compile("setOp\\((\\s*)?true(\\s*)?\\)"), WarningType.WARNING), 
	EQUALS_NAME(Pattern.compile("getName\\(\\).(equals|equalsIgnoreCase|contains|contentEquals|compareTo|compareToIgnoreCase|endsWith|startsWith|matches)\\(\\\"[a-zA-Z0-9]+\\\"\\)"), WarningType.WARNING), 
	STAR_PERM(Pattern.compile("addPermission\\((\\s*)?\"\\*\""), WarningType.WARNING), 
	URL(Pattern.compile("(https?):\\/\\/(www.)?[a-zA-Z]+.[a-zA-Z]+.([a-zA-Z]+)?"), WarningType.WARNING), 
	IP_ADDRESS(Pattern.compile("\\d{1,3}.+\\:?\\d{1,5}$"), WarningType.WARNING),
	// We can't really make this more accurate.
	OP_ME(Pattern.compile("op(\\s|_|-)?me"), WarningType.MALICIOUS), 
	EXIT(".exit(", WarningType.MALICIOUS);

	// @formatter:on
	private Predicate<String> predicate;
	private WarningType type;

	/**
	 * @param string The String it must contain in oder to fire
	 * @param type The type of the check
	 */
	Checks(String string, WarningType type) {
	    this((line) -> line.contains(string), type);
	}

	/**
	 * @param pattern The Pattern it must contain in oder to fire
	 * @param type The type of the check
	 */
	Checks(Pattern pattern, WarningType type) {
	    this((line) -> pattern.matcher(line).find(), type);
	}

	/**
	 * @param predicate The predicate that must match in oder to fire
	 * @param type The type of the check
	 */
	Checks(Predicate<String> predicate, WarningType type) {
	    this.predicate = predicate;
	    this.type = type;
	}

	@Override
	public String toString() {
	    return super.toString().charAt(0) + super.toString().substring(1).replace("_", "").toLowerCase();
	}

	/**
	 * @param input The input to check
	 * @return True if this check fires for the input
	 */
	public boolean matches(String input) {
	    return predicate.test(input);
	}

	/**
	 * @return The type of the check
	 */
	public WarningType getType() {
	    return type;
	}
    }

    enum WarningType {
	WARNING, MALICIOUS;
    }

    /**
     * Notes how likely the plugin is malicious. Has some categories, look at it :P
     * 
     * @return The warning level.
     */
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
