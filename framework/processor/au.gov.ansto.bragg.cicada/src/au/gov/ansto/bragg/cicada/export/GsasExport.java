package au.gov.ansto.bragg.cicada.export;

import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;

import java.net.URI;
import java.io.*;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
/**
 *  This class implements a GSAS format exporter for powder data
 *  @author jrh
 *  
 */ 
public class GsasExport extends FormatedExport {
   
	private double[] outcounts = null;    //Data on an even grid
	private double[] outvar = null;//Variance on an even grid
	private double   start_tth = 999.0;
	private double   grid_spacing = 0.0;
	private int      thlen = 0;
	
   public void resultExport(URI fileURI, IGroup signal) throws IOException {
	   PrintWriter outputfile = new PrintWriter(new FileWriter(getFile(fileURI, "gsa")));
	   IArray outputnumbers;
	try {
		outputnumbers = ((NcGroup) signal).getSignalArray();
	} catch (SignalNotAvailableException e) {
		throw new IOException();
	}
	   String topcomment = signal.getLocation();
	   double [] output_dbl = ConverterLib.get1DDouble(outputnumbers);
	   int data_Rank = outputnumbers.getRank();
	   IArray tth_points = ((Plot) signal).getAxis(data_Rank-1).getData();
	   double [] out2th = ConverterLib.get1DDouble(tth_points);
	   IArray err_array = ((Plot) signal).getVariance().getData();
	   double [] err_dbl = ConverterLib.get1DDouble(err_array);
	   // Attempt to get a sensible grid spacing from previously stored metadata
	   try {
		   grid_spacing = Double.parseDouble(signal.harvestMetadata("CIF").get("_[local]_scan_step_size"));
	   } catch (Exception e) {
		   grid_spacing = 0;
	   }
	   make_even_grid(out2th,output_dbl,err_dbl);
	   Integer extra = thlen % 5;   /* GSAS has 5 points per line */
	   Integer nlines = thlen / 5;  /* GSAS has 5 points per line */
	   if(extra > 0) nlines +=1;        /* for the last line */
	   outputfile.format("%79s%n",topcomment);
	   outputfile.format("BANK %2d %5d %5d CONST %10.3f %7.2f 0 0 ESD%n",
			   1,thlen,nlines,start_tth*100.0,grid_spacing*100.0);
	   for(int i=0;i<thlen;i++) {
		   outputfile.format("%8.2f%8.2f",outcounts[i],Math.sqrt(outvar[i]));
		   if((i+1)%5==0) outputfile.format("%n");
	   }
	   outputfile.format("%n");
	   outputfile.close();
   }
   
   private void make_even_grid(double [] thetapoints, double[] datapoints, double [] varpoints) {
	   
	// TODO automatically determine the appropriate grid spacing
	
	// values for working out pixel location in our array. As discussed in the stitching algorithm,
	// we assume that the 2theta value corresponds to the centre of the pixel.  Therefore, we expect 
	// thlen-1 bins from thlen points.
	int inpoints = thetapoints.length;
	// Backup way of working out the grid spacing; divide into total range.
	if(grid_spacing==0) {
		grid_spacing = (thetapoints[inpoints-1]- thetapoints[0])/(inpoints-1);
	}
	thlen = (int) Math.ceil((thetapoints[inpoints-1] -thetapoints[0]) / grid_spacing) + 1;  //negative if order is reversed
	System.out.printf("Theta grid regularised to spacing %f, %d points\n", grid_spacing,thlen);
	start_tth = thetapoints[0];
	outcounts = new double[thlen];
	outvar = new double[thlen];
	double [] outangles = new double[thlen];
	/* now we loop over the output points, doing a linear interpolation between the closest observed points.  We improve
	 * efficiency by not searching our point array from the beginning each time.  As the observed point array is sorted,
	 * we know that we will never need to step back in it to find the nearest point.
	 */
	int upper_neighbour = 0;
	for(int pointno=0;pointno<thlen;pointno++) {
		double thispoint = pointno*grid_spacing+start_tth;
		outangles[pointno]=thispoint;
		//Find closest point that is less than this point
		try {
		while(thetapoints[upper_neighbour]<=thispoint) upper_neighbour++;
		} catch (ArrayIndexOutOfBoundsException e) {
			break;
		}
		//So by definition, the next point is greater than this point. Perform the
		//linear interpolation.
		double lowerpoint = thetapoints[upper_neighbour-1];
		double upperpoint = thetapoints[upper_neighbour];
		double slope = (datapoints[upper_neighbour]-datapoints[upper_neighbour-1])/(upperpoint-lowerpoint);//slope
		double distance = thispoint - lowerpoint;              //distance from lower point
		double error_fac = distance/(upperpoint-lowerpoint);   //this appears in the error equation
		outcounts[pointno] = datapoints[upper_neighbour-1] + slope*distance;
		outvar[pointno] = varpoints[upper_neighbour-1]*(1-error_fac)*(1-error_fac) + varpoints[upper_neighbour]*error_fac;
		// Some debugging output
		if(pointno>100 && pointno < 200) {
			System.out.printf("%8.4f %8.4f(%8.4f) from (%8.4f,%8.4f) (%8.4f %8.4f)%n", thispoint,outcounts[pointno],outvar[pointno],
					lowerpoint,datapoints[upper_neighbour-1],upperpoint,datapoints[upper_neighbour]);
		}
	}
   }

   @Override
	public void signalExport(URI fileURI, Object signal) throws IOException {
		if (signal instanceof IGroup)
			resultExport(fileURI, (IGroup) signal);
	}

	@Override
	public void signalExport(URI fileURI, Object signal, String title)
	throws IOException {
		signalExport(fileURI, signal);
	}

	@Override
	public void signalExport(URI fileURI, Object signal, boolean isTranspose)
	throws IOException {
		signalExport(fileURI, signal);
	}	
	public boolean is1D() {return true; }
	public boolean is2D() {return false; } //but we can adapt to suit this as well
}
