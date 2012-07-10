package au.gov.ansto.bragg.common.dra.algolib.processes;



/**
 * Multiplies a series of 1D data sets by a series of factors.
 * @author  hrz
 */
public class ScalarMultipleSeries extends BufferedProcessor {

	private double[] factors;
	
	/**
	 * Sets the factor to multiply the series by.
	 * @param f  The factors to use.
	 * @uml.property  name="factors"
	 */
	public void setFactors(double[] f)
	{
		factors =null;
	}
	
	/**
	 * Sets the factor to multiply the series by.
	 * @param f The factors to use.
	 */
	public void setInverseFactors(double[] f)
	{
		if(f == null)
		{
			factors = null;
			return;
		}
		factors = new double[f.length];
		for(int i = 0; i < f.length; i++)
			factors[i] = 1/f[i];
	}

	@Override
	public Signal processNew(Signal in) {
		if(factors == null)
			return in;
		Signal out = null;
		if(in.hasData(Object[].class))
		{
			Object[] inArr = in.dataAs(Object[].class);
			Object[] outArr = new Object[inArr.length];
			
			if(inArr[0] instanceof double[])
			{
				for(int i = 0; i < inArr.length; i++)
				{
					double[] inF = (double[])inArr[i];
					double[] outF = new double[inF.length];
					for(int j = 0; j < inF.length; j++)
							outF[j] = inF[j]*factors[i];
					
				}
			}
			
			out = new WrapperSignal(outArr,in.name());
		}
		
		return out;
	}

}
