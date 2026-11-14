/**
 * 
 */
package org.gumtree.control.server.log;

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
import java.util.concurrent.atomic.AtomicReference;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsControllerAdapter;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nxi
 *
 */
public class ControlLogManager {

	enum LogType{TERTIARY, SECONDARY, STATUS}
	
	private final static Logger logger = LoggerFactory.getLogger(ControlLogManager.class);
	public final static String PROPERTY_LOGGING_PATH = "gumtree.logging.path";
	public final static String PROPERTY_LOGGING_SHUTTER_ENABLED = "gumtree.logging.shutterEnabled";
	private final static String SICS_NODE_SECONDARY = "gumtree.sics.path.secondaryshutter";
	private final static String SICS_NODE_TERTIARY = "gumtree.sics.path.tertiaryshutter";
	private final static String DEFAULT_NODE_SECONDARY = "/instrument/sis/guide/b03_secondary_open";
	private final static String DEFAULT_NODE_TERTIARY = "/instrument/sis/instrument/b00_tertiary_open";
	private final static String SHUTTER_OPEN = "OPEN";
	private final static String SHUTTER_CLOSED = "CLOSED";
	private final static String SHUTTER_UNKNOWN = "UNKNOWN_LOW";
	private static ControlLogManager instance;
	private Map<LogType, PrintWriter> logFiles;
	private Map<LogType, String> lastLogEntries;
	private int dayOfWeek;
	private String logFolder;
	private boolean isShutterEnabled;
	public DateFormat logDateFormat;
	public DateFormat fileDateFormat;
	private ISicsProxyListener proxyListener;
	private final AtomicReference<ISicsProxy> registeredProxy = new AtomicReference<ISicsProxy>();
	// Guards the shutter listener registration only. Lock order is:
	// proxy listeners -> registrationLock -> controller listeners -> this (log files).
	// Never call into the proxy or controllers while holding 'this'.
	private final Object registrationLock = new Object();
	private final Map<LogType, ISicsController> shutterControllers = new HashMap<LogType, ISicsController>();
	private final Map<LogType, SicsControllerAdapter> shutterListeners = new HashMap<LogType, SicsControllerAdapter>();
	private ControlLogStatistics statistics;

	/**
	 * 
	 */
	private ControlLogManager() {
		dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);;
		logFiles = new HashMap<LogType, PrintWriter>();
		lastLogEntries = new HashMap<LogType, String>();
		logFolder = System.getProperty(PROPERTY_LOGGING_PATH);
		isShutterEnabled = Boolean.valueOf(System.getProperty(PROPERTY_LOGGING_SHUTTER_ENABLED, "false"));
		logDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		proxyListener = new SicsProxyListenerAdapter() {
			
			@Override
			public void setStatus(ServerStatus newStatus) {
				log(LogType.STATUS, newStatus.getText());
			}
			
			@Override
			public void disconnect() {
				log(LogType.STATUS, ServerStatus.UNKNOWN.getText());
			}
			
			@Override
			public void modelUpdated(final ISicsModel sicsModel) {
				initModelListener(sicsModel);
			}

		};
		

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

	/**
	 * Registers the log manager with the proxy. Safe to call repeatedly (the
	 * connection monitor calls it on every connection attempt): the proxy
	 * listener is only ever registered once.
	 */
	public void initiate(ISicsProxy sicsProxy) {
		if (sicsProxy != null) {
			ISicsProxy previous = registeredProxy.getAndSet(sicsProxy);
			if (previous != sicsProxy) {
				if (previous != null) {
					previous.removeProxyListener(getProxyListener());
				}
				sicsProxy.addProxyListener(getProxyListener());
			}
			if (sicsProxy.isConnected()) {
				log(LogType.STATUS, sicsProxy.getServerStatus().getText());
				initModelListener(sicsProxy.getSicsModel());
			}
		}
	}

	private void initModelListener(final ISicsModel sicsModel) {
		if (sicsModel == null) {
			return;
		}
		synchronized (registrationLock) {
			attachShutterListener(sicsModel, LogType.SECONDARY,
					System.getProperty(SICS_NODE_SECONDARY, DEFAULT_NODE_SECONDARY));
			attachShutterListener(sicsModel, LogType.TERTIARY,
					System.getProperty(SICS_NODE_TERTIARY, DEFAULT_NODE_TERTIARY));
		}
	}

	private void attachShutterListener(ISicsModel sicsModel, final LogType type, String path) {
		SicsControllerAdapter listener = shutterListeners.get(type);
		if (listener == null) {
			listener = new SicsControllerAdapter() {
				@Override
				public void updateValue(Object oldValue, Object newValue) {
					log(type, convertShutterStatus(newValue));
				}
			};
			shutterListeners.put(type, listener);
		}
		// detach from the controller of the previous model, if any
		ISicsController oldController = shutterControllers.remove(type);
		if (oldController != null) {
			oldController.removeControllerListener(listener);
		}
		ISicsController controller = sicsModel.findController(path);
		if (controller == null) {
			return;
		}
		if (controller instanceof IDynamicController) {
			Object value;
			try {
				value = ((IDynamicController) controller).getValue();
			} catch (SicsModelException e) {
				value = SHUTTER_UNKNOWN;
			}
			log(type, convertShutterStatus(value));
		}
		// remove first in case the model reused the same controller instance
		controller.removeControllerListener(listener);
		controller.addControllerListener(listener);
		shutterControllers.put(type, controller);
	}

	private String convertShutterStatus(Object value) {
		String data = String.valueOf(value);
		if (data.equalsIgnoreCase("Opened")
				| data.equalsIgnoreCase("OPEN") | data.startsWith("1")) {
			data = SHUTTER_OPEN;
		} else if (data.equalsIgnoreCase("Closed")
				| data.equalsIgnoreCase("CLOSE") | data.startsWith("0")) {
			data = SHUTTER_CLOSED;
		} else {
			data = SHUTTER_UNKNOWN;
		}
		return data;
	}
	
	public ISicsProxyListener getProxyListener() {
		return proxyListener;
	}
	
	private synchronized void createFiles(String parentPath) {
		Date date = new Date();
		LogType[] types = isShutterEnabled ? LogType.values() : new LogType[] {LogType.STATUS};
		for (LogType type : types) {
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
			// close off the old day's file with the current state
			if (lastEntry != null) {
				writeEntry(type, lastEntry);
			}
			try {
				PrintWriter outputfile = new PrintWriter(new FileWriter(file, true), true);
				PrintWriter oldFile = logFiles.get(type);
				logFiles.put(type, outputfile);
				if (oldFile != null) {
					oldFile.close();
				}
				// open the new day's file with the current state
				if (lastEntry != null) {
					writeEntry(type, lastEntry);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Logs a status value. A value identical to the previous entry for the
	 * same log type is skipped.
	 */
	public synchronized void log(LogType type, String text) {
		if (text == null || text.equals(lastLogEntries.get(type))) {
			return;
		}
		logger.debug(text);
		writeEntry(type, text);
	}

	private void writeEntry(LogType type, String text) {
		PrintWriter outputFile = logFiles.get(type);
		if (outputFile != null) {
			outputFile.append(logDateFormat.format(new Date()) + " \t" + text + "\n");
			outputFile.flush();
			lastLogEntries.put(type, text);
		}
	}

	public synchronized void close() {
		for (Entry<LogType, PrintWriter> entry: logFiles.entrySet()){
			entry.getValue().close();
		}
	}

	public static ControlLogManager getInstance() {
		if (instance == null) {
			instance = new ControlLogManager();
		}
		return instance;
	}

	
	private synchronized ControlLogStatistics getStatistics() {
		if (statistics == null && logFolder != null) {
			statistics = new ControlLogStatistics(logFolder);
		}
		return statistics;
	}

	/**
	 * Returns the status (and shutter, if enabled) durations in milliseconds
	 * for the days from start to end, inclusive. Completed months are read from
	 * the cached monthly summaries, see {@link ControlLogStatistics}.
	 */
	public Map<String, Long> processLog(final Date start, final Date end) {
		ControlLogStatistics stats = getStatistics();
		if (stats == null) {
			return new HashMap<String, Long>();
		}
		return stats.processLog(start, end, isShutterEnabled);
	}

	/**
	 * Builds the missing monthly summaries of all completed months.
	 * @return number of summaries written
	 */
	public int prepareMonthlyStatistics(boolean overwrite) {
		ControlLogStatistics stats = getStatistics();
		return stats == null ? 0 : stats.prepareMonthlyStatistics(overwrite);
	}

	public String getStartDate() {
		ControlLogStatistics stats = getStatistics();
		if (stats == null) {
			return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		return stats.getStartDate();
	}
}
