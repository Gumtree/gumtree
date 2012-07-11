/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lindsay Winkler (Bragg Institute) - initial implementation
 *******************************************************************************/

package au.gov.ansto.bragg.quokka.dra.core;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.quokka.dra.algolib.core.InputParameterWrapper;
import au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor;

/**
 * Handles wrapping of parameters into a data object. This class is itself
 * a wrapper for the class which actually performs this function, enabling it
 * to run within the Cicada algorithm manager.
 *  
 * @author lwi
 */
public class ParameterWrapper implements ConcreteProcessor {

	// Input
	private IGroup wrap_trigger;
	
	// Variables
	private Double wrap_l1;
	private Double wrap_l2;
	private Double wrap_s1;
	private Double wrap_s2;
	private Double wrap_lambda;
	private Double wrap_lambdaWidth;
	private Double wrap_bS;
	private Double wrap_detWidth;
	private Double wrap_dDet;
	private Double wrap_aPixel;
	private Double wrap_offset;
	private Double wrap_guideWidth;
	private Double wrap_phi0;
	private Double wrap_s1Max;
	private Double wrap_t1;
	private Double wrap_t2;
	private Double wrap_t3;
	private Double wrap_lGap;
	private Double wrap_guideTrans;
	private Double wrap_lambdaT;
	private Double wrap_qMinRes;
	private Double wrap_qMaxRes;
	
	// Output
	private InputParameterWrapper wrap_output;
	
	public InputParameterWrapper getWrap_output() {
		return wrap_output;
	}

	public void setWrap_trigger(IGroup wrap_control) {
		this.wrap_trigger = wrap_control;
	}

	public void setWrap_l1(Double wrap_l1) {
		this.wrap_l1 = wrap_l1;
	}

	public void setWrap_l2(Double wrap_l2) {
		this.wrap_l2 = wrap_l2;
	}

	public void setWrap_s1(Double wrap_s1) {
		this.wrap_s1 = wrap_s1;
	}

	public void setWrap_s2(Double wrap_s2) {
		this.wrap_s2 = wrap_s2;
	}

	public void setWrap_lambda(Double wrap_lambda) {
		this.wrap_lambda = wrap_lambda;
	}

	public void setWrap_lambdaWidth(Double wrap_lambdaWidth) {
		this.wrap_lambdaWidth = wrap_lambdaWidth;
	}

	public void setWrap_bS(Double wrap_bS) {
		this.wrap_bS = wrap_bS;
	}

	public void setWrap_detWidth(Double wrap_detWidth) {
		this.wrap_detWidth = wrap_detWidth;
	}

	public void setWrap_dDet(Double wrap_dDet) {
		this.wrap_dDet = wrap_dDet;
	}

	public void setWrap_aPixel(Double wrap_aPixel) {
		this.wrap_aPixel = wrap_aPixel;
	}

	public void setWrap_offset(Double wrap_offset) {
		this.wrap_offset = wrap_offset;
	}

	public void setWrap_guideWidth(Double wrap_guideWidth) {
		this.wrap_guideWidth = wrap_guideWidth;
	}

	public void setWrap_phi0(Double wrap_phi0) {
		this.wrap_phi0 = wrap_phi0;
	}

	public void setWrap_s1Max(Double wrap_s1Max) {
		this.wrap_s1Max = wrap_s1Max;
	}

	public void setWrap_lGap(Double wrap_lGap) {
		this.wrap_lGap = wrap_lGap;
	}

	public void setWrap_guideTrans(Double wrap_guideTrans) {
		this.wrap_guideTrans = wrap_guideTrans;
	}

	public void setWrap_lambdaT(Double wrap_lambdaT) {
		this.wrap_lambdaT = wrap_lambdaT;
	}

	public void setWrap_qMinRes(Double wrap_qMinRes) {
		this.wrap_qMinRes = wrap_qMinRes;
	}

	public void setWrap_qMaxRes(Double wrap_qMaxRes) {
		this.wrap_qMaxRes = wrap_qMaxRes;
	}

	public void setWrap_t1(Double wrap_t1) {
		this.wrap_t1 = wrap_t1;
	}

	public void setWrap_t2(Double wrap_t2) {
		this.wrap_t2 = wrap_t2;
	}

	public void setWrap_t3(Double wrap_t3) {
		this.wrap_t3 = wrap_t3;
	}
	
	public Boolean process() throws Exception {
		wrap_output = new InputParameterWrapper(
			wrap_l1,
			wrap_l2,
			wrap_s1,
			wrap_s2,
			wrap_lambda,
			wrap_lambdaWidth,
			wrap_bS,
			wrap_detWidth,
			wrap_dDet,
			wrap_aPixel,
			wrap_offset,
			wrap_guideWidth,
			wrap_phi0,
			wrap_s1Max,
			wrap_t1,
			wrap_t2,
			wrap_t3,
			wrap_lGap,
			wrap_guideTrans,
			wrap_lambdaT,
			wrap_qMinRes,
			wrap_qMaxRes
		);
		
		return false;
	}

}
