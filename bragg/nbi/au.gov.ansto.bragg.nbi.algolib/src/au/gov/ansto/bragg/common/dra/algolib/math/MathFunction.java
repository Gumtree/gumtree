package au.gov.ansto.bragg.common.dra.algolib.math;

/**
 * @author jgw
 *
 */
import java.lang.Math;
public class MathFunction  {
	
	
	public static double OneDGaussian(double xVal, double mu, double sigma)
	{
		double norm = 1.0F;
		
		return  (double)(norm * Math.exp(-(xVal - mu) * (xVal - mu) /(2*sigma*sigma) ));
	}
	
	public static double TwoDGaussian(double xVal, double yVal, double mu1, double mu2,double sigma1, double sigma2)
	{
		double norm = 1.0F;
		
		return  (double)(norm * Math.exp(-((xVal - mu1) * (xVal - mu1) /(2*sigma1*sigma1) ) 
				+ (yVal - mu2) * (yVal - mu2) /(2*sigma2*sigma2) ) );
	}
	
	public static double LorentzianFunction(double xVal, double mu, double sigma) {
		
		return (sigma /((xVal-mu)*(xVal-mu) + sigma*sigma))/Math.PI;
	}
	
	public static double  VoigtPeakmodel (double xVal, double mean, double sigma, double aVal, double bVal ){
		double norm = 1.0;
		return   norm*(aVal*Math.exp(-(xVal - mean)*(xVal - mean)/(2*sigma*sigma)) 
                               + (1-aVal)*bVal/((xVal-mean)*(xVal-mean)+bVal*bVal)/3.14159);
	}
}
