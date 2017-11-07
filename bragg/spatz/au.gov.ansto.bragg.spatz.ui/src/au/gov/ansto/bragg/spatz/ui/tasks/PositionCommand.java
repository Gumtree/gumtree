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
package au.gov.ansto.bragg.spatz.ui.tasks;

import java.beans.PropertyChangeListener;

import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanCommand;
import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanParameter;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class PositionCommand extends AbstractScanCommand {

	private static final float POSITION_SELECTION_TIME = 30; 

	/**
	 * 
	 */
	public PositionCommand() {
		super();
		setCommandName("Position");
//		PositionParameter parameter = new PositionParameter();
//		insertParameter(parameter);
	}


	public String toScript() {
		// Return empty line if variable is not properly set
		String script = "# define position table\n";
		int size = parameters.size();
		if (size > 0) {
			script += "proc positioner {idx} {\n";
			script += "\tif {$idx < 1 || $idx > " + size + "} {error \"position index doesn't exist.\"}\n";
			for (AbstractScanParameter parameter : getParameterList()) {
				script += parameter.getDriveScript(null, "\t");
			}
		}
		script += "}\n";
		return script;
	}

	@Override
	public String getScanDescription(){
		return getCommandName();
	}

//	private String loopParameters(List<AbstractScanParameter> parameters, IntegerWrapper runNumber) {
//		if (parameters == null || parameters.size() == 0)
//			return "";
//		String script = "";
//		if (parameters.size() == 1){
//			AbstractScanParameter parameter = parameters.get(0);
//			parameter.startIteration();
//			while (parameter.iterationHasNext()) {
//				script += parameter.iterationGetNext();
//				if (((SimpleNDParameter) parameter).getDoCreateFile())
//					runNumber.setValue(0);
//				script += getHistmemScript(runNumber.getValue()) + "\n";
//				runNumber.stepUp();
//			}
//			return script;
//		}
//		AbstractScanParameter parameter = parameters.get(0);
//		parameter.startIteration();
//		while (parameter.iterationHasNext()) {
//			if (((SimpleNDParameter) parameter).getDoCreateFile())
//				runNumber.setValue(0);
//			script += parameter.iterationGetNext();
//			script += loopParameters(parameters.subList(1, parameters.size()), runNumber);
//		}
//		return script;
//	}

	@Override
	public float getEstimatedTime() {
		return POSITION_SELECTION_TIME;
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
//	
	@Override
	public String getPrintable() {
		String text = "# define position table\n";
		text += "#position,\t sx,\t sz,\t sth,\t sphi,\t samplename\n";
		for (AbstractScanParameter parameter : getParameterList()) {
			text += parameter.getPritable(false);
		}
		return text;
	}

	@Override
		public void insertParameter(int i, AbstractScanParameter parameter) {
			// TODO Auto-generated method stub
			parameters.add(i, parameter);
			int idx = 0;
			for (AbstractScanParameter par : parameters) {
				((PositionParameter) par).setPosition(++idx);
			}
			firePropertyChange("parameter_add", null, parameter);
		}
}

