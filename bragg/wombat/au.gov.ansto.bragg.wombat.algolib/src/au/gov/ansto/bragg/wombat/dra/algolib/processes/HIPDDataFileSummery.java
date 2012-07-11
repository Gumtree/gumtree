package au.gov.ansto.bragg.wombat.dra.algolib.processes;

public interface HIPDDataFileSummery {
	/**
	 * 
	 * @param stds                Three D data files to be flatted to two D array file
	 * @param detHpixels      Detector horisontal resolution
	 * @param  detVpixels     Detector vertical resolution
	 * 	@param  nScan           number of scan
	 * @param  deltaTheta     distance between two tubes
	 * @return The stiched 2D data set with detHpixels * nScan in horisontal direction
	 * @return
	 */
	public double[][] multiDataSetStich(double [][][] stds, double twotheta0,  double  deltaTheta);

}
