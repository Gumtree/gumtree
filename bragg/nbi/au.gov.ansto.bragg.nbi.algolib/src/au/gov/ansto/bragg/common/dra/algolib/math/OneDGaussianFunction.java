package au.gov.ansto.bragg.common.dra.algolib.math;

/**
 * @author jgw
 *
 */
import java.lang.Math;
public class OneDGaussianFunction  {
	
	
	public static double OneDGaussian(double xVal, double mu, double sigma)
	{
		double norm = 1.0;
		
		return (norm * Math.exp(-(xVal - mu) * (xVal - mu) /(2*sigma*sigma) ));
	}

}
