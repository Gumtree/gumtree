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
package au.gov.ansto.bragg.kowari.exp.command;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.gumnix.sics.batch.ui.commands.AbstractSicsCommand;

public abstract class AbstractScanCommand extends AbstractSicsCommand{

	public final static String NEW_FILE_TEXT = "newfile HISTOGRAM_XYT\n";
	protected List<AbstractScanParameter> parameters;
	protected String scan_mode;
	protected float preset;

	private String title;
	private String commandName;

	
	/**
	 * 
	 */
	public AbstractScanCommand() {
		super();
		parameters = new ArrayList<AbstractScanParameter>();
	}

	public String getCommandName(){
		return commandName;
	}
	
	public void setCommandName(String name){
		commandName = name;
	}
	
	@Override
	public String toScript() {
		return null;
	}

	protected String getHistmemPreset(){
		return "histmem mode " + scan_mode + "\n" + 
		"histmem preset " + preset + "\n";
	}
	
	protected String getHistmemScript(int runNumber) {
		return "histmem start block\n" + "save " + runNumber + "\n";
	}
	
	protected String getHistmemScript() {
		return "histmem start block\n";
	}
	
	public String getScanDescription(){
		String script = "";
		if (parameters.size() == 1){
			script += commandName + " " + parameters.get(0).toString() + " " + scan_mode + " " + preset;
		}
		if (parameters.size() > 1){
			script += commandName + " (";
			for (AbstractScanParameter parameter : parameters){
				script += parameter.toString() + ":";
			}
			script = script.substring(0, script.length() - 1);
			script += ") " + scan_mode + " " + preset;
		}
		return script;
	}
	
	public void insertParameter(int i, AbstractScanParameter parameter){
		parameters.add(i, parameter);
		firePropertyChange("parameter_add", null, parameter);
	}
	
	public int indexOfParameter(AbstractScanParameter parameter){
		return parameters.indexOf(parameter);
	}
	
	public List<AbstractScanParameter> getParameterList(){
		return parameters;
	}
	
	public void insertParameter(AbstractScanParameter parameter){
		parameters.add(parameter);
	}
	
	public void removeParameter(AbstractScanParameter parameter){
		int oldValue = parameters.size();
		parameters.remove(parameter);
		firePropertyChange("parameter_remove", oldValue, parameters.size());
	}

	/**
	 * @return the scan_mode
	 */
	public String getScan_mode() {
		return scan_mode;
	}

	/**
	 * @param scanMode the scan_mode to set
	 */
	public void setScan_mode(String scan_mode) {
		String oldValue = this.scan_mode;
		this.scan_mode = scan_mode;
		firePropertyChange("scan_mode", oldValue, scan_mode);
	}

	/**
	 * @return the preset
	 */
	public float getPreset() {
		return preset;
	}

	/**
	 * @param preset the preset to set
	 */
	public void setPreset(float preset) {
		float oldValue = this.preset;
		this.preset = preset;
		firePropertyChange("preset", oldValue, preset);
	}
	
	protected class IntegerWrapper{
		private int value;
		public IntegerWrapper(int value){
			this.value = value;
		}
		/**
		 * @return the value
		 */
		public int getValue() {
			return value;
		}
		/**
		 * @param value the value to set
		 */
		public void setValue(int value) {
			this.value = value;
		}
		public void stepUp(){
			value ++;
		}
	}
	
	public abstract float getEstimatedTime();
	
	@Override
	public String getEstimationUnits() {
		String mode = getScan_mode();
		if ("time".equals(mode)) {
			return "secs";
		} else if ("count".equals(mode)) {
			return "cts";
		} else {
			return "";
		}
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

}
