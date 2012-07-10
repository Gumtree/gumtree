package au.gov.ansto.bragg.common.dra.algolib.processes;


import au.gov.ansto.bragg.common.dra.algolib.plot.PlotData1D;

/**
 * Converts a series of 1D data sets into a single 2D data set.
 * @author hrz
 *
 */
public class SeriesTo2DProcessor extends BufferedProcessor {

	@Override
	public Signal processNew(Signal in){
		Object[] arr = in.dataAs(Object[].class);
		double[][] rv = new double[arr.length][];
		for(int i = 0; i < arr.length; i++)
		{
			PlotData1D pd = (PlotData1D)arr[i];
			rv[i] = pd.y;
		}
		return new WrapperSignal(rv,in.name());
	}
}
