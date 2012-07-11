package au.gov.ansto.bragg.echidna.dra.core;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

import java.io.*;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
/**
 *  This class implements a GSAS format exporter for powder data
 *  @author jrh
 *  
 */ 
public class GsasExport extends ConcreteProcessor {
   String outputFilename = null;
   IGroup inputdata = null;
   Boolean GSAS_Skip_Flag = true;
   Boolean GSAS_tth_wrong = false;
   
   public Boolean process() throws Exception {
	   if (GSAS_Skip_Flag) return false;
	   System.out.println("Now outputting GSAS file");
	   PrintWriter outputfile = new PrintWriter(new FileWriter(outputFilename));
	   IArray outputnumbers = ((NcGroup) inputdata).findSignal().getData();
	   String topcomment = inputdata.getLocation();
	   double [] output_dbl = ConverterLib.get1DDouble(outputnumbers);
	   IArray tth_points = inputdata.findDataItem("twoTheta_vector").getData();
	   double [] out2th = ConverterLib.get1DDouble(tth_points);
	   Integer no_points = tth_points.getShape()[0];
	   Integer extra = no_points % 5;   /* GSAS has 5 points per line */
	   Integer nlines = no_points / 5;  /* GSAS has 5 points per line */
	   if(extra > 0) nlines +=1;        /* for the last line */
	   double stepsize = (out2th[no_points-1] - out2th[0])/((double) (no_points-1));
	   double start_tth = out2th[0];
	   /* As of March 2008 the tth axis orientation is fixed during stitching, so this reversal
	    * is no longer necessary
	    */
	   if (GSAS_tth_wrong) {
		   stepsize *= -1.0;
		   start_tth = out2th[no_points-1];
	   }
	   IArray err_array = inputdata.findDataItem("variance").getData();
	   double [] err_dbl = ConverterLib.get1DDouble(err_array);
	   outputfile.format("%79s%n",topcomment);
	   outputfile.format("BANK %2d %5d %5d CONST %10.3f %7.5f 0 0 ESD%n",
			   1,no_points,nlines,start_tth*100.0,stepsize*100.0);
	   for(int i=0;i<no_points;i++) {
		   outputfile.format("%8.2f%8.2f",output_dbl[i],Math.sqrt(err_dbl[i]));
		   if((i+1)%5==0) outputfile.format("%n");
	   }
	   outputfile.format("%n");
	   outputfile.close();
	   return false;
   }
   public void setGSASinputdata(IGroup outputsignal){
	   inputdata = outputsignal;
   }
   public void setGSASOutputFilename (String filename) {
	   /* check readability */
	   File location = new File(filename);
	   if(!location.isAbsolute()) {
		   String abs_path = System.getProperty("user.home");
		   outputFilename = new File(abs_path,filename).getPath();
	   }
	   else outputFilename = filename;
   }
   public void setGSAS_Skip_Flag (Boolean new_val) {
	   GSAS_Skip_Flag = false;
   }
   public void setGSAS_tth_wrong (Boolean new_val) {
	   GSAS_tth_wrong = new_val;
   }
   public IGroup getGSASOnwardData() {
	   return inputdata;   /* nothing changes */
   }
public DataDimensionType getDataDimensionType() {
	// TODO Auto-generated method stub
	return DataDimensionType.undefined;
}
public DataStructureType getDataStructureType() {
	// TODO Auto-generated method stub
	return DataStructureType.undefined;
}
}
