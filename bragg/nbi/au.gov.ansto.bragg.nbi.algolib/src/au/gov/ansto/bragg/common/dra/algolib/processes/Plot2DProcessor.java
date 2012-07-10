package au.gov.ansto.bragg.common.dra.algolib.processes;

import java.util.ArrayList;

//import org.gumtree.dra.common.processes.BufferedProcessor;
//import org.gumtree.dra.common.processes.Signal;

import au.gov.ansto.bragg.common.dra.algolib.data.DataProvider2D;
import au.gov.ansto.bragg.common.dra.algolib.data.IDataUpdateListener;
/**
 * A simple processor that can have a TwoDTabViewer attatched to it.
 * @author  hrz
 */
public class Plot2DProcessor extends BufferedProcessor implements DataProvider2D {

	double[][] plotData;
	private String name;
	
	@Override
	public Signal processNew(Signal in){
		if(!in.hasData(double[][].class))
			return null;
		if(plotData != in.dataAs(double[][].class))
		{
			plotData = in.dataAs(double[][].class);
			update();
		}
		return null;
	}

	public double[][] getDataSet(int index) {
		return plotData;
	}

	public int getDataSetCount() {
		return 1;
	}

	public double[] getScales(int index) {
		return new double[] {0,0,1,1};
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
	 * Sets the default display name of any plotters using this data.
	 * @param n  The new name.
	 * @uml.property  name="name"
	 */
	public void setName(String n)
	{
		name = n;
	}

	public String getName(int index) {
		return name;
	}

	public void setCorrectSensitivity(boolean selection) {
		// TODO Auto-generated method stub
		
	}
}
