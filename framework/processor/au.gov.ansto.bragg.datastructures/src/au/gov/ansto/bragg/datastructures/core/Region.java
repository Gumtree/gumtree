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

import java.io.IOException;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.exception.CreateRegionFailedException;
import au.gov.ansto.bragg.datastructures.core.exception.NoSuchGeometryTypeException;

/**
 * 
 * @author nxi
 * Created on 20/03/2008
 * @deprecated on 20/03/08
 */
public final class Region {

//	public enum GeometryType{rectilineal, radial, polygonal} 
	public enum GeometryType{rectilineal}
//	public enum InclusiveType{inclusive, exclusive}

	private Region(){}
	
	/**
	 * Create a temporary dataset holder of the region sets.
	 * @throws Exception 
	 */
	public static IDataset regionDatasetFactory() throws IOException {
		return Factory.createEmptyDatasetInstance();
	}

	/**
	 * Add a region set into the region set dataset holder.
	 * @param dataset a temporary dataset holder.
	 * @return
	 */
	public static IGroup addRegionSet(final IDataset dataset){
		List<IGroup> groupList = dataset.getRootGroup().getGroupList();
		String regionName = "region_set_" + groupList.size();

//		regionName = regionName.
		IGroup rootGroup = dataset.getRootGroup();
		IGroup regionSet = Factory.createGroup(rootGroup, regionName, true);
		rootGroup.addSubgroup(regionSet);
		return regionSet;
	}

	/**
	 * Create an empty region set which use a temporary dataset holder.
	 * @return a region set in Group type
	 * @throws Exception failed to create the region set
	 */
	public static IGroup createEmptyRegionSet() throws Exception{
		IDataset regionDataset = regionDatasetFactory();
		return addRegionSet(regionDataset);
	}
	/**
	 * Add a region to the region set by providing the necessary required data, e.g.,
	 * vertices, physical reference, physical region, geometry type and inclusive type.
	 * @param regionSet the parent region set as a Group
	 * @param name region name in String type
	 * @param type GeometryType, only takes rectilineal for right now
	 * @param inclusive boolean value, true if the region is included in use, 
	 * false if the region is excluded in use
	 * @param horizontalUnit horizontal unit in String type
	 * @param verticalUnit vertical unit in String type
	 * @param reference physical reference in 1D primary double array type
	 * @param range physical region in 1D primary double array type
	 * @return a region as a DataItem 
	 * @throws InvalidArrayTypeException 
	 * @throws NoSuchGeometryTypeException  
	 * @throws CreateRegionFailedException 
	 */
	public static IDataItem addRegion(IGroup regionSet, String name, String type, 
			boolean inclusive, double[] reference, double[] range, String dimensionUnit0,
			String dimensionUnit1) 
	throws NoSuchGeometryTypeException, CreateRegionFailedException {
		GeometryType geometryType;
		try{
			geometryType = GeometryType.valueOf(type);
		}catch (Exception ex){
			throw new NoSuchGeometryTypeException("can not create region in " + type + " type");
		}
		IArray referenceArray = Factory.createArray(reference);
		IArray rangeArray = Factory.createArray(range);
		double[][] vertices = null;
		switch(geometryType){
		case rectilineal : 
			vertices = new double[4][2];
			reference[0] = range[0] >= 0 ? reference[0] : reference[0] + range[0];
			reference[1] = range[1] >= 0 ? reference[1] : reference[1] + range[1];
			range[0] = Math.abs(range[0]);
			range[1] = Math.abs(range[1]);
			vertices[0][0] = reference[0];
			vertices[0][1] = reference[1];
			vertices[1][0] = reference[0];
			vertices[1][1] = reference[1] + range[1];
			vertices[2][0] = reference[0] + range[0];
			vertices[2][1] = vertices[1][1];
			vertices[3][0] = vertices[2][0];
			vertices[3][1] = reference[1];
//			vertices[0][0] = range[0] >= 0 ? reference[0] : reference[0] + range[0];
//			vertices[0][1] = range[1] >= 0 ? reference[1] : reference[1] + range[1];
//			vertices[1][0] = vertices[0][0];
//			vertices[1][1] = range[1] < 0 ? reference[1] : reference[1] + range[1];
//			vertices[2][0] = range[0] < 0 ? reference[0] : reference[0] + range[0];
//			vertices[2][1] = vertices[1][1];
//			vertices[3][0] = vertices[2][0];
//			vertices[3][1] = vertices[0][1];
//			default: 
		}
		IArray verticesArray = Factory.createArray(vertices);
		IArray inclusiveArray = Factory.createArray(new boolean[]{inclusive});
		IAttribute referenceAttribute = Factory.createAttribute("reference", referenceArray);
		IAttribute rangeAttribute = Factory.createAttribute("range", rangeArray);
		IAttribute geometryTypeAttribute = Factory.createAttribute("geometry", type.toString());
		IAttribute inclusiveTypeAttribute = Factory.createAttribute("inclusive", inclusiveArray);
		IAttribute unitAttribute = Factory.createAttribute("units", 
				dimensionUnit0 + ":" + dimensionUnit1);
		IDataItem region = null;
		try {
			region = Factory.createDataItem(regionSet.getDataset(), 
					regionSet, name, verticesArray);
		} catch (InvalidArrayTypeException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			throw new CreateRegionFailedException(e);
		}
		region.addOneAttribute(referenceAttribute);
		region.addOneAttribute(rangeAttribute);
		region.addOneAttribute(geometryTypeAttribute);
		region.addOneAttribute(inclusiveTypeAttribute);
		region.addOneAttribute(unitAttribute);
		regionSet.addDataItem(region);
		return region;
	}

	/**
	 * Add multiple regions into a region set.
	 * @param regionSet a region set in Group type
	 * @param regions unlimited region items in DataItem type
	 */
	public static void addRegions(IGroup regionSet, IDataItem ...regions ){
		for (int i = 0; i < regions.length; i++) {
			regionSet.addDataItem(regions[i]);
		}
	}

	public static IDataItem addPolygonRegion(IGroup regionSet, String name, boolean inclusive){
		return null;
	}
	
	public static IDataItem addRectilinearRegion(IGroup regionSet, String name, boolean inclusive, 
			int[] pixelReference, int[] pixelRange, IGroup frame) 
	throws NoSuchGeometryTypeException, CreateRegionFailedException{
		String type = "rectilineal"; 
		double[] physicalReference = new double[pixelReference.length];
		double[] physicalRange = new double[pixelRange.length];
		String[] units = new String[2];
		boolean physicallyConverted = false; 
//		double[] reference, double[] range, String horizontalUnit,	String verticalUnit
		if (frame != null){
			IDataItem signal = ((NcGroup) frame).findSignal();
			if (signal != null){
				List<IDataItem> axes = ((NcGroup) frame).findAxes();
				physicallyConverted = physicallyConvert(physicalReference, physicalRange, 
						pixelReference, pixelRange, units, axes);
			}
		}
		if (!physicallyConverted) {
			for (int i = 0; i < 2; i ++){
				physicalReference[i] = (int) pixelReference[i];
				physicalRange[i] = (int) pixelRange[i];
				units[i] = "pixel";
			}
		}
		validateRegion(physicalReference, physicalRange);
		return addRegion(regionSet, name, type, inclusive, physicalReference, 
				physicalRange, units[0], units[1]);
	}
//	public static DataItem addRegion(Group regionSet, String name, GeometryType type, 
//	double[][] vertices) throws InvalidArrayTypeException{
//	Array referenceArray = Factory.createArray(reference);
//	Array rangeArray = Factory.createArray(range);
//	double[][] vertices = null;
//	Array verticesArray = Factory.createArray(vertices);
//	Attribute referenceAttribute = Factory.createAttribute("reference", referenceArray);
//	Attribute rangeAttribute = Factory.createAttribute("range", rangeArray);
//	Attribute geometryTypeAttribute = Factory.createAttribute("type", type.toString());
//	DataItem region = Factory.createDataItem(regionSet.getDataset(), 
//	regionSet, name, verticesArray);
//	region.addOneAttribute(referenceAttribute);
//	region.addOneAttribute(rangeAttribute);
//	region.addOneAttribute(geometryTypeAttribute);
//	regionSet.addDataItem(region);
//	return region;
//	return null;
//	}
//	public static 

	private static void validateRegion(double[] physicalReference,
			double[] physicalRange) {
		// TODO Auto-generated method stub
		for (int i = 0; i < physicalRange.length; i++) {
			if (physicalRange[i] < 0){
				physicalReference[i] = physicalReference[i] + physicalRange[i];
				physicalRange[i] = 0 - physicalRange[i];
			}
		}
		
	}

	private static boolean physicallyConvert(double[] physicalReference,
			double[] physicalRange, int[] pixelReference, int[] pixelRange, 
			String[] units, List<IDataItem> axes) {
		// TODO Auto-generated method stub
		if (axes.size() == 2){
			IArray array = null;
			for (int i = 0; i < 2; i ++){
				IDataItem axis = axes.get(i);
				try {
					array = axis.getData();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				int[] shape = array.getShape();
				if (shape.length == 1 || ( shape.length == 2 && shape[0] == 1 )){
					int size = (int) array.getSize();
					IIndex index = array.getIndex();
					if (pixelReference[i] > size) return false;
					physicalReference[i] = array.getDouble(index.set(pixelReference[i]));
					if (pixelReference[i] + pixelRange[i] < size){ 
						index.set(pixelReference[i] + pixelRange[i]);
					}else
						index.set(size);
					physicalRange[i] = array.getDouble(index) - physicalReference[i];
					units[i] = axis.getUnitsString();
				}
				else return false;
			}
			return true;
		}
		return false;
	}
}
