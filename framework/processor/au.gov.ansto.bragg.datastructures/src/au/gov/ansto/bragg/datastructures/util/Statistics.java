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
package au.gov.ansto.bragg.datastructures.util;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.interfaces.IDataItem;

import au.gov.ansto.bragg.datastructures.core.plot.Plot;

/**
 * @author nxi
 * Created on 23/04/2009
 */
public class Statistics {

	public final static String CENTROID_ITEM_NAME = "centroid";
	public final static String TOTALSUM_ITEM_NAME = "total_sum";
	public final static String RMS_ITEM_NAME = "rms";
	public final static String MAX_ITEM_NAME = "max";
	public final static String MIN_ITEM_NAME = "min";
	public final static String AVERAGE_ITEM_NAME = "average";
	public final static String MAX_LOCATION_ITEM_NAME = "max_location";
	public final static String MIN_LOCATION_ITEM_NAME = "min_location";
	
	public static List<IDataItem> findGeneralStatistics(Plot plot){
		List<IDataItem> statistics = new ArrayList<IDataItem>();
		statistics.add(findCentroid(plot));
		statistics.add(findTotalSum(plot));
		statistics.add(findRms(plot));
		statistics.add(findMax(plot));
		statistics.add(findMin(plot));
		statistics.add(findAverage(plot));
		return statistics;
	}

	private static IDataItem findAverage(Plot plot) {
		return null;
	}

	private static IDataItem findMin(Plot plot) {
		return null;
	}

	private static IDataItem findMax(Plot plot) {
		return null;
	}

	private static IDataItem findRms(Plot plot) {
		return null;
	}

	private static IDataItem findTotalSum(Plot plot) {
		return null;
	}

	private static IDataItem findCentroid(Plot plot) {
		return null;
	}
}
