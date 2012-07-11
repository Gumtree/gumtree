 package au.gov.ansto.bragg.wombat.dra.algolib.processes;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
import au.gov.ansto.bragg.common.dra.algolib.processes.WrapperSignal;

import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDataSet;
import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDetector;
/**
 
 * @jgw  HPRD data sets stiching.
 */
public class HIPDDataFileSummeryImpl extends HIPDProcessor implements HIPDDataFileSummery{

	private HIPDDataSet[] d;
	private HIPDDataSet[] out;
	private HIPDDetector detc = new HIPDDetector();
	HIPDDataSet compound = new HIPDDataSet();
//	HIPDDataSetStich hdss = HIPDDataSetStich();
//	int nScan = hdss.getNScan();
	int xpixels = detc.xPixels;
	int ypixels = detc.yPixels;
//	float [][][] data3d = new float [xpixels][ypixels][d.length];
	public HIPDDataSet[] getScans()
	{
		return out;
	}
	/**
	 * Creates a new dialog instance.
	 * @param parent The shell to float over.
	 * @param data The GTDObject to read
	 * from and write to.
	 */
/*	public HIPDDataSetStich(HIPDDataSet[] data) {
//		super();
//		d = data;
	}
*/
	public double[][] multiDataSetStich(HIPDDataSet[] d)
			{
				HIPDDataSet compound = new HIPDDataSet();
//				compound.name = compoundName.getText();
				compound.monSample = 0;
				compound.monEmptyCell = 0;
				compound.monBlocked = 0;
				int ndset = d.length;
	for(int i = 0; i < d.length; i++)
		{
			try{
				compound.transmissionEmpty = d[i].transmissionEmpty;
				compound.transmissionSample = d[i].transmissionSample;
				compound.beamX = d[i].beamX;
				compound.beamY = d[i].beamY;
				compound.detector = d[i].detector;
				double[][] ds = d[i].sample;
				if(compound.sample == null || compound.sample.length == 0)
				 compound.sample = new double[ds.length][ds[0].length];
				int l= 0;
				for(int j = 0; j < ypixels; j++){
				 for(int k = i; k < (d.length)*xpixels; k+=ndset )
					{
				//		compound.sample[j][k] = data3d[l][k][d.length] ;
						compound.sample[j][k] = ds[l][k];
						l++;
						}
				compound.monSample += d[i].monSample;
						}

					}
					catch(Exception ex)
					{
						//Something went wrong with that dataset; move onto the next
					}
				}

				return compound.sample;
			}
	
	/**
	 * 
	 * @param stds                Three D data files to be flatted to two D array file
	 * 	@param  theta0           two theta of  the first detector in first scan
	 * @param  deltaTheta     distance between two tubes
	 * @return The stiched 2D data set with detHpixels * nScan in horisontal direction
	 * @return
	 */
	public double[][] multiDataSetStich(double [][][] stds,   double theta0, double  deltaTheta)
	{
		HIPDDataSet compound = new HIPDDataSet();
//		compound.name = compoundName.getText();
		compound.monSample = 0;
		compound.monEmptyCell = 0;
		compound.monBlocked = 0;
		int ndset = stds.length;
		int nydat = stds[0].length;
		int nxdat = stds[0][0].length;	
//		System.out.println("ndset,nydat,nxdat =" + ndset + ","+ nydat + ","+ nxdat ) ;
		double[][] dset2d = new double [nydat][nxdat*ndset];
  for(int i = 0; i < ndset; i++)
   {
	try{
		if(dset2d == null || dset2d.length == 0)
		 dset2d = new double[nydat][nxdat*ndset];

		for(int j = 0; j < nydat; j++){

		 for(int k = i; k < xpixels; k++ )
			{
		//		flat2d[j][k] = data3d[l][k][d.length] ;

				dset2d[j][i*xpixels + k] = stds[i][j][k];
		//		System.out.println("i,j,l =" + i + " " + j + " "  + l);
		//		System.out.println("Data input for stiching = " + stds[i][j][l]);
		//		System.out.println("Data output for GeoCorrection = " + dset2d[j][k] );				
				
				}

				}

			}
			catch(Exception ex)
			{
				//Something went wrong with that dataset; move onto the next
			}
		}

		return dset2d;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected Signal processNew(Signal in) {
		// TODO Auto-generated method stub
		HIPDDataSet data = in.dataAs(HIPDDataSet.class);
		double [][][] stds = null;
		int detHpixels = stds[0].length;
		int detVpixels = stds[0][0].length;
		double deltaTheta =0.03125;
		double theta0 =0.0;
		if(data.corrected != null && data.detector == hid)
			return new WrapperSignal(data.corrected, data.name);
		if(data == null)
			throw new NullPointerException("ProcessNew given null data!");
		try{
		double[][] stiched  = multiDataSetStich(stds, theta0,    deltaTheta);
		data.stiched = stiched;
		data.detector = hid;
		return new WrapperSignal(stiched, data.name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new WrapperSignal(data, "Error - Uncorrected Data");
		}
	}
	/**
	 * @param d
	 * @param out
	 * @param detc
	 * @param compound
	 * @param nxPixels
	 * @param nyPixels
	 */
//	public HIPDDataSetStich(HIPDDataSet[] d, HIPDDataSet[] out, HIPDDetector detc, HIPDDataSet compound, int xpixels, int ypixels) {
		public HIPDDataFileSummeryImpl() {
		super();
//		this.d = d;
//		this.out = out;
//		this.detc = detc;
//		this.compound = compound;
//		this.xpixels = xpixels;
//		this.ypixels = ypixels;
	}
	/**
	 * 
	 */
	public void open() {
		// TODO Auto-generated method stub
		
	}	
}


