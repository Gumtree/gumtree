package au.gov.ansto.bragg.echidna.dra.algolib.processes;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
import au.gov.ansto.bragg.common.dra.algolib.processes.WrapperSignal;
import au.gov.ansto.bragg.common.dra.algolib.data.DataStore;
import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDataSet;
import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDetector;
/**
 
 * @jgw  HPRD data sets stiching.
 */
public class HRPDDataSetStitchImpl extends HRPDProcessor implements HRPDDataSetStitch {

	private HRPDDataSet[] d;
	private HRPDDataSet[] out;
	private HRPDDetector detc = new HRPDDetector();
	HRPDDataSet compound = new HRPDDataSet();
//	HRPDDataSetStich hdss = HRPDDataSetStich();
//	int nScan = hdss.getNScan();
	int xpixels = detc.xpixels;
	int ypixels = detc.ypixels;
	private int[] ncontrBin=null;
//	float [][][] data3d = new float [xpixels][ypixels][d.length];
	public HRPDDataSet[] getScans()
	{
		return out;
	}
	/**
	 * Creates a new dialog instance.
	 * @param parent The shell to float over.
	 * @param data The GTDObject to read
	 * from and write to.
	 */
/*	public HRPDDataSetStich(HRPDDataSet[] data) {
//		super();
//		d = data;
	}
*/
	public double[][] multiDataSetStich(HRPDDataSet[] d)
			{
		int nscan = d.length;

				HRPDDataSet compound = new HRPDDataSet();
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

				for(int j = 0; j < ypixels; j++){
					int l= 0;
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
	 * Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata.
	 * 	This  algorithm is developed for NO overlap multiple scan data set stitching
	 * @param  twoTheta0   the initial position of first tube
	 * @param  deltaTheta   distance between two tubes
	 * @param 3D exp data set  to be stitchedwith format  nDataSet * nDetCount * nTubes. 
	 * 					DataSet[nDataSet ][nDetCount ][ nTubes]
	 * @return 2D array data double object with nCounts* nScan elements
	 *                  Special return array retData[nCounts] (normally retData[512]) present new theta vector.
	 */
	public double[][] multiDataSetStich(double[][][] stds, double  twoTheta0, double deltaTheta, double[] stepsize)
	{
		HRPDDataSet compound = new HRPDDataSet();
//		compound.name = compoundName.getText();
		compound.monSample = 0;
		compound.monEmptyCell = 0;
		compound.monBlocked = 0;
		int ndset = stds.length;
		int nydat = stds[0].length;
		int nxdat = stds[0][0].length;	
		System.out.println("ndset,nydat,nxdat =" + ndset + ","+ nydat + ","+ nxdat ) ;
//		if (detHpixels != nxdat || detVpixels != nydat || nScan != ndset) {
//			System.out.println("The demension of the dataset are NOT match with inputed demensions!");
//			return null;
//		}
		
		double[][] dset2d = new double [nydat][nxdat*ndset];
		double[] twoTheta = new double [nxdat*ndset];
  for(int i = 0; i < ndset; i++)
   {
	try{
		if(dset2d == null || dset2d.length == 0)
		 dset2d = new double[nydat][nxdat*ndset];

		for(int j = 0; j < nydat; j++){
			int l= 0;		
		 for(int k = i; k < ndset*nxdat; k+=ndset )
			{
		//		flat2d[j][k] = data3d[l][k][d.length] ;
			    l = k /ndset;
				dset2d[j][k] = stds[i][j][l];
		//		System.out.println("i,j,l =" + i + " " + j + " "  + l);
		//		System.out.println("Data input for stiching = " + stds[i][j][l]);
		//		System.out.println("Data output for GeoCorrection = " + dset2d[j][k] );				
				
				}

				}

			}
			catch(Exception ex)
			{
				//Something went wrong with that dataset; move onto the next
				ex.printStackTrace();
			}
		}
	   int l = 0;
     for(int i =0; i < ndset; i++ ) {
	     for(int k =0; k < nxdat; k++ ) {
	    	 l ++;
 		     twoTheta[l] = twoTheta0 + k * deltaTheta + i *stepsize[i];
	        }
        }
       for (int k = 0; k < ndset*nxdat; k++) {
    	   dset2d[nydat][k] =  twoTheta[k];
       }
		return dset2d;
	}
	 /**
	  * This  algorithm is developed for overlap multiple scan data set stitching
	  *	Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata. 
	  * @param inDat      Input 3D file with nScan * nCounts * nTubes
	  * @param array2theta  Multiple scan 2D 2theta array nScan * nTubes
	  * @param stepsize  Multiple scan step size 1D array with nScan elements
	  * @param binsize   Optional user control parameter for number of bins stitching to.
	  * @param twotheta0 The two theta for first tube in the first scan.
	  * @return 2D array data double object with nCounts*((detctorArc + nScan*stepsize)/stepsize) elements
	  *                  Special return array retData[nCounts] (normally retData[512]) present new theta vector.
	  */
	public double [][] echidnaDataStitch( double[][][] inDat, double [][] array2theta, double[] stepsize, double binsize, double twotheta0){
	
		double[][] stitchDat = null;
		double[][] returnDat =null;
		int nScan = inDat.length;
		int nCount = inDat[0].length;
		int nTubes =inDat[0][0].length;
		double dbinSize = 0.0;
		double averstepsiz = 0;
// oooooooooooooooooooooooooooooooooEXEXEXEXEXEX, 
//		o...o    is detector arc,      EX...EX is overlap range.
		double extrasiz  = 0.0;
		double dTheta = array2theta[0][1] - array2theta[0][0] ;
		
		System.out.println("Data Dimensions: " + nScan + "; " + nCount +"; " + nTubes);
		for (int n= 0; n < nScan - 1; n++){
//			System.out.println("Stepsize = " + stepsize[n]);
			averstepsiz += stepsize[n]/(nScan-1);
			extrasiz += stepsize[n];
		}
		//System.out.println("Twotheta0 = " 	+ twotheta0 );
		//manually debug 
        //    binsize = 0.05;
//		System.out.println("AverageStepsize = " + averstepsiz);    
		System.out.println("Extrasize =  " + extrasiz); 
		if (binsize != 0) dbinSize = binsize;
		           else dbinSize = averstepsiz;         // Use stepsize as bin size if there is no user input.
		
	    double lengthCurve =  detc.horisonCurv + extrasiz;
//        int nBins = (int) (lengthCurve / averstepsiz);
        int nBins = (int) (lengthCurve / dbinSize);
//		System.out.println("nBin and length of curve: " + nBins + "; " + lengthCurve);
        double[] thetaVect = new double[nBins];      
// To make algorithm library with interlegent function, following passage presents some conditional judgement blocks
        int nVirtTubes = nScan * nTubes;
        if (nBins > 4.0 * nVirtTubes / 5.0) {
        	System.out.println(" Warnning!!!!!!! \n "
        										+ "Number of scan in this data set is " + nScan + ". \n" 
        										+ "Average step size is  "  + averstepsiz  + ", \n" 
        										+ "Number of  bins is too large for small number of scan!  \n"
        										+ "Algorithm will reset the number of bin to consistent with small scan number.");
        	dbinSize = 5.0* dbinSize / 4.0;
            nBins = (int) (lengthCurve / dbinSize);
        }
	
//			for (int k = 0; k < nBins; k++)
//				if(array2theta[0][0] <= array2theta[0][nTubes-1])
//				thetaVect[k] = twotheta0 + k * averstepsiz;	
//				else thetaVect[k] = twotheta0 - k * averstepsiz;	
		for (int k = 0; k < nBins; k++)
		if(array2theta[0][0] <= array2theta[0][nTubes-1])
		thetaVect[k] = twotheta0 + k * dbinSize;	
		else thetaVect[k] = twotheta0 - k * dbinSize;	
//			System.out.println("thetaVect: " + thetaVect[1234] + "; " + thetaVect[3000]);		
		stitchDat = new double[nCount][nBins];
	//       ncontrBin = new int[nScan*nTubes];
	       ncontrBin = new int[nBins];

//		for(int ith = 0; ith < nScan*nTubes; ith++){
		for(int ith = 0; ith < nBins; ith++){			
			ncontrBin[ith]=0;
			for(int k = 0; k < nScan; k++){
				for(int j = 0; j < nTubes; j++){		

//					if(array2theta[k][j]>(twotheta0+ ith*dbinSize) && array2theta[k][j] <=  (twotheta0+ (ith+1)*dbinSize)) {
//					if(array2theta[k][j]>(twotheta0+ k*dbinSize + j*dTheta ) && 
//							array2theta[k][j] <=  (twotheta0+ (k+1) * dbinSize +  (j+1) * dTheta)) {			
// Following "if " condition expression to consider about two theta value setting such as  "360"  ---> "0"  or from "0"   ---> " 360"
			if(array2theta[0][0] <= array2theta[0][nTubes-1]) {
					if(array2theta[k][j]>=(twotheta0 - dbinSize/2.0 + ith*dbinSize  ) 
							&& array2theta[k][j] <  (twotheta0 - dbinSize/2.0 + (ith+1) * dbinSize))
						{								
						ncontrBin[ith]++;					
						for  (int nc =0; nc < nCount; nc++)
	
						stitchDat[nc][ith] += inDat[k][nc][j];
//						System.out.println("ncounttrBin: " + ncontrBin[ith]);     
					}
           } else {
				if(array2theta[k][j] <(twotheta0 + dbinSize/2.0 - ith*dbinSize  ) 
						&& array2theta[k][j] >=  (twotheta0 + dbinSize/2.0 - (ith+1) * dbinSize))
				{								
				ncontrBin[ith]++;					
				for  (int nc =0; nc < nCount; nc++)

				stitchDat[nc][ith] += inDat[k][nc][j];
//				System.out.println("ncounttrBin: " + ncontrBin[ith]);     
			}      	   
           }
				}
			}
		}
		      returnDat = new double[nCount+1][nBins];		
		      DataStore ds = new DataStore();
		      double[] contrib2Bin = new double[nBins];

		for(int ith = 0; ith < nBins; ith++){
			 contrib2Bin[ith] = ncontrBin[ith];
//			 System.out.println("ncounttrBin: " + ncontrBin[ith]); 		
			for(int k = 0; k < nCount; k++)
				if (ncontrBin[ith] != 0)
			        returnDat[k][ith] = stitchDat[k][ith]/ncontrBin[ith];
				else returnDat[k][ith] = Double.NaN;
		}
		for(int ith = 0; ith < nBins; ith++)
			returnDat[nCount][ith] = 	thetaVect[ith];
//		for(int j=0;j<nCount;j++)
//		System.out.println("Return Data: " +  returnDat[j][200]);
//	      try {
//			ds.DataAsciiOutput1D(contrib2Bin, "D:\\datadir", "binninginfor.txt");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return returnDat;
	}
	 /**
	  * This  algorithm is developed for overlap multiple scan data set stitching and use ideal two theta vector
	  *  for each detector tube. Algorithm will take 1.25 degree and separation of two tubes and average stepsize
	  *  as binsize. two theta value for each detector position will be calculated instead of data from meta data
	  *  table. Number of bins will take value as nScan * nTubes if there is no overlap of the tubes. Otherwise 
	  *  number of bins will be calculated use (detctorArc + nScan*stepsize)/stepsize.
	  *	Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata. 
	  * @param inDat      Input 3D file with nScan * nCounts * nTubes
	  * @param array2theta  Multiple scan 2D 2theta array nScan * nTubes
	  * @param stepsize  Multiple scan step size 1D array with nScan elements
	  * @param binsize   Optional user control parameter for number of bins stitching to.
	  * @param twotheta0 The two theta for first tube in the first scan.
	  * @return 2D array data double object with nCounts*((detctorArc + nScan*stepsize)/stepsize) elements
	  *                  Special return array retData[nCounts] (normally retData[512]) present new theta vector.
	  */
	public double [][] echidnaIdealDataStitch( double[][][] inDat, double [][] array2theta, double[] stepsize, 
			                        double binsize, double twotheta0){
	
		double[][] stitchDat = null;
		double[][] returnDat =null;
		int nScan = inDat.length;
		int nCount = inDat[0].length;
		int nTubes =inDat[0][0].length;
		double dbinSize = 0.0;
		double averstepsiz = 0;
		double [][] twothetarray = new double[nScan][nTubes];
//oooooooooooooooooooooooooooooooooEXEXEXEXEXEX, 
//		o...o    is detector arc,      EX...EX is extra scan range.
		double extrasiz  = 0.0;
		double dTheta = array2theta[0][1] - array2theta[0][0] ;
				      twotheta0 = array2theta[0][127] ;
		System.out.println("Data Dimensions: " + nScan + "; " + nCount +"; " + nTubes);
		for (int n= 0; n < nScan - 1; n++){
//			System.out.println("Stepsize = " + stepsize[n]);
			averstepsiz += stepsize[n]/(nScan-1);
			extrasiz += stepsize[n];
		} 
//		String astepsiz = String.valueOf(averstepsiz);
//		averstepsiz = Double.parseDouble(astepsiz.substring(0, 5));

		averstepsiz = ((int) (averstepsiz* 1000.0 + 0.5))/1000.0;

		System.out.println("averstepsiz = " + averstepsiz );
		
		//System.out.println("Twotheta0 = " 	+ twotheta0 );
		//manually debug 
       //    binsize = 0.05;
//		System.out.println("AverageStepsize = " + averstepsiz);    
//		System.out.println("Extrasize =  " + extrasiz); 
//		System.out.println("Two Theta0 =  " +  twotheta0 ); 	
		if (binsize != 0) dbinSize = binsize;
		           else dbinSize = averstepsiz;         // Use stepsize as bin size if there is no user input.
		
	    double lengthCurve =  detc.horisonCurv + extrasiz;
//       int nBins = (int) (lengthCurve / averstepsiz);
       int nBins = (int) (lengthCurve / dbinSize);
//		System.out.println("nBin and length of curve: " + nBins + "; " + lengthCurve);
       double[] thetaVect = new double[nBins];      
	
		 double  deltaTheta = detc.seperation;
		for (int i = 0; i < nScan; i++){
			for (int j = 0; j < nTubes; j++){
				twothetarray[i][j] =twotheta0 + averstepsiz * i + deltaTheta*j;
			}
		}
//			for (int k = 0; k < nBins; k++)
//				if(array2theta[0][0] <= array2theta[0][nTubes-1])
//				thetaVect[k] = twotheta0 + k * averstepsiz;	
//				else thetaVect[k] = twotheta0 - k * averstepsiz;	
		for (int k = 0; k < nBins; k++)
//		if(array2theta[0][0] <= array2theta[0][nTubes-1])
		thetaVect[k] = twotheta0 + k * dbinSize;	
//		else thetaVect[k] = twotheta0 - k * dbinSize;	
//			System.out.println("thetaVect: " + thetaVect[1234] + "; " + thetaVect[3000]);		
		stitchDat = new double[nCount][nBins];
	//       ncontrBin = new int[nScan*nTubes];
	       ncontrBin = new int[nBins];

//		for(int ith = 0; ith < nScan*nTubes; ith++){
		for(int ith = 0; ith < nBins; ith++){			
			ncontrBin[ith]=0;
			for(int k = 0; k < nScan; k++){
				for(int j = 0; j < nTubes; j++){		

//					if(array2theta[k][j]>(twotheta0+ ith*dbinSize) && array2theta[k][j] <=  (twotheta0+ (ith+1)*dbinSize)) {
//					if(array2theta[k][j]>(twotheta0+ k*dbinSize + j*dTheta ) && 
//							array2theta[k][j] <=  (twotheta0+ (k+1) * dbinSize +  (j+1) * dTheta)) {			
//Following "if " condition expression to consider about two theta value setting such as  "360"  ---> "0"  or from "0"   ---> " 360"
//			if(array2theta[0][0] <= array2theta[0][nTubes-1]) {
					if(twothetarray[k][j]>=(twotheta0 - dbinSize/2.0 + ith*dbinSize  ) 
							&& twothetarray[k][j] <  (twotheta0 - dbinSize/2.0 + (ith+1) * dbinSize))
						{								
						ncontrBin[ith]++;					
						for  (int nc =0; nc < nCount; nc++)
	
						stitchDat[nc][ith] += inDat[k][nc][j];
//						System.out.println("ncounttrBin: " + ncontrBin[ith]);     
					}
//          } else {
//				if(twothetarray[k][j] <(twotheta0 + dbinSize/2.0 - ith*dbinSize  ) 
//						&& twothetarray[k][j] >=  (twotheta0 + dbinSize/2.0 - (ith+1) * dbinSize))
//				{								
//				ncontrBin[ith]++;					
//				for  (int nc =0; nc < nCount; nc++)
//
//				stitchDat[nc][ith] += inDat[k][nc][j];
//				System.out.println("ncounttrBin: " + ncontrBin[ith]);     
//			}      	   
 //         }
				}
			}
		}
		      returnDat = new double[nCount+1][nBins];		
		      DataStore ds = new DataStore();
		      double[] contrib2Bin = new double[nBins];

		for(int ith = 0; ith < nBins; ith++){
			 contrib2Bin[ith] = ncontrBin[ith];
//			 System.out.println("ncounttrBin: " + ncontrBin[ith]); 		
			for(int k = 0; k < nCount; k++)
				if (ncontrBin[ith] != 0)
			        returnDat[k][ith] = stitchDat[k][ith]/ncontrBin[ith];
				else returnDat[k][ith] = Double.NaN;
		}
		for(int ith = 0; ith < nBins; ith++){
			int m = nBins-ith-1;
			returnDat[nCount][ith] = 	thetaVect[m];
//		returnDat[nCount][ith] = 	thetaVect[ith];	
		}
//		for(int j=0;j<nCount;j++)
//		System.out.println("Return Data: " +  returnDat[j][200]);
//	      try {
//			ds.DataAsciiOutput1D(contrib2Bin, "D:\\datadir", "binninginfor.txt");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return returnDat;
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
	public Signal processNew(Signal in) {
		// TODO Auto-generated method stub
		HRPDDataSet data = in.dataAs(HRPDDataSet.class);
		double [][][] stds = null;
		int nScan = stds.length;
		int nypixes = stds[0].length;
		int nxpixes = stds[0][0].length;	
		double twoTheta0=0;
		double dTheta=0;
		if(data.corrected != null && data.detector == detector)
			return new WrapperSignal(data.corrected, data.name);
		if(data == null)
			throw new NullPointerException("ProcessNew given null data!");
		try{
		double[][] stiched  = multiDataSetStich(stds,twoTheta0,dTheta, null);
		data.stiched = stiched;
		data.detector = detector;
		return new WrapperSignal(stiched, data.name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new WrapperSignal(data, "Error - Uncorrected Data");
		}
	}	
}


