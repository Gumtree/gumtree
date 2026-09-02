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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.exception.DimensionNotSupportedException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDimension;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IRange;
import org.gumtree.data.utils.Utilities.ModelType;

import ucar.ma2.DataType;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainerMutable;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;

/**
 * GDM DataItem implementation as an extension of Netcdf Variable class.
 * 
 * @author nxi
 * 
 */

public class NcDataItem extends VariableDS implements IDataItem {

	/**
	 * The parent dataset.
	 */
	private NcDataset dataset;

	/**
	 * Constructor from a Netcdf VariableDS object.
	 * 
	 * @param from
	 *            Netcdf Variable
	 */
	public NcDataItem(final VariableDS from) {
		super(from, true);
//		Cache oldCache = cache;
//		cache = from.
//		cache.cachingSet = oldCache.cachingSet;
//		cache.data = oldCache.data;
//		// make it caching all the time
////		cache.isCaching = oldCache.isCaching;
//		cache.isCaching = true;
		// setCaching(true);
		if (!(from instanceof NcDataItem)) {
			ArrayList<Attribute> newAttributes = new ArrayList<Attribute>();
			ArrayList<Dimension> newDimensions = new ArrayList<Dimension>();
			for (Iterator<?> iter = attributes.iterator(); iter.hasNext();) {
				ucar.nc2.Attribute attribute = (ucar.nc2.Attribute) iter.next();
				newAttributes.add(new NcAttribute(attribute.getName(),
						new NcArray(attribute.getValues())));
			}
			for (Iterator<?> iter = dimensions.iterator(); iter.hasNext();) {
				ucar.nc2.Dimension dimension = (ucar.nc2.Dimension) iter.next();
				newDimensions.add(new NcDimension(dimension.getName(),
						dimension));
			}
			attributes = new AttributeContainerMutable("attributes", newAttributes);
			dimensions = newDimensions;
		}
	}

	/**
	 * Constructor with a parent group, name and value storage.
	 * 
	 * @param group
	 *            GDM group object
	 * @param shortName
	 *            String value
	 * @param array
	 *            GDM Array object
	 * @throws InvalidArrayTypeException
	 *             Array type is wrong
	 */
	public NcDataItem(final NcGroup group, final String shortName,
			final IArray array) throws InvalidArrayTypeException {
		super(null, group, null, shortName, DataType.getType(array
				.getElementType(), ((NcArray) array).getArray().isUnsigned()), null, null, null);
		// group.insertVariable(this);
		setDataType(array.getElementType());
		setCachedData(array, true);
		createDimension(array);
		this.dataset = group.getDataset();
	}

	/**
	 * Constructor with full parameter list.
	 * 
	 * @param ncDataset
	 *            a GDM Dataset object
	 * @param group
	 *            a GDM Group object
	 * @param shortName
	 *            String value
	 * @param array
	 *            a GDM Array object
	 * @throws InvalidArrayTypeException
	 *             array type is wrong
	 */
	public NcDataItem(final NcDataset ncDataset, final NcGroup group,
			final String shortName, final IArray array)
			throws InvalidArrayTypeException {
		super((NetcdfDataset) ncDataset.getNetcdfDataset(), group, null,
				shortName, DataType.getType(array.getElementType(), ((NcArray) array).getArray().isUnsigned()), null,
				null, null);
		setDataType(array.getElementType());
		// group.insertVariable(this);
		setCachedData(array, true);
		createDimension(array);
		this.dataset = ncDataset;
	}

	@Override
	public ModelType getModelType() {
		return ModelType.DataItem;
	}
	
	/**
	 * Create dimension from an array storage.
	 * 
	 * @param array
	 *            GDM Array object
	 */
	private void createDimension(final IArray array) {
		int[] shape = array.getShape();
		List<Dimension> dimensionList = new ArrayList<Dimension>();
		for (int i = 0; i < shape.length; i++) {
			if (shape[i] > 0) {
				String dimensionName = String.valueOf(shape[i]);
				Dimension dimension = null;
				if (group != null) {
					// reuse the dimension already declared by this group or by
					// one of its ancestors instead of duplicating it
					dimension = group.findDimension(dimensionName);
					if (dimension == null) {
						// a dimension added to a group has to be shared
						dimension = new NcDimension(dimensionName, shape[i],
								true);
						group.addDimension(dimension);
					}
				} else {
					dimension = new NcDimension(dimensionName, shape[i], false);
				}
				dimensionList.add(dimension);
			}
		}
		setDimensions(dimensionList);
	}

	/**
	 * Modified _read method. Adapted to always read data in cache.
	 * 
	 * @return a Netcdf array
	 * @throws IOException
	 *             I/O error
	 */
	@Override
	protected ucar.ma2.Array _read() throws IOException {
//		if (cache != null && cache.data != null) {
//			if (debugCaching) {
//				System.out.println("got data from cache " + getName());
//			}
//			return cache.data;
//		} else {
//			setCachedData(super._read(), false);
//			return cache.data;
//		}
		return super._read();
	}

	@Override
	public NcArray getData() throws IOException {
		return new NcArray(read());
	}

	@Override
	public boolean isCaching() {
		System.out.println("called");
		return true;
	}
	
	@Override
	public IArray getData(final int[] origin, final int[] shape)
			throws IOException, InvalidRangeException {
		try {
			return new NcArray(read(origin, shape));
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	/**
	 * Set the units of the data item as String.
	 * 
	 * @param units
	 *            String value
	 */
	public void setUnits(final String units) {
		NcAttribute unitsAttribute = new NcAttribute("units", units);
		this.addOneAttribute(unitsAttribute);
	}

	@Override
	public NcAttribute getAttribute(final String name) {
		return adopt(super.findAttribute(name));
	}

	/**
	 * Adapt an attribute of this variable to the GDM model. Netcdf 5 adds plain
	 * ucar.nc2.Attribute instances of its own (the enhancement layer does so for
	 * "units", for instance), and such an attribute is replaced here by its GDM
	 * counterpart, so that an update made on the returned attribute still
	 * applies to this variable.
	 *
	 * @param attribute
	 *            Netcdf Attribute object, may be null
	 * @return a GDM attribute, or null when the given attribute is null
	 */
	private NcAttribute adopt(final Attribute attribute) {
		if (attribute == null) {
			return null;
		}
		if (attribute instanceof NcAttribute) {
			return (NcAttribute) attribute;
		}
		NcAttribute adopted = new NcAttribute(attribute);
		// replaces the plain attribute carrying the same name
		addAttribute(adopted);
		return adopted;
	}

	@Override
	public void setCachedData(final IArray cacheData, final boolean isMetadata)
			throws InvalidArrayTypeException {
		if (cacheData instanceof NcArray) {
			super.setCachedData(((NcArray) cacheData).getArray(), isMetadata);
		} else {
			throw new InvalidArrayTypeException("not a netcdf Array");
		}
	}

	@Override
	public void addOneAttribute(final IAttribute att) {
		if (att instanceof NcAttribute) {
			super.addAttribute((NcAttribute) att);
		}
	}

	@Override
	public void setParent(final IGroup group) {
		if (group instanceof NcGroup) {
			setParentGroup((NcGroup) group);
		}
	}

	@Override
	public boolean removeAttribute(final IAttribute a) {
		if (a instanceof NcAttribute) {
			return super.remove((NcAttribute) a);
		}
		return false;
	}

	@Override
	public void setDataType(final Class<?> dataType) {
		super.setDataType(ucar.ma2.DataType.getType(dataType, false));
	}

	@Override
	public Class<?> getType() {
		return getDataType().getPrimitiveClassType();
	}

	@Override
	public NcDataItem getSlice(final int dim, final int value)
			throws InvalidRangeException {
		try {
			return new NcDataItem((VariableDS) super.slice(dim, value));
		} catch (ucar.ma2.InvalidRangeException ex) {
			throw new InvalidRangeException(ex);
		}
	}

	@Override
	public NcDataItem getSection(final List<IRange> section)
			throws InvalidRangeException {
		List<Range> ncRangeList = new ArrayList<Range>();
		for (IRange range : section) {
			ncRangeList.add(((NcRange) range).getNetcdfRange());
		}
		try {
			return new NcDataItem((VariableDS) super.section(ncRangeList));
		} catch (Exception e) {
			throw new InvalidRangeException(e);
		}
	}

	@Override
	public NcGroup getParentGroup() {
		Group parent = super.getParentGroup();
		if (parent instanceof NcGroup) {
			return (NcGroup) parent;
		}
		// a plain Netcdf group is no GDM group and adapting it would detach
		// this data item from the group it really belongs to
		return null;
	}
	
	@Override
	public List<IDimension> getDimensions(final int i) {
        List<IDimension> list = new ArrayList<IDimension>();
        list.add(NcDimension.wrap(super.getDimension(i)));
        return list;
	}

	@Override
	public NcAttribute findAttributeIgnoreCase(final String name) {
		// the Netcdf enhancement layer calls this while a variable is still
		// being constructed, hence this variable is left untouched here
		return NcAttribute.wrap(super.findAttributeIgnoreCase(name));
	}

	/**
	 * Find the unit attribute of the variable and retrieve the String value.
	 * 
	 * @return unit in String type.
	 */
	public String getUnits() {
		NcAttribute unitAttribute = getAttribute("units");
		if (unitAttribute == null) {
			return "";
		}
		return unitAttribute.getStringValue();
	}

	@Override
	public NcDataset getDataset() {
		return dataset;
	}

	/**
	 * Set the dataset holder of the data item.
	 * 
	 * @param ncDataset
	 *            NcDataset object
	 */
	public void setDataset(final NcDataset ncDataset) {
		this.dataset = ncDataset;
		if (ncfile == null) {
			ncfile = ncDataset.getNetcdfDataset();
		}
	}

	@Override
	public NcDataItem clone() {
		return new NcDataItem(this);
	}

	@Override
	public String toString() {
		String result = "";
		result += "<DataItem>" + getShortName() + "\n";
		// try {
		// result += "value = " + getData().toString() + "\n";
		// } catch (IOException e) {
		// // e.printStackTrace();
		// result += "value = null\n";
		// }
		List<?> attributeList = getAttributes();
		for (Iterator<?> iterator = attributeList.iterator(); iterator
				.hasNext();) {
			NcAttribute attribute = NcAttribute
					.wrap((Attribute) iterator.next());
			result += attribute.toString() + "\n";
		}
		result += "</DataItem>\n";
		return result;
	}

	@Override
	public void addStringAttribute(final String name, final String value) {
		NcAttribute attribute = null;
		if (value == null) {
			attribute = new NcAttribute(name, "");
		} else {
			attribute = new NcAttribute(name, value);
		}
		addAttribute(attribute);
	}

	@Override
	public NcDataItem getASlice(final int dimension, final int value)
			throws InvalidRangeException {
		NcDataItem variable = null;
		try {
			variable = new NcDataItem((VariableDS) slice(dimension, value));
		} catch (ucar.ma2.InvalidRangeException e) {
			// e.printStackTrace();
			throw new InvalidRangeException("dimension out of boundary");
		}
		return variable;
	}

	/**
	 * Enhance the data item.
	 * 
	 * @param group
	 *            Netcdf Group object
	 * @return Netcdf Variable object
	 * @see Variable#
	 */
	Variable enhance(final ucar.nc2.Group group) {
		return new VariableDS(getParentGroup(), this, true);
	}

	@Override
	public boolean hasAttribute(final String name, final String value) {
		IAttribute attribute = getAttribute(name);
		if (attribute == null) {
			return false;
		}
		if (attribute.getStringValue().equals(value)) {
			return true;
		}
		return false;
	}

	@Override
	public void getNameAndDimensions(final StringBuffer buf,
			final boolean useFullName, final boolean showDimLength) {
	}

	@Override
	public List<IRange> getSectionRanges() {
		return null;
	}

	// @Override
	// public void setDimensionsAnonymous(final int[] shape) {
	// try {
	// super.setDimensionsAnonymous(shape);
	// } catch (ucar.ma2.InvalidRangeException e) {
	//			
	// }
	// }

	@Override
	public List<IAttribute> getAttributeList() {
		if (attributes() == null) {
			return null;
		}
		List<IAttribute> attributeList = new ArrayList<IAttribute>();
		for (Attribute attribute : attributes()) {
			attributeList.add(new NcAttribute(attribute));
		}
		return attributeList;
	}

	@Override
	public List<IDimension> getDimensionList() {
		if (getDimensions() == null) {
			return null;
		}
		List<IDimension> dimensionList = new ArrayList<IDimension>();
		for (Dimension dimension : getDimensions()) {
			dimensionList.add(NcDimension.wrap(dimension));
		}
		return dimensionList;
	}

	@Override
	public List<IRange> getRangeList() {
		if (getRanges() == null) {
			return null;
		}
		List<IRange> rangeList = new ArrayList<IRange>();
		for (Range range : getRanges()) {
			rangeList.add(new NcRange(range));
		}
		return rangeList;
	}

	@Override
	public String getLocation() {
		return getParentGroup().getLocation();
	}

	@Override
	public IGroup getRootGroup() {
		return getParentGroup().getRootGroup();
	}

	@Override
	public void setShortName(final String name) {
		super.setName(name);
	}
	
    @Override
	public void setDimension(IDimension dim, int ind) throws DimensionNotSupportedException {
        if (!(dim instanceof Dimension)) {
            throw new DimensionNotSupportedException("not a netcdf dimension");
        }
        super.setDimension(ind, (Dimension) dim);
        
    }

	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

	@Override
	public boolean isUnsigned() {
		return getDataType().isUnsigned();
	}

	@Override
	public String writeCDL(String indent, boolean useFullName, boolean strict) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
