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
package au.gov.ansto.bragg.wombat.exp.command;

import org.gumtree.gumnix.sics.batch.ui.commands.AbstractSicsCommand;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class RunTCommand extends AbstractSicsCommand {

	private float temperature;
	private float delay;
	private int numsteps;
	private int oscno;
	
	/**
	 * 
	 */
	public RunTCommand() {
		super();
	}
	/**
	 * @return the startang
	 */
	public float getTemperature() {
		return temperature;
	}
	/**
	 * @param startang the startang to set
	 */
	public void setTemperature(float temperature) {
		float oldValue = this.temperature;
		this.temperature = temperature;
		firePropertyChange("temperature", oldValue, temperature);

	}
	/**
	 * @return the finishang
	 */
	public float getDelay() {
		return delay;
	}
	/**
	 * @param finishang the finishang to set
	 */
	public void setDelay(float delay) {
		float oldValue = this.delay;
		this.delay = delay;
		firePropertyChange("delay", oldValue, delay);
	}
	/**
	 * @return the stepsize
	 */
	public int getNumsteps() {
		return numsteps;
	}
	/**
	 * @param nosteps the stepsize to set
	 */
	public void setNumsteps(int numsteps) {
		int oldValue = this.numsteps;
		this.numsteps = numsteps;
		firePropertyChange("numsteps", oldValue, numsteps);
	}

	/**
	 * @return the tot_time
	 */
	public int getOscno() {
		return oscno;
	}
	/**
	 * @param totTime the tot_time to set
	 */
	public void setOscno(int oscno) {
		int oldValue = this.oscno;
		this.oscno = oscno;
		firePropertyChange("oscno", oldValue, oscno);
	}
	public String toScript() {
//		String script = "RunT ";
//		// Return empty line if variable is not properly set
//		script += 
//			(temperature != Float.NaN ? String.valueOf(temperature) + " " : "") + 
//			(delay != Float.NaN ? String.valueOf(delay) + " " : "") +
//			" " + String.valueOf(numsteps) +
//			" " + String.valueOf(oscno);
		String temperatureString = temperature != Float.NaN ? String.valueOf(temperature) : "";
		String script = "#RunT " + temperatureString + " " + delay 
				+ " " + numsteps + " " + oscno + "\n";
		script += "drive tc1_driveable " + temperatureString + " " 
				+ "tc1_driveable2 " + temperatureString + "\n";
		script += "wait " + delay + "\n";
		script += "newfile HISTOGRAM_XY\n";
		for (int i = 0; i < numsteps; i++) {
			script += "oscmd start " + oscno + "\n";
			script += "hmm countblock\n";
			script += "save " + i + "\n";
			script += "\n";
		}
		return script;
	}

}
