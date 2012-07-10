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

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.region.internal.NcRectilinearRegion;
import au.gov.ansto.bragg.datastructures.core.region.internal.NcRegionSet;

/**
 * @author nxi
 * Created on 16/04/2008
 */
public class RegionFactory {

	/**
	 * Create a Rectilinear Region object. The region can be defined by its 
	 * reference point and the range it covers, both of which are primary 
	 * double array objects. 
	 * @param parent in Group type
	 * @param shortName in String type
	 * @param physicalReference in double[] type. It's where the region is 
	 * referenced. Usually use the lower boundary of each dimension.
	 * @param physicalRange in double[] type.
	 * @param units array of String type
	 * @return GDM Group type
	 * @throws InvalidArrayTypeException
	 * Created on 17/04/2008
	 */
	public static IGroup createRectilinearRegion(IGroup parent, String shortName,
			double[] physicalReference, double[] physicalRange, String[] units, 
			boolean isInclusive) 
	throws InvalidArrayTypeException {
		return new NcRectilinearRegion(parent, shortName, physicalReference, 
				physicalRange, units, isInclusive);
	}
	
	/**
	 * Create a RegionSet type of data that is in the Group container. 
	 * @param parent Group object that will be the parent group of the new one
	 * @param shortName in String type
	 * @return Group object, which actually is a RegionSet object
	 * Created on 19/03/2008
	 */
	public static IGroup createRegionSet(IGroup parent, String shortName){
		return new NcRegionSet(parent, shortName);
	};
}
