package au.gov.ansto.bragg.echidna.dra.algolib.processes;

public class EchidnaDataNormalization {
	double maxNorm = Double.MIN_VALUE;
	double minNorm = Double.MAX_VALUE;
	double meanNorm = 0.0;	
	double stdDev = 0.0;
	double[][][] normDat =null;

	/**
	 *  Constructor for Echidna data normalization. The design is actually quite general. This algorithm can be applied to
	 *   any other data normalization. Be ware the first dimension  length of multiDats should be same as the length of 
	 *   scanNorm 
	 * @param multiDats  three D data set to be normalized.
	 * @param scanNorm Normalization data array.
	 * Return data can be obtained using get methods
	 */
	public EchidnaDataNormalization(double[][][] multiDats, double[] scanNorm) {
		dataNormalizationMultiScan( multiDats, scanNorm);
	}
	
	
	/**
	 * 
	 for Echidna data normalization. The design is actually quite general. This algorithm can be applied to
	 *   any other data normalization. Be ware the first dimension  length of multiDats should be same as the length of 
	 *   scanNorm 
	 * @param multiDats  three D data set to be normalized.
	 * @param scanNorm Normalization data array.
	 * @Return normDat  three D normalized data set
	 */
	private double[][][] dataNormalizationMultiScan(double[][][] multiDats, double[] scanNorm) {
		
		int nScans = scanNorm.length;
	    int nScDat = multiDats.length;
	    int vPixels =  multiDats[0].length;
	    int hPixels =  multiDats[0][0].length;

		double[] normWeight = new double[nScans];
		normDat = new double[nScDat][vPixels][hPixels];

		double stdDevsq = 0.0;
// Process to get  mean of scanNorm data set
		for ( int n = 0; n < nScans; n++) {
			 meanNorm += scanNorm[n]/nScans;
			 if( scanNorm[n] > maxNorm ) maxNorm = scanNorm[n];
			 if( scanNorm[n] < minNorm ) minNorm = scanNorm[n];
		 }
// Calculate the normaliaztion weight of each scan
		for ( int n = 0; n < nScans; n++) {
			 normWeight[n] = meanNorm / scanNorm[n];
			 stdDevsq += (scanNorm[n] - meanNorm)*(scanNorm[n] - meanNorm)/(nScans-1);
		 }
		
		stdDev = Math.sqrt(stdDevsq);
		
		EchidnafileProcess  efp = new EchidnafileProcess();
		
		for ( int n = 0; n < nScans; n++) {
			normDat[n] = efp.echidnDataReNorm(multiDats[n], normWeight[n]);
		 }
		return normDat;
	}
	/**
	 * 
	 * @return
	 */
	public double getMaxNorm() {
		return maxNorm;
	}
	/**
	 * 
	 * @param maxNorm
	 */
	public void setMaxNorm(double maxNorm) {
		this.maxNorm = maxNorm;
	}
	/**
	 * 
	 * @return
	 */
	public double getMinNorm() {
		return minNorm;
	}
	/**
	 * 
	 * @param minNorm
	 */
	public void setMinNorm(double minNorm) {
		this.minNorm = minNorm;
	}
	/**
	 * 
	 * @return
	 */
	public double getMeanNorm() {
		return meanNorm;
	}
	/**
	 * 
	 * @param meanNorm
	 */
	public void setMeanNorm(double meanNorm) {
		this.meanNorm = meanNorm;
	}
	/**
	 * 
	 * @return
	 */
	public double getStdDev() {
		return stdDev;
	}
	/**
	 * 
	 * @param stdDev
	 */
	public void setStdDev(double stdDev) {
		this.stdDev = stdDev;
	}
	/**
	 * 
	 * @return
	 */
	public double[][][] getNormDat() {
		return normDat;
	}
	/**
	 * 
	 * @param normDat
	 */
	public void setNormDat(double[][][] normDat) {
		this.normDat = normDat;
	}
}
