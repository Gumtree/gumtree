/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.nexus.netcdf;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.ISignal;

import ucar.nc2.dataset.VariableDS;

/**
 * Netcdf implementation of NeXus NXsignal.
 * @author nxi
 * 
 */
public class NXsignal extends NXDataItem implements ISignal {

	/**
	 * Copy constructor from a Netcdf Variable.
	 * 
	 * @param from
	 *            Netcdf VariableDS object
	 */
	public NXsignal(final VariableDS from) {
		super(from);
	}

	/**
	 * Construct a new NXsignal instance with initialisation.
	 * 
	 * @param group
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 * @param array
	 *            GDM Array object
	 * @throws InvalidArrayTypeException
	 *             invalid array type
	 */
	public NXsignal(final NcGroup group, final String shortName,
			final IArray array) throws InvalidArrayTypeException {
		this(group.getDataset(), group, shortName, array);
	}

	/**
	 * Construct a new NXsingal instance with initialisation.
	 * 
	 * @param dataset
	 *            NcDataset object
	 * @param group
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 * @param array
	 *            GDM Array object
	 * @throws InvalidArrayTypeException
	 *             invalid array type
	 */
	public NXsignal(final NcDataset dataset, final NcGroup group,
			final String shortName, final IArray array)
			throws InvalidArrayTypeException {
		super(dataset, group, shortName, array);
		addStringAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL,
				NXConstants.SIGNAL_CLASS_VALUE);
	}

	@Override
	public List<IAxis> findAxes() {
		List<IAxis> axisList = new ArrayList<IAxis>();
		IAttribute axesAttribute = getAttribute(NXConstants.SIGNAL_AXES_LABEL);
		if (axesAttribute != null) {
			String value = axesAttribute.getStringValue();
			String[] axisNames = value.split(":");
			for (String string : axisNames) {
				IDataItem axisItem = getParentGroup().getDataItem(string);
				if (axisItem instanceof IAxis) {
					axisList.add((IAxis) axisItem);
				}
			}
		}
		return axisList;
	}

}
