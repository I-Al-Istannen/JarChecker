package com.bwfcwalshy.jarchecker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.bwfcwalshy.jarchecker.jfx_gui.AppMain;
import com.bwfcwalshy.jarchecker.jfx_gui.Logger;

/**
 * Decompiles a jar file
 */
public class JarDecompiler {

	/**
	 * @param file
	 *            The file to decompile
	 * @param settings
	 *            The {@link Settings} object
	 * @return The path of the decompiled jar or an empty optional if an error occured
	 */
	public static Optional<Path> decompile(Path file, Settings settings) {
		if (Files.notExists(file) || Files.isDirectory(file)) {
			return Optional.empty();
		}

		// download fernflower if needed
		if (!settings.existsFernflower()) {
			// well, the download failed. You have a problem.
			if(downloadFernflower(settings) == DecompilerDownloadResult.DOWNLOAD_FAILED) {
				return Optional.empty();
			}
		}

		try {
			Path targetDir = settings.getWorkingDir().resolve(file.getName(file.getNameCount() - 1).toString().replace(".jar", ""));
			if(Files.notExists(targetDir)) {
				Files.createDirectories(targetDir);
			}
			
			// find the next free number
			
			// @formatter:off
			Optional<Integer> currentNumber = Files.list(targetDir)
					.filter(path -> Files.isRegularFile(path) && path.toString().matches(".+-[0-9]+.jar"))
					.map(path -> path.getName(path.getNameCount() - 1).toString().replaceAll(".+-", "").replace(".jar", ""))
					.map(Integer::valueOf)
					.sorted()
					.reduce((o1, o2) -> o1.compareTo(o2) < 1 ? o2 : o1);
			// @formatter:on
			
			int nextNumber = currentNumber.orElse(0) + 1;
			Path targetFile = targetDir.resolve(file.getName(file.getNameCount() - 1).toString().replace(".jar", "") + "-" + nextNumber + ".jar");
			Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
			
			stripPackagedSource(targetFile.toFile());
			
			// one for start, one for end
			int lineCount = getClassFileCount(targetFile) * 2;

			Path target = targetFile.resolveSibling(targetFile.getName(targetFile.getNameCount() - 1).toString().replace(".jar", ""));
			
			if(Files.notExists(target)) {
				Files.createDirectories(target);
			}
			
			// Starts fernflower
			ProcessBuilder builder = new ProcessBuilder("java", "-jar",
					settings.getFernflowerFile().toAbsolutePath().toString(), targetFile.toAbsolutePath().toString(),
					target.toAbsolutePath().toString());
			// Redirects stderr into stdout for ease of use
			builder.redirectErrorStream(true);
			
			Process decompilerProcess = builder.start();
			
			try(InputStreamReader inStreamReader = new InputStreamReader(decompilerProcess.getInputStream());
					BufferedReader reader = new BufferedReader(inStreamReader);) {

				StringBuilder existingLog = new StringBuilder();
				Pattern startClass = Pattern.compile("INFO:  Decompiling class (.*)");
				Pattern completedClass = Pattern.compile("INFO:  ... done");
				
				int readLines = 0;
				
				String tmp;
				while(decompilerProcess.isAlive()) {
    				while((tmp = reader.readLine()) != null) {
    					existingLog.append(tmp + System.lineSeparator());
    					Logger.log(Level.FINER, tmp);
    					
    					if(startClass.matcher(tmp).find() || completedClass.matcher(tmp).find()) {
    						readLines++;
    					}
    					else {
    						Logger.log(Level.SEVERE, "Encountered an unexcepted line: '" + tmp + "'");
    						decompilerProcess.destroy();
    						Logger.log(Level.SEVERE, "==== Decompiler log ====");
    						Logger.log(Level.SEVERE, existingLog.toString());
    						Logger.log(Level.SEVERE, "==== ============== ====");
    						return Optional.empty();
    					}
    					double percentage = (double) readLines / lineCount;
    					if(!AppMain.getInstance().getSettings().isNoGui()) {
    						AppMain.getInstance().getMainWindowController().setProgress(percentage);
    					}
    				}
    			}
				Logger.log(Level.INFO, "Decompiled " + targetFile.getName(targetFile.getNameCount() - 1));
				return Optional.of(target.resolve(targetFile.getName(targetFile.getNameCount() - 1)));
			}
		} catch (IOException e) {
			Logger.logException(Level.SEVERE, e);
		}
		
		return Optional.empty();
	}

	/**
	 * @param file The amount of class files in the given jar file
	 * @return The amount or -1 if an error occured.
	 */
	private static int getClassFileCount(Path file) {
		if(Files.notExists(file) || Files.isDirectory(file) || !file.getName(file.getNameCount() - 1).toString().endsWith(".jar")) {
			return -1;
		}
		int counter = 0;
		try(ZipFile zipFile = new ZipFile(file.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if(entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
					counter++;
				}
			}
		} catch (IOException e) {
			Logger.logException(Level.SEVERE, e);
			return -1;
		}
		
		return counter;
	}
	
	/**
	 * @param file The file to strip the source from
	 * @return True if it worked
	 */
	private static boolean stripPackagedSource(File file) {
		try {

			// strip the source files
			Path tmpFile = Files.createTempFile(file.getName(), "stripped");
			try(ZipFile jarFile = new ZipFile(file);
					FileOutputStream fileOutStream = new FileOutputStream(tmpFile.toFile());
					ZipOutputStream outStream = new ZipOutputStream(fileOutStream)) {
				
				Enumeration<? extends ZipEntry> entries = jarFile.entries();
				while(entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if(!entry.getName().endsWith(".java")) {
						outStream.putNextEntry(entry);
						BufferedInputStream inStream = new BufferedInputStream(jarFile.getInputStream(entry));
						while(inStream.available() > 0) {
							outStream.write(inStream.read());
						}
					}
				}
			}
			Files.move(tmpFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			return true;
		} catch (IOException e) {
			Logger.logException(Level.SEVERE, e);
		}
		return false;
	}

	/**
	 * @param settings
	 *            The settings to use
	 * @return The {@link DecompilerDownloadResult}
	 */
	public static DecompilerDownloadResult downloadFernflower(Settings settings) {
		if(settings.existsFernflower()) {
			return DecompilerDownloadResult.ALREADY_EXISTED;
		}

		try {
			final InputStream fromInternet = new URI(
					"https://dl.dropboxusercontent.com/s/b9cna8hproe2smg/fernflower.jar?dl=0").toURL().openStream();
			try {
				Files.copy(fromInternet, settings.getFernflowerFile(), StandardCopyOption.REPLACE_EXISTING);
				Logger.log(Level.INFO, "Fernflower downloaded!");
				return DecompilerDownloadResult.DOWNLOADED;
			} catch (IOException e) {
				Logger.logException(Level.SEVERE, e);
			}
		} catch (IOException | URISyntaxException e) {
			com.bwfcwalshy.jarchecker.jfx_gui.Logger.logException(Level.SEVERE, e);
		}

		return DecompilerDownloadResult.DOWNLOAD_FAILED;
	}

	/**
	 * The result from trying to download fernflower
	 */
	public static enum DecompilerDownloadResult {
		/**
		 * The decompiler already existed before
		 */
		ALREADY_EXISTED,
		/**
		 * Successfully downloaded it
		 */
		DOWNLOADED,
		/**
		 * The download failed
		 */
		DOWNLOAD_FAILED;
	}
}
