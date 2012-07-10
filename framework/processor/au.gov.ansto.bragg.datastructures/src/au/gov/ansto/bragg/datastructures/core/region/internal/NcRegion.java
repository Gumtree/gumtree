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

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.RegionType;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.region.Region;

/**
 * @author nxi
 * Created on 16/04/2008
 */
public abstract class NcRegion extends NcGroup implements Region {

	protected NcRegion(IGroup parent, String shortName,
			RegionType regionType, String[] units, boolean isInclusive) {
		super((NcDataset) parent.getDataset(), (NcGroup) parent, shortName, true);
		addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.region.name());
		addStringAttribute(StaticDefinition.REGION_TYPE,
				regionType.name());
		addLog("Created");
//		Array unitsArray = Factory.createArray(units);
		String unitString = "";
		for (String unit : units) unitString += unit.trim() + ":";
		if (unitString.endsWith(":")) 
			unitString = unitString.substring(0, unitString.length() - 1);
		addOneAttribute(org.gumtree.data.Factory.createAttribute("units", unitString));
		addOneAttribute(Factory.createAttribute("isInclusive", String.valueOf(isInclusive)));
	}

	protected NcRegion(IGroup from){
//		super((GroupData) from, (NcDataset) from.getDataset());
		super(from);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.Region#getRegionType(org.gumtree.data.gdm.core.Group)
	 */
	public RegionType getRegionType() {
		RegionType regionType;
		try {
			regionType = RegionType.valueOf(findAttribute(StaticDefinition.REGION_TYPE)
					.getStringValue());
		} catch (Exception e) {
			// TODO: handle exception
			return RegionType.undefined; 
		} 
		return regionType;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.Region#getUnits()
	 */
	public String[] getUnits() {
		String[] units;
		try{
			IAttribute attribute = getAttribute("units");
//			Array stringArray = attribute.getValue();
			String unitString = attribute.getStringValue();
//			units = (String[]) stringArray.copyTo1DJavaArray();
			units = unitString.split(":");
		}catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		// TODO Auto-generated method stub
		return units;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.region.Region#isInclusive()
	 */
	public boolean isInclusive() throws StructureTypeException {
		// TODO Auto-generated method stub
		boolean isInclusive;
		try {
			IAttribute attribute = getAttribute("isInclusive");
			isInclusive = Boolean.valueOf(attribute.getStringValue());
		} catch (Exception e) {
			// TODO: handle exception
			throw new StructureTypeException("can not decide if the region is inclusive");
		}
		return isInclusive;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#addDescription(java.lang.String)
	 */
	public void addDescription(String description) {
		// TODO Auto-generated method stub
		addStringAttribute("description", description);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		String description;
		try {
			description = findAttribute("description").getStringValue();
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		return description;
	}

	public abstract int getRank();
	
}
