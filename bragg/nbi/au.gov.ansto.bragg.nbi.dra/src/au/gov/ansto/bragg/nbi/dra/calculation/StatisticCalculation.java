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
package au.gov.ansto.bragg.nbi.dra.calculation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.util.AxisRecord;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 22/12/2008
 */
public class StatisticCalculation extends ConcreteProcessor {

	private Plot statistic_inputPlot;
	private Plot statistic_outputPlot;
	private Boolean statistic_skip = false;
	private Boolean statistic_stop = false;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	@Override
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		statistic_outputPlot = statistic_inputPlot;
		if (statistic_skip)
			return statistic_stop;
		DataDimensionType dimensionType = statistic_inputPlot.getDimensionType();
		IArray dataArray = statistic_inputPlot.findSignalArray();
		IArray varianceArray = statistic_inputPlot.findVarianceArray();
		IArray centroidArray = null;
		IArray centroidVariance = null;
		IArray totalSumArray = null;
		IArray totalSumVariance = null;
		IArray RMS = null;
		IArray RMSVariance = null;
		IArray maxArray = null;
		IArray maxVariance = null;
		IArray minArray = null;
		IArray minVariance = null;
		EData<IArray> centroidEData = null;
		EData<?> totalSumEData = null;
		
		List<Axis> allAxes = statistic_inputPlot.getAxisList();
		List<Axis> axes = new ArrayList<Axis>();
		int[] shape = statistic_inputPlot.findSingal().getShape();
		switch(dimensionType){
		case map:
			axes.add(allAxes.get(allAxes.size() - 2));
			axes.add(allAxes.get(allAxes.size() - 1));
			centroidEData = calculateCentroid(dataArray.getArrayUtils().reduce().getArray(), varianceArray.getArrayUtils().reduce().getArray(), 
					axes, null, null);
			totalSumEData = EMath.sum(dataArray, varianceArray);
			break;
		case pattern:
			axes.add(allAxes.get(allAxes.size() - 1));
			centroidEData = calculateCentroid(dataArray.getArrayUtils().reduce().getArray(), varianceArray.getArrayUtils().reduce().getArray(), 
					axes, null, null);
			totalSumEData = EMath.sum(dataArray, varianceArray);
			break;
		case volume:
			axes.add(allAxes.get(allAxes.size() - 3));
			axes.add(allAxes.get(allAxes.size() - 2));
			axes.add(allAxes.get(allAxes.size() - 1));
			centroidEData = calculateCentroid(dataArray.getArrayUtils().reduce().getArray(), varianceArray.getArrayUtils().reduce().getArray(), 
					axes, null, null);
			totalSumEData = EMath.sum(dataArray, varianceArray);
			break;
		case mapset: 
			centroidArray = Factory.createArray(Double.TYPE, new int[]{shape[0], 2});
			centroidVariance = Factory.createArray(Double.TYPE, new int[]{shape[0], 2});
			axes.add(allAxes.get(allAxes.size() - 2));
			axes.add(allAxes.get(allAxes.size() - 1));
			for (int i = 0; i < shape[0]; i++) {
				IArray dataSlice = statistic_inputPlot.findSignalArray().getArrayUtils().slice(0, i).getArray();
				IArray varianceSlice = statistic_inputPlot.findVarianceArray().getArrayUtils().slice(0, i).getArray();
				IArray centroidSlice = centroidArray.getArrayUtils().slice(0, i).getArray();
				IArray centroidVarianceSlice = centroidVariance.getArrayUtils().slice(0, i).getArray();
				calculateCentroid(dataSlice, varianceSlice, axes, centroidSlice, centroidVarianceSlice);
			}
			centroidEData = new EData<IArray>(centroidArray, centroidVariance);
			totalSumEData = EMath.sumForDimension(dataArray.getArrayUtils().reduceTo(3).getArray(), 0, varianceArray.getArrayUtils().reduceTo(3).getArray());
			break;
		case patternset:
			centroidArray = Factory.createArray(Double.TYPE, new int[]{shape[0], 1});
			centroidVariance = Factory.createArray(Double.TYPE, new int[]{shape[0], 1});
			axes.add(allAxes.get(allAxes.size() - 1));
			for (int i = 0; i < shape[0]; i++) {
				IArray dataSlice = statistic_inputPlot.findSignalArray().getArrayUtils().slice(0, i).getArray();
				IArray varianceSlice = statistic_inputPlot.findVarianceArray().getArrayUtils().slice(0, i).getArray();
				IArray centroidSlice = centroidArray.getArrayUtils().slice(0, i).getArray();
				IArray centroidVarianceSlice = centroidVariance.getArrayUtils().slice(0, i).getArray();
				calculateCentroid(dataSlice, varianceSlice, axes, centroidSlice, centroidVarianceSlice);
			}
			centroidEData = new EData<IArray>(centroidArray, centroidVariance);
			totalSumEData = EMath.sumForDimension(dataArray.getArrayUtils().reduceTo(2).getArray(), 0, varianceArray.getArrayUtils().reduceTo(2).getArray());
			break;
		case volumeset:
			centroidArray = Factory.createArray(Double.TYPE, new int[]{shape[0], 3});
			centroidVariance = Factory.createArray(Double.TYPE, new int[]{shape[0], 3});
			axes.add(allAxes.get(allAxes.size() - 3));
			axes.add(allAxes.get(allAxes.size() - 2));
			axes.add(allAxes.get(allAxes.size() - 1));
			for (int i = 0; i < shape[0]; i++) {
				IArray dataSlice = statistic_inputPlot.findSignalArray().getArrayUtils().slice(0, i).getArray();
				IArray varianceSlice = statistic_inputPlot.findVarianceArray().getArrayUtils().slice(0, i).getArray();
				IArray centroidSlice = centroidArray.getArrayUtils().slice(0, i).getArray();
				IArray centroidVarianceSlice = centroidVariance.getArrayUtils().slice(0, i).getArray();
				calculateCentroid(dataSlice, varianceSlice, axes, centroidSlice, centroidVarianceSlice);
			}
			centroidEData = new EData<IArray>(centroidArray, centroidVariance);
			totalSumEData = EMath.sumForDimension(dataArray.getArrayUtils().reduceTo(4).getArray(), 0, varianceArray.getArrayUtils().reduceTo(4).getArray());
			break;			
		default:
			centroidEData = calculateCentroid(dataArray.getArrayUtils().reduce().getArray(), varianceArray.getArrayUtils().reduce().getArray(), 
					statistic_inputPlot.getAxisList(), null, null);
		totalSumEData = EMath.sumForDimension(dataArray.getArrayUtils().reduceTo(3).getArray(), 0, varianceArray.getArrayUtils().reduceTo(3).getArray());
			break;
		}
		System.out.println(centroidEData.getData());
		System.out.println(centroidEData.getVariance());
		statistic_outputPlot.addCalculationData("centroid", centroidEData.getData(), "Centroid", "", 
				centroidEData.getVariance());
		Object totalSumData = totalSumEData.getData();
		Object totalSumDataVariance = totalSumEData.getVariance();
		if (totalSumData instanceof Double){
			totalSumArray = Factory.createArray(Double.TYPE, new int[]{1}, new double[]{(Double) totalSumData});
			totalSumVariance = Factory.createArray(Double.TYPE, new int[]{1}, new double[]{(Double) totalSumDataVariance});
		}else {
			totalSumArray = (IArray) totalSumEData.getData();
			totalSumVariance = (IArray) totalSumEData.getVariance();
		}
		statistic_outputPlot.addCalculationData("total_sum", totalSumArray, "Total Sum", "counts", totalSumVariance);
		return statistic_stop;
	}

	private EData<IArray> calculateCentroid(IArray data, IArray variance, List<Axis> axes, IArray resultArray,
			IArray resultVariance) 
	throws ShapeNotMatchException, IOException {
		// TODO Auto-generated method stub
		int rank = data.getRank();
		if (axes.size() != rank)
			throw new ShapeNotMatchException("the axes size does not match the data");
		if (resultArray == null) 
			resultArray = Factory.createArray(Double.TYPE, new int[]{rank});
		if (resultVariance == null)
			resultVariance = Factory.createArray(Double.TYPE, new int[]{rank});
		IIndex resultIndex = resultArray.getIndex();
		for(int i = 0; i < rank; i ++){
			resultIndex.set(i);
			AxisRecord axis = AxisRecord.createRecord(axes.get(i), i, data.getShape());
			IArray widthArray = Factory.createArray(Double.TYPE, new int[]{(int) axis.length()});
			IIndex widthIndex = widthArray.getIndex();
			for (int j = 0; j < widthArray.getSize(); j++) 
				widthArray.setDouble(widthIndex.set(j), axis.width(j));
			
			EData<IArray> sumForDimension = EMath.sumForDimension(data, i, variance);
			EData<Double> centroid = EMath.vecDot(sumForDimension.getData(), axis.centres(), 
					sumForDimension.getVariance(), widthArray);
			EData<Double> totalSum = EMath.sum(sumForDimension.getData(), sumForDimension.getVariance());
			resultArray.setDouble(resultIndex, centroid.getData() / totalSum.getData());
			resultVariance.setDouble(resultIndex, centroid.getVariance() / totalSum.getVariance() / totalSum.getVariance());
		}
		return new EData<IArray>(resultArray, resultVariance);
	}

	/**
	 * @return the statistic_outputPlot
	 */
	public Plot getStatistic_outputPlot() {
		return statistic_outputPlot;
	}

	/**
	 * @param statistic_inputPlot the statistic_inputPlot to set
	 */
	public void setStatistic_inputPlot(Plot statistic_inputPlot) {
		this.statistic_inputPlot = statistic_inputPlot;
	}

	/**
	 * @param statistic_skip the statistic_skip to set
	 */
	public void setStatistic_skip(Boolean statistic_skip) {
		this.statistic_skip = statistic_skip;
	}

	/**
	 * @param statistic_stop the statistic_stop to set
	 */
	public void setStatistic_stop(Boolean statistic_stop) {
		this.statistic_stop = statistic_stop;
	}

}
