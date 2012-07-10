package au.gov.ansto.bragg.common.dra.algolib.processes;

import java.util.ArrayList;
import java.util.Date;

import au.gov.ansto.bragg.common.dra.algolib.data.DataProvider1D;
import au.gov.ansto.bragg.common.dra.algolib.data.DataStore;
import au.gov.ansto.bragg.common.dra.algolib.data.IDataUpdateListener;
import au.gov.ansto.bragg.common.dra.algolib.plot.PlotDataShaped;
import au.gov.ansto.bragg.common.dra.algolib.plot.PlotData1D;



/**
 * A processor that seriesTabViewer plots can be attatched to.
 * @author  hrz
 */
public class Series1DProcessor extends BufferedProcessor implements DataProvider1D {

	/**
	 * @uml.property  name="dataArray"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private PlotData1D[] dataArray;
	private PlotData1D dataSingular;
	private PlotData1D getData(int index)
	{
		if(iterable != null)
		{
			iterable.select(index);
			return dataSingular;
		}
		return dataArray[index];
	}
	
	private int length()
	{
		if(iterable != null)
		{
			return iterable.length();
		}
		if(dataArray == null)
			return 0;
		return dataArray.length;
	}
	/**
	 * Sets an 'iterable' allowing the processor to lazily evaluate data sets.
	 * @param store  The iterable.
	 * @uml.property  name="iterable"
	 */
	public void setIterable(DataStore store)
	{
		this.iterable = (Iterable) store;
	}
	private String[] labels;
	private String name = "Unlabelled Plot";
	double[] ancillaryX = null ;
	double[] ancillaryY = null ;
	private Iterable iterable;
	/**
	 * Creates a new Series 1D binding processor with standard labels.
	 *
	 */
	public Series1DProcessor()
	{
		this(new String[] {"X","Y","Intensity"});
	}
	
	/**
	 * Creates a new Series 1D binding processor with custom labels.
	 * @param labels The labels to use.
	 */
	public Series1DProcessor(String[] labels)
	{
		this.labels = labels;
	}
	
	@Override
	public Signal processNew(Signal in){
		if(in.name() != null && in.name().length() != 0)
			setName(in.name());
		if(in.hasData(PlotData1D[].class))
		{
			if(iterable != null)
				dataSingular = in.dataAs(PlotData1D[].class)[0];
			dataArray = in.dataAs(PlotData1D[].class);
		}
		else if(in.hasData(double[][].class))
		{
			double[][] vals = in.dataAs(double[][].class);
			dataArray = new PlotData1D[vals.length];
			for(int i = 0; i < dataArray.length; i++)
			{
				double[] x = new double[vals[i].length];
				double[] y = vals[i];
				for(int j = 0; j < x.length; j++)
					x[j] = j;
				dataArray[i] = new PlotData1D(x,y,null,null,"Row "+i);
			}
		}
		else if(in.hasData(PlotData1D.class))
		{
			if(iterable != null)
			{
				dataSingular=in.dataAs(PlotData1D.class);
			}
			else
			{
				ancillaryX = (in.dataAs(PlotData1D.class)).x;
				ancillaryY = (in.dataAs(PlotData1D.class)).y;
			}
		}
		total = null;
		/*else if(in.hasData(Object[].class))
		{
			Object[] arr = in.dataAs(Object[].class);
			if(arr == null || arr.length == 0)
				return null;
			if(arr[0] instanceof PlotData1D)
			{
				data = new PlotData1D[arr.length];
				for(int i = 0; i < data.length; i++)
					data[i] = (PlotData1D)arr[i];
			}
			else if(arr[0] instanceof double[])
			{
				data = new PlotData1D[arr.length];
				for(int i = 0; i < data.length; i++)
				{
					double[] y = (double[])arr[i];
					double[] x = new double[y.length];
					for(int j = 0; j < x.length; j++)
						x[j] = j;
					data[i] = new PlotData1D(x,y,null,null,"Row "+i);
				}
			}
		}*/
		postUpdate();
		
		return null;
	}

	public boolean canDoShapedDataSet() {
		return false;
	}

	public PlotDataShaped getShapedDataSet() {
		double[][] x = new double[length()][];
		double[][] y = new double[length()][];
		double[][] z = new double[length()][];
		for(int i = 0; i < x.length; i++)
		{
			PlotData1D data = getData(i);
			x[i] = data.x;
			y[i] = data.y;
			z[i] = new double[x[i].length];
			for(int j = 0; j < z[i].length; j++)
				z[i][j] = i;
		}
		PlotDataShaped pds = new PlotDataShaped(x,y,z,labels);
		return pds;
	}

	private PlotData1D total = null;
	public PlotData1D getDataSet1D(int index) {
		if(iterable == null && dataArray == null)
			return null;
		if(index == -1)
		{
			if(total != null)
				return total;
			if(length() == 0)
				return null;
			PlotData1D data = getData(0);
			double[] x = data.x;
			double[] y = new double[x.length];
			double[] e = data.err==null?null:new double[x.length];
			for(int i = 0; i < y.length; i++)
			{
				y[i] = 0;
				if(e != null)
					e[i] = 0;
			}
			for(int ind = 1; ind < length(); ind++)
			{
				PlotData1D d = getData(ind);
				for(int i = 0; i < x.length; i++)
				{
					y[i] += d.y[i]/length();
					if(e != null)
						e[i] += d.err[i]/length();
				}
			}
			total = new PlotData1D(x,y,e,null,"Average");
			return total;
		}
		return getData(index);
	}

	public double getDataSetZ(int index) {
		return index;
	}

	public int getDataSetCount1D() {
		return length();
	}

	/**
	 * @return  the ancillaryX
	 * @uml.property  name="ancillaryX"
	 */
	public double[] getAncillaryX() {
		return ancillaryX;
	}

	/**
	 * @return  the ancillaryY
	 * @uml.property  name="ancillaryY"
	 */
	public double[] getAncillaryY() {
		return ancillaryY;
	}

	private Date update = new Date();
	public Date lastUpdate() {
		return update;
	}

	public String[] twoDLabels() {
		return labels;
	}

	public void getSupValues(String[] labels, double[] mins, double[] mults) {
		// TODO Auto-generated method stub
		
	}

	ArrayList<IDataUpdateListener> listeners = new ArrayList<IDataUpdateListener>();
	
	protected void postUpdate()
	{
		update = new Date();
		for(IDataUpdateListener listener : listeners)
			listener.dataUpdated();
	}
	
	public void registerInterest(IDataUpdateListener l) {
		listeners.add(l);
	}
	

	public String getPlotName() {
		return name;
	}
	/**
	 * Sets the name of this plot.
	 * @param  name
	 * @uml.property  name="name"
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	public PlotData1D getDuplicity() {
		// TODO Auto-generated method stub
		return null;
	}

}
