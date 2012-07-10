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
package au.gov.ansto.bragg.datastructures.core.result;

import org.gumtree.data.interfaces.IGroup;

/**
 * @author nxi
 * Created on 17/04/2008
 */
public interface Calculation extends IGroup {

	public final static String SIGNAL_ATTRIBUTE_NAME = "signal";
	public final static String INPUT_ATTRIBUTE_VALUE = "input";
	public final static String OUTPUT_ATTRIBUTE_VALUE = "output";
	public final static String STATISTICS_ATTRIBUTE_VALUE = "statistics";
	
	/**
	 * Get the input of the calculation result
	 * @return GDM Group
	 * Created on 18/06/2008
	 */
	public IGroup getInput();
	
	/**
	 * Get the output of the calculation result
	 * @return GDM Group
	 * Created on 18/06/2008
	 */
	public IGroup getOutput();
	
	/**
	 * Get the statistics of the calculation result
	 * @return GDM Group
	 * Created on 18/06/2008
	 */
	public IGroup getStatistics();
	
	/**
	 * Add a statistic to the calculation result
	 * @param statistics in GDM Group 
	 * Created on 18/06/2008
	 */
	public void addStatistics(IGroup statistics);
	
}
