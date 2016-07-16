package com.bwfcwalshy.jarchecker.symbol_tables;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bwfcwalshy.jarchecker.Checker;

/**
 * A test file
 */
@SuppressWarnings({"javadoc", "unused"})
public class TestFile {

	com.bwfcwalshy.jarchecker.Logger logger;
	
	public static void main(String[] args) {
		BufferedReader reader;
		Checker c;
		com.bwfcwalshy.jarchecker.gui.MainWindow window;
		
		{
			com.bwfcwalshy.jarchecker.Decompiler logger;
		}
		
		com.bwfcwalshy.jarchecker.symbol_tables.TestFile file;
		
		{
			String joined = Arrays.stream(args).sequential().collect(Collectors.joining(" "));
			joined = joined.replaceAll("\"(.+?)\"", "$1|SEP|");
			args = joined.split(Pattern.quote("|SEP|"));
		}
	}
}
