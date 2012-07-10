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
package au.gov.ansto.bragg.datastructures.core.configuration.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import ucar.nc2.Variable;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.configuration.TunerConfiguration;

/**
 * @author nxi
 * Created on 08/05/2008
 */
public class NcTunerConfiguration extends NcGroup implements TunerConfiguration {

	/**
	 * @param dataset
	 * @param parent
	 * @param shortName
	 * @param init
	 */
	public NcTunerConfiguration(NcDataset dataset, NcGroup parent,
			String shortName, boolean init) {
		super(dataset, parent, shortName, init);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param dataset
	 * @param parent
	 * @param shortName
	 */
	public NcTunerConfiguration(IGroup parent, String shortName, String recipeID, String algorithmName) {
		super((NcDataset) parent.getDataset(), (NcGroup) parent, shortName, true);
		addStringAttribute("recipeID", recipeID);
		addStringAttribute("algorithmName",algorithmName);
		addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.configuration.name());
		
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.configuration.Configuration#getAlgorithmName()
	 */
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		IAttribute name = getAttribute("algorithmName");
		if (name != null)
			return name.getStringValue();
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.configuration.Configuration#getRecipeID()
	 */
	public String getRecipeID() {
		// TODO Auto-generated method stub
		IAttribute name = getAttribute("recipeID");
		if (name != null)
			return name.getStringValue();
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.configuration.Configuration#getTunerNameList()
	 */
	public List<String> getTunerNameList() {
		// TODO Auto-generated method stub
		List<String> nameList = new ArrayList<String>();
		for (Variable varialbe : getVariables())
			nameList.add(varialbe.getShortName());
		return nameList;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.configuration.Configuration#getTunerValue(java.lang.String)
	 */
	public String getTunerValue(String tunerName) {
		// TODO Auto-generated method stub
		IDataItem tunerConfiguration = findDataItem(tunerName);
		if (tunerConfiguration == null)
			return null;
		String stringValue = null;
		try {
			stringValue = tunerConfiguration.getData().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return stringValue;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.configuration.Configuration#getValueGroup(java.lang.String)
	 */
	public IGroup getValueGroup(String valueName) {
		// TODO Auto-generated method stub
		IGroup group = findGroup(valueName);
		if (group == null) 
			return null;
		return group;
	}

	public void addTunerConfiguration(String tunerName, String tunerValue) 
	throws InvalidArrayTypeException {
		// TODO Auto-generated method stub
		IArray valueArray = Factory.createArray(tunerValue.toCharArray());
		IDataItem tunerConfiguration = Factory.createDataItem(this, tunerName, valueArray);
		addDataItem(tunerConfiguration);
	}

	public void addTunerConfiguration(String tunerName, IGroup tunerValue) 
	throws InvalidArrayTypeException {
		// TODO Auto-generated method stub
		addTunerConfiguration(tunerName, tunerValue.getShortName());
		addGroup(tunerValue);
	}

}
