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

import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.utils.Utilities;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.DataAccessException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class Blank_Efficiency extends ConcreteProcessor {
	
	private static final String processClass = "Blank_Efficiency"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009022304; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private Plot inPlot;
	private Plot outPlot;
	private Boolean doEnable = true;
	private Boolean doForceStop = false;
	
//	private final static String KEY_WAVELENGTH = "LambdaA";
	private URI mapUri;
	
	public Blank_Efficiency() {
		this.setReprocessable(false);
	}
	
	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	
    	//stampProcessLog(inPlot);
    	
		if (!this.doEnable) {
			stampProcessSkip(inPlot);			
		} else {
			if(isVarsSet()) {
				applyEfficiency();
				stampProcessEnd(inPlot);
			}
		}
		return doForceStop;
	}
	private Boolean isVarsSet() {
		Boolean ok = (null!=mapUri);
		if (ok) {
			// mapUri no standard nexus file, cannot use DataLib methods
			//try {
				// ok = ok && (null!=DataLib.fetchEntry(mapUri));
			//} catch (DataAccessException e) {
			//	ok = false;
			//}
		}
		return ok;
	}
	
	private IArray fetchEfficiency() throws FileAccessException, IOException {
		IArray iEff = null;
		try {
			IDataItem item = NexusUtils.getNexusSignal(NexusUtils.getNexusData(NexusUtils.getNexusEntry(mapUri)));
			iEff = item.getData();
		} catch (StructureTypeException e) {
			throw new FileAccessException("can not read from sensitivity file", e);
		}
//		DataItem item = data.findDataItem("div");
		return iEff; 
		// root.findGroup("data").findDataItem("div").getData();
	}
	
	private void applyEfficiency() throws DataAccessException {
//		Group mapEntry = DataLib.fetchEntry(mapUri);
//		if (null==mapEntry) { 
//			throw new DataAccessException("Unable to access signal data"); 
//		}	
		IArray Ieff;
		
		try {
			//Group data = NexusUtils.getNexusData(mapEntry);
			//Array Imap = data.getSignalArray();
			IArray Imap = fetchEfficiency();
			Imap = Imap.getArrayUtils().reduce().getArray();
			Double mapSum = Imap.getArrayMath().sum();
			Double mapAvg = mapSum/((double)Imap.getSize());
			Ieff = Utilities.copyToDoubleArray(Imap);
			Ieff.getArrayMath().eltInverseSkipZero();
			Ieff.getArrayMath().scale(mapAvg);
//		} catch (StructureTypeException e) {
//			throw new DataAccessException(e);
//		} catch (SignalNotAvailableException e) {
//			e.printStackTrace();
//			throw new DataAccessException("Unable to access signal data"); 
		} catch (FileAccessException e) {
			e.printStackTrace();
			throw new DataAccessException("Unable to access signal data"); 
		} catch (IOException e) {
			throw new DataAccessException("Unable to access signal data"); 
		}

		try { /* Check handle to data and apply efficiency matrix */
			IArray Icor = ((NcGroup) inPlot).getSignalArray(); 
			// Raw ucar.netcdf4 provides a copy of cached signal item
			if (null==Icor) { 
				throw new DataAccessException("Unable to access signal data"); 
			}	
			//outPlot = inPlot.eltMultiply(Ieff); //not raw ucar4 compatible
			IDataItem dataCor = inPlot.findSingal();			
			Icor.getArrayMath().eltMultiply(Ieff);
			dataCor.setCachedData(Icor, false);
			
		//} catch (PlotMathException e) {
		//	e.printStackTrace();
		//	throw new DataAccessException("Plot multiply failed",e);
		} catch (SignalNotAvailableException e) {
			e.printStackTrace();
			throw new DataAccessException("Unable to access signal data"); 
		} catch (ShapeNotMatchException e) {
			e.printStackTrace();
			throw new DataAccessException("Unable to access signal data"); 
		} catch (InvalidArrayTypeException e) {
			e.printStackTrace();
			throw new DataAccessException("Unable to access signal data"); 
		}
	}
	
	/* Methods for audit trail support - plan to promote these to super class methods */
	private void stampProcessLog(Plot plot) {
		plot.addProcessingLog("ProcessClass/Version/ID: ["
				+processClass+";"
				+processClassVersion+";"
				+processClassID+"]");		
	}
	
	private void stampProcessSkip(Plot plot) {
		plot.addProcessingLog("["+processClassID+"]:"+"SKIP process");		
	}
	
	private void stampProcessEntry(Plot plot, String logEntry) {
		plot.addProcessingLog("["+processClassID+"]: "+logEntry);		
	}
	
	private void stampProcessEnd(Plot plot) {
		plot.addProcessingLog("["+processClassID+"]:"+"END process");		
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

	public Boolean getEnable() {
		return doEnable;
	}

	public void setEnable(Boolean doEnable) {
		this.doEnable = doEnable;
	}
	
	public Boolean getStop() {
		return doForceStop;
	}

	public void setStop(Boolean doForceStop) {
		this.doForceStop = doForceStop;
	}

    /* Var-Ports (tuners) -------------------------------------------*/

	public void setMapURI(URI mapURI) {
		this.mapUri = mapURI;
		informVarValueChange("mapURI", mapURI);
	}

}
