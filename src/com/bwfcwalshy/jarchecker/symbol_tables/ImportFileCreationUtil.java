package com.bwfcwalshy.jarchecker.symbol_tables;

import static com.bwfcwalshy.jarchecker.symbol_tables.DefaultImports.simplifyName;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Assists you in the creation of the bukkit import files.
 */
public class ImportFileCreationUtil {

	/**
	 * @param jar
	 *            The jar file to read from
	 * @param writeTo
	 *            The path to write it to
	 */
	public static void writeJarImportsToFile(File jar, Path writeTo) {
		Predicate<ZipEntry> filter = entry -> entry.getName().endsWith(".class") && !entry.getName().contains("$");

		if (!jar.getAbsolutePath().endsWith(".jar")) {
			return;
		}
		try (JarFile jarFile = new JarFile(jar)) {

			Enumeration<JarEntry> entries = jarFile.entries();

			List<String> list = new LinkedList<>();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (!filter.test(entry)) {
					continue;
				}
				String fullyQuallified = entry.getName().replace("/", ".").replace(".class", "");
				String together = simplifyName(fullyQuallified) + "=" + fullyQuallified;
				list.add(together);
			}
			Collections.sort(list);
			Files.write(writeTo, list, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Merges all the txts in this package to one, called
	 * "bukkit-imports-joined.txt", which MUST exist.
	 * 
	 * @param outputFile
	 *            The output file.
	 */
	public static void mergeImportFiles(Path outputFile) {
		URL location = SymbolTreeParser.class.getProtectionDomain().getCodeSource().getLocation();
		Set<String> imports = new HashSet<>();
		if (location != null && location.getFile().endsWith(".jar")) {
			System.out.println("Merging imports from Jar file...");
			try (JarFile jarFile = new JarFile(new File(location.toURI()))) {

				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (entry.getName().startsWith("com/bwfcwalshy/jarchecker/symbol_tables")
							&& entry.getName().endsWith(".txt")) {
						try (InputStream inStream = jarFile.getInputStream(entry);
								InputStreamReader inStreamReader = new InputStreamReader(inStream);
								BufferedReader reader = new BufferedReader(inStreamReader);) {

							String tmp;
							while ((tmp = reader.readLine()) != null) {
								imports.add(tmp);
							}
						}
					}
				}
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			try {
				System.out.println("Merging imports from the File system...");
				File dir = new File("src/com/bwfcwalshy/jarchecker/symbol_tables");
				for (Path path : Files.list(dir.toPath()).filter(path -> path.toString().endsWith(".txt"))
						.collect(Collectors.toList())) {
					imports.addAll(Files.readAllLines(path, StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("...Merge size: " + imports.size());
		try {
			Files.write(outputFile, imports.stream().sorted().collect(Collectors.toList()), StandardCharsets.UTF_8,
					StandardOpenOption.WRITE);
			System.out.println("Wrote merged file to the file system (" + outputFile.toString() + ")");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
