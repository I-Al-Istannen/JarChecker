package com.bwfcwalshy.jarchecker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public static void print(String msg){
	String x = getTime() + " [INFO] " + msg;
	if(!Main.nogui) Main.mw.log.append(x + "\n");
	System.out.println(x);
    }

    public static void printNoInfo(String msg) {
	System.out.println(msg);
	if(!Main.nogui) Main.mw.log.append(msg + "\n");
    }

    public static void debug(String msg){
	if(!Main.printDebug()) return;
	String x = getTime() + " [DEBUG] "+ msg;
	if(!Main.nogui) Main.mw.log.append(x + "\n");
	System.out.println(x);
    }

    public static void error(String msg){
	String x = getTime() + " [ERROR] " + msg;
	if(!Main.nogui) Main.mw.log.append(x + "\n");
	System.err.println(x);
    }

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

    public static void emptyLine() {
	System.out.println();
	if(!Main.nogui) Main.mw.log.append("\n");
    }

    public static String getTime(){
	Date date = new Date(System.currentTimeMillis());
	return "[" + sdf.format(date) + "]";
    }

}
