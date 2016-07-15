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

public class Decompiler {

    public boolean decompile(File f, File export){
	if(!export.exists())
	    export.mkdir();

	ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "\"java -jar fernflower.jar " + f.getAbsolutePath() + " " + export.getAbsolutePath()+"\"");
	builder.redirectErrorStream(true);
	try {
	    Process p = builder.start();
	    AtomicBoolean wait = new AtomicBoolean(true);
	    final FinalString a = new FinalString();
	    Pattern p1 = Pattern.compile("INFO:  Decompiling class (.*)");
	    Pattern p2 = Pattern.compile("INFO:  ... done");
	    int ec = 0;
	    ZipFile from = new ZipFile(f);
	    Enumeration<? extends ZipEntry> entries = from.entries();
	    while(entries.hasMoreElements()) {
		ZipEntry next = entries.nextElement();
		if(next.getName().endsWith(".class") && !next.isDirectory()) ec++;
	    }
	    from.close();
	    Main.mw.decomp.setMaximum(ec*2);
	    new Thread(new Runnable() {

		@Override
		public void run() {
		    int lc = 0;
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    while(wait.get()) {
			String line = "";
			try {
			    while((line = br.readLine()) != null) {
				a.appendLine(line);
				Matcher m1 = p1.matcher(line);
				Matcher m2 = p2.matcher(line);
				if(m1.matches() || m2.matches()) {
				    lc++;
				} else {
				    p.destroy();
				    wait.set(false);
				    Logger.error("There was an error running fernflower!");
				    Logger.error("Here is the output:");
				    for(String s : a.get().split(Pattern.quote("\n"))) {
					Logger.error(s);
				    }
				}
				if(!Main.nogui) {
				    Main.mw.decomp.setValue(lc);
				}
				wait.set(p.isAlive());
			    }
			} catch (IOException e) {
			    e.printStackTrace();
			}

		    }

		}

	    }, "ListenerThread").start();
	    while(wait.get()) {}
	} catch (IOException e) {
	    Logger.error(e);
	}


	return true;
    }
}
