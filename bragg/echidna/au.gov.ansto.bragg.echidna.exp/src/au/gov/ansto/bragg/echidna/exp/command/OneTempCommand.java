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
package au.gov.ansto.bragg.echidna.exp.command;

import org.gumtree.gumnix.sics.batch.ui.commands.AbstractSicsCommand;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class OneTempCommand extends AbstractSicsCommand {

	private float startang;
	private float finishang;
	private float stepsize;
	private String sampname;
	private String temp;
	private float tot_time;
	
	/**
	 * 
	 */
	public OneTempCommand() {
		super();
	}
	/**
	 * @return the startang
	 */
	public float getStartang() {
		return startang;
	}
	/**
	 * @param startang the startang to set
	 */
	public void setStartang(float startang) {
		float oldValue = this.startang;
		this.startang = startang;
		firePropertyChange("startang", oldValue, startang);

	}
	/**
	 * @return the finishang
	 */
	public float getFinishang() {
		return finishang;
	}
	/**
	 * @param finishang the finishang to set
	 */
	public void setFinishang(float finishang) {
		float oldValue = this.finishang;
		this.finishang = finishang;
		firePropertyChange("finishang", oldValue, finishang);
	}
	/**
	 * @return the stepsize
	 */
	public float getStepsize() {
		return stepsize;
	}
	/**
	 * @param nosteps the stepsize to set
	 */
	public void setStepsize(float stepsize) {
		float oldValue = this.stepsize;
		this.stepsize = stepsize;
		firePropertyChange("stepsize", oldValue, stepsize);
	}
	/**
	 * @return the sampname
	 */
	public String getSampname() {
		return sampname;
	}
	/**
	 * @param sampname the sampname to set
	 */
	public void setSampname(String sampname) {
		String oldValue = this.sampname;
		this.sampname = sampname;
		firePropertyChange("sampname", oldValue, oldValue);
	}
	/**
	 * @return the samppos
	 */
	public String getTemp() {
		return temp;
	}
	/**
	 * @param samppos the samppos to set
	 */
	public void setTemp(String temp) {
		String oldValue = this.temp;
		this.temp = temp;
		firePropertyChange("temp", oldValue, temp);
	}
	/**
	 * @return the tot_time
	 */
	public float getTot_time() {
		return tot_time;
	}
	/**
	 * @param totTime the tot_time to set
	 */
	public void setTot_time(float tot_time) {
		float oldValue = this.tot_time;
		this.tot_time = tot_time;
		firePropertyChange("tot_time", oldValue, tot_time);
	}
	public String toScript() {
		String script = "oneTemp ";
		// Return empty line if variable is not properly set
		script += 
			(sampname != null && sampname.trim().length() > 0 ? "{" + sampname.trim() + "} " : "") +
			(temp != null && temp.trim().length() > 0 ? temp.trim() + " " : "") +
			(startang != Float.NaN ? String.valueOf(startang) + " " : "") + 
			(finishang != Float.NaN ? String.valueOf(finishang) + " " : "") +
			(stepsize != 0 ? String.valueOf(stepsize) + " " : "") +
			(tot_time != Float.NaN ? String.valueOf(tot_time) + " " : "");
		return script;
	}

}
