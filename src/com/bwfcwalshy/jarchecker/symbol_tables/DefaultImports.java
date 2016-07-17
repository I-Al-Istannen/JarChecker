package com.bwfcwalshy.jarchecker.symbol_tables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import com.bwfcwalshy.jarchecker.Logger;

/**
 * The default imports
 */
public enum DefaultImports {

    /**
     * All the imports from the Java lang package
     */
    JAVA_LANG(() -> {
	String rtJarPath = System.getProperty("sun.boot.class.path");
	rtJarPath = Arrays.stream(rtJarPath.split(";")).filter(string -> string.endsWith("rt.jar")).findAny().get();

	return DefaultImports.getImportsFromJar(new File(rtJarPath), entry -> {
	    return entry.getName().contains("java/lang") && entry.getName().split("/").length == 3
		    && !entry.getName().contains("$");
	}).stream().map(fullyQualified -> fullyQualified + "=" + simplifyName(fullyQualified))
		.collect(Collectors.toMap(string -> string.split("=")[1], string -> string.split("=")[0]));
    }),
    /**
     * All the imports from the supplied "bukkit-imports-joined.txt" file.
     */
    BUKKIT(() -> {
	try (InputStream stream = DefaultImports.class.getResourceAsStream("bukkit-imports-joined.txt");
		InputStreamReader inR = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(inR);) {

	    String tmp;
	    Map<String, String> map = new HashMap<>();
	    while ((tmp = reader.readLine()) != null) {
		if (!tmp.contains("=")) {
		    continue;
		}
		map.put(tmp.split("=")[0], tmp.split("=")[1]);
	    }
	    return map;
	} catch (IOException e) {
	    Logger.error(e);
	}
	// better crash right! xD Silently is too boring
	return null;
    });

    private Map<String, String> imports;

    private DefaultImports(Map<String, String> imports) {
	this.imports = imports;
    }

    private DefaultImports(Supplier<Map<String, String>> supplier) {
	this(supplier.get());
    }

    /**
     * @return The Imports. Unmodifiable.
     */
    public Map<String, String> getImports() {
	return Collections.unmodifiableMap(imports);
    }

    /**
     * @param file
     *            The file to read from
     * @param filter
     *            The filter the files must match
     * @return A set with all imports from the jar file.
     */
    public static Set<String> getImportsFromJar(File file, Predicate<ZipEntry> filter) {
	if (!file.getAbsolutePath().endsWith(".jar")) {
	    return Collections.emptySet();
	}
	Set<String> toReturn = new HashSet<>();
	try (JarFile jarFile = new JarFile(file)) {

	    Enumeration<JarEntry> entries = jarFile.entries();

	    while (entries.hasMoreElements()) {
		ZipEntry entry = entries.nextElement();
		if (!filter.test(entry)) {
		    continue;
		}
		toReturn.add(entry.getName().replace("/", ".").replace(".class", ""));
	    }
	} catch (IOException e) {
	    Logger.error(e);
	}

	return toReturn;
    }

    /**
     * @param fullyQualified
     *            The fully qualified name
     * @return The simplified name
     */
    public static String simplifyName(String fullyQualified) {
	fullyQualified = fullyQualified.replace(";", "");
	return fullyQualified.substring(fullyQualified.lastIndexOf(".") + 1);
    }
}
