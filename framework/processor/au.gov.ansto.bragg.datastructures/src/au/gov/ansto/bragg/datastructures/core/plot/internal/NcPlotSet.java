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
package au.gov.ansto.bragg.datastructures.core.plot.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotSet;

/**
 * @author nxi
 * Created on 06/03/2008
 */
public class NcPlotSet extends NcGroup implements PlotSet {

	public NcPlotSet(IGroup parent, String shortName) {
		super((NcDataset) parent.getDataset(), (NcGroup) parent, shortName, true);
		addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.plotset.name());
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.PlotSet#addPlot(au.gov.ansto.bragg.datastructures.core.plot.Plot)
	 */
	public void addPlot(Plot plot) {
		addGroup(plot);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.PlotSet#getPlot(java.lang.String)
	 */
	public Plot getPlot(String name) throws Exception {
		return (NcPlot) getGroup(name);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.PlotSet#removePlot(au.gov.ansto.bragg.datastructures.core.plot.Plot)
	 */
	public void removePlot(Plot plot) {
//		getGroups().remove(plot);
		removeGroup(plot);
	}

	public List<Plot> getPlotList() {
		List<Plot> plotList = new ArrayList<Plot>();
		for (Iterator<?> iterator = groups.iterator(); iterator.hasNext();) {
			IGroup group = (IGroup) iterator.next();
			if (group instanceof Plot) {
				plotList.add((Plot) group);
			}
		}
		return plotList;
	}

}
