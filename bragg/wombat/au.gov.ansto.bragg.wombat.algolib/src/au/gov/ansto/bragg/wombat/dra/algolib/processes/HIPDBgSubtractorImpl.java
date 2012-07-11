package au.gov.ansto.bragg.wombat.dra.algolib.processes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;

import au.gov.ansto.bragg.common.dra.algolib.data.DataProvider1D;
import au.gov.ansto.bragg.common.dra.algolib.data.IDataUpdateListener;
import au.gov.ansto.bragg.common.dra.algolib.processes.Processor;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
import au.gov.ansto.bragg.common.dra.algolib.plot.PlotDataShaped;
import au.gov.ansto.bragg.common.dra.algolib.plot.PlotData1D;

/**
 * Subtracts the background from the foreground.
 * @author hrz
 */
public class HIPDBgSubtractorImpl extends Processor implements DataProvider1D,HIPDBgSubtractor  {

	private PlotData1D foreground;
	private PlotData1D background;
	private PlotData1D difference;
	public  double[][] bgData = null;
	public static final String BACKGROUND = "background";
	public void setBackground(PlotData1D background)
	{
		this.background = background;
	}
	
	@Override
	public Signal process(Signal in) {
		foreground = in.dataAs(PlotData1D.class);
		foreground.color = Color.BLACK;
		difference = null;
		lastUpdate = new Date();
		alertListeners();
		return null;
	}

	public boolean canDoShapedDataSet() {
		return false;
	}

	public PlotDataShaped getShapedDataSet() {
		return null;
	}

	public PlotData1D getDataSet1D(int index) {
		if(index == 0 && foreground != null)
			return foreground;
		if(index == 1 || foreground == null)
			return background;
		else
		{
			if(difference == null && foreground != null && background != null)
			{
				double[] x = foreground.x;
				double[] y = new double[x.length];
				double[] err = new double[x.length];
				for(int i = 0; i < x.length; i++)
				{
					y[i] = foreground.y[i]-background.y[i];
					if(foreground.err != null && background.err != null)
					err[i] = (double)Math.sqrt(foreground.err[i]*foreground.err[i]+
										background.err[i]*background.err[i]);
				}
				difference = new PlotData1D(x,y,err,Color.BLUE,"Q-R background subtracted");
			}
			return difference;
		}
	}
	
    /**
     * Applies experiment background subtraction to the data.
     * @param data The twoD  array data to be filtered. with format data[yCounts][xTubes]
     * @param bgData  The detector background data sets from electronics or other equipment.
     * @param ration1 The ratio background proportional to time following bgData.
     * @param elecBD  The detector background data sets from known background source.
     * @param ration The ratio2 background proportional to time following elecBG.
     * @param effDat   the Detector efficiency table fro BG correction
     * 
     * @return The corrected twoD  arraydata set  with format data[yCounts][xTubes]
     */
    public  double[][] removeBackground(double[][] data, double[][] bgData,  double ratio1,  
    		      double[][] elecBD, double ratio2, double[][] effDat, boolean remNag ) {
        if (bgData == null || data == null) {
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
            }
        }
        return out;
    }
	   /**
     * Applies experiment background subtraction to the data. This method is simplified for one background source only.
     * @param data The data array to be filtered.
     * @param bgData The detector background data sets.
     * @return The filtered array.
     */
    public  double[][] removeBackground(double[][] data, double[][] bgData, double ratio ) {
        if (bgData == null || data == null) {
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
                out[i][j] = data[i][j] - bgData[i][j]*ratio;
            }
        }
        return out;
    }
	
    /**
     * Applies experiment background subtraction to the data. This method is designed  for 
     * multiple BG sources. Be careful about  "ArrayList".
     * @param data The data array to be filtered.
     * @param bgData The detector background ArrayList data sets.
     * @param ratio       propotion of  Arraylist BGs against time or other facts.
     * @return The filtered data array.
     */
    public  double[][] removeBackground(double[][] data, ArrayList<double[][]> bgData,  ArrayList<Double> ratio ) {
        if (bgData == null || data == null) {
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
                out[i][j] = bgatrac;
            }
        }
        return out;
    }


	public double getDataSetZ(int index) {
		return index;
	}

	public int getDataSetCount1D() {
		if(foreground == null && background == null)
			return 0;
		if(foreground != null && background != null)
			return 3;
		return 1;
	}

	public double[] getAncillaryX() {
		return null;
	}

	public double[] getAncillaryY() {
		return null;
	}

	private Date lastUpdate = new Date();
	public Date lastUpdate() {
		return lastUpdate;
	}

	public String[] twoDLabels() {
		return new String[] {"X","Y"};
	}

	public void getSupValues(String[] labels, double[] mins, double[] mults) {
		// Ignored
	}

	ArrayList<IDataUpdateListener> listeners = new ArrayList<IDataUpdateListener>();
	public void registerInterest(IDataUpdateListener l) {
		listeners.add(l);
	}
	
	private void alertListeners()
	{
		for(IDataUpdateListener l : listeners)
			l.dataUpdated();
	}

	public String getPlotName() {
		return "Reflectivity Curve";
	}

	public HIPDBgSubtractorImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static String getBACKGROUND() {
		return BACKGROUND;
	}
	public void setBGData(double[][] bgData) {
		this.bgData = bgData;
	}
	public  double[][] getBGData() {
		return bgData;
	}

	public PlotData1D getDuplicity() {
		// TODO Auto-generated method stub
		return null;
	}

}
