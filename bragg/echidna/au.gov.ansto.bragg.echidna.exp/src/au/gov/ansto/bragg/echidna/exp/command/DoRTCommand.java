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
public class DoRTCommand extends AbstractSicsCommand {

//	private final static String DEFAULT_STARTANG_NAME = "command.default.startang";
//	private final static String DEFAULT_FINISHANG_NAME = "command.default.finishang";
	private final static String DEFAULT_OVERLAPS_NAME = "command.default.overlaps";
	private final static String DEFAULT_STEPSIZE_NAME = "command.default.stepsize";
	private final static String DEFAULT_TOT_TIME_NAME = "command.default.tot-time";
//	private final static String DEFAULT_ROTATE_NAME = "command.default.rotate";
	private final static String DEFAULT_SIZE_NAME = "command.default.size";
//	private float startang = 2.75f;
//	private float finishang = 5.2f;
	private String overlaps = "0";
	private float stepsize = 0.05f;
//	private float rotate = 0f;
	private String size = "9mm can";
	private String sampname;
	private String sampposAB;
	private String sampposNumber;
	private float tot_time = 1f;
	
	/**
	 * 
	 */
	public DoRTCommand() {
		super();
		try{
			size = System.getProperty(DEFAULT_SIZE_NAME);
		} catch (Exception e) {
		}
//		try{
//			startang = Float.valueOf(System.getProperty(DEFAULT_STARTANG_NAME));
//		} catch (Exception e) {
//		}
//		try{
//			finishang = Float.valueOf(System.getProperty(DEFAULT_FINISHANG_NAME));
//		} catch (Exception e) {
//		}
		try{
			overlaps = System.getProperty(DEFAULT_OVERLAPS_NAME);
		} catch (Exception e) {
			overlaps = "1";
		}
		try{
			stepsize = Float.valueOf(System.getProperty(DEFAULT_STEPSIZE_NAME));
		} catch (Exception e) {
		}
		try{
			tot_time = Float.valueOf(System.getProperty(DEFAULT_TOT_TIME_NAME));
		} catch (Exception e) {
		}
//		try{
//			rotate = Float.valueOf(System.getProperty(DEFAULT_ROTATE_NAME));
//		} catch (Exception e) {
//		}
	}
	/**
	 * @return the startang
	 */
//	public float getStartang() {
//		return startang;
//	}
	/**
	 * @param startang the startang to set
	 */
//	public void setStartang(float startang) {
//		float oldValue = this.startang;
//		this.startang = startang;
//		firePropertyChange("startang", oldValue, startang);
//
//	}
	/**
	 * @return the finishang
	 */
//	public float getFinishang() {
//		return finishang;
//	}
	/**
	 * @param finishang the finishang to set
	 */
//	public void setFinishang(float finishang) {
//		float oldValue = this.finishang;
//		this.finishang = finishang;
//		firePropertyChange("finishang", oldValue, finishang);
//	}
	
	public String getOverlaps() {
		return overlaps;
	}

	public void setOverlaps(String overlaps) {
		String oldValue = this.overlaps;
		this.overlaps = overlaps;
		firePropertyChange("overlaps", oldValue, overlaps);
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
//	/**
//	 * @return the rotate
//	 */
//	public float getRotate() {
//		return rotate;
//	}
//	/**
//	 * @param nosteps the rotate to set
//	 */
//	public void setRotate(float rotate) {
//		float oldValue = this.rotate;
//		this.rotate = rotate;
//		firePropertyChange("rotate", oldValue, rotate);
//	}
	/**
	 * @return the size
	 */
	public String getSize() {
		return size;
	}
	/**
	 * @param nosteps the size to set
	 */
	public void setSize(String size) {
		String oldValue = this.size;
		this.size = size;
		firePropertyChange("size", oldValue, size);
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
	 * @return the sampposAB
	 */
	public String getSampposAB() {
		return sampposAB;
	}
	/**
	 * @param samppos the sampposAB to set
	 */
	public void setSampposAB(String sampposAB) {
		String oldValue = this.sampposAB;
		this.sampposAB = sampposAB;
		firePropertyChange("samppos", oldValue, sampposAB);
	}
	
	
	/**
	 * @return the sampposNumber
	 */
	public String getSampposNumber() {
		return sampposNumber;
	}
	/**
	 * @param sampposNumber the sampposNumber to set
	 */
	public void setSampposNumber(String sampposNumber) {
		String oldValue = this.sampposNumber;
		this.sampposNumber = sampposNumber;
		firePropertyChange("sampposNumber", oldValue, sampposNumber);
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
		String script = "doRT ";
		// Return empty line if variable is not properly set
		script += 
			(sampname != null && sampname.trim().length() > 0 ? "{" + sampname.trim() + "} " : "") +
			(size != null && size.trim().length() > 0 ? "{" + size.trim() + "} " : "") +
			(sampposAB != null && sampposAB.trim().length() > 0 ? sampposAB.trim() : "") +
			(sampposNumber != null && sampposNumber.trim().length() > 0 ? sampposNumber.trim() + " " : "") +
//			(startang != Float.NaN ? String.valueOf(startang) + " " : "") + 
//			(finishang != Float.NaN ? String.valueOf(finishang) + " " : "") +
			(overlaps != null && sampposNumber.trim().length() > 0 ? overlaps.trim() + " ": "") +
			(stepsize != 0 ? String.valueOf(stepsize) + " " : "") +
			(tot_time != Float.NaN ? String.valueOf(tot_time) + " " : "") +
//			(rotate != Float.NaN ? String.valueOf(rotate) + " " : "");
			"0 ";
		return script;
	}

}
