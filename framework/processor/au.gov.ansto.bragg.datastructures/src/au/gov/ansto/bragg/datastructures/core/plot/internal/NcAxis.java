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

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Variance;

/**
 * @author nxi
 * Created on 06/03/2008
 */
public class NcAxis extends NcDataItem implements Axis {


	public NcAxis(NcGroup group, String shortName, IArray array, String title, String units, int dimension) 
	throws InvalidArrayTypeException {
		super(group, shortName, array);
		addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.axis.name());
		if (title != null) addStringAttribute("title", title);
		if (units != null) addStringAttribute("units", units);
		addStringAttribute("dimension", String.valueOf(dimension));
	}

	public NcAxis(NcGroup group, String shortName, IArray array, String title, String units, 
			int dimension, IArray varianceArray) 
	throws InvalidArrayTypeException {
		this(group, shortName, array, title, units, dimension);
		addVariance(varianceArray);
	}

	private void addVariance(IArray varianceArray) throws InvalidArrayTypeException {
		NcGroup parent = getParentGroup();
		String varianceName = getShortName() + "Variance";
		new NcVariance(parent, varianceName, varianceArray, this);
		addOneAttribute(Factory.createAttribute(StaticDefinition.DATA_VARIANCE_REFERENCE_NAME, 
				shortName));
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Axis#getTitle()
	 */
	public String getTitle() {
		IAttribute title = getAttribute("title");
		if (title != null)	return title.getStringValue();
		return null;
	}
	
	public void setTitle(String titleString) {
		IAttribute title = getAttribute("title");
		if (null==title) {
			this.addStringAttribute("title", titleString);
		} else {
			title.setStringValue(titleString);
		}
	}
	
	public String getUntis() {
		IAttribute unitsAttribute = getAttribute("units");
		if (unitsAttribute != null) return unitsAttribute.getStringValue();
		return null;
	}

	public int getDimensionName() throws StructureTypeException {
		IAttribute dimensionAttribute = getAttribute("dimension");
		if (dimensionAttribute == null)
			throw new StructureTypeException("no dimension information");
		int dimension;
		try{
			dimension = Integer.valueOf(dimensionAttribute.getStringValue());
		}catch (Exception e) {
			throw new StructureTypeException("bad dimension attribute");
		}
		return dimension;
	}

	public Variance getVariance() {
		IAttribute attribute = getAttribute(StaticDefinition.DATA_VARIANCE_REFERENCE_NAME);
		if (attribute == null) return null;
		return (Variance) getParentGroup().findDataItem(attribute.getStringValue());
	}
}
