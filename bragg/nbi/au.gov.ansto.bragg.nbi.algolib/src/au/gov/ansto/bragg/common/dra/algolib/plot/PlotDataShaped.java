package au.gov.ansto.bragg.common.dra.algolib.plot;

public class PlotDataShaped extends PlotData{
	public double[][] x, y;
	public double[][] z;
	public String[] labels;
	
	public PlotDataShaped(double[][] x, double[][] y, double[][] z)
	{
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public PlotDataShaped(double[][] x, double[][] y, double[][] z, String[] labels)
	{
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.labels = labels;
	}
}
