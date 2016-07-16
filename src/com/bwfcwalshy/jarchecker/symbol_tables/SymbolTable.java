package com.bwfcwalshy.jarchecker.symbol_tables;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A table with variable types
 */
public class SymbolTable {

    private Map<String, String> nameToType = new HashMap<>();

    /**
     * @param name
     *            The name of the variable
     * @return The type of it or an empty Optional if not found
     */
    public Optional<String> getType(String name) {
	return Optional.ofNullable(nameToType.get(name));
    }

    /**
     * @param name
     *            The name of the variable
     * @param type
     *            It's type
     */
    public void setType(String name, String type) {
	nameToType.put(name, type);
    }

    /**
     * @param name
     *            The name to check
     * @return True if there is a variable with this name registered
     */
    public boolean contains(String name) {
	return getType(name).isPresent();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "SymbolTable [nameToType=" + nameToType + "]";
    }
}
