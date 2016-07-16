package com.bwfcwalshy.jarchecker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple Logger class, printing to the GUI and the console 
 */
public class Logger {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_TIME;

    /**
     * Prints a message, prefix is "{@link #getTime()} [INFO]"
     * 
     * @param msg The message to print
     */
    public static void print(String msg){
	String x = getTime() + " [INFO] " + msg;
	if(!Main.isNoGui()) Main.getMainWindow().log.append(x + "\n");
	System.out.println(x);
    }

    /**
     * Will just print the message with no prefix
     * 
     * @param msg The message to print
     */
    public static void printNoInfo(String msg) {
	System.out.println(msg);
	if(!Main.isNoGui()) Main.getMainWindow().log.append(msg + "\n");
    }

    /** 
     * Will print the message with the prefix: "{@link #getTime()} [DEBUG]"
     * 
     * @param msg The message to print
     */
    public static void debug(String msg){
	if(!Main.printDebug()) return;
	msg += " (" + new Throwable().getStackTrace()[1] + ")";
	String x = getTime() + " [DEBUG] "+ msg;
	if(!Main.isNoGui()) Main.getMainWindow().log.append(x + "\n");
	System.out.println(x);
    }

    /**
     * Will print the message with the prefix: "{@link #getTime()} [ERROR]"
     * 
     * @param msg The message to print
     */
    public static void error(String msg){
	String x = getTime() + " [ERROR] " + msg;
	if(!Main.isNoGui()) Main.getMainWindow().log.append(x + "\n");
	System.err.println(x);
    }

    /**
     * Will print the exception with the prefix: "{@link #getTime()} [ERROR]"
     * 
     * @param e The exception to print
     */
    public static void error(Exception e) {
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	e.printStackTrace(pw);
	error(sw.toString());
	pw.close();
	try {
	    sw.close();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
    }

    /**
     * Prints an empty line
     */
    public static void emptyLine() {
	System.out.println();
	if(!Main.isNoGui()) Main.getMainWindow().log.append("\n");
    }

    /**
     * Formatted using a {@link DateTimeFormatter}.
     * <br>
     * <br>Current pattern {@link DateTimeFormatter#ISO_TIME}
     * 
     * @return The system time
     */
    public static String getTime() {
	return "[" + LocalTime.now().format(DATE_TIME_FORMATTER) + "]";
    }

}
