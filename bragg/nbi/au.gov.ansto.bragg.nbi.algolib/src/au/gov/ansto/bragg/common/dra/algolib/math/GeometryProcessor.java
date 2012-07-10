package au.gov.ansto.bragg.common.dra.algolib.math;


import au.gov.ansto.bragg.common.dra.algolib.processes.BufferedProcessor;

//import org.gumtree.dra.common.model.Detector;
// A general class for integration process for all detectors

/**
 * @author  jgw
 */
public abstract class GeometryProcessor extends BufferedProcessor {

	protected int numSlices;
	protected static float detarc;
	protected GeometryCorrecter ac = null;
	protected float position = 0;
	
	public abstract String getName();
	public abstract String getDescription();
	
	protected static boolean filtered(float in)
	{
		return Float.isNaN(in);
	}
	
	/**
	 * @param numSlices  the numSlices to set
	 * @uml.property  name="numSlices"
	 */
	public void setNumSlices(int num)
	{
		numSlices = num;
		forget();
	}
	
	
	/**
	 * @param position  the position to set
	 * @uml.property  name="position"
	 */
	public void setPosition(float position)
	{
		this.position = position;
		forget();
	}
	
	public void setPositionCorrector(GeometryCorrecter ac)
	{
		this.ac = ac;
		forget();
	}
	
	//Common algorithm
	protected static float underLine(float x1, float y1, float x2, float y2, float sx, float sy)
    {
    	if(sx * sy == 0)
    	{
    		return (x2-x1)*(y2-y1);
    	}
    	float ratio = sy/sx;
    	float x1int = ratio*x1;
    	float x2int = ratio*x2;
    	float y1int = y1/ratio;
    	float y2int = y2/ratio;
    	float area = 0;
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
    public static void inPlaceArrayDivide(float[] num, float[] denom) {
        int i;
        for (i = 0; i < num.length; i++) {
            if (denom[i] != 0) {
                num[i] = num[i] / denom[i];
            } else {
                num[i] = Float.NaN;
            }
        }
    }
	/**
	 * @return  the detarc
	 * @uml.property  name="detarc"
	 */
	public static float getDetarc() {
		return detarc;
	}
	/**
	 * @param detarc  the detarc to set
	 * @uml.property  name="detarc"
	 */
	public static void setDetarc(float detarc) {
		GeometryProcessor.detarc = detarc;
	}
	/**
	 * @return  the numSlices
	 * @uml.property  name="numSlices"
	 */
	public int getNumSlices() {
		return numSlices;
	}
	/**
	 * @return  the position
	 * @uml.property  name="position"
	 */
	public float getPosition() {
		return position;
	}

}
