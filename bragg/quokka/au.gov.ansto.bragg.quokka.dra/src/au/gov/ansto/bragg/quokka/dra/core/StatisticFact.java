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

import java.io.IOException;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.Statistics;
import au.gov.ansto.bragg.quokka.dra.algolib.core.DimensionNotMatchException;
import au.gov.ansto.bragg.quokka.dra.algolib.core.Image;
import au.gov.ansto.bragg.quokka.dra.algolib.core.RegionalStatistics;
import au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor;

public class StatisticFact implements ConcreteProcessor {
	
	private RegionalStatistics statistics = null;
	private Boolean statisticFact_stop = false;
	private IGroup statisticFact_scanData = null;
	private IGroup statisticFact_output = null;
	
	public Boolean process() throws IOException, InvalidArrayTypeException, DimensionNotMatchException {
		// TODO Auto-generated method stub
		IArray sampleData = ((NcGroup) statisticFact_scanData).findSignal().getData();
		statistics = new RegionalStatistics(sampleData, sampleData);
//		statisticFact_output = findStatisticFact(statistics);
		findStatisticFact(statistics);
//		statisticFact_output.buildResultGroup(centerDataItem);	
		Image image = new Image(statisticFact_scanData);
		double QCentroid = image.getQCentroid();
		IArray QCentroidArray = Factory.createArray(new double[]{QCentroid});
		IDataItem QCentroidDataItem = Factory.createDataItem(
				statisticFact_output, "Q_centroid", QCentroidArray);
		statisticFact_output.addDataItem(QCentroidDataItem);
		return statisticFact_stop;
	}

	private void findStatisticFact(RegionalStatistics statistics) throws InvalidArrayTypeException{
		statistics.findAll();
		double durationTime = 0.;
		IArray centroidArray = statistics.findCentroid();
		IIndex index = centroidArray.getIndex();
		index.set(0);
		centroidArray.setDouble(index, (centroidArray.getDouble(index) + 0.5) * 5.08);
		index.set(1);
		centroidArray.setDouble(index, (centroidArray.getDouble(index) - 0.5) * 5.08);
		statisticFact_output = Statistics.createStatisticsGroup(statistics.findCentroid(), 
				statistics.getCentroidError(), statistics.findRMS(), statistics.getRMSError(), 
				statistics.findRMSWidth(), statistics.getRMSWidthError(),
				statistics.findTotalSum(), statistics.getTotalSumError(), 
				statistics.findMaxCount(),
				statistics.findMinCount(), durationTime, statisticFact_scanData);
		System.out.println(statisticFact_output);
//		statistics.getTotalSumFit(), statistics.getTotalSumFitPeak(),
		
//		double rms = 0.;
//		double totalSum = 0.;
////		Number[] array = (Number[]) sample.copyTo1DJavaArray();
//		int size = size(sample.getShape());
//		double[] error = new double[size];
//		Index index = Factory.createIndex(new int[]{size});
//		double max = 0.;
//		for (int i = 0; i < size; i ++){
//			index.set0(i);
//			double count = sample.getDouble(index);
//			if (count > max) max = count;
//			rms += count * count;
//			totalSum += count;
//			error[i] = Math.sqrt(count);
//		}
//		System.out.println("max = " + max);
////		index.set0()
////		index.
//		rms = Math.sqrt(rms/size);
//		Array rmsArray = Factory.createArray(Double.class, new int[]{1}, new double[]{rms});
////		Factory.createAttribute("rms", rmsArray);
//		DataItem rmsDataItem = Factory.createDataItem(null, statisticFact_output, "rms", rmsArray);
//		Array totalSumArray = Factory.createArray(Double.class, new int[]{1}, new double[]{totalSum});
//		DataItem totalSumDataItem = Factory.createDataItem(null, statisticFact_output, "total_sum", totalSumArray);
//		Array errorArray = Factory.createArray(Double.class, sample.getShape(), error);
//		DataItem errorDataItem = Factory.createDataItem(null, statisticFact_output, "error", errorArray);
//		Array centerArray = SANSStaticLib.findCenterOfMass(sample);
//		DataItem centerDataItem = Factory.createDataItem(null, statisticFact_output, "beam_center", centerArray);
//		statisticFact_output.buildResultGroup(errorDataItem, rmsDataItem, totalSumDataItem, centerDataItem);
		
	}
	
//	private int size(int[] array){
//		int size = 1;
//		for (int i = 0; i < array.length; i ++){
//			size *= array[i];
//		}
//		return size;
//	}
	
	public IGroup getStatisticFact_output() {
		return statisticFact_output;
	}

	public void setStatisticFact_stop(Boolean statisticFact_stop) {
		this.statisticFact_stop = statisticFact_stop;
	}

	public void setStatisticFact_scanData(IGroup statisticFact_scanData) {
		this.statisticFact_scanData = statisticFact_scanData;
	}

}
