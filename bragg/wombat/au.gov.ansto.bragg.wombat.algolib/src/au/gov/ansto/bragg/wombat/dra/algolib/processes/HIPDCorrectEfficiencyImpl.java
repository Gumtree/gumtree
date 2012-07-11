package au.gov.ansto.bragg.wombat.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.processes.Processor;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
import au.gov.ansto.bragg.common.dra.algolib.processes.WrapperSignal;

import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDetector;

/**
 * @author hrz
 * A process for applying sensitivity correction to reflectometer data.
 */
public class HIPDCorrectEfficiencyImpl extends Processor implements HIPDCorrectEfficiency{

	double[][] sensMap = null;
	HIPDDetector detector;
	private boolean inverse = false;
	/**
	 * Applies detector sensitivity correction to the data
	 * @param Signal in  Using Sinal object as input object. sigal and sensMap 
	 * are also provided from Signal object
	 * 
	 */		
	@Override
	public Signal process(Signal in) {
		if(sensMap == null)
			return in;
		if(in.hasData(double[][].class))
		{
			double[][] d = in.dataAs(double[][].class);
			double[][] out = new double[d.length][d[0].length];
			
			for(int i = 0; i < d.length; i++)
				for(int j = 0; j < d[0].length; j++)
					if(inverse)
						out[i][j] = d[i][j] / sensMap[i][j];
					else
						out[i][j] = d[i][j] * sensMap[i][j];
						
			
			return new WrapperSignal(out,in.name());
		}
		return null;
	}
	/**
	 * 
	 * @param inData    Raw exp data double 2D set for Input
	 * @param effMap    Presetting 2D efficiency table from instrument scientist or anywhere else
	 * @param flag        control para for doing correction (flag=1) or not (flag=0)
	 * @param flag contrl parameter flag =1 do correction; flag=0 do nothing
	 * @param Control threshold parameter to reject very small efficiency block
	 * @param inverse   boolean control parameter 
	 *                                if detection efficience less than 100%  set it false
	 *                                if detection efficience greater than 100%  set it true
	 * @return The filtered array.
	 * @return  foo        corrected  2D double data set
	 */
    
    public  double[][] doSensitivity(double[][] data, double[][] sensitivity, int flag, double thresh, boolean inverse) {

    	if (flag == 0){
            return data;
        }
    	
    	if (sensitivity == null || data == null) {
            return data;
        }
        if (sensitivity.length != data.length
                || sensitivity[0].length != data[0].length) {
            return null;
        }
 
        double[][] out = new double[data.length][data[0].length];
        int i, j;
        for (i = 0; i < data.length; i++) {
            for (j = 0; j < data[0].length; j++) {
                if (sensitivity[i][j] < thresh )
                	out[i][j] = Double.NaN;
				if(inverse)             
                out[i][j] = data[i][j] / sensitivity[i][j];
				else
				out[i][j] = data[i][j] * sensitivity[i][j];
            }
        }
        return out;
    }
	/**
	 * Sets whether this filter should act in a forwards or referse
	 * direction.
	 * @param val True if this filter should be done in reverse.
	 */
	public void setInverse(boolean val)
	{
		inverse = val;
	}
	
	public void setSensMap(double[][] map)
	{
		sensMap = map;
	}
	/**
	 * @param d
	 */
	public void setDetector(HIPDDetector d) {
		// TODO Auto-generated method stub
		this.detector =d;
	}

}
