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
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.RegionType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;
import au.gov.ansto.bragg.datastructures.core.region.internal.NcRectilinearRegion;


@SuppressWarnings("deprecation")
public class RegionUtils {

	/**
	 * Get the region type of a given Region object
	 * @param group
	 * @return RegionType enum value
	 * Created on 17/04/2008
	 */
	public static RegionType getRegionType(IGroup group){
		RegionType regionType;
		try {
			regionType = RegionType.valueOf(group.getAttribute(StaticDefinition.
					REGION_TYPE).getStringValue());
		} catch (Exception e) {
			return RegionType.undefined; 
		} 
		return regionType;	
	}

	/**
	 * Get all regions from a GDM Group
	 * @param group GDM Group
	 * @return List of Region objects
	 * Created on 18/06/2008
	 */
	public static List<Region> getRegionFromGroup(IGroup group){
		if (group instanceof RegionSet) 
			return ((RegionSet) group).getRegionList();
		List<Region> regionList = new ArrayList<Region>();
		if (group instanceof Region){
			regionList.add((Region) group);
			return regionList;
		}
		List<IGroup> subGroupList = group.getGroupList();
		for (Iterator<?> iterator = subGroupList.iterator(); iterator.hasNext();) {
			IGroup item = (IGroup) iterator.next();
			if (item instanceof Region) 
				regionList.add((Region) item);
			else {
				DataStructureType structureType = Util.getDataStructureType(item);
				if (structureType == DataStructureType.regionset){
					List<Region> subRegionList = getRegionFromGroup(item);
					regionList.addAll(subRegionList);
				}
				if (structureType == DataStructureType.region){
					RegionType regionType = getRegionType(item);
					RectilinearRegion region = null;
					switch (regionType){
					case rectilinear:
						try{
							region = new NcRectilinearRegion(item);
						}catch (Exception e) {
							e.printStackTrace();
						}
						break;
					default: break;
					}
					if (region != null) 
						regionList.add(region);
				}
			}
		}
		return regionList;
	}

	public static IGroup applyRegionToGroup(IGroup data, IGroup regionSet) 
	throws StructureTypeException, IOException, PlotFactoryException, 
	InvalidArrayTypeException 
	{
		Plot plot = null;
		DataDimensionType dimensionType;
		String units;
		if (data instanceof Plot){
			plot = (Plot) data;
			dimensionType = PlotUtil.getDimensionType(plot);
			units = plot.findSingal().getUnitsString();
		}else{
			int rank = ((Plot) data).getRank();
			if (rank == 2)
				dimensionType = DataDimensionType.map;
			else if (rank == 3)
				dimensionType = DataDimensionType.mapset;
			else throw new StructureTypeException(
			"Can't apply region on the group, wrong dimension");
			try {
				plot = (Plot) PlotFactory.copyToPlot(data, data.getShortName() + 
						"GroupCopy", dimensionType);
			} catch (PlotFactoryException e) {
				throw new StructureTypeException(e);
			}
			units = plot.findSingal().getUnitsString();
		}
//		Array dataArray = plot.findSignalArray();
//		Array varianceArray = plot.getVariance().getData();
		IArray dataWithRegion = applyRegion(data, regionSet);
		IArray varianceWithRegion = applyRegionToVariance(data, regionSet);
		Plot plotWithRegion = (Plot) PlotFactory.createPlot(data, data.getShortName() + 
				"_withRegion", dimensionType);
		PlotFactory.addDataToPlot(plotWithRegion, plot.findSingal().getShortName(), 
				dataWithRegion, "data with region", units, varianceWithRegion);
		int dimension = 0;
		for (Axis axis : plot.getAxisList()){
			plotWithRegion.addAxis(axis, dimension ++);
		}
		return plotWithRegion;
	}

	private static IArray applyRegionToVariance(IGroup data, IGroup regionSet) 
	throws StructureTypeException{
		if (regionSet == null)
			try {
				return ((NcGroup) data).findSignal().getData();
			} catch (IOException e1) {
				throw new StructureTypeException(e1);
			}
		List<Region> regionList = getRegionFromGroup(regionSet);
//		DataItem dataItem = data.findSignal();
		IDataItem dataItem = null;
		if (data instanceof Plot){
			dataItem = ((Plot) data).getVariance();
			if (dataItem == null)
				return null;
		}
		else {
			IAttribute attribute = ((NcGroup) data).findSignal().getAttribute(
					StaticDefinition.DATA_VARIANCE_REFERENCE_NAME);
			if (attribute == null) 
				return null;
			dataItem = data.findDataItem(attribute.getStringValue());
		}
		if (regionList.size() > 0){
			int[] shape = dataItem.getShape();
			int rank = dataItem.getRank();
			int regionRank = regionList.get(0).getRank();
			if (dataItem.getRank() > regionRank){
				int[] newShpe = new int[regionRank];
				for (int i = 0; i < newShpe.length; i++) {
					newShpe[i] = shape[shape.length - regionRank + i];
				}
				shape = newShpe;
			}
			IArray truthMap = Factory.createArray(boolean.class, shape);
			List<IDataItem> axes = ((NcGroup) data).findAxes();
			if (dataItem.getRank() > regionRank){
				List<IDataItem> newAxes = new ArrayList<IDataItem>();
				for (int i = 0; i < regionRank; i++) {
					newAxes.add(axes.get(rank - regionRank + i));
				}
				axes = newAxes;
			}
			List<Region> inclusiveRegions = new ArrayList<Region>();
			List<Region> exclusiveRegions = new ArrayList<Region>();
			for (Iterator<?> iterator = regionList.iterator(); iterator.hasNext();) {
				Region region = (Region) iterator.next();
				try{
					if (region.isInclusive()) inclusiveRegions.add(region);
					else exclusiveRegions.add(region);
				}catch (Exception e) {}
			}
			if (inclusiveRegions.size() == 0){
				RegionSelector.fillTruthMap(truthMap, true);
			}

			for (Iterator<?> iterator = inclusiveRegions.iterator(); iterator.hasNext();) {
				Region region = (Region) iterator.next();
				region.applyToTruthMap(truthMap, axes);
			}
			for (Iterator<?> iterator = exclusiveRegions.iterator(); iterator.hasNext();) {
				Region region = (Region) iterator.next();
				region.applyToTruthMap(truthMap, axes);
			}			
			return ApplyMap(dataItem, truthMap);
		} else
			try {
				return dataItem.getData();
			} catch (IOException e) {
				throw new StructureTypeException("failed to read source data");
			}
	}
//	private static Array applyRegionToVariance(Group data, Group regionSet) 
//	throws StructureTypeException{
//		List<Region> regionList = getRegionFromGroup(regionSet);
//		DataItem dataItem = null;
//		if (data instanceof Plot){
//			dataItem = ((Plot) data).getVariance();
//			if (dataItem == null)
//				return null;
//		}
//		else {
//			Attribute attribute = data.findSignal().findAttribute(
//					StaticDefinition.DATA_VARIANCE_REFERENCE_NAME);
//			if (attribute == null) 
//				return null;
//			dataItem = data.findDataItem(attribute.getStringValue());
//		}
//		if (regionList.size() > 0){
//			int[] shape = dataItem.getShape();
//			Array truthMap = Factory.createArray(boolean.class, shape);
//			List<DataItem> axes = data.findAxes();
//
//			List<Region> inclusiveRegions = new ArrayList<Region>();
//			List<Region> exclusiveRegions = new ArrayList<Region>();
//			for (Iterator<?> iterator = regionList.iterator(); iterator.hasNext();) {
//				Region region = (Region) iterator.next();
//				try{
//					if (region.isInclusive()) inclusiveRegions.add(region);
//					else exclusiveRegions.add(region);
//				}catch (Exception e) {
//				}
//			}
//			if (inclusiveRegions.size() == 0){
//				RegionSelector.fillTruthMap(truthMap, true);
//			}
//
//			for (Iterator<?> iterator = inclusiveRegions.iterator(); iterator.hasNext();) {
//				Region region = (Region) iterator.next();
//				region.applyToTruthMap(truthMap, axes);
//			}
//			for (Iterator<?> iterator = exclusiveRegions.iterator(); iterator.hasNext();) {
//				Region region = (Region) iterator.next();
//				region.applyToTruthMap(truthMap, axes);
//			}			
//			return ApplyMap(dataItem, truthMap);
//		} else
//			try {
//				return dataItem.getData();
//			} catch (IOException e) {
//				throw new StructureTypeException("failed to read source data");
//			}
//	}


	/**
	 * Apply a region set to a GDM Group data 
	 * @param data GDM Group
	 * @param regionSet GDM Group, assume to be a RegionSet
	 * @return GDM Array
	 * @throws StructureTypeException
	 * Created on 18/06/2008
	 */
	public static IArray applyRegion(IGroup data, IGroup regionSet) 
	throws StructureTypeException{
		if (regionSet == null)
			try {
				return ((NcGroup) data).findSignal().getData();
			} catch (IOException e1) {
				throw new StructureTypeException(e1);
			}
		List<Region> regionList = getRegionFromGroup(regionSet);
		IDataItem dataItem = ((NcGroup) data).findSignal();
		if (regionList.size() > 0){
			int[] shape = dataItem.getShape();
			int rank = dataItem.getRank();
			int regionRank = regionList.get(0).getRank();
			if (dataItem.getRank() > regionRank){
				int[] newShpe = new int[regionRank];
				for (int i = 0; i < newShpe.length; i++) {
					newShpe[i] = shape[shape.length - regionRank + i];
				}
				shape = newShpe;
			}
			IArray truthMap = Factory.createArray(boolean.class, shape);
			List<IDataItem> axes = ((NcGroup) data).findAxes();
			if (dataItem.getRank() > regionRank){
				List<IDataItem> newAxes = new ArrayList<IDataItem>();
				for (int i = 0; i < regionRank; i++) {
					newAxes.add(axes.get(rank - regionRank + i));
				}
				axes = newAxes;
			}
			List<Region> inclusiveRegions = new ArrayList<Region>();
			List<Region> exclusiveRegions = new ArrayList<Region>();
			for (Iterator<?> iterator = regionList.iterator(); iterator.hasNext();) {
				Region region = (Region) iterator.next();
				try{
					if (region.isInclusive()) inclusiveRegions.add(region);
					else exclusiveRegions.add(region);
				}catch (Exception e) {
				}
			}
			if (inclusiveRegions.size() == 0){
				RegionSelector.fillTruthMap(truthMap, true);
			}

			for (Iterator<?> iterator = inclusiveRegions.iterator(); iterator.hasNext();) {
				Region region = (Region) iterator.next();
				region.applyToTruthMap(truthMap, axes);
			}
			for (Iterator<?> iterator = exclusiveRegions.iterator(); iterator.hasNext();) {
				Region region = (Region) iterator.next();
				region.applyToTruthMap(truthMap, axes);
			}			
			return ApplyMap(dataItem, truthMap);
		} else
			try {
				return dataItem.getData();
			} catch (IOException e) {
				throw new StructureTypeException("failed to read source data");
			}
	}

	/**
	 * Apply the truth map on a GDM DataItem. Assume the truth map and the DataItem 
	 * has the same shape. It will create a new storage for the result.
	 * @param data GDM DataItem
	 * @param truthMap GDM Array
	 * @return GDM Array
	 * @throws StructureTypeException
	 * Created on 18/06/2008
	 */
	static IArray ApplyMap(IDataItem data, IArray truthMap) throws StructureTypeException {
		IArray array = null;
		try {
			array = data.getData();
		} catch (IOException e) {
//			e.printStackTrace();
			throw new StructureTypeException("failed to read source data");
		}
		if (array.getSize() < truthMap.getSize())
			throw new StructureTypeException("region size not match");
//		double[] cach = new double[(int) array.getSize()];
//		ArrayIterator dataIterator = array.getIterator();
//		ArrayIterator mapIterator = truthMap.getIterator();
//		int index = 0;
//		while (mapIterator.hasNext() && dataIterator.hasNext() && index < array.getSize()){
//			boolean flag = mapIterator.getBooleanNext();
//			if (flag) {
//				cach[index] = dataIterator.getDoubleNext();
////				if (cach[index] > 0) System.out.println(index);
//			}
//			else{
//				cach[index] = Double.NaN;
//				dataIterator.next();
//			}
//			index ++;
//		}		
//		return Factory.createArray(double.class, array.getShape(), cach);
		try {
			return array.getArrayUtils().eltAnd(truthMap).getArray();
		} catch (ShapeNotMatchException e) {
			throw new StructureTypeException(e);
		}
	}

	/**
	 * Function to calculate the area of a polygon, according to the algorithm
	 * defined at http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
	 * 
	 * @param vList list of points in the polygon
	 * @return area of the polygon defined by pgPoints
	 */
	public static double area(List<Vertex> vList) {

		int i = 0, j = 0, n = vList.size();
		double area = 0;

		for (i = 0; i < n; i++) {
			j = (i + 1) % n;
			area += vList.get(i).getXLoc() * vList.get(j).getYLoc();
			area -= vList.get(j).getXLoc() * vList.get(i).getYLoc();
		}
		area /= 2.0;
		return (area);
	}

	/**
	 * Function to calculate the center of mass for a given polygon, according
	 * ot the algorithm defined at
	 * http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
	 * 
	 * @param vList list of points in the polygon
	 * @return point that is the center of mass
	 */
	public static Vertex centerOfMass(List<Vertex> vList) {

		double cx = 0, cy = 0;
		double area = area(vList);
		// could change this to Point2D.Float if you want to use less memory
		Vertex res = new Vertex();
		int i, j, n = vList.size();

		double factor = 0;
		for (i = 0; i < n; i++) {
			j = (i + 1) % n;
			factor = (vList.get(i).getXLoc() * vList.get(j).getYLoc()
					- vList.get(j).getXLoc() * vList.get(i).getYLoc());
			cx += (vList.get(i).getXLoc() + vList.get(j).getXLoc()) * factor;
			cy += (vList.get(i).getYLoc() + vList.get(j).getYLoc()) * factor;
		}
		area *= 6.0f;
		factor = 1 / area;
		cx *= factor;
		cy *= factor;
		res.setXLoc((int)cx);
		res.setYLoc((int)cy);

		return res;

	}

	/**
	 * Check if a point is inside of a polygon region. The polygon is reference by a list of 
	 * vertex. 
	 * @param vList List of Vertex
	 * @param numVertices in integer
	 * @param pointOfInterest a Vertex value
	 * @return true or false
	 * Created on 18/06/2008
	 */
	public static final boolean isPointInsidePolygon(List<Vertex> vList, int numVertices, Vertex pointOfInterest)
	{
		int counter = 0;
		int i;
		double xinters;
		Vertex p1, p2;

		double scaleFactor = 1.0;

		if (vList.isEmpty() || vList.size() < 3) return false;


		p1 = vList.get(0);

		for (i = 1 ; i <= numVertices; i++) {
			p2 = vList.get(i % numVertices);
			if (pointOfInterest.getYLoc() > Math.min(p1.getYLoc()*scaleFactor,p2.getYLoc()*scaleFactor)) {
				if (pointOfInterest.getYLoc() <= Math.max(p1.getYLoc()*scaleFactor,p2.getYLoc()*scaleFactor)) {
					if (pointOfInterest.getXLoc() <= Math.max(p1.getXLoc()*scaleFactor,p2.getXLoc()*scaleFactor)) {
						if (p1.getYLoc()*scaleFactor != p2.getYLoc()*scaleFactor) {
							xinters = (pointOfInterest.getYLoc()-p1.getYLoc()*scaleFactor)*(p2.getXLoc()*scaleFactor-p1.getXLoc()*scaleFactor)/(p2.getYLoc()*scaleFactor-p1.getYLoc()*scaleFactor)+p1.getXLoc()*scaleFactor;
							if (p1.getXLoc()*scaleFactor == p2.getXLoc()*scaleFactor || pointOfInterest.getXLoc() <= xinters)
								counter++;
						}
					}
				}
			}
			p1 = p2;
		}

		if (counter % 2 == 0) { 
			return false; // outside
		}

		return true; // inside
	}

	public static Plot applyRegionToPlot(Plot plot, Region region) throws StructureTypeException, 
	IOException, PlotFactoryException, InvalidArrayTypeException {
		//TODO not fully implemented
//		int regionRank = region.getRank();
//		int plotRank = plot.getRank();
//		if (plotRank < regionRank){
//			throw new  StructureTypeException("can not apply a region with larger rank");
//		}
//		Plot plotWithRegion = (Plot) PlotFactory.createPlot(plot.getShortName() + 
//				"_withRegion", plot.getDimensionType());
//		if (plotRank == regionRank){
//			
//		}
//		return null;
		return (Plot) applyRegionToGroup(plot, region);
	}
	
	public static int[] convertBoundary(double[] physicalBoundary, IArray axisArray) {
		int[] boundary = new int[2];
		IIndex index = axisArray.getIndex();
		double firstValue = axisArray.getDouble(index.set(0));
		double lastValue = axisArray.getDouble(index.set((int) axisArray.getSize() - 1));
		boolean ascent = firstValue <= lastValue;
		if (ascent){
			boundary[0] = findCeilIndex(axisArray, physicalBoundary[0], 0, 
					(int) index.getSize() - 1, ascent);
			boundary[1] = findFloorIndex(axisArray, physicalBoundary[1], 0, 
					(int) index.getSize() - 1, ascent);
		}else{
			boundary[0] = findCeilIndex(axisArray, physicalBoundary[1], 0, 
					(int) index.getSize() - 1, ascent);
			boundary[1] = findFloorIndex(axisArray, physicalBoundary[0], 0, 
					(int) index.getSize() - 1, ascent);
		}

		return boundary;
	}

	public static int findCeilIndex(IArray axisArray, double value, int startIndex, 
			int endIndex, boolean ascent) {
		IIndex index = axisArray.getIndex();
		if (ascent){
			double first = axisArray.getDouble(index.set(startIndex));
			if (value <= first) return startIndex;
			double last = axisArray.getDouble(index.set(endIndex));
			if (value > last) return endIndex + 1;
			if (value == last) return endIndex;
			if (startIndex >= endIndex - 1) return endIndex;
			int centerIndex = (startIndex + endIndex) / 2;
			double center = axisArray.getDouble(index.set(centerIndex));
			if (value == center) return centerIndex;
			if (value < center) return findCeilIndex(axisArray, value, startIndex, 
					centerIndex, ascent);
			else
				return findCeilIndex(axisArray, value, centerIndex, endIndex, ascent);
		}
		else{
			double first = axisArray.getDouble(index.set(startIndex));
			if (value > first) return startIndex - 1;
			double last = axisArray.getDouble(index.set(endIndex));
			if (value < last) return endIndex;
			if (startIndex >= endIndex - 1) return endIndex;
			int centerIndex = (startIndex + endIndex) / 2;
			double center = axisArray.getDouble(index.set(centerIndex));
			if (value == center) return centerIndex;
			if (value > center) return findCeilIndex(axisArray, value, startIndex, 
					centerIndex, ascent);
			else
				return findCeilIndex(axisArray, value, centerIndex, endIndex, ascent);
		}
//		return 0;
	}

	public static int findFloorIndex(IArray axisArray, double value, int startIndex, 
			int endIndex, boolean ascent) {
		IIndex index = axisArray.getIndex();
		if (ascent){
			double first = axisArray.getDouble(index.set(startIndex));
			if (value <= first) return startIndex;
			double last = axisArray.getDouble(index.set(endIndex));
			if (value > last) return endIndex + 1;
			if (value == last) return endIndex;
			if (startIndex >= endIndex - 1) return startIndex;
			int centerIndex = (startIndex + endIndex) / 2;
			double center = axisArray.getDouble(index.set(centerIndex));
			if (value == center) return centerIndex;
			if (value < center) return findFloorIndex(axisArray, value, startIndex, 
					centerIndex, ascent);
			else
				return findFloorIndex(axisArray, value, centerIndex, endIndex, ascent);
		}
		else{
			double first = axisArray.getDouble(index.set(startIndex));
			if (value > first) return startIndex - 1;
			double last = axisArray.getDouble(index.set(endIndex));
			if (value < last) return endIndex;
			if (startIndex >= endIndex - 1) return endIndex;
			int centerIndex = (startIndex + endIndex) / 2;
			double center = axisArray.getDouble(index.set(centerIndex));
			if (value == center) return centerIndex;
			if (value > center) return findFloorIndex(axisArray, value, startIndex, 
					centerIndex, ascent);
			else
				return findFloorIndex(axisArray, value, centerIndex, endIndex, ascent);
		}
	}

}
