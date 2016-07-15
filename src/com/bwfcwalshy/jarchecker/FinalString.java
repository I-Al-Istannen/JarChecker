package com.bwfcwalshy.jarchecker;

public class FinalString {
    private String s;
    
    public FinalString() {
	this("");
    }
    
    public FinalString(String def) {
	s = def;
    }
    
    public void append(String s) {
	this.s += s;
    }
    
    public String get() {
	return s;
    }
    
    public void appendLine(String line) {
	s += line + "\n";
    }
}
