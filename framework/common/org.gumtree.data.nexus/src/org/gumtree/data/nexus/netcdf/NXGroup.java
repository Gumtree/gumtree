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

import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcDimension;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDimension;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.INXDataItem;
import org.gumtree.data.nexus.INXGroup;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.VariableDS;

/**
 * Netcdf implementation of NeXus Group.
 * @author nxi
 * 
 */
public class NXGroup extends NcGroup implements INXGroup {

	/**
	 * Constructor with initialisation.
	 * 
	 * @param dataset
	 *            NcDataset object
	 * @param parent
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 * @param updateParent
	 *            if parent group knows this as a child
	 */
	public NXGroup(final NcDataset dataset, final NcGroup parent,
			final String shortName, final boolean updateParent) {
		super(dataset, parent, shortName, updateParent);
	}

	/**
	 * Construct a NXGroup object under a parent Group.
	 * 
	 * @param parent
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 */
	public NXGroup(final NcGroup parent, final String shortName) {
		this(parent.getDataset(), parent, shortName, true);
	}

	/**
	 * @param dataset
	 * @param parent
	 * @param shortName
	 */
	// public NXGroup(NcDataset dataset, Group parent, String shortName) {
	// super(dataset, parent, shortName);
	// }

	/**
	 * Copy constructor from a Netcdf Group object.
	 * 
	 * @param from
	 *            Netcdf Group object
	 * @param dataset
	 *            NcDataset object
	 */
	public NXGroup(final Group from, final NcDataset dataset) {
		this(dataset, null, from.getShortName(), false);
		for (Variable variable : from.getVariables()) {
			NXDataItem dataItem = new NXDataItem((VariableDS) variable);
			dataItem.setParent(this);
			dataItem.setDataset(dataset);
			variables.add(dataItem);
		}
		for (Dimension d : from.getDimensions()) {
			NcDimension dimension = new NcDimension(d.getName(), d);
			dimension.setGroup(this);
			dimensions.add(dimension);
		}
		for (Group group : from.getGroups()) {
			NXGroup nxGroup = new NXGroup(group, dataset);
			nxGroup.setParent(this);
			groups.add(nxGroup);
		}
		for (Attribute attribute : from.getAttributes()) {
			NXAttribute ncAttribute = new NXAttribute(attribute);
			attributes.add(ncAttribute);
		}
		if (isRoot()) {
			this.parent = null;
		}
	}

	/**
	 * Copy constructor from a GDM IGroup object.
	 * 
	 * @param from
	 *            GDM IGroup object
	 */
	public NXGroup(final IGroup from) {
		this((NcDataset) from.getDataset(), (NcGroup) from.getParentGroup(),
				from.getShortName(), true);
		for (IDataItem dataItem : from.getDataItemList()) {
			NXDataItem nxDataItem = new NXDataItem((NcDataItem) dataItem);
			nxDataItem.setParent(this);
			nxDataItem.setDataset((NcDataset) from.getDataset());
			variables.add(nxDataItem);
		}
		for (IDimension dimension : from.getDimensionList()) {
			if (dimension instanceof NcDimension) {
				addDimension((NcDimension) dimension);
			}
		}

		for (IGroup group : from.getGroupList()) {
			NXGroup nxGroup = new NXGroup(group);
			nxGroup.setParent(this);
			groups.add(nxGroup);
		}

		for (IAttribute attribute : from.getAttributeList()) {
			NXAttribute ncAttribute = new NXAttribute(
					(ucar.nc2.Attribute) attribute);
			attributes.add(ncAttribute);
		}
		if (isRoot()) {
			this.parent = null;
		}
	}

	@Override
	public NcGroup findCurrentEntry() {
		if (isRoot()) {
			return super.findCurrentEntry();
		}
		return getParentGroup().findCurrentEntry();
	}

	@Override
	public List<INXDataItem> getNXDataItemList() {
		List<INXDataItem> list = new ArrayList<INXDataItem>();
		for (IDataItem dataItem : getDataItemList()) {
			if (dataItem instanceof INXDataItem) {
				list.add((INXDataItem) dataItem);
			}
		}
		return list;
	}

	@Override
	public List<INXGroup> getNXGroupList() {
		List<INXGroup> list = new ArrayList<INXGroup>();
		for (IGroup group : getGroupList()) {
			if (group instanceof INXGroup) {
				list.add((INXGroup) group);
			}
		}
		return list;
	}

	@Override
	public String getTitle() {
		IAttribute attribute = getAttribute(NXConstants.TITLE_ATTRIBUTE_NAME);
		if (attribute != null) {
			return attribute.getStringValue();
		} else {
			return null;
		}
	}

	@Override
	public void setTitle(String title) {
		IAttribute attribute = getAttribute(NXConstants.TITLE_ATTRIBUTE_NAME);
		if (attribute != null) {
			if (title == null) {
				removeAttribute(attribute);
			} else {
				attribute.setStringValue(title);
			}
		} else {
			if (title != null) {
				addStringAttribute(NXConstants.TITLE_ATTRIBUTE_NAME, title);
			}
		}
	}
}
