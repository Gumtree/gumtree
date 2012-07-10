/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.region;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.mask.Abstract2DMask;
import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.RectangleMask;

import au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion;
import au.gov.ansto.bragg.datastructures.core.region.Region;
import au.gov.ansto.bragg.datastructures.core.region.RegionFactory;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.kakadu.core.data.Operation;

/**
 * @author nxi
 *
 */
public class RegionParameterManager {

	private List<RegionParameter> parameterList = new ArrayList<RegionParameter>();
	
	public RegionParameterManager() {
		
	}
	
	public List<AbstractMask> findMaskList(Operation operation) {
		for (RegionParameter parameter : parameterList) {
			if (parameter.getOperation() == operation) {
				return parameter.getMaskList();
			}
		}
		return null;
	}
	
	public List<RegionParameter> getParameterList() {
		return parameterList;
	}
	
	public void addParameter(RegionParameter parameter) {
		parameterList.add(parameter);
	}
	
	public void removeParameter(RegionParameter parameter) {
		parameterList.remove(parameter);
	}

	public RegionParameter findParameter(Operation operation) {
		for (RegionParameter parameter : parameterList) {
			if (parameter.getOperation() == operation) {
				return parameter;
			}
		}
		return null;
	}
	
	public static List<AbstractMask> convertToUIObject(IGroup group) {
		List<AbstractMask> regions = new ArrayList<AbstractMask>();

		//parse regions
//		if (group != null) {
//		for (Iterator<?> iterator = group.getDataItems().iterator(); iterator.hasNext();) {
//		DataItem region = (DataItem) iterator.next();
//		Attribute regionType = region.findAttribute("geometry");
//		if (regionType.getStringValue().matches("rectilineal")){
//		double[] reference = (double[]) region.findAttribute("reference").getValue().copyTo1DJavaArray();
//		double[] range = (double[]) region.findAttribute("range").getValue().copyTo1DJavaArray();
//		boolean isInclusive =  ((boolean[]) region.findAttribute("inclusive").getValue().copyTo1DJavaArray())[0];

//		regions.add(new UIRegion(isInclusive, (int) reference[1], (int) reference[0], 
//		(int) (reference[1] + range[1]), (int) (reference[0] + range[0]) ) );
//		}			
//		}

//		}
		if (group != null) {
//			List<Region> regionList = ((RegionSet) group).getRegionList();
			List<Region> regionList = RegionUtils.getRegionFromGroup(group);
			for (Region region : regionList){
				if (region instanceof RectilinearRegion) {
					RectilinearRegion rectilinearRegion = (RectilinearRegion) region;
					double[] reference = rectilinearRegion.getPrimaryPhysicalReference();
					double[] range = rectilinearRegion.getPrimaryPhysicalRange();
					boolean isInclusive = false;
					try {
						isInclusive = rectilinearRegion.isInclusive();
					} catch (Exception e) {
						e.printStackTrace();
					}
//					regions.add(new UIRegion(isInclusive, (int) reference[1], (int) reference[0], 
//							(int) (reference[1] + range[1]), (int) (reference[0] + range[0]) ) );
					AbstractMask mask = new RectangleMask(isInclusive, reference[1], reference[0], 
							range[1], range[0]);
					mask.setName(region.getShortName());
					regions.add(mask);
				}
			}
		}
		return regions;
	}
	
	public static IGroup createRegionSet(List<AbstractMask> maskList) throws Exception {
		IGroup regionSet = RegionFactory.createRegionSet(
				Factory.createEmptyDatasetInstance().getRootGroup(), "regionSet");


		for (AbstractMask mask : maskList) {
			if (mask instanceof Abstract2DMask) {
				Rectangle2D region = ((Abstract2DMask) mask).getRectangleFrame();
				System.err.println("RegionUtil.java: convertToServerObjecT() isInclusive: " + mask.isInclusive());
				RegionFactory.createRectilinearRegion(regionSet, mask.getName(), 
						new double[]{region.getMinY(), region.getMinX()}, new double[]{region.getMaxY() 
					- region.getMinY(), region.getMaxX() - region.getMinX()}, new String [] { 
					"degrees", "counts"}, mask.isInclusive()); 
			}

		}
		return regionSet;
	}

}
