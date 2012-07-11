/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Template source file for setting up processor block interfaces
* 
* Contributors: 
*    Paul Hathaway - February 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;
import java.net.URI;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.DataAccessException;
import au.gov.ansto.bragg.datastructures.core.exception.PlotMathException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.nbi.dra.correction.DetectorMode;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

public class Blank_Background extends ConcreteProcessor {
	
	private static final String processClass = "Blank_Background"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009022302; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private Plot inPlot;
	private Plot outPlot;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;

	private String modeString = "monitor1";	
	private DetectorMode scaleMode = DetectorMode.MONITOR1;
	private URI bkgUri;
	
	private IGroup bkgData;
	
	public Blank_Background() {
		this.setReprocessable(false);
	}
	
	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	
    	stampProcessLog();
    	
		if (this.doForceSkip || (bkgUri==null)) {
			stampProcessSkip();			
		} else {
			if (bkgUri != null) {
				bkgData = NexusUtils.getNexusData(bkgUri);
			} else {
				stampProcessEntry("Background data URI not found");
				throw new IOException("Background data URI not found");
			}
			correctBackground();
			stampProcessEnd();
		}
		super.informVarValueChange("scatterBackgroundUri", bkgUri);
		return doForceStop;
	}

	private void correctBackground() throws DataAccessException, PlotMathException {
		double atten_sam = 1.0;
		double atten_bkg = 1.0;
		double atten_rot = 0.0;
		double factor_sam = 1.0;
		double factor_bkg = 1.0;
		double mRef = 1e8;
		
		double bkg_scaler = 1.0;
		double sam_scaler = 1.0;
		double max_scaler = 1e8;
		
		String monString;
		String samString;
		
		final double defaultWavelength = 5.0;
		final double atten_min = DataLib.lookupAttFactor(5.0,330.0);
		
		/* find attenuation factors */
		try{
//			atten_bkg = bkgData.getParentGroup()
//						.findGroup("instrument")
//						.findGroup("parameters")
//						.findGroup("derived_parameters")
//						.findDataItem("AttFactor").getData().getMaximum();
//			if (atten_bkg < atten_min) {
				atten_rot = bkgData.getParentGroup()
					.findGroup("instrument")
					.findGroup("collimator")
					.findDataItem("att").getData().getArrayMath().getMaximum();
				atten_bkg = DataLib.lookupAttFactor(defaultWavelength,atten_rot);
//			}
		} catch (Exception e) {
			throw new DataAccessException("Cannot access background dataset");
		}

		try {
//			atten_sam = inPlot.getDataItem("AttFactor").getData().getMaximum();
//			if (atten_sam < atten_min) {
				atten_rot = inPlot.getDataItem("AttRotDeg").getData().getArrayMath().getMaximum();
				atten_sam = DataLib.lookupAttFactor(defaultWavelength,atten_rot);
//			}
		} catch (IOException e) {
			throw new DataAccessException("Cannot access sample dataset");
		}
		
		/* find scale factors */
		
		switch (scaleMode) {
			case TIME:
				try{
					factor_bkg = bkgData.getParentGroup()
						.findGroup("instrument")
						.findGroup("detector")
						.findDataItem("time").getData().getArrayMath().getMaximum();
					if (factor_bkg < 1.0)
						factor_bkg = bkgData.getParentGroup()
						.findGroup("instrument")
						.findGroup("detector")
						.findDataItem("preset").getData().getArrayMath().getMaximum();
				} catch (Exception e) {
					throw new DataAccessException("Cannot find time stamp background dataset");
				}
				samString = "detector_time";
				break;
				
			case MONITOR1:
			case MONITOR2:
			case MONITOR3:
				switch (scaleMode) {
					case MONITOR1: monString = "bm1_counts"; break;
					case MONITOR2: monString = "bm2_counts"; break;
					case MONITOR3: monString = "bm3_counts"; break;
					default: throw new IllegalArgumentException("Unknown scaling mode");
				}
				try{
					factor_bkg = bkgData.getParentGroup()
						.findGroup("monitor")
						.findDataItem(monString).getData().getArrayMath().getMaximum();
				} catch (Exception e) {
					throw new DataAccessException("Cannot find "+monString+" in background dataset");
				}
				samString = monString;
				break;
				
			case DETECTOR_TOTAL:
				try{
					factor_bkg = bkgData.getParentGroup()
						.findGroup("instrument")
						.findGroup("detector")
						.findDataItem("total_counts").getData().getArrayMath().getMaximum();
				} catch (Exception e) {
					throw new DataAccessException("Cannot find total count background dataset");
				}
				samString = "total_counts";
				break;
				
    		default:
    			throw new IllegalArgumentException(
    					"Invalid scaling mode detected instead of {TIME|MONITORn|DETECTOR_TOTAL}");				
		}

		try {
			factor_sam = inPlot.getDataItem(samString).getData().getArrayMath().getMaximum();
		} catch (IOException e) {
			throw new DataAccessException("Cannot access sample dataset");
		}					
		if ((factor_bkg<1.0)||(factor_sam<1.0)) {
			throw new DataAccessException("Invalid scaling factor in one dataset (<1.0)");
		} 

		/**
		 * If normalising to 1e8 monitor counts, 
		 * 	set mRef to 1e8
		 * else normalising to sample scatter measurement monitor counts
		 *  set mRef to factor_sam
		 *  
		 *  In this implementation, set mRef=factor_sam
		 */
		mRef = factor_sam; 
		bkg_scaler = 0.0 - Math.abs(mRef / factor_bkg / atten_bkg);
		sam_scaler = mRef / factor_sam / atten_sam;

		if ((sam_scaler>max_scaler)||(Math.abs(bkg_scaler)>max_scaler)) {
			throw new DataAccessException("Invalid scaling factor in one dataset");
		}
		
		
		try {
			/**
			 *  TODO: Review this method when ucar.netcdf4 library usage stable
			 *  
			 *  original line:
			 *  outPlot = inPlot.toScale(sam_scaler,0.0)
			 *  	.toAdd(bkgData.getSignalArray().reduce().toScale(bkg_scaler));
			 */
			IArray bkgArr = ((NcGroup) bkgData).getSignalArray().getArrayUtils().reduce().getArray();

			// bkgArray is int need to set to double - done here with scaling
			IArray bkgArrDbl = bkgArr.getArrayMath().toScale(bkg_scaler).getArray();
			
			IDataItem data = inPlot.findSingal();
			
			IArray workArr = data.getData();
			workArr.getArrayMath().scale(sam_scaler);
			workArr.getArrayMath().add(bkgArrDbl);
			/* .setCachedData required by ucar.netcdf4*/
			data.setCachedData(workArr, false);
			
			/* TODO: add scaleMode or modeString into audit trail and data structure */
			stampProcessEntry("scale mode = "+modeString);
		} catch (Exception e) {
			throw new PlotMathException (e);
		}
	}

	private void stampProcessLog() {
		outPlot.addProcessingLog("ProcessClass/Version/ID: ["
				+processClass+";"
				+processClassVersion+";"
				+processClassID+"]");		
	}
	
	private void stampProcessSkip() {
		outPlot.addProcessingLog("["+processClassID+"]:"+"SKIP process");		
	}
	
	private void stampProcessEntry(String logEntry) {
		outPlot.addProcessingLog("["+processClassID+"]: "+logEntry);		
	}
	
	private void stampProcessEnd() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]:"+"END process");		
	}
	
	/* Client Support methods -----------------------------------------*/
	
	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public static DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	public void setIsDebugMode(Boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}

	public Boolean getIsDebugMode() {
		return isDebugMode;
	}	
	
	/* Port get/set methods -----------------------------------------*/
    
    /* In-Ports -----------------------------------------------------*/

	public void setInPlot(Plot inPlot) {
		this.inPlot = inPlot;
	}

    /* Out-Ports ----------------------------------------------------*/

	public Plot getOutPlot() {
		return this.outPlot;
	}    

	/* Var-Ports (options) ------------------------------------------*/

	public Boolean getSkip() {
		return doForceSkip;
	}

	public void setSkip(Boolean doForceSkip) {
		this.doForceSkip = doForceSkip;
	}
	
	public Boolean getStop() {
		return doForceStop;
	}

	public void setStop(Boolean doForceStop) {
		this.doForceStop = doForceStop;
	}

    /* Var-Ports (tuners) -------------------------------------------*/

	public void setScaleMode(String modeString) {
		this.modeString = modeString;
		this.scaleMode = DetectorMode.getInstance(modeString);
	}

	public void setScatterBackgroundUri(URI bkgFile) {
		this.bkgUri = bkgFile;
	}
}

