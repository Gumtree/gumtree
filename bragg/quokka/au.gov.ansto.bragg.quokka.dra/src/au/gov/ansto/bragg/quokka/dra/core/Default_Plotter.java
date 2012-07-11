/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html

* Same interface as "Blank_Plotter" class, but does not allow axis selection
* or parameter modification.
* 
* Contributors: 
*    Paul Hathaway - March 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.math.EData;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

public class Default_Plotter extends ConcreteProcessor {
	
	private static final String processClass = "Default_Plotter"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009040101; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.pattern;
    private Boolean isDebugMode = true;

	private Plot inPlot;
	private Plot outPlot;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;
	
	private String title;
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

	public Default_Plotter() {
		this.setReprocessable(false);
	}
	
	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	
    	stampProcessLog(outPlot);
		
		if (this.doForceSkip) {
			stampProcessSkip(outPlot);			
		} else {
			formatXAxis();
			formatYAxis();
			publishResult();
			stampProcessEnd(outPlot);
		}
		return doForceStop;
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
			pattern = inPlot;
			xAxis = pattern.getAxisArrayList().get(0);
			inputEdata = new EData<IArray>(pattern.findSignalArray(), pattern.findVarianceArray());
		} catch (SignalNotAvailableException e) {
			throw new IOException("X-Axis Array not found");
		}

	}

	private void formatYAxis() throws IOException 
	{
			yType = YAxisType.getInstance(yAxisName);
			resultEData = inputEdata;
	}

	private void publishResult() throws IOException 
	{
//		try {
//			outPlot = (Plot) PlotFactory.createPlot(
//					yType.name() +"_vs_"+ xType.name(), 
//					DataDimensionType.pattern);
//			
//			PlotFactory.addDataToPlot(outPlot, 
//					"data", 
//					resultEData.getData(), 
//					yType.name() + " vs " + xType.name(), "", 
//					resultEData.getVariance());
//			
//			PlotFactory.addAxisToPlot(outPlot, 
//					xType.name(), 
//					xAxis, 
//					xType.name(), 
//					"", 
//					0);
//			
			IDataItem notes = inPlot.findDataItem(DataLib.KEY_NOTES);
			if(null!=notes) {
				outPlot.addDataItem(notes);
			}
			
//		} catch (PlotFactoryException e) {
//			throw new IOException("Cannot create new Plot for result");
//		}
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

	public String getTitle() {
		return title;
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


	public void setTitle(String title) {
		this.title = title;
		this.inPlot.setTitle(title);
	}

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
