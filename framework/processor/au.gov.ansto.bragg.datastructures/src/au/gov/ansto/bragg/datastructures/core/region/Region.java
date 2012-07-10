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

import java.util.List;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.RegionType;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;

/**
 * @author nxi
 * Created on 16/04/2008
 */
public interface Region extends IGroup {


	public final static String REFERENCE_NAME = "physicalReference";
	public final static String RANGE_NAME = "physicalRange";
	
	/**
	 * Get the region type in enum value
	 * @return RegionType enum value
	 * Created on 18/06/2008
	 */
	public RegionType getRegionType();
	
	/**
	 * Check if the region is inclusive region (region of interests or mask).
	 * @return boolean type
	 * @throws StructureTypeException
	 * Created on 18/06/2008
	 */
	public boolean isInclusive() throws StructureTypeException;
	
	/**
	 * Get the units of the region. 
	 * @return java array of String values
	 * Created on 18/06/2008
	 */
	public String[] getUnits();
	
	/**
	 * Add a description to the region.
	 * @param description in String type
	 * Created on 18/06/2008
	 */
	public void addDescription(String description);
	
	/**
	 * Get the description of the region.
	 * @return String type
	 * Created on 18/06/2008
	 */
	public String getDescription();

	/**
	 * Apply the region to a truth map. Physical reference will be used in matching the 
	 * axes of the truth map 
	 * @param truthMap GDM Array in Boolean data type
	 * @param axes list of DataItems
	 * @throws StructureTypeException
	 * Created on 18/06/2008
	 */
	public void applyToTruthMap(IArray truthMap, List<IDataItem> axes) 
	throws StructureTypeException;
	
	/**
	 * Return the rank of the region. 
	 * @return integer value
	 * Created on 04/08/2008
	 */
	public int getRank();
}
