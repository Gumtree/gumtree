/**
 * 
 */
package org.gumtree.sics.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
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

	public final static String PROPERTY_LOGGING_PATH = "gumtree.logging.path";
	public final static String PROPERTY_LOGGING_SHUTTER_ENABLED = "gumtree.logging.shutterEnabled";
	private static SicsLogManager instance;
	private Map<LogType, PrintWriter> logFiles;
	private Map<LogType, String> lastLogEntries;
	private int dayOfWeek;
	private String logFolder;
	private boolean isShutterEnabled;
	public DateFormat logDateFormat;
	public DateFormat fileDateFormat;
	
	/**
	 * 
	 */
	private SicsLogManager() {
		dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);;
		logFiles = new HashMap<ISicsLogManager.LogType, PrintWriter>();
		lastLogEntries = new HashMap<ISicsLogManager.LogType, String>();
		logFolder = System.getProperty(PROPERTY_LOGGING_PATH);
		isShutterEnabled = Boolean.valueOf(System.getProperty(PROPERTY_LOGGING_SHUTTER_ENABLED, "false"));
		logDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		if (logFolder == null) {
			return;
		}
		File parent = new File(logFolder);
		if (!parent.exists()) {
			parent.mkdirs();
		}
		createFiles(logFolder);
		Thread timeMonitorThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
					if (day != dayOfWeek) {
						createFiles(logFolder);
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
		Date date = new Date();
		if (isShutterEnabled) {
			for (LogType type : LogType.values()) {
				File file = new File(parentPath + "/" + type.name() + "_" + fileDateFormat.format(date));
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
		} else {
			File file = new File(parentPath + "/" + LogType.STATUS.name() + "_" + fileDateFormat.format(date));
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			String lastEntry = lastLogEntries.get(LogType.STATUS);
			if (lastEntry != null) {
				log(LogType.STATUS, lastEntry);
			}
			try {
				PrintWriter outputfile = new PrintWriter(new FileWriter(file, true), true);
				PrintWriter oldFile = logFiles.get(LogType.STATUS); 
				logFiles.put(LogType.STATUS, outputfile);
				if (oldFile != null) {
					oldFile.close();
				}
				if (lastEntry != null) {
					log(LogType.STATUS, lastEntry);
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
			Date date = new Date();
			outputFile.append(logDateFormat.format(date) + " \t" + text + "\n");
//			outputFile.append(String.valueOf(System.currentTimeMillis()) + "\t" + text + "\n");
			outputFile.flush();
			lastLogEntries.put(type, text);
		}
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

	
	public Map<String, Long> processLog(final Date start, final Date end) {
		Map<String, Long> logCounts = new HashMap<String, Long>();
		File folder = new File(logFolder);
		if (!folder.exists()) {
			return logCounts;
		}
		String[] files = folder.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith(LogType.STATUS.name())) {
					String dataString = name.split("_")[1];
					try {
						Date readDate = fileDateFormat.parse(dataString);
						if ((readDate.after(start) && readDate.before(end)) || readDate.equals(start) || readDate.equals(end)) {
							return true;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		for (String filename : files){
			processStatusFile(logCounts, folder + "/" + filename);
		}
		if (isShutterEnabled) {
			files = folder.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (name.startsWith(LogType.SECONDARY.name())) {
						String dataString = name.split("_")[1];
						try {
							Date readDate = fileDateFormat.parse(dataString);
							if ((readDate.after(start) && readDate.before(end)) || readDate.equals(start) || readDate.equals(end)) {
								return true;
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					return false;
				}
			});
			if (files.length > 0) {
				logCounts.put(LogType.SECONDARY.name(), 0L);
			}
			for (String filename : files){
				processShutterFile(logCounts, LogType.SECONDARY.name(), folder + "/" + filename);
			}
			files = folder.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (name.startsWith(LogType.TERTIARY.name())) {
						String dataString = name.split("_")[1];
						try {
							Date readDate = fileDateFormat.parse(dataString);
							if ((readDate.after(start) && readDate.before(end)) || readDate.equals(start) || readDate.equals(end)) {
								return true;
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					return false;
				}
			});
			if (files.length > 0) {
				logCounts.put(LogType.TERTIARY.name(), 0L);
			}
			for (String filename : files){
				processShutterFile(logCounts, LogType.TERTIARY.name(), folder + "/" + filename);
			}
		}
		return logCounts;
	}

	private void processShutterFile(Map<String, Long> logCounts,
			String shutterName, String filename) {
		BufferedReader br = null;
		try{
			boolean oldStatus = false;
			long timeStamp = 0;
			br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			long currTime = 0;
			Long oldTime = null;
	        while (line != null) {
	            String[] pair = line.split("\t");
	            currTime = logDateFormat.parse(pair[0]).getTime();
	            if (currTime > timeStamp) {
	            	oldTime = logCounts.get(shutterName);
	            	boolean newStatus = "OPEN".equals(pair[1]);
	            	if (oldStatus) {
	            		if (oldTime != null) {
	            			logCounts.put(shutterName, currTime - timeStamp + oldTime);
	            		} else {
	            			logCounts.put(shutterName, currTime - timeStamp);
	            		}
	            	}
	            	timeStamp = currTime;
	            	oldStatus = newStatus;
	            }
	            line = br.readLine();
	        }
	        Date now = new Date();
            if (oldStatus) {
            	if (filename.endsWith(fileDateFormat.format(now))){
		            oldTime = logCounts.get(shutterName);
	            	if (oldTime != null) {
	            		logCounts.put(shutterName, now.getTime() - timeStamp + oldTime);
	            	} else {
	            		logCounts.put(shutterName, now.getTime() - timeStamp);
	            	}
	            }	        	
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void processStatusFile(Map<String, Long> logCounts, String filename) {
		BufferedReader br = null;
		try{
			String oldStatus = null;
			long timeStamp = 0;
			br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			if (line == null) {
				return;
			}
			long startTime = logDateFormat.parse(line.split("\t")[0]).getTime();
			long currTime = 0;
			Long oldTime = null;
	        while (line != null) {
	        	String[] pair = line.split("\t");
	        	currTime = logDateFormat.parse(pair[0]).getTime();
	        	if (currTime >= timeStamp) {
	        		if (oldStatus != null) {
	        			oldTime = logCounts.get(oldStatus);
	        	        if (currTime - startTime > 420000000) {
	        	        	System.err.println(currTime - startTime);
	        	        }
	        			if (oldTime != null) {
	        				logCounts.put(oldStatus, currTime - timeStamp + oldTime);
	        			} else {
	        				logCounts.put(oldStatus, currTime - timeStamp);
	        			}
	        		}
	        		timeStamp = currTime;
	        		oldStatus = "STATUS." + pair[1];
	        	}
	            line = br.readLine();
	        }
	        Date now = new Date();
            if (filename.endsWith(fileDateFormat.format(now))){
            	if (oldStatus != null) {
            		oldTime = logCounts.get(oldStatus);
	            	if (oldTime != null) {
	            		logCounts.put(oldStatus, now.getTime() - timeStamp + oldTime);
	            	} else {
	            		logCounts.put(oldStatus, now.getTime() - timeStamp);
	            	}
	            	currTime = now.getTime();
	            }
	        }
	        oldTime = logCounts.get("TOTAL");
	        if (oldTime != null) {
    			logCounts.put("TOTAL", currTime - startTime + oldTime);
    		} else {
    			logCounts.put("TOTAL", currTime - startTime);
    		}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(filename);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		
	}

	public String getStartDate() {
		Date earliest = new Date();
		File folder = new File(logFolder);
		if (!folder.exists()) {
			return fileDateFormat.format(earliest);
		}
		String[] files = folder.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith(LogType.STATUS.name())) {
					return true;
				}
				return false;
			}
		});
		if (files.length == 0){
			return fileDateFormat.format(earliest);
		}
		for (String filename : files) {
			String dataString = filename.split("_")[1];
			try {
				Date readDate = fileDateFormat.parse(dataString);
				if (readDate.before(earliest)) {
					earliest = readDate;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return fileDateFormat.format(earliest);
	}
}
