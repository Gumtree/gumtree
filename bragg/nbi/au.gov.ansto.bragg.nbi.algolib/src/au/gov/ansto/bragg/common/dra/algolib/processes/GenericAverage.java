package au.gov.ansto.bragg.common.dra.algolib.processes;

/**
 * A processor that takes a horizontal or vertical average of the data set.
 * @author hrz
 *
 */
public class GenericAverage extends BufferedProcessor {

	private boolean vertical = false;
	/**
	 * Sets whether to use a vertical average.
	 * @param vert If true, a vertical average will be used.
	 */
	public void setVertical(boolean vert)
	{
		vertical = vert;
	}
	
	public boolean getVertical()
	{
		return vertical;
	}
	
	private double[][] transpose(double[][] in)
	{
		double[][] out = new double[in[0].length][in.length];
		for(int i = 0; i < in.length; i++)
			for(int j = 0; j < out.length; j++)
				out[j][i] = in[i][j];
		return out;
	}

	@Override
	protected Signal processNew(Signal in) {
		// TODO Auto-generated method stub
		return null;
	}
	


}
