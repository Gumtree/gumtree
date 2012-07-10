package au.gov.ansto.bragg.common.dra.algolib.processes;

/**
 * An exception that is created when a Processor is given data
 * of the wrong type.
 * @author hrz
 *
 */
public class WrongDataTypeException extends Exception {

	private static final long serialVersionUID = 3609266035195095440L;
	
	/**
	 * Creates a new exception.
	 * @param message The error message.
	 */
	public WrongDataTypeException(String message)
	{
		super(message);
	}
}
