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

package au.gov.ansto.bragg.quokka.dra.algolib.core;

/**
 * This class wraps all the input parameters in a way that makes them easy to pass around.
 * Almost all the methods end up requiring the use of almost all the input parameters, 
 * either to use themselves or to pass to another method, leading to large numbers of
 * parameters being passed to every method if such a wrapper object is not used.
 * @author lwi
 *
 */
public class InputParameterWrapper {
	
	private double QminRes;
	private double QmaxRes;
	
	private double L2;
	private double L1;
	private double lambda;
	private double Bs;
	private double det_width;
	private double DDet;
	private double aPixel;
	private double offset;
	private double S1;
	private double S2;
	private double l_gap;
	private double guide_width;
	private double S1max;
	private double t1;
	private double t2;
	private double t3;
	private double guide_trans;
	private double phi0;
	private double lambdaT;
	private double lambdaWidth;
	
	private double ApOff = 0;
	
	

	public InputParameterWrapper(double L1, double L2, double S1, double S2,
			double lambda, double lambdaWidth, double Bs, double det_width, double DDet,
			double aPixel, double offset,  double guide_width, double phi0, 
			double S1max, double t1, double t2, double t3, double l_gap,
			double guide_trans,  double lambdaT, 
			double QminRes, double QmaxRes) {
		
		this.QminRes = QminRes;
		this.QmaxRes = QmaxRes;
		this.L1 = 100 * L1; // Conversion from m to cm
		this.L2 = 100 * L2; // Conversion from m to cm
		this.lambda = lambda;
		this.Bs = Bs;
		this.det_width = det_width;
		this.DDet = DDet;
		this.aPixel = aPixel;
		this.offset = offset;
		this.S1 = S1;
		this.S2 = S2;
		this.l_gap = l_gap;
		this.guide_width = guide_width;
		this.S1max = 100 * S1max; // Conversion from m to cm
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
		this.guide_trans = guide_trans;
		this.phi0 = phi0;
		this.lambdaT = lambdaT;
		this.lambdaWidth = lambdaWidth;
	}

	public double getQminRes() {
		return QminRes;
	}

	public double getQmaxRes() {
		return QmaxRes;
	}

	public double getL2() {
		return L2;
	}

	public double getL1() {
		return L1;
	}

	public double getLambda() {
		return lambda;
	}

	public double getBs() {
		return Bs;
	}

	public double getDet_width() {
		return det_width;
	}

	public double getDDet() {
		return DDet;
	}

	public double getAPixel() {
		return aPixel;
	}

	public double getOffset() {
		return offset;
	}

	public double getS1() {
		return S1;
	}

	public double getS2() {
		return S2;
	}

	public double getL_gap() {
		return l_gap;
	}

	public double getGuide_width() {
		return guide_width;
	}

	public double getS1max() {
		return S1max;
	}

	public double getT1() {
		return t1;
	}

	public double getT2() {
		return t2;
	}

	public double getT3() {
		return t3;
	}

	public double getGuide_trans() {
		return guide_trans;
	}

	public double getPhi0() {
		return phi0;
	}

	public double getLambdaT() {
		return lambdaT;
	}

	public double getLambdaWidth() {
		return lambdaWidth;
	}

	public double getApOff() {
		return ApOff;
	} 
	
	
}
