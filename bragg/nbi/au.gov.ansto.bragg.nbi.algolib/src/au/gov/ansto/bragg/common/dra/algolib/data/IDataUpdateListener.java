package au.gov.ansto.bragg.common.dra.algolib.data;
/**
 * A listener that listens for when a data set has been updated.
 * @author hrz
 *
 */
public interface IDataUpdateListener {
	/**
	 * Called when the data set has been changed.
	 *
	 */
	public void dataUpdated();
}
