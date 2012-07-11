package au.gov.ansto.bragg.wombat.dra.algolib.core;

import java.util.ArrayList;

import au.gov.ansto.bragg.common.dra.algolib.data.DataStore;

public class DataIOTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int ncolu = 0;
		int nrow = 0;
		DataStore ds = new DataStore();
        double[][] datread = new double[nrow][ncolu];
  //      ArrayList<Double> dat1d = new ArrayList<Double>();
        double[]  oned = new double[nrow];   
        try{
        datread =ds.ASCIIData2DInput("/home/jgw/opaldra/xml","HRPDDataSet0.txt" , ncolu, nrow);
        oned = ds.ASCIIData1DInput("/home/jgw/opaldra/xml", "HInteCDataSet.txt", 0);
        System.out.println("ASCII data file has already read in!  " + oned[10]);
        ds.DataAsciiOutput2D(datread, "/home/jgw/opaldra/xml", "testiofile.txt");
        ds.DataAsciiOutput1D(oned, "/home/jgw/opaldra/xml", "testio1d.txt");
        System.out.println("ASCII data file has already output!, Pls Check!") ;
        }catch (Exception e){
        	e.printStackTrace();
        }

	}

}
