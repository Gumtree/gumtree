package au.gov.ansto.bragg.common.dra.algolib.processes;


/**
 * An processor for 'purely functional' processes, this superclass buffers the last input and parameters, and if they are the same, will return the last output, avoiding calculation. Subclasses should implement processNew and setParametersChanged instead of process and setParameters. These will only be called when the input data has changed.
 * @author  hrz
 */
public abstract class BufferedProcessor extends Processor {

	private transient Object lastIn;
	private transient Signal lastOut;
	private transient boolean paramsChanged = false;
	/**
	 * Removes information about the last cached result.
	 */
	protected void forget()
	{
		lastIn = null;
	}

	@Override
	public Signal process(Signal in) {
		if(paramsChanged || in.rawData() != lastIn)
		{
			lastIn = in.rawData();
			lastOut = processNew(in);
		}
		return lastOut;
	}
	
	protected abstract Signal processNew(Signal in);

}
