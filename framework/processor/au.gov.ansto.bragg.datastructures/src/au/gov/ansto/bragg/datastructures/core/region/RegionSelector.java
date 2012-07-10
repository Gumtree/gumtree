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
package au.gov.ansto.bragg.datastructures.core.region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

public class RegionSelector {


	/**
	 * 
	 * @param inputData
	 * @param regionSet
	 * @return
	 * @throws IOException
	 * Created on 18/06/2008
	 * @deprecated use RegionUtils instead.
	 * @see RegionUtils
	 */
	public static IArray applyInterestedRegion(IGroup inputData, IGroup regionSet) throws IOException {
		// TODO Auto-generated method stub
		IDataItem signal = ((NcGroup) inputData).findSignal();
		boolean[][] signalMap = null;
		int[] shape = signal.getShape();
		if (shape.length == 2){
			signalMap = new boolean[shape[0]][shape[1]];
		}
		List<IDataItem> axes = ((NcGroup) inputData).findAxes();
		List<IDataItem> regions = regionSet.getDataItemList();
		if (regions.size() > 0){
			List<IDataItem> inclusiveRegions = new ArrayList<IDataItem>();
			List<IDataItem> exclusiveRegions = new ArrayList<IDataItem>();
			for (Iterator<?> iterator = regions.iterator(); iterator.hasNext();) {
				IDataItem region = (IDataItem) iterator.next();
				if (isInclusive(region)){
					inclusiveRegions.add(region);
				}else{
					exclusiveRegions.add(region);
				}
			}
			if (inclusiveRegions.size() == 0){
				fillSignalMap(signalMap);
			}
			for (Iterator<?> iterator = inclusiveRegions.iterator(); iterator.hasNext();) {
				IDataItem region = (IDataItem) iterator.next();
				IAttribute regionType = region.getAttribute("geometry");
				if (regionType.getStringValue().equals("rectilineal")){
					double[] reference = (double[]) region.getAttribute("reference").getValue().
					getArrayUtils().copyTo1DJavaArray();
					double[] range = (double[]) region.getAttribute("range").getValue().
					getArrayUtils().copyTo1DJavaArray();
					int[][] rangeInt = new int[2][2];
					boolean physicallyConverted = false;
					if (shape.length == 2){
						physicallyConverted = physicallyConvert(axes, rangeInt, region, reference, range);
					}
					if (!physicallyConverted){
						rangeInt[0][0] = (int) Math.ceil(reference[0]);
						rangeInt[0][1] = (int) Math.floor(reference[0] + range[0]);
						rangeInt[1][0] = (int) Math.ceil(reference[1]);
						rangeInt[1][1] = (int) Math.floor(reference[1] + range[1]);

					}
					addRectilinealMap(signalMap, rangeInt);
				}
			}
			for (Iterator<?> iterator = exclusiveRegions.iterator(); iterator.hasNext();) {
				IDataItem region = (IDataItem) iterator.next();
				IAttribute regionType = region.getAttribute("geometry");
				if (regionType.getStringValue().matches("rectilineal")){
					double[] reference = (double[]) region.getAttribute("reference").getValue().
					getArrayUtils().copyTo1DJavaArray();
					double[] range = (double[]) region.getAttribute("range").getValue().
					getArrayUtils().copyTo1DJavaArray();
					int[][] rangeInt = new int[2][2];
					boolean physicallyConverted = false;
					if (shape.length == 2){
						physicallyConverted = physicallyConvert(axes, rangeInt, region, reference, range);
					}
					if (!physicallyConverted){
						rangeInt[0][0] = (int) Math.ceil(reference[0]);
						rangeInt[0][1] = (int) Math.floor(reference[0] + range[0]);
						rangeInt[1][0] = (int) Math.ceil(reference[1]);
						rangeInt[1][1] = (int) Math.floor(reference[1] + range[1]);

					}
					removeRectilinealMap(signalMap, rangeInt);
				}			
			}

			return ApplyMap(signal, signalMap);
		}else
			return signal.getData();
	}

	/**
	 * 
	 * @param signalMap
	 * Created on 17/04/2008
	 * @deprecated use filleTruthMap() instead.
	 */
	static void fillSignalMap(boolean[][] signalMap) {
		// TODO Auto-generated method stub
		for (int i = 0; i < signalMap.length; i++) {
			for (int j = 0; j < signalMap[0].length; j++) {
				signalMap[i][j] = true;
			}
		}
	}
	
	static void fillTruthMap(IArray truthMap, boolean flag) {
		// TODO Auto-generated method stub
		if (truthMap.getElementType() != boolean.class)
			return;
		IArrayIterator iterator = truthMap.getIterator();
		while(iterator.hasNext()){
			iterator.next().setBooleanCurrent(flag);
		}
	}
	
	static boolean physicallyConvert(List<IDataItem> axes,
			int[][] rangeInt, IDataItem region, double[] reference, double[] range) throws IOException {
		// TODO Auto-generated method stub
		if (axes.size() >= 2){
			IAttribute regionUnits = region.getAttribute("units");
			if (regionUnits != null){
				String[] regionUnitsArray = regionUnits.getStringValue().split(":");
				for (int i = 0; i < 2; i ++){
					IDataItem axis = axes.get(i);
					String unit = axis.getUnitsString();
					if (unit.equals(regionUnitsArray[i])){
						double[] axisArray = (double[]) axis.getData().getArrayUtils().copyTo1DJavaArray();
						rangeInt[i][0] = indexAfter(axisArray, reference[i]);
						rangeInt[i][1] = indexBefore(axisArray, reference[i] + range[i]);
					}
				}
			}
		}
		return false;
	}

	static IArray ApplyMap(IDataItem signal, boolean[][] signalMap) throws IOException {
		// TODO Auto-generated method stub
		IArray signalArray = signal.getData();
		int[] shape = signalArray.getShape();
		double[] cach = new double[(int) signal.getSize()];
		if (shape.length == 2){
//			Index index = Factory.createIndex(shape);
			IIndex index = signalArray.getIndex();
			for (int i = 0; i < signalMap.length; i ++)
				for (int j = 0; j < signalMap[0].length; j++) {
					index.set(i, j);
					if (signalMap[i][j])
						cach[i * shape[1] + j] = signalArray.getDouble(index);
					else
						cach[i * shape[1] + j] = Double.NaN;
				}
		}
		return Factory.createArray(Double.class, shape, cach);
	}

	static int indexBefore(double[] axis, double value) {
		for (int i = 0; i < axis.length; i ++){
			if (axis[i] >= value) return i - 1;
		}
		return axis.length;
	}

	static int indexAfter(double[] axis, double value) {
		// TODO Auto-generated method stub
		for (int i = 0; i < axis.length; i ++){
			if (axis[i] >= value) return i;
		}
		return axis.length;
	}

	static void addRectilinealMap(boolean[][] signalMap, int[][] rangeInt) {
		// TODO Auto-generated method stub
		rangeInt[0][0] = rangeInt[0][0] < 0 ? 0 : rangeInt[0][0];
		rangeInt[0][1] = rangeInt[0][1] < signalMap.length ? rangeInt[0][1] : signalMap.length - 1;
		rangeInt[1][0] = rangeInt[1][0] < 0 ? 0 : rangeInt[1][0];
		rangeInt[1][1] = rangeInt[1][1] < signalMap[0].length ? rangeInt[1][1] : signalMap[0].length - 1;
		for (int i = rangeInt[0][0]; i <= rangeInt[0][1]; i ++) 
			for (int j = rangeInt[1][0]; j <= rangeInt[1][1]; j++) {
				if (i < signalMap.length && j < signalMap[0].length)
					signalMap[i][j] = true;
			}
	}

	static void removeRectilinealMap(boolean[][] signalMap, int[][] rangeInt) {
		// TODO Auto-generated method stub
		rangeInt[0][0] = rangeInt[0][0] < 0 ? 0 : rangeInt[0][0];
		rangeInt[0][1] = rangeInt[0][1] < signalMap.length ? rangeInt[0][1] : signalMap.length - 1;
		rangeInt[1][0] = rangeInt[1][0] < 0 ? 0 : rangeInt[1][0];
		rangeInt[1][1] = rangeInt[1][1] < signalMap[0].length ? rangeInt[1][1] : signalMap[0].length - 1;
		for (int i = rangeInt[0][0]; i <= rangeInt[0][1]; i ++) 
			for (int j = rangeInt[1][0]; j <= rangeInt[1][1]; j++) {
				signalMap[i][j] = false;
			}
	}

	static boolean isInclusive(IDataItem region) {
		// TODO Auto-generated method stub
		IAttribute regionType = region.getAttribute("inclusive");
		boolean inclusive = ((boolean[]) regionType.getValue().getArrayUtils().copyTo1DJavaArray())[0];
		return inclusive;
	}


}
