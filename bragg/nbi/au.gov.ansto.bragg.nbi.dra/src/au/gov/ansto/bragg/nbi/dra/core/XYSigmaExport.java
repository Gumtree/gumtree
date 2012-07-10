package au.gov.ansto.bragg.nbi.dra.core;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.NoSuchElementException;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;

import au.gov.ansto.bragg.common.dra.algolib.math.BinTypeChange;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
/**
 *  This class implements a standard X,Y,Sigma export format
 *  @author jrh
 *  
 */ 
public class XYSigmaExport extends ConcreteProcessor {
   URI outputFilename = null;
   IGroup inputdata = null;
   IGroup outputdata = null;
   Boolean xyexport_Skip_Flag = true;
   Boolean xyexport_stop_flag = false;
   Boolean xyexport_sep_flag = false;   //Whether to split up multiple frames
   public final static DataStructureType dataStructureType = DataStructureType.plot;
   public final static DataDimensionType dataDimensionType = DataDimensionType.patternset;

   public Boolean process() throws Exception {
	   outputdata = inputdata;           //We don't actually alter the data
	   if (xyexport_Skip_Flag) return false;
	   IArray outputnumbers = ((NcGroup) inputdata).findSignal().getData();
	   int[] dataShape = outputnumbers.getShape();
	   int dataRank = outputnumbers.getRank();
	   String base_comment = inputdata.getLocation();
	   System.out.println("Now outputting 2theta-intensity file named "+outputFilename);
	   // For each dimension in the data, write a file
	   IArray tth_points = ((Plot) inputdata).getAxis(dataRank-1).getData();
	   int tth_rank = tth_points.getRank();
	   IArray err_array = ((Plot) inputdata).getVariance().getData();
	   if(tth_points.getShape()[tth_rank-1]==dataShape[dataRank-1]+1) {      //do bin boundary processing
		   tth_points = BinTypeChange.ToCentres(tth_points);
	   }
	   // Do some double-checking of two theta lengths...
	   if(tth_points.getShape()[tth_rank-1]!=dataShape[dataRank-1]) {
		   throw new Exception(String.format("Horizontal coordinate length %d does not match data length %d",
				   tth_points.getShape()[tth_rank-1],dataShape[dataRank-1]));
	   }
	   IArray second_axis = null;
	   String ax_name = "Run";
	   if(dataRank==2) {                                                     //get other axis
		   Axis scnd_ax = ((Plot) inputdata).getAxis(0);
		   second_axis = scnd_ax.getData();
		   ax_name = scnd_ax.getTitle();
	   } else {
		   second_axis = Factory.createArray(double.class, new int[] {1});
	   }
	   //Note that tth may be 2 dimensional but may also be single-dimensional
	   ISliceIterator tth_iter = tth_points.getSliceIterator(1);
	   ISliceIterator out_iter = outputnumbers.getSliceIterator(1);
	   ISliceIterator err_iter = err_array.getSliceIterator(1);
	   IArrayIterator scnd_iter = second_axis.getIterator();                   //iterate over other axis
	   int frame_ct = 0;                                                      //be sure that file is unique
	   PrintWriter outputfile = null;
	   if(!xyexport_sep_flag)
	       outputfile = new PrintWriter(new FileWriter(new File(outputFilename.getRawPath()+"_allframes"+".xyd")));
       while(out_iter.hasNext()) {
    	   IArray tth_row = null;
    	   try {
    		   tth_row = tth_iter.getArrayNext();
    	   } catch(NoSuchElementException e) {  //This might happen if horizontal coordinate is fixed for all frames
    		   tth_iter = tth_points.getSliceIterator(1);   //re-initialise
    		   tth_row = tth_iter.getArrayNext();
    	   }
    	   IArray data_row = out_iter.getArrayNext();
    	   IArray err_row = err_iter.getArrayNext();
    	   IArrayIterator row_iter = tth_row.getIterator();
    	   IArrayIterator data_iter = data_row.getIterator();
    	   IArrayIterator error_iter = err_row.getIterator();
    	   double scnd_loc = scnd_iter.getDoubleNext();
    	   frame_ct++;
    	   if(xyexport_sep_flag) 
    		   outputfile = new PrintWriter(new FileWriter(new File(outputFilename.getRawPath()+"_frame_"+String.valueOf(frame_ct)+".xyd")));
		   String topcomment = String.format(" "+ax_name+" value %f", scnd_loc);
		   outputfile.format("# %-79s%n",base_comment+topcomment);
//		   outputfile.format("# Two theta         Intensity          Sigma%n");
		   outputfile.format("# " + ((Plot) inputdata).getAxis(0).getTitle() + "         " + 
				   ((Plot) inputdata).findSingal().getTitle() + "         " + 
				   ((Plot) inputdata).getVariance().getShortName() + "\n");
		   while(row_iter.hasNext()) {
			   outputfile.format("%10.5f %10.5f %10.5f%n",row_iter.getDoubleNext(),data_iter.getDoubleNext(),
					   Math.sqrt(error_iter.getDoubleNext()));
		   }
		   outputfile.format("%n");  //one blank line between histograms - this is good for Gnuplot
		   if(xyexport_sep_flag) outputfile.close();
	   }
	   outputfile.close();
	   return false;
   }
   
   public void setXYinputdata(IGroup outputsignal){
	   inputdata = outputsignal;
   }
   
   /* Set the output file name.  We add an absolute path in case we have been given a relative value,
    * in which case the file goes to the user's home directory
    */
   
   public void setXYOutputFilename (URI filename) {
	   /* check readability */
	   if (filename == null || filename.getPath().trim().length() == 0){
		   String abs_path = System.getProperty("user.home");
		   outputFilename = new File(abs_path,filename.toString()).toURI();
		   xyexport_Skip_Flag = true;
		   return;
	   }
	   outputFilename = filename;
	   xyexport_Skip_Flag = false;
//	   try {
////		   outputFilename = new File(filename).toURI();
//	   } catch (IllegalArgumentException e) {      //not absolute path in File constructor
//		   String abs_path = System.getProperty("user.home");
//		   outputFilename = new File(abs_path,filename.toString()).toURI();  
//	   }
	   System.out.printf("XY data output to %s%n", outputFilename.toString());
   }
   public void setXY_Skip_Flag (Boolean new_val) {
	   xyexport_Skip_Flag = new_val;
   }
   public IGroup getXYOnwardData() {
	   return outputdata;   /* nothing changes */
   }
   
   public void setXY_sep_flag (Boolean sep_flag) {
	   xyexport_sep_flag = sep_flag;
   }
   
   public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}
}
