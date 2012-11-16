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

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class SimpleTableScanCommand extends AbstractScanCommand {


//	private float tot_time;
//	public boolean isSingleFile = true;
	private int numberOfMotor = 4;
	private boolean column1 = true;
	private boolean column2 = true;
	private boolean column3 = true;
	private boolean column4 = true;
	private boolean column5 = true;
	private boolean column6 = true;
	private boolean column7 = true;
	
	/**
	 * 
	 */
	public SimpleTableScanCommand() {
		super();
		setCommandName("simpleScan");
		setScan_mode("time");
	}

	public boolean getColumn1() {
		return column1;
	}



	public void setColumn1(boolean column1) {
		boolean oldValue = this.column1;
		this.column1 = column1;
		firePropertyChange("column1", oldValue, column1);
	}



	public boolean getColumn2() {
		return column2;
	}



	public void setColumn2(boolean column2) {
		boolean oldValue = this.column2;
		this.column2 = column2;
		firePropertyChange("column2", oldValue, column2);
	}



	public boolean getColumn3() {
		return column3;
	}



	public void setColumn3(boolean column3) {
		boolean oldValue = this.column3;
		this.column3 = column3;
		firePropertyChange("column3", oldValue, column3);
	}



	public boolean getColumn4() {
		return column4;
	}



	public void setColumn4(boolean column4) {
		boolean oldValue = this.column4;
		this.column4 = column4;
		firePropertyChange("column4", oldValue, column4);
	}

	public boolean getColumn5() {
		return column5;
	}



	public void setColumn5(boolean column5) {
		boolean oldValue = this.column5;
		this.column5 = column5;
		firePropertyChange("column5", oldValue, column5);
	}

	public boolean getColumn6() {
		return column6;
	}



	public void setColumn6(boolean column6) {
		boolean oldValue = this.column6;
		this.column6 = column6;
		firePropertyChange("column6", oldValue, column6);
	}

	public boolean getColumn7() {
		return column7;
	}



	public void setColumn7(boolean column7) {
		boolean oldValue = this.column7;
		this.column7 = column7;
		firePropertyChange("column7", oldValue, column7);
	}

	public String toScript() {
		// Return empty line if variable is not properly set
		String script = "# ";
		String title = getTitle();
		if (title == null) {
			script += "Simple Table Scan Block\n";
			title = getScanDescription();
		} else {
			script += title + "\n";;
		}
		script += "title " + "\"" + title + "\"\n";
		if (getSelectedParameterCount() > 0) {
			script += "histmem mode " + scan_mode + "\n"; 
			if (isSingleFile()) {
				script += AbstractScanCommand.NEW_FILE_TEXT;
			}
			script += "\n";
			script += "set START_NUMBER 0\n";
			script += "set scan_number 0\n";
			int saveNum = 0;
			for (AbstractScanParameter parameter : getParameterList()) {
				if (((TableScanParameter) parameter).getIsSelected()) {
					script += "if {$START_NUMBER <= $scan_number} {\n";
					script += parameter.getDriveScript(String.valueOf(saveNum), "\t");
					script += "}\n";
					script += "incr scan_number\n";
					saveNum++;
				}
			}
		}
		return script;
	}

	@Override
	public String getScanDescription(){
		String script = "";
		script += getCommandName() + " " + getSelectedParameterCount() + " steps (" + ((int) getEstimatedTime()) 
				+ " " + getEstimationUnits() + ")";
		return script;
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

	public int getSelectedParameterCount() {
		int count = 0;
		for (AbstractScanParameter parameter : getParameterList()) {
			if (((TableScanParameter) parameter).getIsSelected()) {
				count ++;
			}
		}
		return count;
	}
	
	@Override
	public float getEstimatedTime() {
		if (parameters == null || parameters.size() == 0)
			return 0;
		float totalTime = 0;
		for (AbstractScanParameter parameter : getParameterList()) {
			if (((TableScanParameter) parameter).getIsSelected()) {
				totalTime += ((TableScanParameter) parameter).getTime() + 8;
			}
		}
		return totalTime;
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
		// TODO: add implementation 
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
		String text = "#";
		if (column1) {
			text += "sx, \t";
		}
		if (column2) {
			text += "sy, \t";
		}
		if (column3) {
			text += "sz, \t";
		}
		if (column4) {
			text += "som, \t";
		}
		if (getNumberOfMotor() == 7) {
			if (column5) {
				text += "ga, \t";
			}
			if (column6) {
				text += "gb, \t";
			}
			if (column7) {
				text += "gc, \t";
			}
		} 
		text += "time(s)\n";
		for (AbstractScanParameter parameter : getParameterList()) {
			if (((TableScanParameter) parameter).getIsSelected()) {
				text += parameter.getPritable(false);
			}
		}
		return text;
	}

	/**
	 * @return the numberOfMotor
	 */
	public int getNumberOfMotor() {
		return numberOfMotor;
	}

	/**
	 * @param numberOfMotor the numberOfMotor to set
	 */
	public void setNumberOfMotor(int numberOfMotor) {
		this.numberOfMotor = numberOfMotor;
	}
}
