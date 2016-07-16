package com.bwfcwalshy.jarchecker.symbol_tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Organizes the symbol tables
 */
public class SymbolTableTree {

    private static int ID_COUNTER = 0;

    // list to keep the children in order
    private List<SymbolTableTree> children = new ArrayList<>();
    private SymbolTableTree parent;

    private SymbolTable table;

    private int id;

    private int lineStart, lineEnd;

    /**
     * You must set the lineEnd later, or it won't work.
     * 
     * @param parent
     *            The parent. Null for the root node
     * @param lineStart
     *            The line this block starts at
     */
    public SymbolTableTree(SymbolTableTree parent, int lineStart) {
	this(parent, lineStart, lineStart);
    }

    /**
     * @param parent
     *            The parent. Null for the root node
     * @param lineStart
     *            The line this block starts at
     * @param lineEnd
     *            The line this block ends at
     */
    public SymbolTableTree(SymbolTableTree parent, int lineStart, int lineEnd) {
	this.parent = parent;
	this.lineStart = lineStart;
	this.lineEnd = lineEnd;

	id = ++ID_COUNTER;
	table = new SymbolTable();
    }

    /**
     * @return The ID
     */
    public int getId() {
	return id;
    }

    /**
     * @return The line it starts at
     */
    public int getLineStart() {
	return lineStart;
    }

    /**
     * @return The line it ends at
     */
    public int getLineEnd() {
	return lineEnd;
    }

    /**
     * @param lineEnd
     *            The line it ends at
     */
    public void setLineEnd(int lineEnd) {
	this.lineEnd = lineEnd;
    }

    /**
     * @param line
     *            The line in question
     * @return True if the line is inside this block
     */
    public boolean isInside(int line) {
	return line >= getLineStart() && line <= getLineEnd();
    }

    /**
     * @return The parent if there is any
     */
    public Optional<SymbolTableTree> getParent() {
	return Optional.ofNullable(parent);
    }

    /**
     * @return All the children. Immutable.
     */
    public List<SymbolTableTree> getChildren() {
	return Collections.unmodifiableList(children);
    }

    /**
     * Returns all the nodes in the whole tree, which are children of this node.
     * 
     * @return The children and their children in a list.
     */
    public List<SymbolTableTree> getChildrenRecursive() {
	// don't add yourself, as it wants children. You will be added by your
	// parent, if you belong in the list.

	List<SymbolTableTree> list = new ArrayList<>();

	list.addAll(getChildren());

	for (SymbolTableTree commandNode : getChildren()) {
	    list.addAll(commandNode.getChildrenRecursive());
	}

	return list;
    }

    /**
     * @param child
     *            The child to add
     */
    public void addChild(SymbolTableTree child) {
	if (children.contains(child)) {
	    return;
	}
	children.add(child);
    }

    /**
     * @return The Symbol table for this entry
     */
    public SymbolTable getTable() {
	return table;
    }

    /**
     * @param line
     *            The line in which the variable occured
     * @return The table for this line.
     */
    public SymbolTable getTableForLine(int line) {
	SymbolTableTree found = null;
	for (SymbolTableTree symbolTableTree : getChildrenRecursive()) {
	    if (symbolTableTree.isInside(line)) {
		found = symbolTableTree;
	    }
	}

	return found.getTable();
    }

    /**
     * @param line
     *            The line it is in
     * @param name
     *            The name of the variable
     * @return The type of the variable or an empty Optional if not found
     */
    public Optional<String> getFullyQualifiedType(int line, String name) {
	String fullyQualifiedName = null;
	for (SymbolTableTree symbolTableTree : getChildrenRecursive()) {
	    if (symbolTableTree.isInside(line)) {
		if (symbolTableTree.getTable().contains(name)) {
		    // works, as the children will be iterated over later.
		    fullyQualifiedName = symbolTableTree.getTable().getType(name).get();
		}
	    }
	}
	return Optional.ofNullable(fullyQualifiedName);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + id;
	return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	SymbolTableTree other = (SymbolTableTree) obj;
	if (id != other.id)
	    return false;
	return true;
    }
}
