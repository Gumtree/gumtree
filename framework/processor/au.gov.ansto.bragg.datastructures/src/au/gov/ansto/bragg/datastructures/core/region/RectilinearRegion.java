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

import org.gumtree.data.interfaces.IArray;

import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;

/**
 * @author nxi
 * Created on 16/04/2008
 */
public interface RectilinearRegion extends Region {

	/**
	 * Get the physical reference of the region in java array type.
	 * @return java array of double values
	 * Created on 18/06/2008
	 */
	public double[] getPrimaryPhysicalReference();

	/**
	 * Get the physical range of the region in java array type.
	 * @return java array of double values
	 * Created on 18/06/2008
	 */
	public double[] getPrimaryPhysicalRange();
	
	/**
	 * Get the physical reference of the region in GDM Array.
	 * @return GDM Array
	 * Created on 18/06/2008
	 */
	public IArray getPhysicalReference();
	
	/**
	 * Get the physical range of the region in GDM Array.
	 * @return GDM Array
	 * Created on 18/06/2008
	 */
	public IArray getPhysicalRange();
	
	/**
	 * Get the section (from lower to higher limit) of the region in the specific dimension.
	 * @param dimension integer value
	 * @return double array
	 * Created on 22/01/2009
	 */
	public double[] getPhysicalSection(int dimension);
	
	/**
	 * Set the physical reference.
	 * @param reference in double array
	 * Created on 30/01/2009
	 * @throws StructureTypeException 
	 */
	public void setPhysicalReference(double[] reference) throws StructureTypeException;
	
	/**
	 * Set the physical range.
	 * @param range in double array
	 * Created on 30/01/2009
	 * @throws StructureTypeException 
	 */
	public void setPhysicalRange(double[] range) throws StructureTypeException;
}
