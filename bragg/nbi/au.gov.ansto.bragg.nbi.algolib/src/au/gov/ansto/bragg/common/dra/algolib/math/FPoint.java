package au.gov.ansto.bragg.common.dra.algolib.math;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class FPoint {
	/**
	 * The first data element.
	 */
	public float x;
	/**
	 * The second data element.
	 */
	public float y;
	/**
	 * Creates and initialises a new FPoint.
	 * @param xv The initial value of x.
	 * @param yv The initial value of y.
	 */
	public  FPoint(){};
	
	public  FPoint(float xv, float yv)
	{
		this.x = xv;
		this.y = yv;
	}
	/**
	 * Creates and initialises a new FPoint.
	 * @param xv The initial double value of x.
	 * @param yv The initial double value of y.
	 */
	public  FPoint(double xv,double yv)
	{
		this.x = (float)xv;
		this.y = (float)yv;
	}
  /**
   * Creates and initialises a new FPoint
   * 
   * @param str  Input a string parameter (x y)
   */
	public  FPoint(String str)
	{
		this.x = string2float(str)[0];
		this.y = string2float(str)[1];
	}
	
	public String toString()
	{
		return  x+" "+y ;
	}
  

	/**
	 * Creates and initialises multiple  new FPoints
	 * @param str input parameters with srting format
	 * @return point array list
	 */	
		public ArrayList<FPoint> serielPoint(String str){
			
			float ninp=0;
			ArrayList<FPoint> retPoint  = new ArrayList<FPoint>();
    	    try{
    	    ArrayList<String> param = new ArrayList<String>();
			StringTokenizer stz = new StringTokenizer( str,  " ");	
			 while (stz.hasMoreTokens())
			 {
				 ninp++;
				 String pas=stz.nextToken();
				 param.add(  pas);	
	       } 
//			  Integer[] inparam = new Integer[2];
//		       inparam =  (Integer[])param.toArray();
			 int np = stz.countTokens();

			 for(int j = 0; j < np; j+=2){
			 float xpoint  = Float.parseFloat(param.get(j));
			 float ypoint  = Float.parseFloat(param.get(j+1));	
			   FPoint xy =new FPoint(xpoint,ypoint);
			   retPoint.add(xy);
			 }
			 
    	    }catch(Exception ein) {
    	    	ein.printStackTrace();
    	    }
 
 
			return retPoint;
			
		}
		
		/**
		 * The method is designed for 
		 * @param str  input parameter "25  500"
		 * @return  float parameter array
		 */
			private  float[] string2float(String str){
				float xPoint  = 0;
				float yPoint  = 0;			
//				float xmaxBox  = 0;
//				float ymaxBox  = 0;
				float ninp=0;
	    	    float[] retparam = null;
	    	    try{
	    	    ArrayList<String> param = new ArrayList<String>();
				StringTokenizer stz = new StringTokenizer( str,  " ");	
				 while (stz.hasMoreTokens())
				 {
					 ninp++;
					 String pas=stz.nextToken();
					 param.add(  pas);	
		       } 
//				  Integer[] inparam = new Integer[2];
//			       inparam =  (Integer[])param.toArray();
					  xPoint  = Float.parseFloat(param.get(0));
					  yPoint  = Float.parseFloat(param.get(1));			

	    	    }catch(Exception ein) {
	    	    	ein.printStackTrace();
	    	    }
	    	    retparam = new float[2];
//	    	    retparam[0]   = Math.min(xPoint, xmaxBox);
//	    	    retparam[1]   = Math.min(yPoint, ymaxBox);		
	  
	    	    retparam[0]   =  xPoint;
	    	    retparam[1]   =  yPoint;	
			return retparam;
			}
}
