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

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.nexus.IAxis;

import ucar.nc2.dataset.VariableDS;

/**
 * Netcdf implementation of axes in NeXus format.
 * @author nxi
 * 
 */
public class NXaxis extends NXDataItem implements IAxis {

	/**
	 * Copy constructor from a Netcdf Variable.
	 * 
	 * @param from
	 *            Netcdf VariableDS object
	 */
	public NXaxis(final VariableDS from) {
		super(from);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Construct a new NXaxis object under a given parent Group.
	 * 
	 * @param group
	 *            Netcdf Group object
	 * @param shortName
	 *            String value
	 * @param array
	 *            Netcdf Array storage
	 * @throws InvalidArrayTypeException
	 *             invalid array type
	 */
	public NXaxis(final NcGroup group, final String shortName,
			final IArray array) throws InvalidArrayTypeException {
		super(group, shortName, array);
		// TODO Auto-generated constructor stub
	}

	// public NXaxis(final NcDataset dataset, final NcGroup group,
	// final String shortName, final IArray array)
	// throws InvalidArrayTypeException {
	// super(dataset, group, shortName, array);
	// // TODO Auto-generated constructor stub
	// }

}
