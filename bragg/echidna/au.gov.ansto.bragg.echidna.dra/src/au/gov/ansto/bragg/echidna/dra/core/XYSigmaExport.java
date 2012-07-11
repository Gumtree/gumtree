package au.gov.ansto.bragg.echidna.dra.core;

import au.gov.ansto.bragg.datastructures.core.plot.*;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

import java.io.*;
import java.net.URI;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
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
   
   public Boolean process() throws Exception {
	   outputdata = inputdata;           //We don't actually alter the data
	   if (xyexport_Skip_Flag) return false;
	   System.out.println("Now outputting 2theta-intensity file");
	   PrintWriter outputfile = new PrintWriter(new FileWriter(new File(outputFilename)));
	   IArray outputnumbers = ((NcGroup) inputdata).findSignal().getData();
	   String topcomment = inputdata.getLocation();
	   double [] output_dbl = ConverterLib.get1DDouble(outputnumbers);
	   IArray tth_points = ((Plot) inputdata).getAxis(0).getData();
	   double [] out2th = ConverterLib.get1DDouble(tth_points);
	   IArray err_array = ((Plot) inputdata).getVariance().getData();
	   double [] err_dbl = ConverterLib.get1DDouble(err_array);
	   outputfile.format("# %79s%n",topcomment);
	   outputfile.format("# Two theta         Intensity          Sigma%n");
	   for(int i=0;i<out2th.length;i++) {
		   outputfile.format("%10.4f %10.4f %10.4f%n",out2th[i],output_dbl[i],Math.sqrt(err_dbl[i]));
	   }
	   outputfile.format("%n");
	   outputfile.close();
	   return false;
   }
   public void setXYinputdata(IGroup outputsignal){
	   inputdata = outputsignal;
   }
   public void setXYOutputFilename (URI filename) {
	   /* check readability */
	   try {
		   outputFilename = new File(filename).toURI();
	   } catch (IllegalArgumentException e) {      //not absolute path in File constructor
		   String abs_path = System.getProperty("user.home");
		   outputFilename = new File(abs_path,filename.toString()).toURI();  
	   }
   }
   public void setXY_Skip_Flag (Boolean new_val) {
	   xyexport_Skip_Flag = new_val;
   }
   public IGroup getXYOnwardData() {
	   return outputdata;   /* nothing changes */
   }
   
   public DataStructureType getDataStructureType() {
		return DataStructureType.undefined;
	}
	public DataDimensionType getDataDimensionType() {
		return DataDimensionType.undefined;
	}
}
