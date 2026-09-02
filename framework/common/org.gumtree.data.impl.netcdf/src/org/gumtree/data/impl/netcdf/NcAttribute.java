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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;

/**
 * Netcdf implementation of GDM Array.
 * @author nxi
 * 
 */
public class NcAttribute extends ucar.nc2.Attribute implements IAttribute {

	/**
	 * The default name label.
	 */
	private static final String DEFAULT_NAME = "default";

	/**
	 * The Netcdf Attribute fields holding the value.
	 */
	private static final List<Field> VALUE_FIELDS = findValueFields();

	/**
	 * Look up the Netcdf Attribute fields holding the value. Netcdf 5 keeps the
	 * String value of an attribute apart from its array value and its
	 * setValues method only refreshes the one matching the given array, leaving
	 * the other one stale, hence both have to be cleared before a new value is
	 * set.
	 *
	 * @return the value fields, empty when the Netcdf implementation no longer
	 *         declares them
	 */
	private static List<Field> findValueFields() {
		List<Field> fields = new ArrayList<Field>();
		for (String name : new String[] { "svalue", "values" }) {
			try {
				Field field = ucar.nc2.Attribute.class.getDeclaredField(name);
				field.setAccessible(true);
				fields.add(field);
			} catch (Exception e) {
				// nothing to clear, an update then leaves behind whichever
				// value does not match the new one
			}
		}
		return fields;
	}

	/**
	 * Wrapper constructor.
	 * 
	 * @param from
	 *            Netcdf Attribute object
	 */
	public NcAttribute(final ucar.nc2.Attribute from) {
		// super(from);
		super(from.getName(), from.getValues());
	}

	/**
	 * Create an empty Attribute object.
	 */
	public NcAttribute() {
		super(DEFAULT_NAME);
	}

	// public NcAttribute(Parameter param) {
	// super(param);
	// }
	//
	/**
	 * Constructor that initialise the name and value.
	 * 
	 * @param name
	 *            String value
	 * @param values
	 *            IArray object
	 */
	public NcAttribute(final String name, final IArray values) {
		super(name, ((NcArray) values).getArray());
	}

	/**
	 * Construct an Attribute and change the name.
	 * 
	 * @param name
	 *            String object
	 * @param from
	 *            Netcdf Attribute object
	 */
	public NcAttribute(final String name, final ucar.nc2.Attribute from) {
		super(name, from);
	}

	/**
	 * Construct an Attribute with a boolean value.
	 * 
	 * @param name
	 *            String value
	 * @param validate
	 *            true or false
	 */
	public NcAttribute(final String name, final boolean validate) {
		super(name, String.valueOf(validate));
	}

	/**
	 * Construct an Attribute with a numeric value.
	 * 
	 * @param name
	 *            String value
	 * @param val
	 *            in numeric type
	 */
	public NcAttribute(final String name, final Number val) {
		super(name, val);
	}

	/**
	 * Construct an Attribute with a String value.
	 * 
	 * @param name
	 *            String value
	 * @param val
	 *            String value
	 */
	public NcAttribute(final String name, final String val) {
		super(name, val);
	}

	/**
	 * Construct an Attribute with empty value.
	 * 
	 * @param name
	 *            String value
	 */
	public NcAttribute(final String name) {
		super(name);
	}

	/**
	 * Adapt a Netcdf Attribute to the GDM model. Netcdf 5 stores plain
	 * ucar.nc2.Attribute instances of its own making in a variable or a group,
	 * so an attribute handed back by the Netcdf API cannot simply be cast.
	 *
	 * @param attribute
	 *            Netcdf Attribute object, may be null
	 * @return the given attribute when it already is a GDM attribute, a GDM
	 *         copy of it otherwise, or null when the given attribute is null
	 */
	public static NcAttribute wrap(final ucar.nc2.Attribute attribute) {
		if (attribute == null) {
			return null;
		}
		if (attribute instanceof NcAttribute) {
			return (NcAttribute) attribute;
		}
		return new NcAttribute(attribute);
	}

	@Override
	public Class<?> getType() {
		return getDataType().getPrimitiveClassType();
	}

	@Override
	public IArray getValue() {
		return new NcArray(getValues());
	}

	@Override
	public void setValue(final IArray value) {
		// netCDF-Java 5.x locks every Attribute in its constructors, so
		// setValues() would always fail with "Cant modify". GDM attributes are
		// mutable by contract, hence the lock is lifted for the update only.
		final boolean wasImmutable = immutable;
		immutable = false;
		try {
			for (Field field : VALUE_FIELDS) {
				try {
					field.set(this, null);
				} catch (IllegalAccessException e) {
					// see findValueFields
				}
			}
			setValues(((NcArray) value).getArray());
		} finally {
			immutable = wasImmutable;
		}
	}

	@Override
	public void setStringValue(final String val) {
		NcArray array = (NcArray) Factory.getFactory(NcFactory.NAME).createArray(val.toCharArray());
		setValue(array);
	}
	
	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

}
