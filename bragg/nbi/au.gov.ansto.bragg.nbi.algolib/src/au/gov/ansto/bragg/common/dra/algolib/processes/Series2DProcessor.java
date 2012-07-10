package au.gov.ansto.bragg.common.dra.algolib.processes;

import java.util.ArrayList;

import au.gov.ansto.bragg.common.dra.algolib.data.DataProvider2D;
import au.gov.ansto.bragg.common.dra.algolib.data.IDataUpdateListener;

/**
 * A processor to display a series of 2D data sets.
 * @author  hrz
 */
public class Series2DProcessor extends BufferedProcessor implements DataProvider2D {

	Object[] plotDataArray;
	Object plotDataSingular;
	private String name;
	
	private Object getPlotData(int index)
	{
		if(iter != null)
		{
			iter.select(index);
			return plotDataSingular;
		}
		return plotDataArray[index];
	}
	
	private int length()
	{
		if(iter != null)
		{
			return iter.length();
		}
		if(plotDataArray == null)
			return 0;
		return plotDataArray.length;
	}
	
	public void setIterable(Iterable iter)
	{
		this.iter = iter;
	}
	private Iterable iter;
	
	@Override
	public Signal processNew(Signal in){
		if(in.name() != null && in.name().length() != 0)
			name = in.name();
		if(iter == null)
		{
		plotDataArray = in.dataAs(Object[].class);
		total = null;
		
		update();
		}
		else
		{
			plotDataSingular = in.dataAs(Object.class);
		}
		return in;
	}

	private double[][] total;
	public double[][] getDataSet(int index) {
		
		if(index == -1)
		{
			if(total == null)
			{
				double[][] pd = (double[][])getPlotData(0);
				total = new double[pd.length]
				               [pd[0].length];
				for(double[] row : total)
					for(int i = 0; i < row.length; i++)
						row[i] = 0;
				for(int in = 0; in < length(); in++)
				{
					Object ele = getPlotData(in);
					double[][] d = (double[][])ele;
					for(int i = 0; i < total.length; i++)
						for(int j = 0; j < total[0].length; j++)
							total[i][j] += d[i][j] / length();
				}
				return total;
			}
			return total;
		}
		
		try{
			return (double[][])getPlotData(index);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public int getDataSetCount() {
		return length();
	}
	
	private double[] scales = null;

	public double[] getScales(int index) {
		if(scales != null)
			return scales;
		if(length() == 0)
			return new double[] {0,0,1,1};
		double[][] dset = getDataSet(index);
		if(dset == null)
			return new double[] {0,0,1,1};
		return new double[] {0,0,dset.length,dset.length==0?0:dset[0].length};
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
		return name;
	}
	
	/**
	 * Sets the default display name of plots based off this data. Signals with non-null non-empty names WILL override this.
	 * @param n  The new name.
	 * @uml.property  name="name"
	 */
	public void setName(String n)
	{
		name = n;
	}

	public void setCorrectSensitivity(boolean selection) {
		// TODO Auto-generated method stub
		
	}

}
