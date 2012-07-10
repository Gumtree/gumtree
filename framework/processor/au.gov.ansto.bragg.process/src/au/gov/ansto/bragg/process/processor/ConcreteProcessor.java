/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.process.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * The abstract concrete processor.
 * @author nxi
 *
 */
public abstract class ConcreteProcessor {

	private String processClass; 
	private String processClassVersion; 
	private long   processClassID; 

    private Boolean isDebugMode = false;

	private Map<String, TunerPortListener> listeners = new HashMap<String, TunerPortListener>();
	private boolean isReprocessable = true;
	public static String ISREPROCESSABLE_METHOD_NAME = "isReprocessable";
	
//	public List<Object> process(Object ...objects );
	public abstract Boolean process() throws Exception;
	
	public void addVarListener(String name, TunerPortListener listener){
		listeners.put(name, listener);
	}
	
	public void removeVarListener(String name, TunerPortListener listener){
		listeners.remove(name);
	}
	
//	public void setField(String filedName, Object value);
	
	protected void informVarValueChange(String name, Object value){
		for (String listenerName : listeners.keySet()){
			if (listenerName.matches(name))
				listeners.get(listenerName).updateValue(value);
		}
	}
	
	protected void informVarMaxChange(String name, Object max){
		for (String listenerName : listeners.keySet()){
			if (listenerName.matches(name))
				listeners.get(listenerName).updateMax(max);
		}		
	}

	protected void informVarMinChange(String name, Object min){
		for (String listenerName : listeners.keySet()){
			if (listenerName.matches(name))
				listeners.get(listenerName).updateMin(min);
		}		
	}
	
	protected void informVarOptionsChange(String name, List<?> options){
		for (String listenerName : listeners.keySet()){
			if (listenerName.matches(name))
				listeners.get(listenerName).updateOptions(options);
		}		
	}

	/**
	 * @return the isReprocessable
	 */
	public boolean isReprocessable() {
		return isReprocessable;
	}

	/**
	 * Set isReprocessable to false if the input into the processor is overwritten hence not available
	 * for run the process again. 
	 * @param isReprocessable the isReprocessable to set
	 */
	public void setReprocessable(boolean isReprocessable) {
		this.isReprocessable = isReprocessable;
	}

	/**----------------------------------------------------------------------------------- 
	 * Extensions for audit of processor instances and debugging
	 *  pvhathaway 17 June 2009 
	 */
	
	/**
	 * Query Processor Class name string for audit 
	 * @return ProcessorClass name string
	 */
	public String getProcessClass() {
		return processClass;
	}

	/**
	 * Set Processor Class name string for audit 
	 */
	public void setProcessClass(String processClass) {
		this.processClass = processClass;
	}

	/**
	 * Query Processor Class Version string for audit 
	 * @return ProcessClassVersion string
	 */
	public String getProcessClassVersion() {
		return processClassVersion;
	}

	/**
	 * Set Processor Class Version string for audit 
	 */
	public void setProcessClassVersion(String processClassVersion) {
		this.processClassVersion = processClassVersion;
	}

	/**
	 * Query Processor Class ID for audit
	 * @return Processor Class ID long integer
	 */
	public long getProcessClassID() {
		return processClassID;
	}

	/**
	 * Set Processor Class ID for audit 
	 */
	public void setProcessClassID(long processClassID) {
		this.processClassID = processClassID;
	}

	/**
	 * Query flag to see if debug options should be allowed
	 * @return isDebugMode - boolean debug mode flag
	 */
	public Boolean getIsDebugMode() {
		return isDebugMode;
	}

	/**
	 * Set boolean flag to indicate debugging options should be made available 
	 */
	public void setIsDebugMode(Boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}
}
