/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - February 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;
import java.net.URI;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.DataAccessException;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

public class Blank_Scaler extends ConcreteProcessor {
	
	/* Fields for audit trail support */
	private static final String processClass = "Blank_Scaler"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009030501; 

	/* Fields to support client-side processing */
	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

    /* Input, Output and Option Var Ports*/
	private Plot inPlot;
	private Plot outPlot;
	private Boolean doEnable = true;
	private Boolean doForceStop = false;
	
	/* Key strings required to access dictionary values via Activator */
	private static final String KEY_WAVELENGTH = "LambdaA";
	private static final String KEY_L2MM = "L2mm";
	private static final String KEY_MONITOR1 = "bm1_counts";
	private static final String KEY_ATTFACTOR = "AttFactor";	
	private static final String KEY_ATTROTDEG = "AttRotDeg";

	/* Standard labels for calculation placeholders */
	private static final String LBL_NORMTXSAMPLESUM = "normTxSampleSum";
	private static final String LBL_NORMTXEMPTYSUM = "normTxEmptySum";
	private static final String LBL_NORMTXDIRECTSUM = "normTxDirectSum";
	private static final String LBL_NORMTXSTANDARDSUM = "normTxStandardSum";
	private static final String LBL_KAPPA = "kappa";
	private static final String LBL_STDCROSSSECTION = "stdCrossSection";
	private static final String LBL_NORMSTDSUMATQ0 = "normStdSumAtQ0";
	private static final String LBL_ABSTXEMPTYCELL = "absTxEmptyCell";
	private static final String LBL_ABSTXSAMPLE = "absTxSample";
	
	/*  If (useStdSample) then refURI=stdURI standard sample
	 *                    else refUri=dirURI direct beam 
	 */
	private Boolean useStdSample = false;
	private IGroup  centroidRoi = null;
	
	private URI stdUri;
	private URI dirUri;
	
	private double detBinWidthX = 5.08; //mm
	private double detBinWidthZ = 5.08; //mm
	
	private Double L2mm = 2000.0; //mm	
	private Double kappa = 1.0;
	private Double stdCrossSection;
	private Double samDepth = 1.0; // in CENTIMETRES
	private Double stdDepth = 1.0;
	
	private URI refUri;
	/* refMon is 1e8 since standard (NIST) kappa is normalised to Mo=1e8 */
	private Double refMon = DataLib.defaultMonitorCount; // 1.0e8 monitor counts;
	private Double calMon;
	private Double calAtt;
	private Double dirSum;

	public Blank_Scaler() {
		this.setReprocessable(false);
	}
	
	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	
    	stampProcessLog(outPlot);
//    	doExport();
		if (this.doEnable && isVarsSet()) {
        	switch (inPlot.getDimensionType())
        	{
        		case map:
        			// is compatible for 2D processes
        			if (this.useStdSample) {
        				refUri = stdUri;
        			} else {
        				refUri = dirUri;
        			}
        			fetchParameters();
        			doScaleAbsolute(refUri);
           			calcAbsoluteTransmissions();
           			super.informVarValueChange("kappa", kappa);

        			break;
        		case mapset:
        		default:
        			throw new IllegalArgumentException(
       					"DataDimensionType of 'map' required.");
        	}        	
        	stampProcessEnd(outPlot);
		} else {
			stampProcessSkip(outPlot);			
		}
		return doForceStop;
	}

	private Boolean isVarsSet() {
		/*
		 *  If (useStdSample) need standard sample URI to proceed
		 *  else need direct URI for direct beam scaling
		 */
		boolean ok;
		if (this.useStdSample) {
			ok = (null!=stdUri);
		} else {
			ok = (null!=dirUri);
		}
		/* TODO: check ADDITIONAL VARS SET 
		 * 	samDepth
		 *  stdDepth
		 *  
		 *  stdCrossSection
		 *  
		 * 
		 */
		return ok;
	}
	
	private void fetchParameters() throws IOException {
		this.detBinWidthX = 5.08; //mm
		this.detBinWidthZ = 5.08; //mm
		this.L2mm = (inPlot.getDataItem(KEY_L2MM)).readScalarDouble();
		// absolute scaling convention displays Q in (1/cm)
		// samDepth converts plot sample.thickness in mm to cm 
		this.samDepth = 0.1 * DataLib.readInputMetaData(inPlot, DataLib.KEY_SAMPLEDEPTH, this.samDepth);
		this.calMon = (inPlot.getDataItem(KEY_MONITOR1)).readScalarDouble();
//		this.calAtt = (inPlot.getDataItem(KEY_ATTFACTOR)).readScalarDouble();
		this.calAtt = 1.0;
//		if (this.calAtt < DataLib.lookupAttFactor(5.0,330.0)) { 
			// i.e. place-holder not set to valid value
			double atten_rot = inPlot.getDataItem(KEY_ATTROTDEG).getData().getArrayMath().getMaximum();
			double wavelength = inPlot.getDataItem(KEY_WAVELENGTH).getData().getArrayMath().getMaximum();
			if (DataLib.minWavelength > wavelength) { 
				// i.e. place-holder not set to valid value
				wavelength = DataLib.defaultWavelength;
			}
			this.calAtt = DataLib.lookupAttFactor(wavelength,atten_rot);
//		}

	}
	
	/**
	 * 	 Absolute scaling of working dataset:
	 *  
	 *   Iabs = Ical * prescale * methodScaler * dRatio * txRatio ; 
	 *   
	 *   where
	 *   		prescale scales for attenuation and 
	 *   				 normalises to refMon monitor counts
	 *   
	 *   		methodScaler depends on absolute scaling method:
	 *   				 standard sample of known cross section, or
	 *   				 direct beam measurement -> kappa
	 *   
	 *   		dRatio   method-dependent sample depth ratio
	 *   
	 *   		txRatio  method-dependent transmission ratio
	 *
	 * @param  refUri - refers to standard sample or direct beam measurement 
	 * @throws DataAccessException
	 * 
	 */
	private void doScaleAbsolute(URI refUri) throws DataAccessException {
		IGroup refEntry;
		String refString;
		double prescale = 1.0;
		double methodScaler = 1.0;
		double txRatio = 1.0;
		double dRatio = 1.0;
		double scaleFactor = prescale * methodScaler * dRatio * txRatio;
		Double normTxSampleSum;
		Double normTxRefSum;
		Double normStdSumAtQ0;
		
		try {
			prescale = this.refMon / (this.calAtt * this.calMon);
			Data data = inPlot.findCalculationData(LBL_NORMTXSAMPLESUM);
			if (null==data) { 
				normTxSampleSum = -1.0; 
			} else {
				normTxSampleSum = data.readScalarDouble();
				if (null==normTxSampleSum) {
					normTxSampleSum = -1.0;
				}
			}
		} catch (IOException e) {
			throw new DataAccessException(e);
		}
		
		if (refUri==this.dirUri) { 
			// use direct (empty) beam method for absolute scaling
			refString = LBL_NORMTXDIRECTSUM; 
			refEntry = DataLib.fetchEntry(refUri);
			this.dirSum = DataLib.calcNormSum(refEntry);

			this.kappa = calcKappa(refEntry);
			methodScaler = 1.0 / kappa;
			dRatio = 1.0 / samDepth;

			normTxRefSum  = DataLib.calcNormSum(refEntry);
			if(0>normTxSampleSum) { // ie invalid
				txRatio = 1.0;
			} else {
				txRatio = normTxRefSum / normTxSampleSum;				
			}
			try {
				outPlot.addCalculationData(LBL_KAPPA,
						Factory.createArray(new double[] {this.kappa}),
						LBL_KAPPA,
						"count/count");

				IArray Iref = Factory.createArray(new double[] {normTxRefSum});
				outPlot.addCalculationData(refString,
						Iref, //Factory.createArray(new double[] {normTxRefSum}),
						refString,
						"count");

			} catch (InvalidArrayTypeException e) {
				throw new DataAccessException("Unable to add calculation data to plot",e);
			}
		} else {
			if (refUri==this.stdUri) {
				// use standard sampe method for absolute scaling
				refString = LBL_NORMTXSTANDARDSUM; 
				refEntry = DataLib.fetchEntry(refUri);

				/* normStdSumAtQ0 normalised to refMon=1e8 monitor counts */
				if (null==this.centroidRoi) {
					normStdSumAtQ0 = DataLib.calcNormSum(refEntry);
				} else {
					normStdSumAtQ0 = DataLib.calcNormSumRoi(refEntry,this.centroidRoi,this.refMon);
				}
				methodScaler = stdCrossSection / normStdSumAtQ0;
				dRatio = stdDepth / samDepth;
				
				normTxRefSum  = DataLib.calcNormSum(refEntry);
				if(0>normTxSampleSum) { // ie invalid
					txRatio = 1.0;
				} else {
					txRatio = normTxRefSum / normTxSampleSum;
				}
				
				try {
					outPlot.addCalculationData(LBL_NORMSTDSUMATQ0,
							Factory.createArray(new double[] {normStdSumAtQ0}),
							LBL_NORMSTDSUMATQ0,
							"count");
					
					outPlot.addCalculationData(LBL_STDCROSSSECTION,
							Factory.createArray(new double[] {this.stdCrossSection}),
							LBL_STDCROSSSECTION,
							"1/mm");
					
					outPlot.addCalculationData(refString,
							Factory.createArray(normTxRefSum),
							refString,
							"count");

				} catch (InvalidArrayTypeException e) {
					throw new DataAccessException("Unable to add calculation data to plot",e);
				}
			} else {
				throw new DataAccessException("Invalid URI for scaling reference");
			}
		}

		try { /* Retrieve handle to Ical data and apply absolute scaling factors */
			IDataItem data = inPlot.findSingal();
			IArray Ical = inPlot.findSignalArray();
			if (null==Ical) { 
				throw new DataAccessException("Unable to access signal data"); 
			}	
			scaleFactor = prescale * methodScaler * dRatio * txRatio;
//			outPlot.addCalculationData(LBL_SCALEFACTOR,
//					Factory.createArray(new double[] {scaleFactor}),
//					LBL_SCALEFACTOR,
//					"count/count");
			Ical.getArrayMath().scale(scaleFactor);
			//outPlot = inPlot.scale(scaleFactor, 0.0);
			data.setCachedData(Ical, false);
		} catch (IOException e) {
			throw new DataAccessException(e);
		//} catch (PlotMathException e) {
		//	e.printStackTrace();
		//	throw new DataAccessException("Failed to scale Plot",e);
    	} catch (InvalidArrayTypeException e) {
			e.printStackTrace();
			throw new DataAccessException("Failed to scale Plot",e);
		}
	}
	
	private void calcAbsoluteTransmissions() throws IOException {
		/* Optional: Retrieve empty cell normalised transmission 
		 * count for absolute transmission ratio calculations 
		 */
		double absTxEmptyCell = -1.0;
		double absTxSample    = -1.0;
		
		if (refUri==this.dirUri) { 
			Double normTxEmptySum = 1.0;
			Double normTxSampleSum = 1.0;
			try {
				Data datumTxEmptySum = inPlot.findCalculationData(LBL_NORMTXEMPTYSUM);
				if(null!=datumTxEmptySum) {
					normTxEmptySum = datumTxEmptySum.readScalarDouble();
					absTxEmptyCell = normTxEmptySum / this.dirSum;
				}
				Data datumTxSamplSum = inPlot.findCalculationData(LBL_NORMTXSAMPLESUM);
				if(null!=datumTxSamplSum) {
					normTxSampleSum = datumTxSamplSum.readScalarDouble();
					absTxSample    = normTxSampleSum / this.dirSum;
				}
			} catch (IOException e) {
				if(isDebugMode) {
					System.out.println("Unable to calculate valid transmissions");
					e.printStackTrace(); // and continue
				}				
			}
			
			try {
				outPlot.addCalculationData(LBL_ABSTXEMPTYCELL,
					Factory.createArray(new double[] { absTxEmptyCell }),
					LBL_ABSTXEMPTYCELL,
					"count/count");
				outPlot.addCalculationData(LBL_ABSTXSAMPLE,
					Factory.createArray(new double[] { absTxSample } ),
					LBL_ABSTXSAMPLE,
					"count/count");
			} catch (InvalidArrayTypeException e) {
				if(isDebugMode) {
					System.out.println("Unable to store transmission calculation data");
					e.printStackTrace(); // and continue
				}
			}			
		} else {
			if(isDebugMode) {
				System.out.print("Unable to calculate valid transmissions");
			}
		}
	}
	
	/*  'entry' parameter here should be direct beam or standard sample dataset 
	 */
	private Double calcKappa(IGroup entry) throws DataAccessException {
		return DataLib.calcNormSum(entry)*detBinWidthX*detBinWidthZ/Math.pow(L2mm,2.0);
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

	public void setUseStdSample(Boolean useStdSample) {
		this.useStdSample = useStdSample;
	}

	public void setCentroidRoi(IGroup centroidRoi) {
		this.centroidRoi = centroidRoi;
	}

	public void setStdUri(URI stdUri) {
		this.stdUri = stdUri;
	}

	public void setDirUri(URI dirUri) {
		this.dirUri = dirUri;
		informVarValueChange("dirUri", dirUri);
	}

	public void setKappa(Double kappa) {
		this.kappa = kappa;
	}

	public void setStdCrossSection(Double stdCrossSection) {
		this.stdCrossSection = stdCrossSection;
	}

	public void setSamDepth(Double samDepth) {
		this.samDepth = samDepth;
	}

	public void setStdDepth(Double stdDepth) {
		this.stdDepth = stdDepth;
	}
	
//	private void doExport(){
//		IGroup group;
//		IWriter writer = null;
//		try {
//			group = org.gumtree.data.utils.Factory.createGroup("entry1");
////			double[] javaArray = new double[100];
////			for (int i = 0; i < javaArray.length; i++) {
////				javaArray[i] = i;
////			}
////			IArray array = org.gumtree.data.util.Factory.createDoubleArray(javaArray, new int[]{10, 10});
//			char[] javaArray = new char[100];
//			String name = "aaaaaaabcda;klhgpowerh,ajnsdgf;weh ouawe;akjsdflajksdfl;jkqweojil,anesf alwekjro;ajiseflkandf" +
//					"aewrkhawelkrhalskdjfhbwehklajhsdflkjnas,ldnfiwueroiqhy3r7y39ry79asd8yuf237rklasdjfh aer";
////			for (int i = 0; i < javaArray.length; i++) {
////				javaArray[i] = name.charAt(i);
////			}
//			IArray array = org.gumtree.data.utils.Factory.createArray(String.class, new int[]{1}, new String[]{name});
//			IDataItem dataItem = org.gumtree.data.utils.Factory.createDataItem(group, "item", array);
//			dataItem.addStringAttribute("NX_class", "NXgroup");
//			group.addDataItem(dataItem);
//			group.addStringAttribute("size", "10x10x1");
//			String filename = "D:/dra/newfile24.hdf";
//			writer = new NcHdfWriter(new File(filename));
//			writer.open();
//			writer.writeToRoot(group, true);
//			writer.close();
//		} catch (Exception e) {
//			if (writer != null)
//				writer.close();
//			e.printStackTrace();
//		}
//	}
}
