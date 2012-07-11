package au.gov.ansto.bragg.echidna.dra.algolib.processes;

import java.util.ArrayList;

import au.gov.ansto.bragg.common.dra.algolib.processes.Processor;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
import au.gov.ansto.bragg.common.dra.algolib.processes.WrapperSignal;

import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDataSet;
import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDetector;

public class HRPDCorrectionImpl extends Processor implements HRPDCorrection {

	private HRPDDetector detector;
	
	@Override
	public Signal process(Signal in) {
		HRPDDataSet data = in.dataAs(HRPDDataSet.class);
		int flag=1;
		double  thresh= 10E-10;
		boolean inverse = false;
		if(data.corrected != null && data.detector == detector)
			return new WrapperSignal(data.corrected, data.name);
		if(data == null)
			throw new NullPointerException("ProcessNew given null data!");
		try{
		double[][] corrected = BGcorrect(data.sample, data.monSample, data.emptyCell, 
				data.monEmptyCell, data.blocked, data.monBlocked, detector.sensitivity, 
				data.transmissionSample, data.transmissionEmpty, removeNegatives, flatBackground);
		corrected = doSensitivity(corrected, detector.sensitivity, flag, thresh, inverse);
		data.corrected = corrected;
		data.detector = detector;
		return new WrapperSignal(corrected, data.name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new WrapperSignal(data, "Error - Uncorrected Data");
		}
	}
	
	private float flatBackground;
	private boolean removeNegatives;
	
	public void setBackground(float background)
	{
		flatBackground = background;
	}
	
	public float getBackround()
	{
		return flatBackground;
	}
	
	public void setRemoveNegatives(boolean removeNegatives)
	{
		this.removeNegatives = removeNegatives;
	}
	
	public boolean getRemoveNegatives()
	{
		return removeNegatives;
	}
	
	public void setDetector(HRPDDetector detector)
	{
		this.detector = detector;
	}
	
	public HRPDDetector getDetector()
	{
		return detector;
	}
	
	public void setUseNistDivision(boolean val)
	{
		useNistDivision = val;
	}
	
	public boolean getUseNistDivision()
	{
		return useNistDivision;
	}
	
	/**
     * If true, the corrected data is divided by
     * the transmission of the sample and empty cells.
     */
    public boolean useNistDivision = true;
    /**
     * Eliminates the background data. Data is normalised by the monitor counts
     * if possible.
     * @param iSample The sample data.
     * @param monSample The monitor counts for the sample scan.
     * @param iEmpty The empty cell data.
     * @param monEmpty The monitor counts for the empty cell scan.
     * @param iBlocked The background data.
     * @param monBlocked The monitor counts for the background scan.
     * @param sensitivity The detector sensitivity.
     * @param tSample The sample transmission.
     * @param tEmpty The empty cell transmission.
     * @param removeNegatives Whether to remove negative values from the
     * data.
     * @param background The flat background reading.
     * @return The corrected data.
     * @throws Exception If the background and data are not the same size
     */
    public double[][] BGcorrect(double[][] iSample, double monSample, double[][] iEmpty,
            double monEmpty, double[][] iBlocked,
            double monBlocked, double[][] sensitivity, double tSample,
            double tEmpty, boolean removeNegatives, double background) throws Exception {
        //System.out.println("sample = "+iSample+"; empty = "+iEmpty+"; blocked = "+iBlocked);       
        return iSample;
    }
    /**
     * Applies experiment background subtraction to the data.
     * @param data The twoD  array data to be filtered. with format data[yCounts][xTubes]
     * @param bgData  The detector background data sets from electronics or other equipment.
     * @param ration1 The ratio background proportional to time following bgData.
     * @param elecBD  The detector background data sets from known background source.
     * @param ration The ratio2 background proportional to time following elecBG.
     * @param effDat   the Detector efficiency table for BG correction
     * 
     * @return The corrected twoD  arraydata set  with format data[yCounts][xTubes]
     */
    public  double[][] removeBackground(double[][] data, double[][] bgData,  double ratio1,  
    		      double[][] elecBD, double ratio2, double[][] effDat, boolean remNag ) {
        if (bgData == null ) {
            return data;
        }
        if (bgData.length != data.length
                || bgData[0].length != data[0].length) {
            return null;
        }
        double[][] out = new double[data.length][data[0].length];
        int i, j;
        for (i = 0; i < data.length; i++) {
            for (j = 0; j < data[0].length; j++) {
                out[i][j] = data[i][j] - (bgData[i][j]*ratio1 + elecBD[i][j]*ratio2 )*effDat[i][j];
                if (out[i][j] < 0.0) out[i][j] = 0.0;
            }
        }
        return out;
    }
	   /**
     * Applies experiment background subtraction to the data. This method is simplified for one background source only.
     * @param data The data array to be filtered.
     * @param bgData The detector background data sets.
     * @param ratio   proportional factor for provided background file.
     * @return The filtered array.
     */
    public  double[][] removeBackground(double[][] data, double[][] bgData, double ratio ) {
        if (bgData == null) {
            return data;
        }
        if (bgData.length != data.length
                || bgData[0].length != data[0].length) {
            return null;
        }
        double[][] out = new double[data.length][data[0].length];
        int i, j;
        for (i = 0; i < data.length; i++) {
            for (j = 0; j < data[0].length; j++) {
            	if( data[i][j] < bgData[i][j]*ratio) out[i][j] = 0.0;
            	else   out[i][j] = data[i][j] - bgData[i][j]*ratio;
            }
        }
        return out;
    }
	
    /**
     * Applies experiment background subtraction to the data. This method is designed  for 
     * multiple BG sources. Be careful about  "ArrayList".
     * @param data The data array to be filtered.
     * @param bgData The detector background ArrayList data sets. Since BG can contribute from different  sources, 
     *                                  we therefore design BG data as a array list to process different bg sources.
     * @param ratio       propotion of  Arraylist BGs against time or other facts.
     * @return The filtered data array.
     */
    public  double[][] removeBackground(double[][] data, ArrayList<double[][]> bgData,  ArrayList<Double> ratio ) {
        if (bgData == null) {
            return data;
        }
        int numBGtyp = bgData.size();
        for (int k = 0; k < numBGtyp; k ++){
        if (bgData.get(k).length != data.length
                || bgData.get(k)[0].length != data[0].length) {
            return null;
        }
  }
        double[][] out = new double[data.length][data[0].length];
        int i, j;
 
        for (i = 0; i < data.length; i++) {
            for (j = 0; j < data[0].length; j++) {
            	double bgatrac = data[i][j];
                for (int k = 0; k < numBGtyp; k ++){
                bgatrac  = bgatrac -  bgData.get(k)[i][j]*ratio.get(k);
                }
                if(bgatrac < 0.0) out[i][j] = 0.0;
                out[i][j] = bgatrac;
            }
        }
        return out;
    }


    /**
     * Applies detector sensitivity to the data.
     * @param data The data array to be filtered.
     * @param sensitivity The detector sensitivity data.
     * @param flag contrl parameter flag =1 do correction; flag=0 do nothing
     * @param Control threshold parameter to reject very small efficiency block
     * @param inverse   boolean control parameter 
     *                                if detection efficience less than 100%  set it false
     *                                if detection efficience greater than 100%  set it true
     * @return The filtered array.
     */
    public  double[][] doSensitivity(double[][] data, double[][] sensitivity,int flag, double thresh, boolean inverse ) {
        if (sensitivity == null ) {
            return data;
        }
        if (flag == 0) return data;
        
        if (sensitivity.length != data.length
                || sensitivity[0].length != data[0].length) {
            return null;
        }
        double[][] out = new double[data.length][data[0].length];
        int i, j;
        try {
        for (j = 0; j < data.length; j++) {
            for (i = 0; i < data[0].length;  i++) {
                if (sensitivity[j][i ]<thresh )
                  	out[i][j] = Double.NaN;
                if (inverse)
                	out[j][i] = data[j][i] / sensitivity[j][i];
                else 
                	out[j][i] = data[j][i] * sensitivity[j][i];
            }
        }
        }catch(Exception e) {
        	e.printStackTrace();
        	System.out.println("Efficiency correction is  NOT  processed");
        }
        return out;
    }


}
