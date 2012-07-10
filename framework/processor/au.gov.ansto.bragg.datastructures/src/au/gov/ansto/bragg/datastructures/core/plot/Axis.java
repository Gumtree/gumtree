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
package au.gov.ansto.bragg.datastructures.core.plot;

import org.gumtree.data.interfaces.IDataItem;

import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;

/**
 * @author nxi
 * Created on 05/03/2008
 * Modified pvh 10/9/2009 
 */
public interface Axis extends IDataItem {

	/**
	 * Return the title attribute of the axis.
	 * @return String object
	 * Created on 06/03/2008
	 */
	public String getTitle();

	/**
	 * Create or set the title attribute of the axis.
	 */
	public void setTitle(String titleString);
	
	/**
	 * Return the units information of the axis.
	 * @return String type object
	 * Created on 12/03/2008
	 */
	public String getUntis();
	
	/**
	 * Return the which dimension does the axis represent for the data. 
	 * @return int type
	 * Created on 13/03/2008
	 * @throws StructureTypeException 
	 */
	public int getDimensionName() throws StructureTypeException;
	
	/**
	 * Return the Variance object of the data.
	 * @return Variance object
	 * Created on 18/04/2008
	 */
	public Variance getVariance();
}
