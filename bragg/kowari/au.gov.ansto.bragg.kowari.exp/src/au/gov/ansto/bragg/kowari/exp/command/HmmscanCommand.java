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
import java.util.ArrayList;
import java.util.List;

public class HmmscanCommand extends AbstractScanCommand{

	/**
	 * 
	 */
	public HmmscanCommand() {
		super();
		setCommandName("hmmscan");
	}

	public String toScript() {
		// Return empty line if variable is not properly set
		IntegerWrapper runNumber = new IntegerWrapper(0);
		String script = "# ";
		String title = getTitle();
		if (title == null) {
			script += "Arbitrary Scan Block\n";
			title = getScanDescription();
		} else {
			script += title + "\n";
		}
		script += "title " + "\"" + title + "\"\n";
		script += AbstractScanCommand.NEW_FILE_TEXT;
		script += getHistmemPreset() + "\n";
		for (AbstractScanParameter parameter : parameters){
			script += parameter.iterationGetNext();
			script += getHistmemScript(runNumber.getValue()) + "\n";
			runNumber.stepUp();
		}
		return script;
	}

	@Override
	public String getScanDescription() {
		if (parameters.size() == 0)
			return "";
		String script = getCommandName() + " " + getScanVariables() + " " + scan_mode + " " + preset;
		
		return script;
	}
	
	private String getScanVariables() {
		List<String> scanVariables = new ArrayList<String>();
		for (AbstractScanParameter parameter : parameters){
			List<String> variables = ((HmmscanParameter) parameter).getScanVariables();
			for (String scanVariable : variables)
				if (!scanVariables.contains(scanVariable))
					scanVariables.add(scanVariable);
		}
		String string = "";
		for (String variable : scanVariables)
			string += variable + ",";
		if (string.length() > 0)
			string = string.substring(0, string.length() - 1);
		return string;
	}

	@Override
	public float getEstimatedTime() {
		if (parameters == null || preset == 0)
			return 0;
		int totalNumber = parameters.size();
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

	@Override
	public String getPrintable() {
		String text = "";
		int index = 0;
		if (parameters.size() > 0) {
			text += "#Position-" + index + "\n";
			HmmscanParameter parameter = (HmmscanParameter) parameters.get(0);
			text += parameter.getPritable(true) + "\t" + scan_mode + "\t" + preset + "\n";
			text += parameter.getPritable(false);
			index++;
		}
		for (int i = 1; i < parameters.size(); i++) {
			text += "#Position-" + index + "\n";
			text += parameters.get(i).getPritable(true) + "\n";
			text += parameters.get(i).getPritable(false);
			index++;
		} 
		return text;
	}
}
