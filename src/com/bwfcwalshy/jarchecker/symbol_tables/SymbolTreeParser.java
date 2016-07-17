package com.bwfcwalshy.jarchecker.symbol_tables;

import static com.bwfcwalshy.jarchecker.symbol_tables.DefaultImports.simplifyName;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bwfcwalshy.jarchecker.Logger;

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

		for (DefaultImports defaultImports : DefaultImports.values()) {
			this.importMap.putAll(defaultImports.getImports());
		}

		parseImports();
		addThisJarImports();
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
					Logger.debug("Imported class found: " + word + " <" + varName + "> (" + importMap.get(word) + ") "
							+ string);
					currentNode.getTable().setType(varName, importMap.get(word));
				}
				currentPos += word.length();
			}

			lineCounter++;
		}

		rootNode.setLineEnd(lineCounter);

		Logger.debug("");
		for (SymbolTableTree symbolTableTree : rootNode.getChildrenRecursive()) {
			Logger.debug(symbolTableTree.getId() + " (" + symbolTableTree.getParent().get().getId() + ") <"
					+ symbolTableTree.getLineStart() + " - " + symbolTableTree.getLineEnd() + "> "
					+ symbolTableTree.getTable());
		}
	}

	/**
	 * Parses the import section at the beginning of a java file.
	 */
	private void parseImports() {
		for (String string : source.split(System.lineSeparator())) {
			if (string.startsWith("import")) {
				addImport(string);
			}
		}
	}

	/**
	 * Removes ";" and "import ", and puts it in the map, along with it's
	 * simplified name.
	 * 
	 * @param string
	 *            The string to add
	 */
	private void addImport(String string) {
		string = string.replace("import ", "");
		string = string.replace(";", "");
		importMap.put(simplifyName(string), string);
	}

	/**
	 * Adds all the classes in this jar as imports
	 */
	private void addThisJarImports() {
		if (!sourceFile.getAbsolutePath().endsWith(".jar")) {
			return;
		}
		DefaultImports.getImportsFromJar(sourceFile, entry -> {
			return !(entry.getName().contains("$") || !entry.getName().endsWith(".class"));
		}).stream().forEach(this::addImport);
	}

	/**
	 * @return The root node
	 */
	public SymbolTableTree getRootNode() {
		return rootNode;
	}
}
