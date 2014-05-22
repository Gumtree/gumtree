/**
 * 
 */
package org.gumtree.sics.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author nxi
 *
 */
public class SicsLogManager implements ISicsLogManager {


	private static SicsLogManager instance;
	private Map<LogType, PrintWriter> logFiles;
	private Map<LogType, String> lastLogEntries;
	private int dayOfWeek;
	
	/**
	 * 
	 */
	private SicsLogManager() {
		dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);;
		logFiles = new HashMap<ISicsLogManager.LogType, PrintWriter>();
		lastLogEntries = new HashMap<ISicsLogManager.LogType, String>();
		final String filename = System.getProperty("gumtree.logging.path");
		if (filename == null) {
			return;
		}
		File parent = new File(filename);
		if (!parent.exists()) {
			parent.mkdirs();
		}
		createFiles(filename);
		Thread timeMonitorThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
					if (day != dayOfWeek) {
						createFiles(filename);
						dayOfWeek = day;
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		timeMonitorThread.start();
	}

	private void createFiles(String parentPath) {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		for (LogType type : LogType.values()) {
			File file = new File(parentPath + "/" + type.name() + "_" + dateFormat.format(date));
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
			String lastEntry = lastLogEntries.get(type);
			if (lastEntry != null) {
				log(type, lastEntry);
			}
			try {
				PrintWriter outputfile = new PrintWriter(new FileWriter(file, true), true);
				PrintWriter oldFile = logFiles.get(type); 
				logFiles.put(type, outputfile);
				if (oldFile != null) {
					oldFile.close();
				}
				if (lastEntry != null) {
					log(type, lastEntry);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.gumtree.sics.io.ISicsLogManager#log(java.lang.String)
	 */
	@Override
	public void log(LogType type, String text) {
		PrintWriter outputFile = logFiles.get(type);
		if (outputFile != null && text != null) {
//			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//			Date date = new Date();
//			outputFile.append(dateFormat.format(date) + " \t" + text + "\n");
			outputFile.append(String.valueOf(System.currentTimeMillis()) + "\t" + text + "\n");
			outputFile.flush();
		}
		lastLogEntries.put(type, text);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.sics.io.ISicsLogManager#close()
	 */
	@Override
	public void close() {
		for (Entry<LogType, PrintWriter> entry: logFiles.entrySet()){
			entry.getValue().close();
		}
	}

	public static SicsLogManager getInstance() {
		if (instance == null) {
			instance = new SicsLogManager();
		}
		return instance;
	}

}
