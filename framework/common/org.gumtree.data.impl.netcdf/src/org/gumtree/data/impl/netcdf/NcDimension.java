/*******************************************************************************
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.impl.netcdf;

import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDimension;

/**
 * Netcdf implementation of GDM Dimension.
 * @author nxi
 * 
 */
public class NcDimension extends ucar.nc2.Dimension implements IDimension {

	private IArray coordinateVariable;
	
	/**
	 * Constructor from Netcdf Dimension object.
	 * 
	 * @param name
	 *            String value
	 * @param from
	 *            Netcdf object
	 */
	public NcDimension(final String name, final ucar.nc2.Dimension from) {
		super(name, from);
	}

	/**
	 * Constructor from name and length.
	 * 
	 * @param name
	 *            String value
	 * @param length
	 *            integer value
	 * @param isShared
	 *            true or false
	 */
	public NcDimension(final String name, final int length,
			final boolean isShared) {
		super(name, length, isShared);
	}

	/**
	 * Create Dimension from name and length.
	 * 
	 * @param name
	 *            String value
	 * @param length
	 *            integer value
	 */
	public NcDimension(final String name, final int length) {
		super(name, length, false);
	}

	/**
	 * Adapt a Netcdf Dimension to the GDM model. Netcdf 5 hands out plain
	 * ucar.nc2.Dimension instances of its own making, a sliced or sectioned
	 * variable carrying anonymous ones for instance, so a dimension coming from
	 * the Netcdf API cannot simply be cast.
	 *
	 * @param dimension
	 *            Netcdf Dimension object, may be null
	 * @return the given dimension when it already is a GDM dimension, a GDM
	 *         copy of it otherwise, or null when the given dimension is null
	 */
	public static NcDimension wrap(final ucar.nc2.Dimension dimension) {
		if (dimension == null) {
			return null;
		}
		if (dimension instanceof NcDimension) {
			return (NcDimension) dimension;
		}
		return new NcDimension(dimension.getShortName(), dimension);
	}

	@Override
	public IArray getCoordinateVariable() {
		return coordinateVariable;
	}
    
    @Override
    public void setCoordinateVariable(IArray array) {
    	coordinateVariable = array;
    }
    
	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

	@Override
	public int compareTo(IDimension o) {
		// TODO Auto-generated method stub
		return super.compareTo((ucar.nc2.Dimension) o);
	}
	
}
