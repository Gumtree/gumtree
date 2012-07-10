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

import org.gumtree.data.interfaces.IGroup;

/**
 * @author nxi
 * Created on 16/04/2008
 */
public interface RegionSet extends IGroup {

	/**
	 * Add a region to the region set
	 * @param region Region object
	 * Created on 18/06/2008
	 */
	public void addRegion(Region region);
	
	/**
	 * Get a region by its name
	 * @param shortName in String type
	 * @return Region type
	 * @throws Exception
	 * Created on 18/06/2008
	 */
	public Region getRegion(String shortName) throws Exception;
	
	/**
	 * Remove a region by its name
	 * @param shortName in String type
	 * Created on 18/06/2008
	 */
	public void removeRegion(String shortName);
	
	/**
	 * Remove a Region in the region set
	 * @param region
	 * Created on 18/06/2008
	 */
	public void removeRegion(Region region);
	
	/**
	 * Get the list of Regions
	 * @return List of Region objects
	 * Created on 18/06/2008
	 */
	public List<Region> getRegionList();
	
	public RegionSet clone();
}
