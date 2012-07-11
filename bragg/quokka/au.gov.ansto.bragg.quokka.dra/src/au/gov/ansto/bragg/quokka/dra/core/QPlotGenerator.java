/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;

/**
 * @author nxi
 * Created on 19/08/2008
 */
public class QPlotGenerator implements ConcreteProcessor {

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

		private String value;

		YAxisType(String value){
			this.value = value;
		}

		public static YAxisType getInstance(String value){
			if (value.equals(I.value))
				return I;
			if (value.equals(LogI.value))
				return LogI;
			if (value.equals(LnI.value))
				return LnI;
			if (value.equals(IReverse.value))
				return IReverse;
			if (value.equals(IPowA.value))
				return IPowA;
			if (value.equals(IXPowB.value))
				return IXPowB;
			if (value.equals(IPowAXPowB.value))
				return IPowAXPowB;
			if (value.equals(SqrtIReverse.value))
				return SqrtIReverse;
			if (value.equals(LnIx.value))
				return LnIx;
			if (value.equals(LnIx2.value))
				return LnIx2;
			return I;
		}
		
		public String getValue(){
			return value;
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
		

		public static XAxisType getInstance(String value){
			if (value.equals(Q.value))
				return Q;
			if (value.equals(LogQ.value))
				return LogQ;
			if (value.equals(Q2.value))
				return Q2;
			if (value.equals(Qc.value))
				return Qc;
			if (value.equals(r.value))
				return r;
			if (value.equals(TwoThetaRad.value))
				return TwoThetaRad;
			if (value.equals(TwoThetaDeg.value))
				return TwoThetaDeg;
			return Q;
		}
		
		private String value;

		XAxisType(String value){
			this.value = value;
		}

		public String getValue(){
			return value;
		}
	};

	private IGroup qPlotGenerator_inputGroup;
	private IGroup qPlotGenerator_outputGroup;
	private String qPlotGenerator_yAxisName;
	private String qPlotGenerator_xAxisName;
	private Double qPlotGenerator_a;
	private Double qPlotGenerator_b;
	private Double qPlotGenerator_c;
	private Boolean qPlotGenerator_skip = false;
	private Boolean qPlotGenerator_stop = false;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		if (qPlotGenerator_skip){
			qPlotGenerator_outputGroup = qPlotGenerator_inputGroup;
			return qPlotGenerator_stop;
		}
		
		Plot inputPlot;
		IArray yAxisArray;
		IArray xAxisArray;
		XAxisType xType = XAxisType.getInstance(qPlotGenerator_xAxisName);
		switch (xType) {
		case Q:
			inputPlot = (Plot) qPlotGenerator_inputGroup;
			xAxisArray = inputPlot.getAxisArrayList().get(0);
			break;
		case LogQ:
			inputPlot = (Plot) qPlotGenerator_inputGroup.findGroup("IvsLogQ");
			xAxisArray = inputPlot.getAxisArrayList().get(0);
			break;
		case Q2:
			inputPlot = (Plot) qPlotGenerator_inputGroup;
			xAxisArray = inputPlot.getAxisArrayList().get(0).getArrayMath().toPower(2).getArray();
			break;
		case Qc:
			inputPlot = (Plot) qPlotGenerator_inputGroup;
			xAxisArray = inputPlot.getAxisArrayList().get(0).getArrayMath().toPower(qPlotGenerator_c).getArray();
			break;
		case r:
			inputPlot = (Plot) qPlotGenerator_inputGroup.findGroup("Ivsr");
			xAxisArray = inputPlot.getAxisArrayList().get(0);
			break;
		case TwoThetaRad:
			inputPlot = (Plot) qPlotGenerator_inputGroup.findGroup("IvsTwoTheta");
			xAxisArray = inputPlot.getAxisArrayList().get(0);
			break;
		case TwoThetaDeg:
			inputPlot = (Plot) qPlotGenerator_inputGroup.findGroup("IvsTwoThetaDeg");
			xAxisArray = inputPlot.getAxisArrayList().get(0);
			break;
		default:
			inputPlot = (Plot) qPlotGenerator_inputGroup;
			xAxisArray = inputPlot.getAxisArrayList().get(0);
			break;
		}
		
		EData<IArray> inputEdata = new EData<IArray>(inputPlot.findSignalArray(), inputPlot.findVarianceArray());
		EData<IArray> resultEData = null;
//		YAxisType yType = YAxisType.valueOf(qPlotGenerator_yAxisName);
		YAxisType yType = YAxisType.getInstance(qPlotGenerator_yAxisName);
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
			resultEData = EMath.toPower(inputEdata.getData(), qPlotGenerator_a, inputEdata.getVariance());
			break;
		case IXPowB:
			resultEData = EMath.toEltMultiply(inputEdata.getData(), xAxisArray.getArrayMath().toPower(qPlotGenerator_b).getArray(), 
					inputEdata.getVariance(), null);
			break;
		case IPowAXPowB:
			resultEData = EMath.toPower(inputEdata.getData(), qPlotGenerator_a, inputEdata.getVariance());
			resultEData = EMath.toEltMultiply(resultEData.getData(), xAxisArray.getArrayMath().toPower(qPlotGenerator_b).getArray(), 
					resultEData.getVariance(), null);
			break;
		case IReverse:
			resultEData = EMath.toEltInverseSkipZero(inputEdata.getData(), inputEdata.getVariance());
			break;
		case LnIx:
			resultEData = EMath.toEltMultiply(inputEdata.getData(), xAxisArray, inputEdata.getVariance(), null);
			resultEData = EMath.ln(resultEData.getData(), resultEData.getVariance());
			break;
		case LnIx2:
			resultEData = EMath.toEltMultiply(inputEdata.getData(), xAxisArray.getArrayMath().power(2).getArray(), inputEdata.getVariance(), null);
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
//		removeNaN(resultEData.getData(), resultEData.getVariance());
		qPlotGenerator_outputGroup = (Plot) PlotFactory.createPlot(yType.name() + 
				"_vs_" + xType.name(), DataDimensionType.pattern);
		PlotFactory.addDataToPlot(qPlotGenerator_outputGroup, "data", resultEData.getData(), 
				qPlotGenerator_yAxisName + " vs " + qPlotGenerator_xAxisName, "", 
				resultEData.getVariance());
		PlotFactory.addAxisToPlot(qPlotGenerator_outputGroup, xType.name(), 
				xAxisArray, qPlotGenerator_xAxisName, "", 0);
		return qPlotGenerator_stop;
	}

	private void removeNaN(IArray I, IArray IVariance) {
		// TODO Auto-generated method stub
		IArrayIterator iIterator = I.getIterator();
		IArrayIterator varianceIterator = IVariance.getIterator();
		while (iIterator.hasNext()){
			varianceIterator.next();
			double value = iIterator.getDoubleNext();
			double varianceValue = varianceIterator.getDoubleNext();
			if (Double.isNaN(value)){
//				iIterator.setDoubleCurrent(Double.NaN);
//				varianceIterator.setDoubleCurrent(Double.NaN);
				iIterator.setDoubleCurrent(0);
				varianceIterator.setDoubleCurrent(0);
				varianceValue = 0;
				continue;
			}
			if (Double.isNaN(varianceValue))
				varianceIterator.setDoubleCurrent(0);
		}
	}
	/**
	 * @return the qPlotGenerator_outputGroup
	 */
	public IGroup getQPlotGenerator_outputGroup() {
		return qPlotGenerator_outputGroup;
	}
	/**
	 * @param plotGenerator_inputGroup the qPlotGenerator_inputGroup to set
	 */
	public void setQPlotGenerator_inputGroup(IGroup plotGenerator_inputGroup) {
		qPlotGenerator_inputGroup = plotGenerator_inputGroup;
	}
	/**
	 * @param plotGenerator_yAxisName the qPlotGenerator_yAxisName to set
	 */
	public void setQPlotGenerator_yAxisName(String plotGenerator_yAxisName) {
		qPlotGenerator_yAxisName = plotGenerator_yAxisName;
	}
	/**
	 * @param plotGenerator_xAxisName the qPlotGenerator_xAxisName to set
	 */
	public void setQPlotGenerator_xAxisName(String plotGenerator_xAxisName) {
		qPlotGenerator_xAxisName = plotGenerator_xAxisName;
	}
	/**
	 * @param plotGenerator_a the qPlotGenerator_a to set
	 */
	public void setQPlotGenerator_a(Double plotGenerator_a) {
		qPlotGenerator_a = plotGenerator_a;
	}
	/**
	 * @param plotGenerator_b the qPlotGenerator_b to set
	 */
	public void setQPlotGenerator_b(Double plotGenerator_b) {
		qPlotGenerator_b = plotGenerator_b;
	}
	/**
	 * @param plotGenerator_c the qPlotGenerator_c to set
	 */
	public void setQPlotGenerator_c(Double plotGenerator_c) {
		qPlotGenerator_c = plotGenerator_c;
	}
	/**
	 * @param plotGenerator_skip the qPlotGenerator_skip to set
	 */
	public void setQPlotGenerator_skip(Boolean plotGenerator_skip) {
		qPlotGenerator_skip = plotGenerator_skip;
	}
	/**
	 * @param plotGenerator_stop the qPlotGenerator_stop to set
	 */
	public void setQPlotGenerator_stop(Boolean plotGenerator_stop) {
		qPlotGenerator_stop = plotGenerator_stop;
	}
}

