package au.gov.ansto.bragg.common.dra.algolib.processes;


/**
 * A generic processor that can be strung together to form data reduction
 * routines.
 * <p>!!Change: No longer uses explicit parameters in the interface; use bean
 * properties instead.
 * @author hrz
 *
 */
public abstract class Processor {
	
	/**
	 * Processes a data set.
	 * @param in The input data.
	 * @return The output data.
	 */
	public abstract Signal process(Signal in);
}
