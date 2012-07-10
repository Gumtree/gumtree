package au.gov.ansto.bragg.common.dra.algolib.math;

// Perform a weighted linear fit to the model y = bx + c

public class LinearFit {
	// TODO: check for bad values in the input arrays, e.g. infinite weights
	public static double[] linear_fit(double[] x_vals, double[] y_vals, double[] weights) {
		/* Do the linear interpolation. The formula is: 
		 * slope = (1/fac)[Sum(w)*Sum(xwy) - Sum(xw)*Sum(yw)]
		 * intercept = (1/fac)[Sum(w x^2)*Sum(wy) - Sum(xw)Sum(xwy)]
		 * fac = Sum(w x^2)Sum(w) - Sum(xw)^2
		 * 
		 * These formulae derived from the linear least squares equation beta = (X^TWX)^-1 X^T Wy
		 * 
		 * Weights w are equal to 1/variance.
		 */
		// Accumulate sums
		double sumw=0,sumxw=0,sumyw=0,sumxyw=0,sumxxw=0;
		for(int i=0;i<x_vals.length;i++) {
			sumw+=weights[i];
			sumxw+=x_vals[i]*weights[i];
			sumyw+=y_vals[i]*weights[i];
			sumxyw+=x_vals[i]*y_vals[i]*weights[i];
			sumxxw+=x_vals[i]*x_vals[i]*weights[i];
		}
		double fact = 1.0/(sumxxw*sumw - sumxw*sumxw);
		double slope = fact*(sumw*sumxyw - sumxw*sumyw);
		double intercept = fact*(sumxxw*sumyw - sumxw*sumxyw);
		double[] result =  {slope,intercept};
		return result;
	}
}
