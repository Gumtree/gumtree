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
*    Paul Hathaway - Original Class  Blank_Patch  February 2009
*    Paul Hathaway - August 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class Patch2 extends ConcreteProcessor {
	
	private static final String processClass = "Patch2"; 
	private static final String processClassVersion = "2.0"; 
	private static final long processClassID = 2009082602; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private Plot inPlot;
	private Plot outPlot;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;
	
 	private Map<String,IDataItem> dictionary = new HashMap<String,IDataItem>();

	private final static String KEY_WAVELENGTH = "LambdaA";
	private final static String KEY_L2MM = "L2mm";
	
	private final static String KEY_SAMPLENAME = "sample.name";
	private final static String KEY_SAMPLEDEPTH = "sample.thickness";
	private static final String KEY_ATTFACTOR = "AttFactor";	
	private static final String KEY_ATTROTDEG = "AttRotDeg";
	private final static String KEY_COUNTTIME = "acquire_duration";
	private final static String KEY_MONITOR1 = "bm1_counts";
	private final static String KEY_DETECTORSUM = "total_counts";
	private final static String KEY_DETECTOROFFSET  = "detector.x";	// mm, detector offset horizontal
	
	/* metadata not yet provided as standard in Quokka NBI datasets*/
	private final static String KEY_TRANSMISSIONFLAG = "txflag";
	private final static String KEY_TEMPERATURE  = "temperature"; // deg
	private final static String KEY_MAGFIELD  = "magField";	// tesla ?, magnetic field
	private final static String KEY_SOURCEAPP  = "srcApp";		// mm, source aperture
	private final static String KEY_SAMPLEAPP = "samApp";		// mm, sample aperture
	private final static String KEY_BEAMSTOPDIA  = "beamstopDia";	// mm, beamstop diameter

	/* metadata not available from inPlot */
	private final static String KEY_TXDETECTORSUM = "txDetCount";	// count, total detector counts on transmission measurement
	private final static String KEY_DELTAWAVELENGTH = "deltaLambda"; // angstrom, wavelength spread

	/* tuner labels for metadata to enable update and exposure */
	private final static String LBL_WAVELENGTH = "wavelength";
	private final static String LBL_L2MM = "L2mm";	
	private final static String LBL_SAMPLENAME = "name";
	private final static String LBL_SAMPLEDEPTH = "thickness";
	private static final String LBL_ATTFACTOR = "attenuation";	
	private static final String LBL_ATTROTDEG = "attRotDeg";
	private final static String LBL_COUNTTIME = "countTime";
	private final static String LBL_MONITOR1 = "monCount";
	private final static String LBL_DETECTORSUM = "detCount";
	private final static String LBL_DETECTOROFFSET  = "detOffset";	// mm, detector offset horizontal

	/* metadata not available from inPlot */
	private final static String LBL_TRANSMISSIONFLAG = "transmission";
	private final static String LBL_TXDETECTORSUM = "txDetCount";	// count, total detector counts on transmission measurement
	private final static String LBL_DELTAWAVELENGTH = "deltaLambda"; // angstrom, wavelength spread
	private final static String LBL_TEMPERATURE  = "temperature"; // deg
	private final static String LBL_MAGFIELD  = "magField";	// tesla ?, magnetic field
	private final static String LBL_SOURCEAPP  = "srcApp";		// mm, source aperture
	private final static String LBL_SAMPLEAPP = "samApp";		// mm, sample aperture
	private final static String LBL_BEAMSTOPDIA  = "beamstopDia";	// mm, beamstop diameter

	private final static Double DEF_WAVELENGTH = 5.0; // angstrom
	private final static Double MIN_WAVELENGTH = 0.01; // angstrom

	private Boolean override = false;
	private Double wavelength = 6.0; // angstrom
	private Double l2mm = 2000.0; //mm
	
	private String name;
	private Double transmission = 0.0; // flag
	private Double thickness    = 1.0; // mm
	private Double attRotDeg    = 0.0; // deg
	private Double attenuation  = 1.0; // count/count
	private Double countTime    = 0.0; // sec
	private Double monCount     = 1.0; // count, monitor counts (beam monitor 1)
	private Double detCount     = 1.0; // count, total detector counts on transmission measurement
	private Double temperature  = 0.0; // deg
	private Double magField     = 0.0; // tesla ?, magnetic field
	private Double srcApp		= 0.0; // mm, source aperture
	private Double samApp       = 0.0; // mm, sample aperture
	private Double detOffset    = 0.0; // mm, detector offset horizontal
	private Double beamstopDia  = 1.0;	// mm, beamstop diameter
	
	private Double txDetCount;	// count, total detector counts on transmission measurement
	private Double deltaLambda; // angstrom, wavelength spread
	
	public Patch2() {
		this.setReprocessable(true);
	}
	
	public Boolean process() throws Exception {
		
    	this.outPlot = this.inPlot;
    	
		if (this.doForceSkip) {
			//stampProcessSkip();			
		} else {
			if (this.override) {
				writeInputMetaData(KEY_L2MM, LBL_L2MM, l2mm, "mm");
				writeInputMetaData(KEY_WAVELENGTH, LBL_WAVELENGTH, wavelength,"1/Ao");
				writeInputStringMetaData(KEY_SAMPLENAME, LBL_SAMPLENAME, name);
				writeInputMetaData(KEY_SAMPLEDEPTH, LBL_SAMPLEDEPTH, thickness, "mm");
				writeInputMetaData(KEY_ATTFACTOR, LBL_ATTFACTOR, attenuation, "count/count");
				writeInputMetaData(KEY_ATTROTDEG, LBL_ATTROTDEG, attRotDeg, "deg");
				writeInputMetaData(KEY_COUNTTIME, LBL_COUNTTIME, countTime, "sec");
				writeInputMetaData(KEY_MONITOR1, LBL_MONITOR1, monCount, "count");
				writeInputMetaData(KEY_DETECTORSUM, LBL_DETECTORSUM, detCount, "count");
				writeInputMetaData(KEY_DETECTOROFFSET, LBL_DETECTOROFFSET, detOffset,"mm");
			} else {
				l2mm = readInputMetaData(KEY_L2MM, LBL_L2MM, l2mm);
				wavelength = readInputMetaData(KEY_WAVELENGTH, LBL_WAVELENGTH, wavelength);
				if ((null==wavelength)||(MIN_WAVELENGTH>wavelength)) { 
					wavelength = DEF_WAVELENGTH;
					writeInputMetaData(KEY_WAVELENGTH, LBL_WAVELENGTH, wavelength,"1/Ao");
				}
				name = readInputStringMetadata(KEY_SAMPLENAME, LBL_SAMPLENAME);
				thickness = readInputMetaData(KEY_SAMPLEDEPTH, LBL_SAMPLEDEPTH, thickness);
				attenuation = readInputMetaData(KEY_ATTFACTOR, LBL_ATTFACTOR, attenuation);
				attRotDeg = readInputMetaData(KEY_ATTROTDEG, LBL_ATTROTDEG, attRotDeg);
				countTime = readInputMetaData(KEY_COUNTTIME, LBL_COUNTTIME, countTime);
				monCount = readInputMetaData(KEY_MONITOR1, LBL_MONITOR1, monCount);
				detCount = readInputMetaData(KEY_DETECTORSUM, LBL_DETECTORSUM, detCount);
				detOffset = readInputMetaData(KEY_DETECTOROFFSET, LBL_DETECTOROFFSET, detOffset);
			}
			//stampProcessEnd();
		}
		return doForceStop;
	}

	private Double readInputMetaData(String key, String portName, Double defVal) {
		Double metadata = defVal;
		try {
			IDataItem item;
			item = inPlot.getDataItem(key);
			if(null==item) {
				item = inPlot.findDataItem(key);
				// item = ((Plot) inPlot).findCalculationData(key);
			}
			if(null!=item) { 
				metadata = item.readScalarDouble(); 
				super.informVarValueChange(portName, metadata);
			} 
		} catch (IOException ioe) {
			if(isDebugMode) System.out.print("Unable to access metadata: "+key);
		}
		return metadata;
	}
	
	private String readInputStringMetadata(String key, String portName) {
		String metadata = null;
		try {
			IDataItem item;
			item = inPlot.getDataItem(key); 
			if(null!=item) {
				metadata = item.getData().toString(); // readScalarString(); 
				super.informVarValueChange(portName, metadata);
			}
		} catch (IOException ioe) {
			if(isDebugMode) System.out.print("Unable to access metadata: "+key);
		}
		return metadata;
	}
	
	private void writeInputMetaData(String key, String portName, Double metadata, String units) {
		// Modified pvh 090820: see Issue GDM-35
		try {
			IDataItem item = Factory.createDataItem(null,key,
								Factory.createDoubleArray(new double[] { metadata }));
			inPlot.updateDataItem(key,item);
			super.informVarValueChange(portName, metadata);
		} catch (InvalidArrayTypeException iate) {
			if(isDebugMode) System.out.print("Unable to write metadata: "+key+"\n");
		} catch (SignalNotAvailableException snae) {
			if(isDebugMode) System.out.print("Unable to write metadata: "+key+"\n");
			snae.printStackTrace();
		}
	}
	
	private void writeInputStringMetaData(String key, String portName, String metadata) {
		try {
			IDataItem item;
			item = inPlot.getDataItem(key);
			//Array sarray = Factory.createArray(String.class, new int[] {1});
			IArray sarray = Factory.createArray(metadata.toCharArray());
			//Index ima = sarray.getIndex();
			//sarray.setObject(ima, metadata);
			if(null==item) {
				item = Factory.createDataItem(null,key,sarray);
						// Factory.createArray(new String[] { metadata } ));
				inPlot.updateDataItem(key,item);
			} else {
				item.setCachedData(sarray,false);
			}
			super.informVarValueChange(portName, metadata);
		} catch (InvalidArrayTypeException iate) {
			if(isDebugMode) System.out.print("Unable to write metadata: "+key);
		} catch (SignalNotAvailableException snae) {
			if(isDebugMode) System.out.print("Unable to write metadata: "+key);
			snae.printStackTrace();
		}
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

	public void setDictionary(Map<String,IDataItem> dictionary) {
		this.dictionary = dictionary;
	}

    /* Out-Ports ----------------------------------------------------*/

	public Map<String,IDataItem> getDictionary() {
		return this.dictionary;
	}

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

	public void setOverride(Boolean override) {
		this.override = override;
	}

	public void setWavelength(Double wavelength) {
		this.wavelength = wavelength;
	}

	public void setL2mm(Double l2mm) {
		this.l2mm = l2mm;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTransmission(Double transmission) {
		this.transmission = transmission;
	}

	public void setThickness(Double thickness) {
		this.thickness = thickness;
	}

	public void setAttRotDeg(Double attRotDeg) {
		this.attRotDeg = attRotDeg;
	}

	public void setAttenuation(Double attenuation) {
		this.attenuation = attenuation;
	}

	public void setCountTime(Double countTime) {
		this.countTime = countTime;
	}

	public void setMonCount(Double monCount) {
		this.monCount = monCount;
	}

	public void setDetCount(Double detCount) {
		this.detCount = detCount;
	}

	public void setTxDetCount(Double txDetCount) {
		this.txDetCount = txDetCount;
	}

	public void setDeltaLambda(Double deltaLambda) {
		this.deltaLambda = deltaLambda;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public void setMagField(Double magField) {
		this.magField = magField;
	}

	public void setSrcApp(Double srcApp) {
		this.srcApp = srcApp;
	}

	public void setSamApp(Double samApp) {
		this.samApp = samApp;
	}

	public void setDetOffset(Double detOffset) {
		this.detOffset = detOffset;
	}

	public void setBeamstopDia(Double beamstopDia) {
		this.beamstopDia = beamstopDia;
	}

}
