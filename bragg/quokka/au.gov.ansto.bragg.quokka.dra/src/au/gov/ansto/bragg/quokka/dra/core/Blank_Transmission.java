/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Template source file for setting up processor block interfaces
* 
* Contributors: 
*    Paul Hathaway - February 2009
*    pvh modified July 2009, making background term in correction optional
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;
import java.net.URI;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.DataAccessException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

/**
 * Use Cases:
 * UC-1 Both Transmission measurements (sample and cell) and Background measurement available
 * UC-2 Both Transmission measurements (sample and cell) available; Background measurement not available
 * UC-3 Only one Transmission measurement (sample or cell) available
 * UC-4 Neither Transmission measurements available
 *
 * UC-1 is the standard implementation.
 * In UC-2 the whole background term is set to zero.
 * In UC-3 and UC-4 no processing is done in this processor.
 *
 *  here 'sam' subscript denotes data set available from processing chain
 *      'ref' is reference for normalisation
 *            - NIST macros use monRef = 1e8 monitor counts
 *            - to date, ANSTO uses the 'sam' data set as reference
 *            
 */
public class Blank_Transmission extends ConcreteProcessor {
	
	private static final String processClass = "Blank_Transmission"; 
	private static final String processClassVersion = "2.0"; 
	private static final long processClassID = 2009030201; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private Plot inPlot;
	private Plot outPlot;
	private Plot display;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;

	private URI masterReportUri;
	private URI txSampleUri;
	private URI txEmptyUri;         // transmission of empty cell
	private URI scatterEmptyUri;    
	private URI scatterBackgroundUri;
	private URI centroidUri;
	
	/* Dictionary keys */
	private final static String KEY_WAVELENGTH = "LambdaA";
	private final static String KEY_ATTFACTOR = "AttFactor";
	private final static String KEY_ATTROTDEG = "AttRotDeg";
	private final static String KEY_DATA = "data";

	/* Standard labels for calculation placeholders */
	private static final String LBL_NORMTXSAMPLESUM = "normTxSampleSum";
	private static final String LBL_NORMTXEMPTYSUM = "normTxEmptySum";

	private String monString = "bm1_counts";
	private Double defaultWavelength = 5.0; // angstrom
	private double minWavelength = 0.1; // angstrom
	
	public Blank_Transmission() {
		this.setReprocessable(false);
	}
	
	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	this.display = this.inPlot;
    	
    	stampProcessLog(outPlot);
    	
		if (this.doForceSkip || (!isVarsSet())) {
			stampProcessSkip(outPlot);			
		} else {
			correctTransmission();
			stampProcessEnd(outPlot);
		}
		return doForceStop;
	}

	/** 
	 *  Current protocol dictates that both transmission data sets be accessible before
	 *  the Empty Cell (Transmission) correction is made
	 *  
	 *  The presence of the background measurement term is optional. 
	 *  
	 * @return true if required parameters are set
	 */
	private Boolean isVarsSet() {
		Boolean ok = (null!=txSampleUri)
			&&(null!=txEmptyUri)
			&&(null!=scatterEmptyUri);
		if (ok) {
			try {
				ok = ok && (null!=DataLib.fetchEntry(txSampleUri))
				   		&& (null!=DataLib.fetchEntry(txEmptyUri))
						&& (null!=DataLib.fetchEntry(scatterEmptyUri));
			} catch (DataAccessException e) {
				ok = false;
			}
		}
		return ok;
	}
	
	private void correctTransmission() throws DataAccessException {
		
		IGroup empEntry = DataLib.fetchEntry(scatterEmptyUri);
		double monEmp = DataLib.fetchMonitor(empEntry);
		double attEmp = DataLib.fetchAtten(empEntry);
		
		Boolean doBackgroundTerm = (null!=scatterBackgroundUri);
		
		try {
        /**
         *  here 'sam' subscript denotes data set available from processing chain
         *      'ref' is reference for normalisation
         *            - NIST macros use monRef = 1e8 monitor counts
         *            - to date, ANSTO uses the 'sam' data set as reference
         *            
         * Use Cases (UC-x)
         * UC-1 Both Transmission measurements (sample and cell) and Background measurement available
         * UC-2 Both Transmission measurements (sample and cell) available; Background measurement not available
         * UC-3 Only one Transmission measurement (sample or cell) available
         * UC-4 Neither Transmission measurements available
         *
         * UC-1 is the standard implementation.
         * In UC-2 the whole background term is set to zero.
         * In UC-3 and UC-4 no processing is done in this processor.
         */
			IArray monitor = inPlot.getDataItem(monString).getData();
			double monSam = monitor.getArrayMath().getMaximum();
			double monRef = monSam;
			
//			double attSam = inPlot.getDataItem(KEY_ATTFACTOR).getData().getMaximum();
			double attSam = 1.0;
//			if (attSam < DataLib.lookupAttFactor(5.0,330.0)) { 
				// i.e. place-holder not set to valid value
				double atten_rot = inPlot.getDataItem(KEY_ATTROTDEG).getData().getArrayMath().getMaximum();
				double wavelength = inPlot.getDataItem(KEY_WAVELENGTH).getData().getArrayMath().getMaximum();
				if (minWavelength > wavelength) { 
					// i.e. place-holder not set to valid value
					wavelength = defaultWavelength;
				}
				attSam = DataLib.lookupAttFactor(wavelength,atten_rot);
//			}
	
			IDataItem data = inPlot.findSingal();
			IArray Isam = inPlot.findSignalArray();
			if (null==Isam) { throw new DataAccessException("Unable to access signal data"); }
	
			IArray Iemp = NexusUtils.getNexusSignal(NexusUtils.getNexusData(empEntry)).getData().getArrayUtils().reduce().getArray();
			if (null==Iemp) { throw new DataAccessException("Unable to access empty cell data"); }
	
			double normTxSampleSum = DataLib.fetchTransmission(txSampleUri);
			double normTxEmptySum  = DataLib.fetchTransmission(txEmptyUri);
			double txRatio = -1.0 * normTxSampleSum / normTxEmptySum;
			
			/* use array variables so that they can be cached in Plot structure */
			outPlot.addCalculationData(LBL_NORMTXSAMPLESUM, 
					Factory.createArray(new double[] {normTxSampleSum}),
					LBL_NORMTXSAMPLESUM,
					"count");
			outPlot.addCalculationData(LBL_NORMTXEMPTYSUM, 
					Factory.createArray(new double[] {normTxEmptySum}),
					LBL_NORMTXEMPTYSUM,
					"count");

			/*
			 *  Icor = Isam - (Tsam/Temp)(Iemp - Ibkg)
			 *  where terms have been suitably pre-scaled
			 *  
			 *  ensure that results are (double) floating point
			 *  as sourced data may be integer data
			 */
			Iemp = Iemp.getArrayMath().toScale(       monRef / (attEmp * monEmp)).getArray();
			Isam.getArrayMath().scale(       monRef / (attSam * monSam));
		
			/*  If Isam references same storage as outPlot, 
			 *  this assignment updates outPlot data
			 *  
			 *  However, ucar.netcdf4 only provides handle to copy of cache,
			 *  so cache (via 'data' needs to be replaced manually.
			 *
			 *  TODO: Review when ucar.netcdf4 stabilised
			 *  
			 *  Working result:
			 *  Isam.add( (Iemp.add(Ibkg)).scale(txRatio) );
			 *  
			 *  the equivalent in three-step sequence:
			 */

			if (doBackgroundTerm) {
				IGroup bkgEntry = DataLib.fetchEntry(scatterBackgroundUri);
				double monBkg = DataLib.fetchMonitor(bkgEntry);
				double attBkg = 1.0;
				
				if ((null!=bkgEntry) && (0.0 < monBkg)) {
					IArray Ibkg = NexusUtils.getNexusSignal(NexusUtils.getNexusData(bkgEntry)).getData().getArrayUtils().reduce().getArray();
					if (null==Ibkg) { throw new DataAccessException("Unable to access background data"); }

					attBkg = DataLib.fetchAtten(bkgEntry);
					Ibkg = Ibkg.getArrayMath().toScale(-1.0 * monRef / (attBkg * monBkg)).getArray();
					Iemp.getArrayMath().add(Ibkg);
				}
			}
			Iemp.getArrayMath().scale(txRatio);
			Isam.getArrayMath().add(Iemp);
			
			data.setCachedData(Isam, false); // manually update cache (see note above)
			
		} catch (IOException ioe) {
			throw new DataAccessException(ioe);
		} catch (ShapeNotMatchException snme) {
			throw new DataAccessException(snme);
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
	}

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

	public Plot getDisplay() {
		this.display = this.inPlot;
		URI refUri = this.centroidUri;
		if (null!=refUri) {
			try {
				IGroup dataGrp = NexusUtils.getNexusData(refUri);
				display = (Plot) PlotFactory.copyToPlot(dataGrp,KEY_DATA,DataDimensionType.map);
			} catch (Exception e) {
				if (isDebugMode) e.printStackTrace();
				this.display = this.inPlot;
			}
		}
		String title = "Beam Reference";
		String runnum = DataLib.getRunNum(display);
		if (null!=runnum) {
			title = title.concat(" ("+runnum+")");
		}
		String config = DataLib.getKeyParameter(display,DataLib.KEY_CONFIG);
		if (null!=config) {
			title = title.concat(" ["+config+"]");
		}
		display.setTitle(title);
		return this.display;
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

	public void setTxSampleUri(URI txSampleUri) {
		if ((this.txSampleUri == null && txSampleUri != null) || 
				this.txSampleUri != null && !this.txSampleUri.equals(txSampleUri)){
			this.txSampleUri = txSampleUri;
			informVarValueChange("txSampleUri", txSampleUri);
		}
	}

	public void setTxEmptyUri(URI txEmptyUri) {
		if ((this.txEmptyUri == null && txEmptyUri != null) || 
				this.txEmptyUri != null && !this.txEmptyUri.equals(txEmptyUri)){
			this.txEmptyUri = txEmptyUri;
			informVarValueChange("txEmptyUri", txEmptyUri);
		}
	}

	public void setScatterEmptyUri(URI scatterEmptyUri) {
		if ((this.scatterEmptyUri == null && scatterEmptyUri != null) || 
				this.scatterEmptyUri != null && !this.scatterEmptyUri.equals(scatterEmptyUri)){
			this.scatterEmptyUri = scatterEmptyUri;
			informVarValueChange("scatterEmptyUri", scatterEmptyUri);
		}
	}

	public void setScatterBackgroundUri(URI scatterBackgroundUri) {
		if ((this.scatterBackgroundUri == null && scatterBackgroundUri != null) || 
				this.scatterBackgroundUri != null && !this.scatterBackgroundUri.equals(scatterBackgroundUri)){
			this.scatterBackgroundUri = scatterBackgroundUri;
			informVarValueChange("scatterBackgroundUri", scatterBackgroundUri);
		}
	}

	public void setCentroidUri(URI centroidUri) {
		if ((this.centroidUri == null && centroidUri != null) || 
				this.centroidUri != null && !this.centroidUri.equals(centroidUri)){
			this.centroidUri = centroidUri;
			informVarValueChange("centroidUri", centroidUri);
		}
	}

	/* Dummy setter to broadcast changes to centroid ROI */
	public void setCentroidRoi(IGroup centroidRoi) {
		/* TODO: validate as mask */
		super.informVarValueChange("centroidX", centroidRoi);
	}

	public void setMasterReportUri(URI masterReportUri) {
		this.masterReportUri = masterReportUri;
	}

	public URI getMasterReportUri() {
		return masterReportUri;
	}
	
	
}
