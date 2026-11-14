package org.gumtree.control.server.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes time statistics from the daily control log files
 * (<code>STATUS_yyyy-MM-dd</code>, <code>SECONDARY_yyyy-MM-dd</code>,
 * <code>TERTIARY_yyyy-MM-dd</code>).
 * <p>
 * To avoid opening every daily file of a long period, the statistics of each
 * completed month are cached in <code>&lt;logFolder&gt;/monthly/STATISTICS_yyyy-MM</code>,
 * and those of each completed year (summed from the monthly summaries) in
 * <code>&lt;logFolder&gt;/yearly/STATISTICS_yyyy</code>.
 * When a request covers a whole completed year or month the cached summary is
 * used; missing summaries are created on demand. They can also be prepared in
 * advance with {@link #prepareStatistics(boolean)} or from the command line:
 * <pre>
 * java -cp org.gumtree.control.server.jar;slf4j-api.jar org.gumtree.control.server.log.ControlLogStatistics &lt;logFolder&gt; [-overwrite]
 * </pre>
 * The returned map holds durations in milliseconds, keyed by
 * <code>STATUS.&lt;status&gt;</code>, <code>TOTAL</code>, and (when shutter files
 * exist) <code>SECONDARY</code> / <code>TERTIARY</code> for the shutter-open time.
 */
public class ControlLogStatistics {

	public static final String MONTHLY_FOLDER = "monthly";
	public static final String YEARLY_FOLDER = "yearly";
	public static final String MONTHLY_PREFIX = "STATISTICS_";
	public static final String KEY_TOTAL = "TOTAL";
	public static final String KEY_STATUS_PREFIX = "STATUS.";

	static final String TYPE_STATUS = "STATUS";
	static final String TYPE_SECONDARY = "SECONDARY";
	static final String TYPE_TERTIARY = "TERTIARY";
	private static final String[] SHUTTER_TYPES = {TYPE_SECONDARY, TYPE_TERTIARY};

	private static final String KEY_FORMAT_VERSION = "FORMAT_VERSION";
	private static final String FORMAT_VERSION = "1";
	private static final String SHUTTER_OPEN = "OPEN";
	private static final String LOG_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	private static final String FILE_DATE_PATTERN = "yyyy-MM-dd";
	private static final String MONTH_PATTERN = "yyyy-MM";
	private static final String YEAR_PATTERN = "yyyy";
	/**
	 * A month is only summarised once this long after it ended, so that the
	 * closing entry written just after midnight into the last daily file is
	 * included.
	 */
	private static final long MONTH_SETTLE_TIME = 3600000L;

	private final static Logger logger = LoggerFactory.getLogger(ControlLogStatistics.class);

	private final File logFolder;
	private final File monthlyFolder;
	private final File yearlyFolder;
	private final Object monthlyLock = new Object();
	private volatile String cachedStartDate;

	public ControlLogStatistics(String logFolder) {
		this.logFolder = new File(logFolder);
		this.monthlyFolder = new File(this.logFolder, MONTHLY_FOLDER);
		this.yearlyFolder = new File(this.logFolder, YEARLY_FOLDER);
	}

	/**
	 * Returns the statistics of all days from <code>start</code> to
	 * <code>end</code> (both inclusive, time of day ignored).
	 */
	public Map<String, Long> processLog(Date start, Date end, boolean includeShutter) {
		Map<String, Long> logCounts = new HashMap<String, Long>();
		if (!logFolder.isDirectory()) {
			return logCounts;
		}
		DateFormat logDateFormat = new SimpleDateFormat(LOG_DATE_PATTERN);
		DateFormat fileDateFormat = new SimpleDateFormat(FILE_DATE_PATTERN);
		String today = fileDateFormat.format(new Date());
		Calendar cursor = toDay(start);
		Calendar last = toDay(end);
		Calendar firstLogMonth = getFirstLogMonth();
		while (!cursor.after(last)) {
			if (cursor.get(Calendar.DAY_OF_YEAR) == 1) {
				Calendar nextYear = (Calendar) cursor.clone();
				nextYear.add(Calendar.YEAR, 1);
				if (!nextYear.after(dayAfter(last)) && isMonthComplete(nextYear)) {
					// a year entirely before the first log has no data
					if (nextYear.after(firstLogMonth)) {
						merge(logCounts, getYearlyStatistics(cursor), includeShutter);
					}
					cursor = nextYear;
					continue;
				}
			}
			if (cursor.get(Calendar.DAY_OF_MONTH) == 1) {
				Calendar nextMonth = (Calendar) cursor.clone();
				nextMonth.add(Calendar.MONTH, 1);
				if (!nextMonth.after(dayAfter(last)) && isMonthComplete(nextMonth)) {
					// a month entirely before the first log has no data
					if (nextMonth.after(firstLogMonth)) {
						merge(logCounts, getMonthlyStatistics(cursor), includeShutter);
					}
					cursor = nextMonth;
					continue;
				}
			}
			processDay(logCounts, cursor.getTime(), includeShutter,
					logDateFormat, fileDateFormat, today);
			cursor.add(Calendar.DAY_OF_MONTH, 1);
		}
		return logCounts;
	}

	/**
	 * Creates the monthly summaries, then the yearly summaries, of every
	 * completed month and year since the first log file.
	 *
	 * @param overwrite if true, existing summaries are rebuilt
	 * @return the number of summaries written, {monthly, yearly}
	 */
	public int[] prepareStatistics(boolean overwrite) {
		return new int[] {prepareMonthlyStatistics(overwrite), prepareYearlyStatistics(overwrite)};
	}

	/**
	 * Creates the monthly summary of every completed month since the first log
	 * file.
	 *
	 * @param overwrite if true, existing summaries are rebuilt
	 * @return the number of summaries written
	 */
	public int prepareMonthlyStatistics(boolean overwrite) {
		int count = 0;
		Calendar month = getFirstLogMonth();
		while (true) {
			Calendar nextMonth = (Calendar) month.clone();
			nextMonth.add(Calendar.MONTH, 1);
			if (!isMonthComplete(nextMonth)) {
				break;
			}
			synchronized (monthlyLock) {
				File file = getMonthlyFile(month);
				if (overwrite || loadSummaryFile(file) == null) {
					if (writeSummaryFile(file, computeMonth(month))) {
						count++;
					}
				}
			}
			month = nextMonth;
		}
		return count;
	}

	/**
	 * Creates the yearly summary of every completed year since the first log
	 * file, from the monthly summaries (which are created if missing). Call
	 * {@link #prepareMonthlyStatistics(boolean)} first when rebuilding.
	 *
	 * @param overwrite if true, existing summaries are rebuilt
	 * @return the number of summaries written
	 */
	public int prepareYearlyStatistics(boolean overwrite) {
		int count = 0;
		Calendar year = getFirstLogMonth();
		year.set(Calendar.MONTH, Calendar.JANUARY);
		while (true) {
			Calendar nextYear = (Calendar) year.clone();
			nextYear.add(Calendar.YEAR, 1);
			if (!isMonthComplete(nextYear)) {
				break;
			}
			synchronized (monthlyLock) {
				File file = getYearlyFile(year);
				if (overwrite || loadSummaryFile(file) == null) {
					if (writeSummaryFile(file, computeYear(year))) {
						count++;
					}
				}
			}
			year = nextYear;
		}
		return count;
	}

	/**
	 * Returns the statistics of a completed month, loaded from the monthly
	 * summary file, which is created first if it doesn't exist.
	 */
	Map<String, Long> getMonthlyStatistics(Calendar monthStart) {
		synchronized (monthlyLock) {
			File file = getMonthlyFile(monthStart);
			Map<String, Long> result = loadSummaryFile(file);
			if (result == null) {
				result = computeMonth(monthStart);
				writeSummaryFile(file, result);
			}
			return result;
		}
	}

	/**
	 * Returns the statistics of a completed year, loaded from the yearly
	 * summary file, which is created first (from the monthly summaries) if it
	 * doesn't exist.
	 */
	Map<String, Long> getYearlyStatistics(Calendar yearStart) {
		synchronized (monthlyLock) {
			File file = getYearlyFile(yearStart);
			Map<String, Long> result = loadSummaryFile(file);
			if (result == null) {
				result = computeYear(yearStart);
				writeSummaryFile(file, result);
			}
			return result;
		}
	}

	/**
	 * Sums the monthly summaries of a year. Months before the first log file
	 * are skipped, so no empty monthly summaries are created for them.
	 */
	private Map<String, Long> computeYear(Calendar yearStart) {
		Map<String, Long> result = new HashMap<String, Long>();
		Calendar firstLogMonth = getFirstLogMonth();
		Calendar month = (Calendar) yearStart.clone();
		int year = month.get(Calendar.YEAR);
		while (month.get(Calendar.YEAR) == year) {
			if (!month.before(firstLogMonth)) {
				merge(result, getMonthlyStatistics(month), true);
			}
			month.add(Calendar.MONTH, 1);
		}
		return result;
	}

	private Calendar getFirstLogMonth() {
		Calendar month;
		try {
			month = toDay(new SimpleDateFormat(FILE_DATE_PATTERN).parse(getStartDate()));
		} catch (ParseException e) {
			logger.error("failed to find the first log date", e);
			month = toDay(new Date());
		}
		month.set(Calendar.DAY_OF_MONTH, 1);
		return month;
	}

	/**
	 * Earliest date of the STATUS log files, in yyyy-MM-dd format. The result is
	 * cached because it requires listing the whole log folder.
	 */
	public String getStartDate() {
		String cached = cachedStartDate;
		if (cached != null) {
			return cached;
		}
		DateFormat fileDateFormat = new SimpleDateFormat(FILE_DATE_PATTERN);
		String today = fileDateFormat.format(new Date());
		String[] files = logFolder.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(TYPE_STATUS + "_");
			}
		});
		if (files == null || files.length == 0) {
			return today;
		}
		Date earliest = null;
		for (String filename : files) {
			try {
				Date readDate = fileDateFormat.parse(filename.substring(TYPE_STATUS.length() + 1));
				if (earliest == null || readDate.before(earliest)) {
					earliest = readDate;
				}
			} catch (ParseException e) {
				logger.warn("unexpected log file name: " + filename);
			}
		}
		if (earliest == null) {
			return today;
		}
		cached = fileDateFormat.format(earliest);
		cachedStartDate = cached;
		return cached;
	}

	private Map<String, Long> computeMonth(Calendar monthStart) {
		Map<String, Long> logCounts = new HashMap<String, Long>();
		DateFormat logDateFormat = new SimpleDateFormat(LOG_DATE_PATTERN);
		DateFormat fileDateFormat = new SimpleDateFormat(FILE_DATE_PATTERN);
		String today = fileDateFormat.format(new Date());
		Calendar day = (Calendar) monthStart.clone();
		int month = day.get(Calendar.MONTH);
		while (day.get(Calendar.MONTH) == month) {
			// always include shutters; they are filtered when merging
			processDay(logCounts, day.getTime(), true, logDateFormat, fileDateFormat, today);
			day.add(Calendar.DAY_OF_MONTH, 1);
		}
		return logCounts;
	}

	private void processDay(Map<String, Long> logCounts, Date day, boolean includeShutter,
			DateFormat logDateFormat, DateFormat fileDateFormat, String today) {
		String dayString = fileDateFormat.format(day);
		boolean isToday = dayString.equals(today);
		File statusFile = new File(logFolder, TYPE_STATUS + "_" + dayString);
		if (statusFile.isFile()) {
			processStatusFile(logCounts, statusFile, isToday, logDateFormat);
		}
		if (includeShutter) {
			for (String type : SHUTTER_TYPES) {
				File shutterFile = new File(logFolder, type + "_" + dayString);
				if (shutterFile.isFile()) {
					if (!logCounts.containsKey(type)) {
						logCounts.put(type, 0L);
					}
					processShutterFile(logCounts, type, shutterFile, isToday, logDateFormat);
				}
			}
		}
	}

	private void processShutterFile(Map<String, Long> logCounts, String shutterName,
			File file, boolean isToday, DateFormat logDateFormat) {
		BufferedReader br = null;
		try {
			boolean oldStatus = false;
			long timeStamp = 0;
			long openTime = 0;
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String[] pair = line.split("\t");
				if (pair.length < 2) {
					continue;
				}
				long currTime = logDateFormat.parse(pair[0]).getTime();
				if (currTime > timeStamp) {
					if (oldStatus) {
						openTime += currTime - timeStamp;
					}
					timeStamp = currTime;
					oldStatus = SHUTTER_OPEN.equals(pair[1]);
				}
			}
			if (oldStatus && isToday) {
				openTime += System.currentTimeMillis() - timeStamp;
			}
			add(logCounts, shutterName, openTime);
		} catch (Exception e) {
			logger.error("failed to process " + file, e);
		} finally {
			close(br);
		}
	}

	private void processStatusFile(Map<String, Long> logCounts, File file, boolean isToday,
			DateFormat logDateFormat) {
		BufferedReader br = null;
		try {
			String oldStatus = null;
			long timeStamp = 0;
			long startTime = -1;
			long currTime = 0;
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String[] pair = line.split("\t");
				if (pair.length < 2) {
					continue;
				}
				currTime = logDateFormat.parse(pair[0]).getTime();
				if (startTime < 0) {
					startTime = currTime;
				}
				if (currTime >= timeStamp) {
					if (oldStatus != null) {
						add(logCounts, oldStatus, currTime - timeStamp);
					}
					timeStamp = currTime;
					oldStatus = KEY_STATUS_PREFIX + pair[1];
				}
			}
			if (startTime < 0) {
				return;
			}
			if (isToday && oldStatus != null) {
				currTime = System.currentTimeMillis();
				add(logCounts, oldStatus, currTime - timeStamp);
			}
			add(logCounts, KEY_TOTAL, currTime - startTime);
		} catch (Exception e) {
			logger.error("failed to process " + file, e);
		} finally {
			close(br);
		}
	}

	private File getMonthlyFile(Calendar monthStart) {
		return new File(monthlyFolder, MONTHLY_PREFIX
				+ new SimpleDateFormat(MONTH_PATTERN).format(monthStart.getTime()));
	}

	private File getYearlyFile(Calendar yearStart) {
		return new File(yearlyFolder, MONTHLY_PREFIX
				+ new SimpleDateFormat(YEAR_PATTERN).format(yearStart.getTime()));
	}

	/**
	 * @return the statistics, or null if the file doesn't exist or is invalid
	 */
	private Map<String, Long> loadSummaryFile(File file) {
		if (!file.isFile()) {
			return null;
		}
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(in);
			if (!FORMAT_VERSION.equals(properties.getProperty(KEY_FORMAT_VERSION))) {
				return null;
			}
			Map<String, Long> result = new HashMap<String, Long>();
			for (String key : properties.stringPropertyNames()) {
				if (!KEY_FORMAT_VERSION.equals(key)) {
					result.put(key, Long.valueOf(properties.getProperty(key).trim()));
				}
			}
			return result;
		} catch (Exception e) {
			logger.warn("invalid statistics file, will rebuild: " + file, e);
			return null;
		} finally {
			close(in);
		}
	}

	private boolean writeSummaryFile(File file, Map<String, Long> stats) {
		File folder = file.getParentFile();
		if (!folder.isDirectory() && !folder.mkdirs()) {
			logger.warn("failed to create folder " + folder);
			return false;
		}
		Properties properties = new Properties();
		properties.setProperty(KEY_FORMAT_VERSION, FORMAT_VERSION);
		for (Entry<String, Long> entry : stats.entrySet()) {
			properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
		}
		File temp = new File(folder, file.getName() + ".tmp");
		OutputStream out = null;
		try {
			out = new FileOutputStream(temp);
			properties.store(out, "Control log statistics (durations in ms)");
		} catch (IOException e) {
			logger.warn("failed to write " + temp, e);
			close(out);
			temp.delete();
			return false;
		} finally {
			close(out);
		}
		if (file.exists() && !file.delete()) {
			logger.warn("failed to replace " + file);
			temp.delete();
			return false;
		}
		if (!temp.renameTo(file)) {
			logger.warn("failed to rename " + temp + " to " + file);
			temp.delete();
			return false;
		}
		return true;
	}

	private static void merge(Map<String, Long> target, Map<String, Long> source,
			boolean includeShutter) {
		for (Entry<String, Long> entry : source.entrySet()) {
			String key = entry.getKey();
			if (!includeShutter && (TYPE_SECONDARY.equals(key) || TYPE_TERTIARY.equals(key))) {
				continue;
			}
			add(target, key, entry.getValue());
		}
	}

	private static void add(Map<String, Long> map, String key, long value) {
		Long old = map.get(key);
		map.put(key, old == null ? value : old + value);
	}

	/**
	 * A month (given by the start of the following month) is complete when it
	 * ended long enough ago for its last daily file to be closed.
	 */
	private static boolean isMonthComplete(Calendar nextMonthStart) {
		return System.currentTimeMillis() - nextMonthStart.getTimeInMillis() > MONTH_SETTLE_TIME;
	}

	private static Calendar toDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	private static Calendar dayAfter(Calendar day) {
		Calendar next = (Calendar) day.clone();
		next.add(Calendar.DAY_OF_MONTH, 1);
		return next;
	}

	private static void close(java.io.Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Prepares the monthly and yearly summaries manually.
	 * Usage: ControlLogStatistics &lt;logFolder&gt; [-overwrite]
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: ControlLogStatistics <logFolder> [-overwrite]");
			return;
		}
		boolean overwrite = args.length > 1 && "-overwrite".equals(args[1]);
		ControlLogStatistics statistics = new ControlLogStatistics(args[0]);
		long time = System.currentTimeMillis();
		int monthly = statistics.prepareMonthlyStatistics(overwrite);
		long monthTime = System.currentTimeMillis();
		int yearly = statistics.prepareYearlyStatistics(overwrite);
		long yearTime = System.currentTimeMillis();
		System.out.println(monthly + " monthly statistics file(s) written to "
				+ statistics.monthlyFolder + " in " + (monthTime - time) + " ms");
		System.out.println(yearly + " yearly statistics file(s) written to "
				+ statistics.yearlyFolder + " in " + (yearTime - monthTime) + " ms");
	}
}
