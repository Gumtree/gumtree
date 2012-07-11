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

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class AdvancedScanCommand extends AbstractScanCommand {

//	public boolean isSingleFile = true;

//	private float tot_time;
	
	/**
	 * 
	 */
	public AdvancedScanCommand() {
		super();
		setCommandName("scan");
//		SimpleNDParameter parameter = new SimpleNDParameter();

	}

//	/**
//	 * @return the tot_time
//	 */
//	public float getTot_time() {
//		return tot_time;
//	}
//	
//	/**
//	 * @param totTime the tot_time to set
//	 */
//	public void setTot_time(float tot_time) {
//		float oldValue = this.tot_time;
//		this.tot_time = tot_time;
//		firePropertyChange("tot_time", oldValue, tot_time);
//	}

	public String toScript() {
		// Return empty line if variable is not properly set
		String script = "# ";
		String title = getTitle();
		if (title == null) {
			script += "Advanced Multi-dimensional Scan Block\n";
			title = getScanDescription();
		} else {
			script += title + "\n";
		}
		script += "title " + "\"" + title + "\"\n";
		script += getHistmemPreset();
		if (isSingleFile()) {
			script += AbstractScanCommand.NEW_FILE_TEXT;
			script += "set savenumber 0\n";
		}
		script += "\n";
		script += "set START_NUMBER 0\n";
		script += "set loopnumber 0\n";
		script += loopParameters(parameters, "", 0, "");
		return script;
	}

	private String loopParameters(List<AbstractScanParameter> parameters, String indent, 
			int id, String broadcastScript) {
		if (parameters == null || parameters.size() == 0)
			return "";
		String script = "";
		AbstractScanParameter parameter = parameters.get(0);
		String indexName = "idx" + id;
		script += parameter.getForLoopHead(indexName, indent);
		if (((AdvancedParameter) parameter).getDoCreateFile()) {
			script += indent + "\tset savenumber 0\n";
		}
		script += indent + "\tif {$START_NUMBER <= $loopnumber} {\n";
		if (((AdvancedParameter) parameter).getDoCreateFile()) {
			script += indent + "\t\t" + AbstractScanCommand.NEW_FILE_TEXT;
		}
		script += parameter.getDriveScript(indexName, indent + "\t\t");
		broadcastScript += parameter.getBroadcastScript(indexName, "");
		if (parameters.size() > 1) {
			script += indent + "\t}\n";
		}
		script += loopParameters(parameters.subList(1, parameters.size()), indent + "\t", 
				id + 1, broadcastScript);
		if (parameters.size() == 1) {
			script += broadcastScript.replace("broadcast", indent + "\t\t" + "broadcast");
			script += indent + "\t\t" + "broadcast CURRENT LOOP = $loopnumber\n";
			script += indent + "\t\t" + getHistmemScript();
			script += indent + "\t\t" + "save $savenumber\n";
			script += indent + "\t}\n";
			script += indent + "\t" + "incr savenumber\n";
			script += indent + "\t" + "incr loopnumber\n";
		}
		script += indent + "}\n";
		return script;
	}
	
//	public String toScript() {
//		// Return empty line if variable is not properly set
//		String script = "# Advanced Multi-dimensional Scan Block\n";
//		IntegerWrapper runNumber = new IntegerWrapper(0);
//		script += "title " + "\"" + getScanDescription() + "\"\n";
//		script += getHistmemPreset() + "\n";
//		if (isSingleFile())
//			script += AbstractScanCommand.NEW_FILE_TEXT + "\n";
//		script += loopParameters(parameters, runNumber);
//		return script;
//	}
//
//	private String loopParameters(List<AbstractScanParameter> parameters, IntegerWrapper runNumber) {
//		if (parameters == null || parameters.size() == 0)
//			return "";
//		String script = "";
//		if (parameters.size() == 1){
//			AbstractScanParameter parameter = parameters.get(0);
//			parameter.startIteration();
//			while (parameter.iterationHasNext()) {
//				script += parameter.iterationGetNext();
//				if (((AdvancedParameter) parameter).getDoCreateFile())
//					runNumber.setValue(0);
//				script += getHistmemScript(runNumber.getValue()) + "\n";
//				runNumber.stepUp();
//			}
//			return script;
//		}
//		AbstractScanParameter parameter = parameters.get(0);
//		parameter.startIteration();
//		while (parameter.iterationHasNext()) {
//			script += parameter.iterationGetNext();
//			if (((AdvancedParameter) parameter).getDoCreateFile())
//				runNumber.setValue(0);
//			script += loopParameters(parameters.subList(1, parameters.size()), runNumber);
//		}
//		return script;
//	}

	@Override
	public float getEstimatedTime() {
		if (parameters == null || parameters.size() == 0 || preset == 0)
			return 0;
		int totalNumber = 1;
		for (AbstractScanParameter parameter : parameters)
			totalNumber *= parameter.getNumberOfPoints();
		String mode = getScan_mode();
		if ("time".equals(mode)) {
			return totalNumber * (preset + 10);
		} else {
			return totalNumber * preset;
		}
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		for (AbstractScanParameter parameter : parameters)
			parameter.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		super.removePropertyChangeListener(listener);
		if (parameters != null)
			for (AbstractScanParameter parameter : parameters)
				parameter.removePropertyChangeListener(listener);
	}
	
	/**
	 * @return the isSingleFile
	 */
	public boolean isSingleFile() {
		for (AbstractScanParameter parameter : parameters){
			if (((AdvancedParameter) parameter).getDoCreateFile())
				return false;
		}
		return true;
	}

//	/**
//	 * @param isSingleFile the isSingleFile to set
//	 */
//	public void setIsSingleFile(boolean isSingleFile) {
//		boolean oldValue = this.isSingleFile;
//		this.isSingleFile = isSingleFile;
//		firePropertyChange("isSingleFile", oldValue, isSingleFile);
//	}
	
	@Override
	public String getPrintable() {
		String text = "";
		int index = 0;
		if (parameters.size() > 0) {
			text += "#Dimension-" + index + "\n";
			AdvancedParameter parameter = (AdvancedParameter) parameters.get(0);
			text += parameter.getPritable(true) + "\t" + scan_mode + "\t" + preset + "\n";
			text += parameter.getPritable(false);
			index++;
		}
		for (int i = 1; i < parameters.size(); i++) {
			text += "#Dimension-" + index + "\n";
			text += parameters.get(i).getPritable(true) + "\n";
			text += parameters.get(i).getPritable(false);
			index++;
		} 
		return text;
	}
}
