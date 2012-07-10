package au.gov.ansto.bragg.common.dra.algolib.data;


/**
 * A common superclass to data providers. A data provider is an object
 * that provides a series data set for a plotter. It may use lazy evaluation,
 * calculating the averaged data only when it is needed.
 * @author hrz
 *
 */
public interface DataProvider {
	/**
	 * @return The display name for the plot.
	 */
	public String getPlotName();
	/**
	 * Registers a plotter to be informed when the data is updated.
	 * @param l The plotter in question.
	 */
	public void registerInterest(IDataUpdateListener l);

}
