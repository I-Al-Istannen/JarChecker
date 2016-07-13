package com.bwfcwalshy.jarchecker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public static void print(String msg){
		String x = getTime() + " [INFO] " + msg;
		Main.mw.log.append(x + "\n");
		System.out.println(x);
	}
	
	public static void printNoInfo(String msg) {
		System.out.println(msg);
		Main.mw.log.append(msg + "\n");
	}
	
	public static void debug(String msg){
		String x = getTime() + " [DEBUG] "+ msg;
		Main.mw.log.append(x + "\n");
		System.out.println(x);
	}
	
	public static void error(String msg){
		String x = getTime() + " [ERROR] " + msg;
		Main.mw.log.append(x + "\n");
		System.err.println(x);
	}
	
	public static void emptyLine() {
		System.out.println();
		Main.mw.log.append("\n");
	}
	
	public static String getTime(){
		Date date = new Date(System.currentTimeMillis());
		return "[" + sdf.format(date) + "]";
	}
}
