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
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import com.bwfcwalshy.jarchecker.jfx_gui.Logger;

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
			return entry.getName().contains("java/lang") && entry.getName().split("/").length == 3;
		}, true).stream()
				.map(fullyQualified -> fullyQualified + "=" + simplifyName(fullyQualified))
				.collect(Collectors.toMap(string -> string.split("=")[1], string -> string.split("=")[0], (valueOne, valueTwo) -> valueOne));
		// TODO: Allow multiple imports per key! (e.g. org.bukkit.block.Chest and org.bukkit.material.Chest
		// it was fixed here by keeping the first ((valueOne, valueTwo) -> valueOne) but we need a nicer solution, allowing both.
		// the same problem also arises in the SymbolTreeParser. There you need to decide based on the file imports and the fully qualified name
		// I wish you good luck!
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
			Logger.logException(Level.WARNING, e);
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
	 * @param addInnerClassesIfNotFiltered
	 *            If true inner classes will be added to the set, if the filer
	 *            allows for them.
	 * 
	 * @return A set with all imports from the jar file.
	 */
	public static Set<String> getImportsFromJar(File file, Predicate<ZipEntry> filter,
			boolean addInnerClassesIfNotFiltered) {
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

				// is inner class
				if (entry.getName().contains("$") && addInnerClassesIfNotFiltered) {
					String[] splitted = entry.getName().replace("/", ".").replace(".class", "").split(Pattern.quote("$"));
					String innerClassName = splitted[1];

					// ignore anonymous ones
					if (innerClassName.matches("[0-9]+")) {
						continue;
					}

					innerClassName = innerClassName.replaceFirst("[0-9]+", "");
					
					String name = splitted[0] + "." + innerClassName;
					toReturn.add(name);
				} else {
					toReturn.add(entry.getName().replace("/", ".").replace(".class", ""));
				}
			}
		} catch (IOException e) {
			Logger.logException(Level.WARNING, e);
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
