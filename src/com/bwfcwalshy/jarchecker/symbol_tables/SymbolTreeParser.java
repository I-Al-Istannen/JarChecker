package com.bwfcwalshy.jarchecker.symbol_tables;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Creates a Symbol tree
 */
public class SymbolTreeParser {

    private SymbolTableTree rootNode;
    private SymbolTableTree currentNode;

    private String source;
    private File sourceFile;

    private Map<String, String> importMap = new HashMap<>();

    private static final List<String> nameForbiddenSequences = new ArrayList<String>() {
	private static final long serialVersionUID = 1L;
	{
	    add("[");
	    add("]");
	    add("{");
	    add("}");
	    add("(");
	    add(")");
	    add(";");
	    add(",");
	}
    };

    /**
     * @param source
     *            The source to parse
     * @param sourceFile
     *            The source file.
     */
    public SymbolTreeParser(String source, File sourceFile) {
	this(source, sourceFile, Collections.emptyMap());
    }

    /**
     * @param source
     *            The source to parse
     * @param sourceFile
     *            The source file.
     * @param extraImports
     *            The extra imports to use
     */
    public SymbolTreeParser(String source, File sourceFile, Map<String, String> extraImports) {
	this.source = source;
	this.rootNode = new SymbolTableTree(null, 0);
	this.sourceFile = sourceFile;
	this.importMap.putAll(extraImports);

	parseImports();
	addThisJarImports();
	try {
	    addJavaLangImports();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Parses a file to a SymbolTableTree
     */
    public void parse() {
	this.currentNode = rootNode;

	int lineCounter = 1;
	for (String string : source.split(System.lineSeparator())) {
	    if (string.contains("{")) {
		SymbolTableTree parent = currentNode;
		currentNode = new SymbolTableTree(currentNode, lineCounter);
		parent.addChild(currentNode);
	    }
	    if (string.contains("}")) {
		currentNode.setLineEnd(lineCounter);
		currentNode = currentNode.getParent().get();
	    }

	    // skip comments and import statements
	    if (string.trim().startsWith("//") || string.startsWith("import") || string.startsWith("package")
		    || string.matches("[\\s]*\\*.*") || string.trim().startsWith("/*")) {

		lineCounter++;
		continue;
	    }

	    // Dependencies are only registered if they are imported. Fully
	    // qualified names are !ignored! if they are not known. Reason:
	    // Method invocations
	    // May register too much, but that shouldn't really matter.
	    int currentPos = 0;
	    for (String word : string.split("([\\s]+)|(\\()|(\\))|(,)")) {
		if (word.contains(".")) {
		    word = simplifyName(word);
		}
		// found an imported class
		if (importMap.containsKey(word)) {
		    String varName = string.substring(string.indexOf(word, currentPos) + word.length());
		    varName = varName.trim();
		    if (varName.contains(" ")) {
			varName = varName.substring(0, varName.indexOf(" "));
		    }

		    for (String s : nameForbiddenSequences) {
			varName = varName.replace(s, "");
		    }
		    if (varName.isEmpty()) {
			currentPos += word.length();
			continue;
		    }
		    System.out.println("Imported class found: " + word + " <" + varName + "> (" + importMap.get(word)
			    + ") " + string);
		    currentNode.getTable().setType(varName, importMap.get(word));
		}
		currentPos += word.length();
	    }

	    lineCounter++;
	}

	rootNode.setLineEnd(lineCounter);

	System.out.println();
	for (SymbolTableTree symbolTableTree : rootNode.getChildrenRecursive()) {
	    System.out.println(symbolTableTree.getId() + " (" + symbolTableTree.getParent().get().getId() + ") <"
		    + symbolTableTree.getLineStart() + " - " + symbolTableTree.getLineEnd() + "> "
		    + symbolTableTree.getTable());
	}
    }

    private void parseImports() {
	for (String string : source.split(System.lineSeparator())) {
	    if (string.startsWith("import")) {
		addImport(string);
	    }
	}
    }

    private void addImport(String string) {
	string = string.replace("import ", "");
	string = string.replace(";", "");
	importMap.put(simplifyName(string), string);
    }

    private void addJavaLangImports() throws IOException {
	String rtJarPath = System.getProperty("sun.boot.class.path");
	rtJarPath = Arrays.stream(rtJarPath.split(";")).filter(string -> string.endsWith("rt.jar")).findAny().get();
	addImportsFromJar(new File(rtJarPath), entry -> {
	    return entry.getName().contains("java/lang") && entry.getName().split("/").length == 3
		    && !entry.getName().contains("$");
	});
    }

    /**
     * @param file
     *            The file to read from
     * @param filter
     *            The filter the files must match
     */
    private void addImportsFromJar(File file, Predicate<ZipEntry> filter) {
	if (!file.getAbsolutePath().endsWith(".jar")) {
	    return;
	}
	try (JarFile jarFile = new JarFile(file)) {

	    Enumeration<JarEntry> entries = jarFile.entries();

	    while (entries.hasMoreElements()) {
		ZipEntry entry = entries.nextElement();
		if (!filter.test(entry)) {
		    continue;
		}
		addImport(entry.getName().replace("/", ".").replace(".class", ""));
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void addThisJarImports() {
	if (!sourceFile.getAbsolutePath().endsWith(".jar")) {
	    return;
	}
	addImportsFromJar(sourceFile, entry -> {
	    return !(entry.getName().contains("$") || !entry.getName().endsWith(".class"));
	});
    }

    private String simplifyName(String fullyQualified) {
	fullyQualified = fullyQualified.replace(";", "");
	return fullyQualified.substring(fullyQualified.lastIndexOf(".") + 1);
    }

    /**
     * @return The root node
     */
    public SymbolTableTree getRootNode() {
	return rootNode;
    }

    /**
     * Just for testing. We need to somehow give him ALL the bukkit/spigot
     * imports, so it can correctly resolve those. Maybe in an external file?
     * 
     * @param args
     *            The arguments passed
     * @throws IOException
     *             If an IO error occured
     */
    public static void main(String[] args) throws IOException {
	File file = new File("src/com/bwfcwalshy/jarchecker/symbol_tables/TestFileTwo.java");
	String source = Files.readAllLines(file.toPath()).stream().collect(Collectors.joining(System.lineSeparator()));

	SymbolTreeParser parser = new SymbolTreeParser(source, new File("C:/Users/Julian/Desktop/JarChecker2.jar"));

	parser.addImportsFromJar(new File("S:/Minecraft/Bukkit Server/Bukkit 1.8.8/spigot-1.8.8.jar"), entry -> {
	    return !(entry.getName().contains("$") || !entry.getName().endsWith(".class"));
	});

	parser.parse();

	System.out.println(parser.getRootNode().getFullyQualifiedType(65, "meta"));
    }
}
