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

import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

public class Blank_Plotter extends ConcreteProcessor {
	
	private static final String processClass = "Blank_Plotter"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009022308; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.pattern;
    private Boolean isDebugMode = true;

	private Plot inPlot;
	private Plot outPlot;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;
	
	private String yAxisName;
	private String xAxisName;
	private Double par_a;
	private Double par_b;
	private Double par_c;
	
	private IArray xAxis;
	private XAxisType xType;
	private YAxisType yType;
	
	private Plot pattern;
	private EData<IArray> inputEdata;
	private EData<IArray> resultEData = null;

	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	
    	stampProcessLog();
		
		if (this.doForceSkip) {
			stampProcessSkip();			
		} else {
			/* TODO: Do interesting stuff here */
			formatXAxis();
			formatYAxis();
			publishResult();
			stampProcessEnd();
		}
		return doForceStop;
	}

	private void stampProcessLog() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("ProcessClass/Version/ID: ["
				+processClass+";"
				+processClassVersion+";"
				+processClassID+"]");		
	}
	
	private void stampProcessSkip() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]:"+"SKIP process");		
	}
	
	private void stampProcessEntry(String logEntry) {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]: "+logEntry);		
	}
	
	private void stampProcessEnd() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]:"+"END process");		
	}
	
	private enum YAxisType{
		I ("I"),
		LogI ("log(I)"),
		LnI ("ln(I)"), 
		IReverse ("1/I"),
		IPowA ("I^a"),
		IXPowB ("I*X^b"),
		IPowAXPowB ("I^a*X^b"),
		SqrtIReverse ("1/sqrt(I)"),
		LnIx ("ln(I*X)"),
		LnIx2 ("ln(I*X^-2)");

		private String label;

		YAxisType(String sel){
			this.label = sel;
		}

		public static YAxisType getInstance(String selection){
			if (selection.equals(I.label))
				return I;
			if (selection.equals(LogI.label))
				return LogI;
			if (selection.equals(LnI.label))
				return LnI;
			if (selection.equals(IReverse.label))
				return IReverse;
			if (selection.equals(IPowA.label))
				return IPowA;
			if (selection.equals(IXPowB.label))
				return IXPowB;
			if (selection.equals(IPowAXPowB.label))
				return IPowAXPowB;
			if (selection.equals(SqrtIReverse.label))
				return SqrtIReverse;
			if (selection.equals(LnIx.label))
				return LnIx;
			if (selection.equals(LnIx2.label))
				return LnIx2;
			return I;
		}
		
		public String getLabel(){
			return label;
		}
	};

	private enum XAxisType{
		Q ("q"),
		LogQ ("log(q)"),
		Q2 ("q^2"), 
		Qc ("q^c"),
		r ("r"),
		TwoThetaRad ("2theta(rad)"),
		TwoThetaDeg ("2theta(deg)");
		
		public static XAxisType getInstance(String selection){
			if (selection.equals(Q.label))
				return Q;
			if (selection.equals(LogQ.label))
				return LogQ;
			if (selection.equals(Q2.label))
				return Q2;
			if (selection.equals(Qc.label))
				return Qc;
			if (selection.equals(r.label))
				return r;
			if (selection.equals(TwoThetaRad.label))
				return TwoThetaRad;
			if (selection.equals(TwoThetaDeg.label))
				return TwoThetaDeg;
			return Q;
		}
		
		private String label;

		XAxisType(String value){
			this.label = value;
		}

		public String getLabel(){
			return label;
		}
	};

	private void formatXAxis() throws IOException 
	{
		try {
			xType = XAxisType.getInstance(xAxisName);
			switch (xType) {
			case Q:
				pattern = inPlot;
				xAxis = pattern.getAxisArrayList().get(0);
				break;
			case LogQ:
				pattern = (Plot) inPlot.findGroup("IvsLogQ");
				xAxis = pattern.getAxisArrayList().get(0);
				break;
			case Q2:
				pattern = inPlot;
				xAxis = pattern.getAxisArrayList().get(0).getArrayMath().toPower(2).getArray();
				break;
			case Qc:
				pattern = inPlot;
				xAxis = pattern.getAxisArrayList().get(0).getArrayMath().toPower(par_c).getArray();
				break;
			case r:
				pattern = (Plot) inPlot.findGroup("Ivsr");
				xAxis = pattern.getAxisArrayList().get(0);
				break;
			case TwoThetaRad:
				pattern = (Plot) inPlot.findGroup("IvsTwoTheta");
				xAxis = pattern.getAxisArrayList().get(0);
				break;
			case TwoThetaDeg:
				pattern = (Plot) inPlot.findGroup("IvsTwoThetaDeg");
				xAxis = pattern.getAxisArrayList().get(0);
				break;
			default:
				pattern = inPlot;
				xAxis = pattern.getAxisArrayList().get(0);
				break;
			}
			
			inputEdata = new EData<IArray>(pattern.findSignalArray(), pattern.findVarianceArray());

		} catch (SignalNotAvailableException e) {
			throw new IOException("X-Axis Array not found");
		}

	}

	private void formatYAxis() throws IOException 
	{
		try {
			yType = YAxisType.getInstance(yAxisName);
			switch (yType) {
				case I:
					resultEData = inputEdata;
					break;
				case LogI:
					resultEData = EMath.toLog10(inputEdata.getData(), inputEdata.getVariance());
					break;
				case LnI:
					resultEData = EMath.toLn(inputEdata.getData(), inputEdata.getVariance());
					break;
				case IPowA:
					resultEData = EMath.toPower(inputEdata.getData(), par_a, inputEdata.getVariance());
					break;
				case IXPowB:
					resultEData = EMath.toEltMultiply(inputEdata.getData(), xAxis.getArrayMath().toPower(par_b).getArray(), 
							inputEdata.getVariance(), null);
					break;
				case IPowAXPowB:
					resultEData = EMath.toPower(inputEdata.getData(), par_a, inputEdata.getVariance());
					resultEData = EMath.toEltMultiply(resultEData.getData(), xAxis.getArrayMath().toPower(par_b).getArray(), 
							resultEData.getVariance(), null);
					break;
				case IReverse:
					resultEData = EMath.toEltInverseSkipZero(inputEdata.getData(), inputEdata.getVariance());
					break;
				case LnIx:
					resultEData = EMath.toEltMultiply(inputEdata.getData(), xAxis, inputEdata.getVariance(), null);
					resultEData = EMath.ln(resultEData.getData(), resultEData.getVariance());
					break;
				case LnIx2:
					resultEData = EMath.toEltMultiply(inputEdata.getData(), xAxis.getArrayMath().power(2).getArray(), inputEdata.getVariance(), null);
					resultEData = EMath.toLn(resultEData.getData(), resultEData.getVariance());
					break;
				case SqrtIReverse:
					resultEData = EMath.toSqrt(inputEdata.getData(), inputEdata.getVariance());
					resultEData = EMath.eltInverseSkipZero(resultEData.getData(), resultEData.getVariance());
					break;			
				default:
					resultEData = inputEdata;
					break;
			}
		} catch (ShapeNotMatchException e){
			throw new IOException("Unable to format Y-Axis data to shape");
		}
	}

	private void publishResult() throws IOException 
	{
		try {
			outPlot = (Plot) PlotFactory.createPlot(
					yType.name() +"_vs_"+ xType.name(), 
					DataDimensionType.pattern);
			
			PlotFactory.addDataToPlot(outPlot, 
					"data", 
					resultEData.getData(), 
					yAxisName + " vs " + xAxisName, "", 
					resultEData.getVariance());
			
			PlotFactory.addAxisToPlot(outPlot, 
					xType.name(), 
					xAxis, 
					xAxisName, 
					"", 
					0);
			
			IDataItem notes = inPlot.findDataItem(DataLib.KEY_NOTES);
			if(null!=notes) {
				outPlot.addDataItem(notes);
			}
			
		} catch (PlotFactoryException e) {
			throw new IOException("Cannot create new Plot for result");
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

	/* Out-Ports ----------------------------------------------------*/

	public IGroup getOutPlot() {
		return (IGroup) this.outPlot;
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


	public void setYAxisName(String name) {
		yAxisName = name;
	}

	public void setXAxisName(String name) {
		xAxisName = name;
	}

	public void setA(Double val) {
		par_a = val;
	}

	public void setB(Double val) {
		par_b = val;
	}

	public void setC(Double val) {
		par_c = val;
	}
}
