package au.gov.ansto.bragg.common.dra.algolib.plot;

import java.util.Date;

/**
 * A parent class for plot data objects.
 * @author hrz
 *
 */
public abstract class PlotData {
	/**
	 * The time at which this plot data object was created.
	 */
	public final Date timeStamp;
	/**
	 * Creates a new plot data object.
	 *
	 */
	public PlotData()
	{
		timeStamp = new Date();
	}
}
