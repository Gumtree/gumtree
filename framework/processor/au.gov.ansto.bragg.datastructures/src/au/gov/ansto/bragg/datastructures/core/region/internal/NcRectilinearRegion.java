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
package au.gov.ansto.bragg.datastructures.core.region.internal;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.RegionType;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;

/**
 * @author nxi
 * Created on 16/04/2008
 */
public class NcRectilinearRegion extends NcRegion implements RectilinearRegion {

	/**
	 * @param parent
	 * @param shortName
	 * @param regionType
	 * @throws InvalidArrayTypeException 
	 */
	public NcRectilinearRegion(IGroup parent, String shortName,
			double[] physicalReference, double[] physicalRange, String[] units, boolean isInclusive) 
	throws InvalidArrayTypeException {
		super(parent, shortName, RegionType.rectilinear, units, isInclusive);
		if (physicalReference.length != physicalRange.length)
			throw new InvalidArrayTypeException("illegal physical reference or range");
		IArray referenceArray = Factory.createArray(physicalReference);
		IArray rangeArray = Factory.createArray(physicalRange);
		IDataItem referenceDataItem = Factory.createDataItem(this, REFERENCE_NAME, 
				referenceArray);
		IDataItem rangeDataItem = Factory.createDataItem(this, RANGE_NAME, rangeArray);
		addDataItem(referenceDataItem);
		addDataItem(rangeDataItem);
		// TODO Auto-generated constructor stub
	}

	public NcRectilinearRegion(IGroup from){
		super(from);
		
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion#getPhysicalRange()
	 */
	public IArray getPhysicalRange() {
		// TODO Auto-generated method stub
		IArray range;
		try {
			range = findDataItem(RANGE_NAME).getData();
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		return range;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion#getPhysicalReference()
	 */
	public IArray getPhysicalReference() {
		// TODO Auto-generated method stub
		IArray reference;
		try {
			reference = findDataItem(REFERENCE_NAME).getData();
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		return reference;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion#getPrimaryPhysicalRange()
	 */
	public double[] getPrimaryPhysicalRange() {
		// TODO Auto-generated method stub
		IArray range = getPhysicalRange();
		if (range != null) 
			try {
				return (double[]) range.getArrayUtils().copyTo1DJavaArray();	
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			}
			return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion#getPrimaryPhysicalReference()
	 */
	public double[] getPrimaryPhysicalReference() {
		// TODO Auto-generated method stub
		IArray reference = getPhysicalReference();
		if (reference != null) 
			try {
				return (double[]) reference.getArrayUtils().copyTo1DJavaArray();	
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			}
			return null;
	}

	public void applyToTruthMap(IArray truthMap, List<IDataItem> axes) 
	throws StructureTypeException {
		// TODO Auto-generated method stub
		if (truthMap.getElementType() != boolean.class)
			throw new StructureTypeException("bad truth map");
		double[] physicalReference = getPrimaryPhysicalReference();
		double[] physicalRange = getPrimaryPhysicalRange();
		int[] shape = truthMap.getShape();
		if (shape.length != physicalReference.length)
			throw new StructureTypeException("region dimensions not match");
		List<int[]> binRegion = convertToBinRegion(physicalReference, 
				physicalRange, axes);
		int[] binReference = binRegion.get(0);
		int[] binRange = binRegion.get(1);
		boolean isInclusive = isInclusive();
		try {
			applyToTruthMap(truthMap, binReference, binRange, isInclusive);
		} catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			throw new StructureTypeException("region is out of range");
		}
	}

	private void applyToTruthMap(IArray truthMap, int[] binReference,
			int[] binRange, boolean flag) throws InvalidRangeException {
		// TODO Auto-generated method stub
//		Index index = truthMap.getIndex();
//		Array section = null;
//		try {
//			section = truthMap.section(binReference, binRange);
//		} catch (InvalidRangeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		ArrayIterator iterator = section.getIterator();
//		for (int i = 0; i < binRange.length; i++) {
//			if (binReference[i] < 0)
//				binReference[i] = 0;
//			if (binRange[i] > )
//		}
		int[] newReference = new int[2];
		int[] newRange = new int[2];
		int[] shape = truthMap.getShape();
		if (newReference[0] < 0)
			newReference[0] = 0;
		else
			newReference[0] = binReference[0];
		if (newReference[1] < 0)
			newReference[1] = 0;
		else
			newReference[1] = binReference[1];
		
		if (binReference[0] + binRange[0] > shape[0])
			newRange[0] = shape[0] - binReference[0];
		else 
			newRange[0] = binRange[0];
		if (binReference[1] + binRange[1] > shape[1])
			newRange[1] = shape[1] - binReference[1];
		else 
			newRange[1] = binRange[1];		
		IArrayIterator iterator = truthMap.getRegionIterator(newReference, newRange);
		while(iterator.hasNext())
			iterator.next().setBooleanCurrent(flag);
	}

	private List<int[]> convertToBinRegion(double[] physicalReference,
			double[] physicalRange, List<IDataItem> axes) {
		List<int[]> binRegion = new ArrayList<int[]>(2);
		int[] binReference = new int[physicalReference.length];
		int[] binRange = new int[physicalRange.length];
		binRegion.add(binReference);
		binRegion.add(binRange);

		for (int i = 0; i < physicalRange.length; i++) {
			IDataItem axis = null;
			try {
				axis = axes.get(i);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (axis == null) {
				binReference[i] = (int) Math.ceil(physicalReference[i]);
				binRange[i] = (int) Math.floor(physicalRange[i] + physicalReference[i]) -
				binReference[i];
			}
			else{
				IArray axisArray = null;
				try {
					axisArray = axis.getData();
				} catch (Exception e) {
					binReference[i] = (int) Math.ceil(physicalReference[i]);
					binRange[i] = (int) Math.floor(physicalRange[i] + physicalReference[i]) -
					binReference[i];
				}
				double[] physicalBoundary = new double[]{physicalReference[i], 
						physicalReference[i] + physicalRange[i]};
				int[] binBoundary = RegionUtils.convertBoundary(physicalBoundary, axisArray);
				binReference[i] = binBoundary[0];
				binRange[i] = binBoundary[1] - binBoundary[0];
			}
		}
		return binRegion;
	}


	@Override
	public int getRank() {
		return (int) getPhysicalReference().getSize();
	}
	
	public double[] getPhysicalSection(int dimension){
		double[] section = new double[2];
		section[0] = getPrimaryPhysicalReference()[dimension];
		section[1] = section[0] + getPrimaryPhysicalRange()[dimension];
		return section;
	}

	public void setPhysicalRange(double[] range) throws StructureTypeException {
		// TODO Auto-generated method stub
		IDataItem rangeDataItem = findDataItem(RANGE_NAME);
		if (rangeDataItem != null)
			try {
				rangeDataItem.setCachedData(Factory.createArrayNoCopy(range), false);
			} catch (InvalidArrayTypeException e) {
				// TODO Auto-generated catch block
				throw new StructureTypeException("can not set range");
			}
	}

	public void setPhysicalReference(double[] reference) throws StructureTypeException {
		// TODO Auto-generated method stub
		IDataItem referenceDataItem = findDataItem(REFERENCE_NAME);
		if (referenceDataItem != null)
			try {
				referenceDataItem.setCachedData(Factory.createArrayNoCopy(reference), false);
			} catch (InvalidArrayTypeException e) {
				// TODO Auto-generated catch block
				throw new StructureTypeException("can not set reference");
			}
	}
	
	public NcRectilinearRegion clone(){
		return new NcRectilinearRegion(this);
	}
	
//	@Override
//	public boolean equals(Object oo) {
//		if (super.equals(oo)) {
//			if (oo instanceof NcRectilinearRegion) {
//				try {
//					Array range = getPhysicalRange();
//					Array ooRange = ((NcRectilinearRegion) oo).getPhysicalRange();
//					if (!range.equals(ooRange)) {
//						return false;
//					}
//					Array reference = getPhysicalReference();
//					Array ooReference = ((NcRectilinearRegion) oo).getPhysicalReference();
//					if (!reference.equals(ooReference)) {
//						return false;
//					}
//				} catch (Exception e) {
//					return false;
//				}
//				return true;
//			} else return true;
//		}
//		return false;
//	}
}
