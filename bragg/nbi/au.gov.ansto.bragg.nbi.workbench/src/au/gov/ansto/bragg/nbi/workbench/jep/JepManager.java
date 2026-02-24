/**
 * 
 */
package au.gov.ansto.bragg.nbi.workbench.jep;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import jep.Interpreter;
import jep.JepConfig;
import jep.SubInterpreter;

/**
 * @author nxi
 *
 */
public class JepManager {

	/**
	 * 
	 */
	private static Map<OutputStream, Interpreter> interpMap = new HashMap<OutputStream, Interpreter>();
	
	private OutputStream writer;
	
	public JepManager() {
	}

	private static Interpreter createInterpreter(OutputStream writer, OutputStream errorWriter) {
		JepConfig config = new JepConfig();
		config.redirectStdout(writer);
		config.redirectStdErr(errorWriter);
		Interpreter interp = new SubInterpreter(config);
		interpMap.put(writer, interp);
		return interp;
	}
	
	public synchronized static Interpreter getInterpreter(OutputStream writer, OutputStream errorWriter) {
		if (interpMap.containsKey(writer)) {
			return interpMap.get(writer);
		} else {
			return createInterpreter(writer, errorWriter);
		}
	}
	
}
