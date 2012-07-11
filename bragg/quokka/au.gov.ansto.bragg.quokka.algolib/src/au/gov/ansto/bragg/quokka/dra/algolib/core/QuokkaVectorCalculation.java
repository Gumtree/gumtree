/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lindsay Winkler(Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.quokka.dra.algolib.core;


public class QuokkaVectorCalculation {
	
	// The Visual Basic source code on which this implementation is based contained
	// its own hard-coded definition for the value of Pi. Differences between this value
	// and the value of Math.PI could potentially cause methods to return slightly 
	// different values.
	//
	// The VB source code specified:
	// PI = 3.14159265358979
	
	// Enforce singleton pattern.
	private QuokkaVectorCalculation() {
	}
	
	private static QuokkaVectorCalculation instance = null;
	
	public static QuokkaVectorCalculation getInstance() {
		if (instance == null) {
			instance = new QuokkaVectorCalculation();
		}
		return instance;
	}

	public double getQMin(InputParameterWrapper parameters) {
		
		double lambda = parameters.getLambda();
		double Bs = parameters.getBs();
		double DDet = parameters.getDDet();
		double aPixel = parameters.getAPixel();
		double L2 = parameters.getL2();
		
		return (Math.PI / lambda) * ((Bs + DDet + aPixel) / L2);
	}
	
	public double getQMax(InputParameterWrapper parameters) {
		
		double lambda = parameters.getLambda();
		double L2 = parameters.getL2();
		
		double radial = getRadial(parameters);
		return 4 * Math.PI / lambda * Math.sin(0.5 * Math.atan(radial / L2));
	}
	
	private double getRadial(InputParameterWrapper parameters) {
		
		double det_width = parameters.getDet_width();
		double offset = parameters.getOffset();
		
		return Math.sqrt(0.25 * det_width * det_width + (0.5 * det_width + offset) * (0.5 * det_width + offset));
	}

	/**
	 * 
	 * @param parameters
	 * @return
	 * @throws MathException This happens if there is a problem with the calculation of
	 * the normal distribution error function.
	 */
	public double getSigmaQMin(InputParameterWrapper parameters) {
		
		double Qmin = getQMin(parameters);
		return getResolution(Qmin, parameters) * 100d / Qmin;
	}
	
	/**
	 * 
	 * @param parameters
	 * @return
	 * @throws MathException This happens if there is a problem with the calculation of
	 * the normal distribution error function.
	 */
	public double getSigmaQMax(InputParameterWrapper parameters) {
		double Qmax = getQMax(parameters);
		return getResolution(Qmax, parameters) * 100d / Qmax;
	}
	
	public double getQMaxV(InputParameterWrapper parameters) {
		
		double lambda = parameters.getLambda();
		double det_width = parameters.getDet_width();
		double L2 = parameters.getL2();
		
		return (4 * Math.PI / lambda) * Math.sin(0.5 * Math.atan(0.5 * det_width / L2));
	}
	
	public double getQMaxH(InputParameterWrapper parameters) {
		
		double lambda = parameters.getLambda();
		double det_width = parameters.getDet_width();
		double L2 = parameters.getL2();
		double offset = parameters.getOffset();
		
		return 4 * Math.PI / lambda * Math.sin(0.5 * Math.atan((0.5 * det_width + offset) / L2));
	}
	
	public double getIntensity(InputParameterWrapper parameters) {
		
		double S1 = parameters.getS1();
		double S2 = parameters.getS2();
		double L1 = parameters.getL1();
		double l_gap = parameters.getL_gap();
		double guide_width = parameters.getGuide_width();
		double guide_trans = parameters.getGuide_trans();
		double S1max = parameters.getS1max();
		double lambda = parameters.getLambda();
		double t1 = parameters.getT1();
		double t2 = parameters.getT2();
		double t3 = parameters.getT3();
		double phi0 = parameters.getPhi0();
		double lambdaT = parameters.getLambdaT();
		double lambdaWidth = parameters.getLambdaWidth();
		
		double alpha = (S1 + S2) / (2 * L1);
		double f = (l_gap * alpha) / (2 * guide_width);
		double t4 = Math.pow(1 - f, 2);
		double t5 = Math.pow(guide_trans, (S1max - L1) / 100);
		
//		double b = 0.04472;
//		double c2 = 0.04504;
//		double t6 = 1 - lambda * ((b - (S1max - L1)) / (b - c2));
		
		double aSample = Math.PI / 4 * S2 * S2;
		
		// Note that in the source Visual Basic code on which this implementation is based,
		// multiplication with the value t6 (defined above) was written but commented out
		// in the line below
		double t = t1 * t2 * t3 * t4 * t5;
		
		/*
		 * d2Phi = (phi0 / (2 * Pi)) * (lambdaT / lambda) ^ 4 _
                * (Exp(-(lambdaT / lambda) ^ 2) / lambda)
		 */
		double d2Phi = (phi0 / (2 * Math.PI)) * Math.pow((lambdaT / lambda), 4) * Math.exp(-Math.pow(-(lambdaT / lambda), 2)) / lambda;
		
		double solid_angle = Math.PI / 4 * S1 * S1 / L1 / L1;
		
		double intensity = aSample * d2Phi * lambdaWidth * solid_angle * t;
		return intensity;
	}
	
	public double getUmbra(InputParameterWrapper parameters) {
		
		double S1 = parameters.getS1();
		double S2 = parameters.getS2();
		double L1 = parameters.getL1();
		double L2 = parameters.getL2();
		
		double d1 = S1 * L2 / L1;
		double d2 = S2 * (L1 + L2) / L1;
		return Math.abs(d1 - d2);
	}
	
	// FIXME
	// There is duplicated code between getBv and getUmbra.
	// This duplication is initially intentional, but should be removed
	// once the code is working and tested.
	public double getBv(InputParameterWrapper parameters) {
		
		double S1 = parameters.getS1();
		double S2 = parameters.getS2();
		double L1 = parameters.getL1();
		double L2 = parameters.getL2();
		double lambda = parameters.getLambda();
		double lambdaWidth = parameters.getLambdaWidth();
		
		double d1 = S1 * L2 / L1;
		double d2 = S2 * (L1 + L2) / L1;
		double bh = d1 + d2;
		return bh + 0.0000000125 * (L1 + L2) * L2 * lambda * lambda * lambdaWidth;
		
	}
	
	private double getResolution(double inQ, InputParameterWrapper parameters) {
		
		// Initialisation of variables
		double wLam = parameters.getLambda();
		double wLW = parameters.getLambdaWidth();
		double wDDet = parameters.getDDet();
		double wApOff = parameters.getApOff();
		double wS1 = 0.5 * parameters.getS1();
		double wS2 = 0.5 * parameters.getS2();
		double wL1 = parameters.getL1();
		wL1 = wL1 - parameters.getApOff();
		double wL2 = parameters.getL2();
		wL2 = wL2 + parameters.getApOff();
		double wBS = 0.5 * parameters.getBs();
		
		// Initialisation of constants
		final double del_r = 0.1d;
		final double vz_1 = 395600d;
		double g = 981d;
		
		// Calculation of values starts here
		double a2 = wS1 * wL2 / wL1 + wS2 * (wL1 + wL2) / wL1;
		double q_small = 2d * Math.PI * (wBS - a2) * (1 - wLW) / (wLam * wL2);
		double lp = 1 / (1 / wL1 + 1 / wL2);
		
		double v_lambda = wLW * wLW / 6;
		double v_b = 0.25 * (wS1 * wS1 * wL2 * wL2 / wL1 / wL1) + 0.25 * (wS2 * wS2 * wL2 * wL2 / lp / lp);
		double v_d = (wDDet * wDDet / 2.3548 / 2.3548) + del_r * del_r / 12;
		double vz = vz_1 / wLam;
		double yg = 0.5 * g * wL2 * (wL1 + wL2) / vz / vz;
		double v_g = 2 * yg * yg * v_lambda;
		
		double r0 = wL2 * Math.tan(2 * Math.asin(wLam * inQ / 4 / Math.PI));
		double delta = 0.5 * (wBS - r0) * (wBS - r0) / v_d;
		
		double inc_gamma;
		if (r0 < wBS) {
			inc_gamma = Math.exp(gammln(1.5)) * (1 - gammp(1.5, delta));
		} else {
			inc_gamma = Math.exp(gammln(1.5)) * (1 + gammp(1.5, delta));
		}
		
		// What should we do here if erf fails to converge?
		double fSubS = 0;
//		try {
//			//-------------------------
//			double argument = (r0 - wBS) / Math.sqrt(2 * v_d);
//			double erf = erf(argument);
//			//-------------------------
			fSubS = 0.5 * (1 + erf((r0 - wBS) / Math.sqrt(2 * v_d)));
//			System.out.println("Convergent: " + (r0 - wBS) / Math.sqrt(2 * v_d));
//		} catch (MathException e) {
//			System.out.println("Non-convergent value: " + (r0 - wBS) / Math.sqrt(2 * v_d));
//		}
		
		if (fSubS <= 0) {
			fSubS = 0.0000000001;
		}
		
		double fr = 1 + Math.sqrt(v_d) * Math.exp(-1 * delta) / (r0 * fSubS * Math.sqrt(2 * Math.PI));
		double fv = inc_gamma / (fSubS * Math.sqrt(Math.PI)) - r0 * r0 * (fr - 1) * (fr - 1) / v_d;
		double rmd = fr * r0;
		double v_r1 = v_b + fv * v_d + v_g;
		
		double rm = rmd + 0.5 * v_r1 / rmd;
		double v_r = v_r1 - 0.5 * (v_r1 * v_r1 / rmd /rmd);
		
		if (v_r < 0) {
			v_r = 0;
		}
		
		double QBar = (4 * Math.PI / wLam) * Math.sin(0.5 * Math.atan(rm / wL2));
		
		return QBar * Math.sqrt(v_r / rmd / rmd + v_lambda);
	}
	
	private double gammln(double inX) {
		double cof1 = 76.18009173;
		double cof2 = -86.50532033;
		double cof3 = 24.01409822;
		double cof4 = -1.231739516;
		double cof5 = 0.00120858003;
		double cof6 = -0.00000536382;
		
		double stp = 2.50662827465;
		
		double x = inX - 1;
		double tmp = x + 5.5;
		tmp = (x + 0.5) * Math.log(tmp) - tmp;
		double ser = 1;
		
		x = x + 1;
		ser = ser + cof1 / x;
		
		x = x + 1;
		ser = ser + cof2 / x;
		
		x = x + 1;
		ser = ser + cof3 / x;
		
		x = x + 1;
		ser = ser + cof4 / x;
		
		x = x + 1;
		ser = ser + cof5 / x;
		
		x = x + 1;
		ser = ser + cof6 / x;
		
		return tmp + Math.log(stp * ser);
	}
	
	private double gammp(double a, double x) {
		double result;
		
		if ((x < 0) && (a <=0)) {
			result = 0;
		} else if (x < a + 1) {
			result = gser(a, x);
		} else {
			result = 1 - gcf(a, x);
		}
		
		return result;
	}
	
	private double gser(double a, double x) {
		double result;
		
		if (x <= 0) {
			result = 0;
		} else {
			double eps = 0.0000003;
			double ap = a;
			double sum = 1 / a;
			double del = sum;
			
			for (int i = 0; i < 100; i++) {
				ap = ap + 1;
				del = del * x / ap;
				sum = sum + del;
				if (Math.abs(del) < Math.abs(sum) * eps) {
					break;
				}
			}
			result = sum * Math.exp(-x + a * Math.log(x) - gammln(a));
		}
		
		return result;
	}
	
	private double gcf(double a, double x) {
		double eps = 0.0000003;
		double gold = 0;
		double a0 = 1;
		double a1 = x;
		double b0 = 0;
		double b1 = 1;
		double fac = 1;
		double g = 1;
		
		for (int i = 0; i < 100; i++) {
			double an = i;
			double ana = an - a;
			a0 = (a1 + a0 * ana) * fac;
			b0 = (b1 + b0 * ana) * fac;
			double anf = an * fac;
			a1 = x * a0 + anf * a1;
			b1 = x * b0 + anf * b1;
			
			if (a1 != 0) {
				fac = 1 / a1;
				g = b1 * fac;
			}
			
			if (Math.abs((g - gold) / g) < eps) {
				i = 100;
			}
			gold = g;
		}
		
		return g * Math.exp(-x + a * Math.log(x) - gammln(a));
	}
	
	private double erf(double inX) {
		
		double result;
		
		if (inX < 0) {
			result = -1 * gammp(0.5, inX * inX);
		} else {
			result = gammp(0.5, inX * inX);
		}
		
		return result;
	}
}

