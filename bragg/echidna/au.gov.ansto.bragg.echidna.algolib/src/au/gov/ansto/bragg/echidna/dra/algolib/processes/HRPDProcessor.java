package au.gov.ansto.bragg.echidna.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.math.GeometryCorrecter;
import au.gov.ansto.bragg.common.dra.algolib.processes.BufferedProcessor;

import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDetector;

public abstract class HRPDProcessor extends BufferedProcessor {

	protected HRPDDetector detector = new HRPDDetector();
	protected int nScan;
	protected int numSlices = detector.xpixels;
	protected  double detarc = detector.heightCurv;

	protected GeometryCorrecter ac = null;
	protected double position = 0;
	
	public abstract String getName();
	public abstract String getDescription();
	
	protected static boolean filtered(double d)
	{
		return Double.isNaN(d);
	}
	
	public void setNumSlices(int num)
	{
		numSlices = num;
		forget();
	}
	
	public void setDetector(HRPDDetector det)
	{
		detector = det;
		forget();
	}
	
	public void setPosition(double d)
	{
		this.position = d;
		forget();
	}
	
	public void setPositionCorrector(GeometryCorrecter ac)
	{
		this.ac = ac;
		forget();
	}
	
	//Common algorithm
	protected static double underLine(double x1, double y1, double x2, double y2, double sx, double sy)
    {
    	if(sx * sy == 0)
    	{
    		return (x2-x1)*(y2-y1);
    	}
    	double ratio = sy/sx;
    	double x1int = ratio*x1;
    	double x2int = ratio*x2;
    	double y1int = y1/ratio;
    	double y2int = y2/ratio;
    	double area = 0;
    	if(ratio > 0)
    	{
    		if(y2int < x1)
    			return (x2-x1)*(y2-y1);
    		if(y1int > x2)
    			return 0;
    		area = (x2-y1int)*(x2int-y1)/2;
    		if(y1int < x1)
    			area -= (x1-y1int)*(x1int-y1)/2;
    		if(x2int > y2)
    			area -= (x2-y2int)*(x2int-y2)/2;
    		return area;
    	}
    	else
    	{
    		if(y1int < x1)
    			return 0;
    		if(y2int > x2)
    			return (x2-x1)*(y2-y1);
    		area = (y1int-x1)*(x1int-y1)/2;
    		if(y2int > x1)
    			area -= (y2int-x1)*(x1int-y2)/2;
    		if(y1int > x2)
    			area -= (y1int-x2)*(x2int-y1)/2;
    		return area;
    	}
    }
	
	/**
     * Divides one array by another. Result is stored in the first array.
     * @param num The array to divide.
     * @param denom The array of denominators.
     */
    public static void inPlaceArrayDivide(double[] num, double[] denom) {
        int i;
        for (i = 0; i < num.length; i++) {
            if (denom[i] != 0) {
                num[i] = num[i] / denom[i];
            } else {
                num[i] = Float.NaN;
            }
        }
    }
	public int getNScan() {
		return nScan;
	}
	public void setNScan(int scan) {
		nScan = scan;
	}
	public Signal processNew(Signal in) {
		// TODO Auto-generated method stub
		return null;
	}

}
