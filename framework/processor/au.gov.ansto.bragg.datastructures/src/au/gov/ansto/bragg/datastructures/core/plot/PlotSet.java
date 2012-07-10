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

import java.util.List;

import org.gumtree.data.interfaces.IGroup;

/**
 * @author nxi
 *
 */
public interface PlotSet extends IGroup {

	/**
	 * Get the Plot object with the specified name.
	 * @param name as String
	 * @return
	 * Created on 05/03/2008
	 * @throws Exception 
	 */
	public Plot getPlot(String name) throws Exception;
	
	/**
	 * Add a Plot object in to the plot set
	 * @param plot
	 * Created on 05/03/2008
	 */
	public void addPlot(Plot plot);
	
	/**
	 * Remove the Plot object from the plot set.
	 * @param plot
	 * Created on 05/03/2008
	 */
	public void removePlot(Plot plot);
	
	/**
	 * Return the list of Plots in the plot set. 
	 * @return in List type
	 * Created on 16/04/2008
	 */
	public List<Plot> getPlotList();
}
