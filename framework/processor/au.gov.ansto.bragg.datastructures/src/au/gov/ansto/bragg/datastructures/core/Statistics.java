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
package au.gov.ansto.bragg.datastructures.core;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

/**
 * 
 * @author nxi
 * Created on 20/03/2008
 * @deprecated on 20/03/08
 */
public class Statistics {

	public static IGroup createStatisticsGroup(IArray centroid, IArray centroidError, 
			double rms, double rmsError, IArray rmsWidth, IArray rmsWidthError, IArray totalSum, 
			IArray totalSumError, //Array totalSumFit, Array totalSumFitPeak, 
			double maxCount, double minCount, double countDuration, 
			IGroup parent) throws InvalidArrayTypeException {
		IGroup statisticGroup = Factory.createGroup(parent.getDataset(), 
				parent, "regional_statistics", true);
		IArray rmsArray = Factory.createArray(Double.class, new int[]{1}, new double[]{rms});
		IArray rmsErrorArray = Factory.createArray(Double.class, new int[]{1}, new double[]{rmsError});
		IDataItem rmsDataItem = Factory.createDataItem(null, statisticGroup, "rms", rmsArray);
		IDataItem rmsErrorDataItem = Factory.createDataItem(null, statisticGroup, "rms_error", rmsErrorArray);
		IDataItem rmsWidthDataItem = Factory.createDataItem(null, statisticGroup, "rms_width", rmsWidth);
		IDataItem rmsWidthErrorDataItem = Factory.createDataItem(null, statisticGroup, "rms_width_error", rmsWidthError);
//		Array totalSumArray = Factory.createArray(Double.class, new int[]{1}, new double[]{totalSum});
		IDataItem totalSumDataItem = Factory.createDataItem(null, statisticGroup, "total_sum", totalSum);
//		Attribute totalSumFitAtt = Factory.createAttribute("fitting", totalSumFit);
//		Attribute totalSumFitPeakAtt = Factory.createAttribute("fitting_peak", totalSumFitPeak);
//		totalSumDataItem.addOneAttribute(totalSumFitAtt);
//		totalSumDataItem.addOneAttribute(totalSumFitPeakAtt);
//		Array totalSumErrorArray = Factory.createArray(Double.class, new int[]{1}, new double[]{totalSumError});
		IDataItem totalSumErrorDataItem = Factory.createDataItem(null, statisticGroup, "total_sum_error", totalSumError);
//		Array errorArray = Factory.createArray(Double.class, sample.getShape(), error);
//		DataItem errorDataItem = Factory.createDataItem(null, statisticFact_output, "error", errorArray);
		IDataItem centroidDataItem = Factory.createDataItem(null, statisticGroup, "centroid", centroid);
		IDataItem centroidErrorDataItem = Factory.createDataItem(null, statisticGroup, "centroid_error", centroidError);
		IArray maxCountArray = Factory.createArray(Double.class, new int[]{1}, new double[]{maxCount});
		IDataItem maxCountDataItem = Factory.createDataItem(null, statisticGroup, "maximum_count", maxCountArray);
		IArray minCountArray = Factory.createArray(Double.class, new int[]{1}, new double[]{minCount});
		IDataItem minCountDataItem = Factory.createDataItem(null, statisticGroup, "minimum_count", minCountArray);
		IArray countDurationArray = Factory.createArray(Double.class, new int[]{1}, new double[]{countDuration});
		IDataItem countDurationDataItem = Factory.createDataItem(null, statisticGroup, "count_duration", countDurationArray);
		((NcGroup) statisticGroup).buildResultGroup(centroidDataItem, centroidErrorDataItem, rmsDataItem, rmsErrorDataItem, 
				rmsWidthDataItem, rmsWidthErrorDataItem, totalSumDataItem, totalSumErrorDataItem, 
				maxCountDataItem, minCountDataItem, countDurationDataItem);
		return statisticGroup;
	}
}
