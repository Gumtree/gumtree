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
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.region.Region;
import au.gov.ansto.bragg.datastructures.core.region.RegionSet;

/**
 * @author nxi
 * Created on 16/04/2008
 */
public class NcRegionSet extends NcGroup implements RegionSet {

	public NcRegionSet(IGroup parent, String shortName) {
		super((NcDataset) parent.getDataset(), (NcGroup) parent, shortName, true);
		addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.regionset.name());
	}
	
	public NcRegionSet(IGroup regionSet){
		super(regionSet);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RegionSet#addRegion(au.gov.ansto.bragg.datastructures.core.region.Region)
	 */
	public void addRegion(Region region) {
		if (region instanceof NcRegion) {
			addGroup(region);			
		}
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RegionSet#getRegion(java.lang.String)
	 */
	public Region getRegion(String shortName) throws Exception {
		IGroup group = getGroup(shortName);
		if (group instanceof NcRegion) {
			return (NcRegion) group;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RegionSet#getRegionList()
	 */
	public List<Region> getRegionList() {
		List<Region> regionList = new ArrayList<Region>();
		for (Iterator<?> iterator = groups.iterator(); iterator.hasNext();) {
			IGroup group = (IGroup) iterator.next();
			if (group instanceof Region) {
				regionList.add((Region) group);
			}
		}
		return regionList;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RegionSet#removeRegion(java.lang.String)
	 */
	public void removeRegion(String shortName) {
		removeGroup(shortName);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.RegionSet#removeRegion(au.gov.ansto.bragg.datastructures.core.region.Region)
	 */
	public void removeRegion(Region region) {
		removeGroup(region);
	}

	public NcRegionSet clone(){
		return new NcRegionSet(this);
	}
	
//	@Override
//	public boolean equals(Object oo) {
//		// TODO Auto-generated method stub
//		if (super.equals(oo)) {
//			if (oo instanceof NcRegionSet) {
//				List<Region> currentList = getRegionList();
//				List<Region> ooList = ((NcRegionSet) oo).getRegionList();
//				for (Region region : ooList) {
//					boolean found = false;
//					for (Region curRegion : currentList) {
//						if (region.equals(curRegion)) {
//							found = true;
//							break;
//						}
//					}
//					if (!found) {
//						return false;
//					}
//				}
//				for (Region region : currentList) {
//					boolean found = false;
//					for (Region curRegion : ooList) {
//						if (region.equals(curRegion)) {
//							found = true;
//							break;
//						}
//					}
//					if (!found) {
//						return false;
//					}
//				}
//				return true;
//			} else {
//				return true;
//			}
//		}
//		return false;
//	}
}
