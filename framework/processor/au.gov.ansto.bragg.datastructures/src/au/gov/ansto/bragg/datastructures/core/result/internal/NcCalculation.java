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
package au.gov.ansto.bragg.datastructures.core.result.internal;

import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.result.Calculation;

/**
 * @author nxi  
 * Created on 17/04/2008
 */
public class NcCalculation extends NcGroup implements Calculation {

	public NcCalculation(IGroup parent, String shortName, IGroup input, IGroup output, 
			IGroup statistics){
		super((NcDataset) parent.getDataset(), (NcGroup) parent, shortName, true);
		addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.region.name());
		addLog("Created");
		if (input != null) {
			addGroup(input);
			if (!Util.hasAttribute(input, SIGNAL_ATTRIBUTE_NAME, INPUT_ATTRIBUTE_VALUE))
				addOneAttribute(Factory.createAttribute(SIGNAL_ATTRIBUTE_NAME, INPUT_ATTRIBUTE_VALUE));
		}
		if (output != null) {
			addGroup(output);
			if (!Util.hasAttribute(output, SIGNAL_ATTRIBUTE_NAME, OUTPUT_ATTRIBUTE_VALUE))
				addOneAttribute(Factory.createAttribute(SIGNAL_ATTRIBUTE_NAME, OUTPUT_ATTRIBUTE_VALUE));
		}
		if (statistics != null) {
			addGroup(statistics);
			if (!Util.hasAttribute(statistics, SIGNAL_ATTRIBUTE_NAME, STATISTICS_ATTRIBUTE_VALUE))
				addOneAttribute(Factory.createAttribute(SIGNAL_ATTRIBUTE_NAME, STATISTICS_ATTRIBUTE_VALUE));
		}
		
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.result.Calculation#addStatistics(org.gumtree.data.gdm.core.Group)
	 */
	@SuppressWarnings("unchecked")
	public void addStatistics(IGroup statistics) {
		// TODO Auto-generated method stub
		if (statistics != null) {
			IGroup oldGroup = getGroupWithAttribute(SIGNAL_ATTRIBUTE_NAME, STATISTICS_ATTRIBUTE_VALUE);
			if (oldGroup == null){
				addGroup(statistics);
				if (!Util.hasAttribute(statistics, SIGNAL_ATTRIBUTE_NAME, STATISTICS_ATTRIBUTE_VALUE))
					addOneAttribute(Factory.createAttribute(SIGNAL_ATTRIBUTE_NAME, STATISTICS_ATTRIBUTE_VALUE));
			}else{
				List oldDataItemList = oldGroup.getDataItemList();
				List newDataItemList = ((NcGroup) statistics).getDimensions();
				oldDataItemList.addAll(newDataItemList);
			}
		}		
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.result.Calculation#getInput()
	 */
	public IGroup getInput() {
		// TODO Auto-generated method stub
		return getGroupWithAttribute(SIGNAL_ATTRIBUTE_NAME, INPUT_ATTRIBUTE_VALUE);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.result.Calculation#getOutput()
	 */
	public IGroup getOutput() {
		// TODO Auto-generated method stub
		return getGroupWithAttribute(SIGNAL_ATTRIBUTE_NAME, OUTPUT_ATTRIBUTE_VALUE);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.result.Calculation#getStatistics()
	 */
	public IGroup getStatistics() {
		// TODO Auto-generated method stub
		return getGroupWithAttribute(SIGNAL_ATTRIBUTE_NAME, STATISTICS_ATTRIBUTE_VALUE);
	}

}
