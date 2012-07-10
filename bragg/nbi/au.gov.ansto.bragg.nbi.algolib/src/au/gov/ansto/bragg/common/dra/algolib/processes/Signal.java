package au.gov.ansto.bragg.common.dra.algolib.processes;

/**
 * A type for data passed between processors.
 * @author hrz
 *
 */
public interface Signal{
	
	/**
	 * @return The name of the signal.
	 */
	public String name();
	/**
	 * @return The signals data set.
	 */
	public Object rawData();
	/**
	 * Tests if a signal has data of a particular type.
	 * @param <T> The type of the data.
	 * @param type The type of data.
	 * @return True if that type of data is present.
	 */
	public <T> boolean hasData(Class<T> type);
	/**
	 * Tries to retrieve data of a specified type.
	 * @param <T> The type of the data.
	 * @param type The type of the data.
	 * @return The data if available.
	 */
	public <T> T dataAs(Class<T> type);
}
