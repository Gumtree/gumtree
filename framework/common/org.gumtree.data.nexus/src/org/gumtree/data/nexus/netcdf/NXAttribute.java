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

import org.gumtree.data.impl.netcdf.NcAttribute;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.nexus.INXAttribute;

import ucar.nc2.Attribute;

/**
 * Netcdf implementation of NeXus Attribute.
 * @author nxi
 * 
 */
public class NXAttribute extends NcAttribute implements INXAttribute {

	/**
	 * Copy constructor from Netcdf Attribute.
	 * 
	 * @param from
	 *            Netcdf Attribute object
	 */
	public NXAttribute(final Attribute from) {
		super(from);
	}

	/**
	 * Default constructor.
	 */
	public NXAttribute() {
		super();
	}

	/**
	 * Construct a NeXus Attribute with a name and an Array storage.
	 * 
	 * @param name
	 *            String value
	 * @param values
	 *            GDM IArray object
	 */
	public NXAttribute(final String name, final IArray values) {
		super(name, values);
	}

	/**
	 * Copy constructor from a Netcdf Attribute and change the name.
	 * 
	 * @param name
	 *            String value
	 * @param from
	 *            Netcdf Attribute
	 */
	public NXAttribute(final String name, final Attribute from) {
		super(name, from);
	}

	/**
	 * Construct an attribute with name and a numeric value.
	 * 
	 * @param name
	 *            String
	 * @param val
	 *            Number type
	 */
	public NXAttribute(final String name, final Number val) {
		super(name, val);
	}

	/**
	 * Construct an attribute with name and a String value.
	 * 
	 * @param name
	 *            String
	 * @param val
	 *            String value
	 */
	public NXAttribute(final String name, final String val) {
		super(name, val);
	}

	/**
	 * Construct an empty attribute with a name.
	 * 
	 * @param name
	 *            String value
	 */
	public NXAttribute(final String name) {
		super(name);
	}

}
