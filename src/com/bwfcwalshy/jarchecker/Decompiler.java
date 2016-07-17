package com.bwfcwalshy.jarchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Decompiles the plugin using a Fernflower jar named "fernflower.jar" in the
 * same directory.
 */
public class Decompiler {

	/**
	 * @param f
	 *            The File to decompile
	 * @param export
	 *            The file to export it to
	 * @return True if it could be decompiled
	 */
	public boolean decompile(File f, File export) {
		if (!export.exists())
			export.mkdir();
		// Starts fernflower
		ProcessBuilder builder = new ProcessBuilder("java", "-jar", "fernflower.jar", f.getAbsolutePath(),
				export.getAbsolutePath());
		// Redirects stderr into stdout for ease of use
		builder.redirectErrorStream(true);
		try {
			// Begin process
			Process p = builder.start();
			// Marks should the Listener Thread wait
			AtomicBoolean wait = new AtomicBoolean(true);
			// Logs all of the lines
			final StringBuilder a = new StringBuilder();
			// How many lines to expect?
			int endLineCount = 0;
			// Start counting
			ZipFile from = new ZipFile(f);
			Enumeration<? extends ZipEntry> entries = from.entries();
			while (entries.hasMoreElements()) {
				ZipEntry next = entries.nextElement();
				if (next.getName().endsWith(".class") && !next.getName().contains("$")) {
					endLineCount++;
				}
			}
			// End counting
			Logger.debug("Expecting " + endLineCount * 2 + " lines");
			// Closes the ZipFile
			from.close();
			// Sets the ProgressBar max to expected line count
			if (!Main.isNoGui())
				Main.getMainWindow().setProgressbarMax(endLineCount * 2);
			// It says it needs to be final, I really do not see why
			final int finalEndLineCount = endLineCount;
			// Makes a listener thread
			new Thread(new Runnable() {

				@Override
				public void run() {
					// Every line must match one of these two
					Pattern p1 = Pattern.compile("INFO:  Decompiling class (.*)");
					Pattern p2 = Pattern.compile("INFO:  ... done");
					// Current line count
					int currentLines = 0;
					// Reads the fernflower process output
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					// Runs untill waitFor ends
					while (wait.get()) {
						// Keeps the current line
						String line = "";
						try {
							// Stores and checks is the current line null, and
							// if it is exits the loop
							while ((line = reader.readLine()) != null) {
								// Adds the line to the log
								a.append(line);
								a.append("\n");
								// Debug
								Logger.debug(line);
								// Checks do the lines match correctly
								Matcher m1 = p1.matcher(line);
								Matcher m2 = p2.matcher(line);
								if (m1.matches() || m2.matches()) {
									currentLines++;
								} else {
									// Something went wrong..
									p.destroy();
									wait.set(false);
									Logger.error("There was an error running fernflower!");
									Logger.error("Here is the output:");
									// Prints the log
									for (String s : a.toString().split("\n")) {
										Logger.error(s);
									}
									return;
								}
								// Sets the ProgressBar in nogui is false
								if (!Main.isNoGui()) {
									Main.getMainWindow().setProgressbarValue(currentLines);
								} else if (!Main.isPrintDebug() && Main.isPrintBar()) {
									// Needs work on progress bar for console.
									float onePercent = ((float) (currentLines) / (float) (finalEndLineCount * 2));
									float percentage = onePercent * 100F;
									int roundPercent = Math.round(percentage);
									StringBuilder barBuilder = new StringBuilder();
									barBuilder.append("\r[");
									while (barBuilder.length() < 102) {
										if (barBuilder.length() < roundPercent) {
											barBuilder.append("=");
										} else if (barBuilder.length() == roundPercent) {
											barBuilder.append(">");
										} else
											barBuilder.append(" ");
									}
									barBuilder.append("] " + roundPercent + "%");
									if (wait.get())
										System.out.print(barBuilder.toString());
								}
							}
						} catch (IOException e) {
							// Something went wrong.. Again..
							Logger.error(e);
						}

					}
					Logger.emptyLine();
				}

			}, "ListenerThread").start();
			// Waits for the process end
			p.waitFor();
			// Breaks the loop in Listener thread
			if (!wait.getAndSet(false)) {
				return false;
			}
		} catch (IOException | InterruptedException e) {
			// Again, Something went wrong
			Logger.error(e);
			return false;
		}

		// Yay were good!
		return true;
	}
}
