package com.bwfcwalshy.jarchecker.jfx_gui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

/**
 * Logs messages
 */
public class Logger {

	/**
	 * @param level The level of the message
	 * @param message The Message itself
	 */
	public static void log(Level level, String message) {
		message = "[" + level.getName() + "] " + message;
		if(!AppMain.getInstance().getSettings().isNoGui()) {
			AppMain.getInstance().getLogPane().addMessage(level, message);
		}
		if(level.intValue() >= AppMain.getInstance().getSettings().getMinLogLevel().intValue()) {
			System.out.println(message);
		}
	}
	
	/**
	 * @param level The level of the message
	 * @param exception The exception to log
	 */
	public static void logException(Level level, Exception exception) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter stream = new PrintWriter(stringWriter);
		exception.printStackTrace(stream);
		log(level, stringWriter.getBuffer().toString());
	}
}
