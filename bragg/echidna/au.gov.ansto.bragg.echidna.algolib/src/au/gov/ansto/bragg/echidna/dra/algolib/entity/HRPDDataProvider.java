package au.gov.ansto.bragg.echidna.dra.algolib.entity;


import java.util.ArrayList;
import java.util.Date;


import au.gov.ansto.bragg.common.dra.algolib.data.DataProvider1D;
import au.gov.ansto.bragg.common.dra.algolib.data.DataProvider2D;
import au.gov.ansto.bragg.common.dra.algolib.data.IDataUpdateListener;
import au.gov.ansto.bragg.common.dra.algolib.math.GeometryCorrecter;
import au.gov.ansto.bragg.common.dra.algolib.math.OneDGaussianFunction;
//import au.gov.ansto.bragg.common.dra.algolib.ui.Slicer;
//import org.gumtree.vis.core.plot0d.PlotDataShaped;
//import org.gumtree.vis.core.plot1d.PlotData1D;
import au.gov.ansto.bragg.common.dra.algolib.plot.PlotData1D;

/**
 * Provides and processes the data sets used by the HRPD
 * DRA plugin.
 * @author hrz
 * @author jgw Modified for Non graphic and HRPD process
 */
public class HRPDDataProvider  {

	HRPDDetector detector = new HRPDDetector();
	public HRPDDetector detector()
	{
		if(detector == null)
			detector = new HRPDDetector();
		return detector;
	}
	/**
	 * The width, in degrees, between the scan steps
	 */
	public static final float scanStepSize = 0.5f/60;
	/**
	 * The number of scan steps taken.
	 */
	public static final int scanStepCount = 10;
	/**
	 * How far the detector is from the sameple in mm.
	 */
	public final double  detectorDistance = detector.distance;
	/**
	 * The wavelength of the beam.
	 */
	public static final double lambda = 1;

//	public Slicer sl;
	
	
	private Object createDataSet()
	{
		Object rval = new Object();
		
		double[][] sensMap = new double[detector().ypixels][detector().xpixels];
		for(double[] row : sensMap)
			for(int i = 0; i < row.length; i++)
				row[i] = (double)(Math.random()+0.5);

		double[][][] rawData = new double[scanStepCount][detector().ypixels][detector().xpixels];
		GeometryCorrecter ac = new GeometryCorrecter(detectorDistance, 
				new GeometryCorrecter.FPoint(detector().count*detector().seperation/2, 0),
				new GeometryCorrecter.FPoint(0,0),true, false,
				new GeometryCorrecter.FPoint((Math.PI/180.0),1),
				lambda);
		for(int i = 0; i < scanStepCount; i++)
			for(int j = 0; j < detector().ypixels; j++)
				for(int k = 0; k < detector().xpixels; k++)
				{
					double x = detector().firstPos + i*scanStepSize + j * detector().seperation;
					double y = (double) (k * detector().pixelHeight);
					rawData[i][j][k] = (double)(Math.max(0,Math.sin(ac.getAngle2theta(x,y)*15)))*sensMap[j][k];
					//System.out.println("rawData["+i+"]["+j+"]["+k+"] = "+rawData[i][j][k]);
				}
	
		System.out.println("raw data generated");
		return rval;
	}
//	/**
//	 * Creates a plot showing how many detectors are 
//	 * covering each data point
//	 * @return The data for a plot of where the 
//	 * detectors overlap
//	 */
//	public PlotData1D getDuplicity()
//	{
//		double len = (detector().count-1)*detector().seperation+scanStepCount*scanStepSize;
//		double[] x = new double[(int)(len/scanStepSize)];
//		double[] y = new double[x.length];
//		for(int i = 0; i < x.length; i++)
//		{
//			x[i] = detector().firstPos+scanStepSize*i;
//			int lastDetect = (int)(x[i]/detector().seperation);
//			int firstDetect = (int)((x[i]-scanStepCount*scanStepSize)/detector().seperation);
//			if(firstDetect < 0)
//				firstDetect = 0;
//			if(lastDetect >= detector().count)
//				lastDetect = detector().count-1;
//			y[i] = lastDetect-firstDetect+1;
//		}
//		
//		return new PlotData1D(x,y,null,null,"Detector Overlap");
//	}
/**
 * Generate HRPD MC datasets depending upon multiple Gaussian distributions on theta direction
 */
	public double[][][] genGaussianDataSet()
	{
		double mu1 = 58.0F;
		double mu2 = 88.0F;
		double sigma1 = 8.0F;
		double sigma2 = 5.0F;
		double[][][] rawData = new double[scanStepCount][detector().ypixels][detector().xpixels];
		
		//GeometryCorrecter ac = new GeometryCorrecter(detectorDistance, 
		//		new GeometryCorrecter.FPoint(detector().count*detector().seperation/2, 0),
		//		new GeometryCorrecter.FPoint(0,0),true, false,
		//		new GeometryCorrecter.FPoint((double)(Math.PI/180.0),1),
		//		lambda);
		//System.out.println("Detect Y resolution =" + detector().ypixels);
		for(int i = 0; i < scanStepCount; i++)
			for(int j = 0; j < detector().ypixels; j++)
				for(int k = 0; k < detector().xpixels; k++)
				{
			//		double x = detector().firstPos + i*scanStepSize + j * detector().seperation;
			//		double y = k * detector().pixelHeight;
//					rawData[i][k][j] = (double)(Math.max(0,Math.sin(ac.getAngle2theta(x,y)*15)))
					rawData[i][j][k] = (double) ((double)(15)*(double)(Math.random()+0.5)*(1.0 
							+ OneDGaussianFunction.OneDGaussian(j, mu1, sigma1)
							+ OneDGaussianFunction.OneDGaussian(j, mu2, sigma2)));
					
//					System.out.println("rawData["+i+"]["+j+"]["+k+"] = "+rawData[i][j][k]);
				}
		return rawData;
		
	}
	
	public double[][] genEffDataSet()
	{		
		double[][] sensMap = new double[detector().ypixels][detector().xpixels];
		for(double[] row : sensMap)
			for(int i = 0; i < row.length; i++)
			{
				row[i] = (double)(Math.random()+0.5);
		//System.out.println("row["+i+"] = "+row[i]);
			}
		return sensMap;
	}
	/**
	 * Converts a 3D scan dataset into a flat 2d
	 * data set based on the instrument metadata.
	 * @param data The dataset to flatten. 
	 * @return The flat data set.
	 */
	public double[][] flatten(double[][][] data)
	{
		double[][] reducedData = new double[detector().ypixels][scanStepCount+(int)(detector().count*detector().seperation/scanStepSize)];
	
		double[][] counts = new double[detector().ypixels][scanStepCount+(int)(detector().count*detector().seperation/scanStepSize)];
		
		for(int i = 0 ; i < reducedData.length; i++)
		{
			for(int j = 0; j < reducedData[i].length; j++)
			{
				reducedData[i][j] = 0;
				counts[i][j] = 0;
			}
		}
		for(int i = 0; i < scanStepCount; i++)
			for(int j = 0; j < detector().count; j++)
			{
				int x = i + (int)(j * detector().seperation / scanStepSize);
				for(int k = 0; k < detector().ypixels; k ++)
				{
					if(counts[k][x] == 0)
					{
						counts[k][x] = 1;
						reducedData[k][x] = data[i][k][j];
					}
					else
					{
						reducedData[k][x] = reducedData[k][x]*counts[k][x]+data[i][k][j];
						counts[k][x]++;
						reducedData[k][x] /= counts[k][x];
					}
				}
			}
		return reducedData;
	}
	
	/**
	 * Generates the reduced data set.
	 * @param useSensitivity Whether to perform sensitivity correction.
	 */
	public void genReducedData(double [][][] data, double[][] effMap, boolean useSensitivity)
	{

		double[][][] rawData = data;
		double[][] reducedData;
		if(useSensitivity)
		{
			try{
			double[][] sensitivity = effMap;
			double[][][] sensData = new double[rawData.length][rawData[0].length][rawData[0][0].length];
			for(int i = 0; i < rawData.length; i++)
			{
				for(int j = 0; j < rawData[0].length; j++)
				{
					for(int k = 0; k < rawData[0][0].length; k++)
					{
						sensData[i][j][k] = rawData[i][j][k] / sensitivity[k][j];
					}
				}
			}
			rawData = sensData;
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("sensitivity correction applied");
		
		reducedData = flatten(rawData);
		System.out.println("reduced data generated");
		System.out.println("reduced data committed");
		update();
	}
	/**
	 * Sets whether or not to use sensitivitity correction
	 * @param correctSens Whether or not to use sensitivity correction.
	 */
	public void setCorrectSensitivity(boolean correctSens)
	{
		genReducedData(null, null, correctSens);
	}
	
	public double[][] getDataSet(int index) {

			return null;
		}
	

	public Object getDataSetCount() {
		return null;
	}

	public double[] getScales(int index) {
		if(index == -1)
		{
			return new double[] {detector().firstPos,
					(double) (-0.5f*detector().pixelHeight*detector().ypixels),					
					detector().firstPos
							+detector().seperation*detector().count
							+scanStepCount*scanStepSize,
							(double) (0.5f*detector().pixelHeight*detector().ypixels)};
		}
		else
		{
			return new double[] {detector().firstPos+index*scanStepSize,
					(double) (-0.5f*detector().pixelHeight*detector().ypixels),
					detector().firstPos
					+detector().seperation*detector().count
					+index*scanStepSize,
					(double) (0.5f*detector().pixelHeight*detector().ypixels)};
		}
	}

	public Date lastUpdate() {
		return new Date();
	}

//	public PlotData1D getDataSet1D(int index) {
//		double[] y = sl.getIndexedSlice(getDataSet(-1),index);
//		double[] x = new double[y.length];
//		for(int i = 0; i < x.length; i++)
//		{
//			x[i] = detector().firstPos+scanStepSize*i;
//		}
//		GTDItem slice = data.getRoot().getItem("Slice "+index);
//		if(slice == null)
//			slice = new GTDItem("Slice "+index,data.getRoot());
//		slice.setDataset(new double[][] {x,y,sl.getErrs()},GTDType.createFloatType(),
//				new long[] {3,x.length},null);
//		return new PlotData1D(x,y,sl.getErrs(),null,"Slice "+index);
//	}

	public double getDataSetZ(int index) {
		return index;
	}

//	public int getDataSetCount1D() {
//		return sl.getIndexedSliceCount((getDataSet(-1)).length);
//	}

	public double[] getAncillaryX() {
		return null;
	}

	public double[] getAncillaryY() {
		return null;
	}
	public boolean canDoShapedDataSet() {
		return false;
	}
//	public PlotDataShaped getShapedDataSet() {
//		return null;
//	}
	public String[] twoDLabels() {
		return new String[] {"X pixel","Data set","Intensity"};
	}
	public void getSupValues(String[] labels, double[] mins, double[] mults) {
		labels[0] = null;
	}

	
	ArrayList<IDataUpdateListener> listeners = new ArrayList<IDataUpdateListener>();


	public void registerInterest(IDataUpdateListener l) {
		listeners.add(l);
	}
	
	private void update()
	{
		for(IDataUpdateListener l : listeners)
			l.dataUpdated();
	}
	public String getPlotName() {
		return "HRPD Data";
	}
//	public int getDataSetCount1D() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//	public PlotData1D getDataSet1D(int index) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	public PlotData1D getDataSet1D(int index) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	public int getDataSetCount1D() {
		// TODO Auto-generated method stub
		return 0;
	}
	public PlotData1D getDuplicity() {
		// TODO Auto-generated method stub
		return null;
	}


}
