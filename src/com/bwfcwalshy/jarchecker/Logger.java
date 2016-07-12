package com.bwfcwalshy.jarchecker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public static void print(String msg){
		System.out.println(getTime() + " [INFO] " + msg);
	}
	
	public static void printNoInfo(String msg) {
		System.out.println(msg);
	}
	
	public static void debug(String msg){
		System.out.println(getTime() + " [DEBUG] "+ msg);
	}
	
	public static void error(String msg){
		System.err.println(getTime() + " [ERROR] " + msg);
	}
	
	public static void emptyLine() {
		System.out.println();
	}
	
	public static String getTime(){
		Date date = new Date(System.currentTimeMillis());
		return "[" + sdf.format(date) + "]";
	}
}
