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

import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.DataAccessException;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

public class ShowCalculation extends ConcreteProcessor {
	
	private static final String processClass = "ShowCalculation"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009031301; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private final static String KEY_WAVELENGTH = "LambdaA";
	private final static String KEY_L2MM = "L2mm";
	private final static String KEY_CENTERX = "BeamCenterX";
	private final static String KEY_CENTERZ = "BeamCenterZ";
	private final static String KEY_NOTES = "notes";

	private static final String LBL_ABSTXEMPTYCELL = "absTxEmptyCell";
	private static final String LBL_ABSTXSAMPLE = "absTxSample";

	private Plot inPlot;
	private Plot outPlot;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;
	
	private Double centroidZ = 480.0; // mm
	private Double centroidX = 480.0; // mm
	private Double l2mm = 2000.0; // mm
	private Double kappa = 1.0; // count/count
	private Double wavelength = -1.0;
	
	private Double countTime; 	// sec
	private Double monCount;	// count, monitor counts (beam monitor 1)
	private Double detCount;	// count, total detector counts on transmission measurement
	private Double txDetCount;	// count, total detector counts on transmission measurement
	private Double deltaLambda; // angstrom, wavelength spread
	private Double absTxEmptyCell;
	private Double absTxSample;
	
	private String sampleName;
	private Double sampleThickness = -1.0;
	private Double detOffset;
	private Double attenuation;
	
	public ShowCalculation() {
		this.setReprocessable(false);
	}
	
	public Boolean process() throws Exception {
		
    	this.outPlot = (Plot) this.inPlot;
    	
		if (this.doForceSkip) {
			//stampProcessSkip();			
		} else {
			checkParameters();
			publishResults();
			publishExports();
		}
		return doForceStop;
	}
	
	private Double updateParameter(String key) {
		Double result = -1.0;
		IDataItem item = inPlot.getDataItem(key);
		if (null!=item) {
			try {
				result = item.getData().getArrayMath().getMaximum();
			} catch (IOException e) {
				if(isDebugMode) { 
					System.out.print("Unable to find parameter '"+key+"'");
					e.printStackTrace(); // and return default
				}
			}
		} 
		return result; 
	}
	
	private Double updateCalculationData(String key) {
		Double result = -1.0;
		Data datum = inPlot.findCalculationData(key);
		if (null!=datum) {
			try {
				result = datum.getData().getArrayMath().getMaximum();
			} catch (IOException e) {
				if(isDebugMode) { 
					System.out.print("Unable to find calculation data '"+key+"'");
					e.printStackTrace(); // and return default
				}
			}
		} 
		return result; 
	}

	private void checkParameters() throws DataAccessException {
		this.sampleName = DataLib.readInputStringMetadata(inPlot, DataLib.KEY_SAMPLENAME);
		this.sampleThickness = updateParameter(DataLib.KEY_SAMPLEDEPTH);
		this.wavelength = updateParameter(KEY_WAVELENGTH);
		this.centroidX = updateParameter(KEY_CENTERX);
		this.centroidZ = updateParameter(KEY_CENTERZ);
		this.l2mm = updateParameter(KEY_L2MM);
		this.detOffset = updateParameter(DataLib.KEY_DETECTOROFFSET);
		this.monCount = updateParameter(DataLib.KEY_MONITOR1);
		this.detCount = updateParameter(DataLib.KEY_DETECTORSUM);
		this.countTime = updateParameter(DataLib.KEY_COUNTTIME);
		this.attenuation = updateParameter(DataLib.KEY_ATTFACTOR);
		this.absTxEmptyCell = updateCalculationData(LBL_ABSTXEMPTYCELL);
		this.absTxSample = updateCalculationData(LBL_ABSTXSAMPLE);
		this.kappa = updateCalculationData(DataLib.LBL_KAPPA);
	}

	private void publishResults() {
		super.informVarValueChange("centroidX", centroidX);
		super.informVarValueChange("centroidZ", centroidZ);		
		super.informVarValueChange("absTxEmptyCell", absTxEmptyCell);		
		super.informVarValueChange("absTxSample", absTxSample);		
	}

	private void publishExports() {
		try {
			String defaultFormat = "%6g";

			DataLib.appendNote(outPlot, "Sample Notes", "------------------------"); 
			DataLib.appendNote(outPlot, " Sample Name", this.sampleName);
			DataLib.appendNote(outPlot, " Sample Thickness (mm)", determine(null,this.sampleThickness));

			DataLib.appendNote(outPlot, "Configuration", "------------------------");
			DataLib.appendNote(outPlot, " Wavelength (Ao)", determine(null,this.wavelength));
			DataLib.appendNote(outPlot, " Beam Centre X (mm)", determine("%5.1f",this.centroidX));
			DataLib.appendNote(outPlot, " Beam Centre Z (mm)", determine("%5.1f",this.centroidZ));
			DataLib.appendNote(outPlot, " L2 (Sample to Det)(mm)", determine("%5g",this.l2mm));
			DataLib.appendNote(outPlot, " Detector Offset (mm)", String.format("%6g",this.detOffset));
			DataLib.appendNote(outPlot, " Attenuation", determine(null,this.attenuation));

			DataLib.appendNote(outPlot, "Measurements", "------------------------"); 
			DataLib.appendNote(outPlot, " Count Time (s)", determine("%g",this.countTime));
			DataLib.appendNote(outPlot, " Monitor Counts", determine("%6g",this.monCount));
			DataLib.appendNote(outPlot, " Detector Counts", determine("%6g",this.detCount));
			
			DataLib.appendNote(outPlot, "Calculations", "------------------------"); 
			DataLib.appendNote(outPlot, " Sample Transmission", determine("%6.4f",this.absTxSample));
			DataLib.appendNote(outPlot, " Empty Cell Transmission", determine("%6.4f",this.absTxEmptyCell));
			DataLib.appendNote(outPlot, " kappa Scaling factor", determine("%6g",this.kappa));
		} catch (IOException e) {
			if(isDebugMode) {
				System.out.println("> Unable to append export note to plot");
			}
		}
	}
	
	private String determine(String format, Double par) {
		String result;
		if(0<par) {
			if(null==format) {
				result = par.toString();
			} else {
				result = String.format(format, par);
			}
		} else {
			result = "n/a";
		}
		return result;
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

	public void setCentroidZ(Double centroidZ) {
		this.centroidZ = centroidZ;
	}

	public void setCentroidX(Double centroidX) {
		this.centroidX = centroidX;
	}

	/*
	 * Dummy setter to broadcast changes to centroid ROI
	 */
	public void setCentroidRoi(IGroup centroidRoi) {
		/* TODO: validate as mask */
		/* UI read only */
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

	public void setKappa(Double kappa) {
		this.kappa = kappa;
	}

	public void setAbsTxEmptyCell(Double absTxEmptyCell) {
		this.absTxEmptyCell = absTxEmptyCell;
	}

	public void setAbsTxSample(Double absTxSample) {
		this.absTxSample = absTxSample;
	}
}
