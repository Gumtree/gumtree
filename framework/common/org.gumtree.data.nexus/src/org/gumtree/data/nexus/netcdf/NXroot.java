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
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.INXentry;
import org.gumtree.data.nexus.INXroot;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.utils.NexusUtils;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.VariableDS;

/**
 * Netcdf implementation of NeXus NXroot.
 * @author nxi
 * 
 */
public class NXroot extends NXGroup implements INXroot {

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
	 *            if parent will know this as a child group
	 */
	public NXroot(final NcDataset dataset, final NcGroup parent,
			final String shortName, final boolean updateParent) {
		super(dataset, parent, shortName, updateParent);
	}

	/**
	 * Construct an empty instance under a parent dataset.
	 * 
	 * @param dataset
	 *            NcDataset object
	 */
	public NXroot(final NcDataset dataset) {
		super(dataset, null, "", false);
		addStringAttribute(NXConstants.ROOT_NEXUS_VERSION_LABEL,
				NXConstants.GLOBAL_NEXUS_VERSION);
	}

	/**
	 * @param dataset
	 * @param parent
	 * @param shortName
	 */
	// public NXroot(NcDataset dataset, Group parent, String shortName) {
	// super(dataset, parent, shortName);
	// }

	/**
	 * Copy constructor from a Netcdf group.
	 * 
	 * @param from
	 *            Netcdf Group
	 * @param dataset
	 *            NcDataset object
	 */
	public NXroot(final Group from, final NcDataset dataset) {
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
			NXGroup entry = null;
			if (NexusUtils
					.isNXclass(group, NXConstants.ENTRY_NEXUS_CLASS_VALUE)) {
				entry = new NXentry(group, dataset);
			} else {
				entry = new NXGroup(group, dataset);
			}
			entry.setParent(this);
			groups.add(entry);
		}
		for (Attribute attribute : from.getAttributes()) {
			NXAttribute ncAttribute = new NXAttribute(attribute);
			attributes.addAttribute(ncAttribute);
		}
		if (from.isRoot()) {
			this.setParent(null);
		}
	}

	/**
	 * Copy constructor from a GDM IGroup object.
	 * 
	 * @param from
	 *            GDM IGroup object
	 */
	public NXroot(final IGroup from) {
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
			NXGroup entry = null;
			if (NexusUtils.isNXclass((NcGroup) group,
					NXConstants.ENTRY_NEXUS_CLASS_VALUE)) {
				entry = new NXentry(group);
			} else {
				entry = new NXGroup(group);
			}
			entry.setParent(this);
			groups.add(entry);
		}

		for (IAttribute attribute : from.getAttributeList()) {
			NXAttribute ncAttribute = new NXAttribute(
					(ucar.nc2.Attribute) attribute);
			attributes.addAttribute(ncAttribute);
		}
		if (isRoot()) {
			this.setParent(null);
		}
	}

	@Override
	public List<INXentry> getEntryList() {
		List<INXentry> entryList = new ArrayList<INXentry>();
		for (IGroup group : getGroupList()) {
			if (group instanceof INXentry) {
				entryList.add((INXentry) group);
			}
		}
		return entryList;
	}

	@Override
	public String getFileTime() {
		IAttribute attribute = getAttribute(NXConstants.ROOT_FILE_TIME_LABEL);
		if (attribute != null) {
			return attribute.getStringValue();
		}
		return null;
	}

	@Override
	public INXentry getFirstEntry() {
		for (IGroup group : getGroupList()) {
			if (group instanceof INXentry) {
				return (NXentry) group;
			}
		}
		List<IGroup> groups = getGroupList();
		if (groups.size() > 0) {
			return new NXentry(groups.get(0));
		}
		return null;
	}

	@Override
	public String getHDFVersion() {
		IAttribute attribute = getAttribute(NXConstants.ROOT_HDF_VERSION_LABEL);
		if (attribute != null) {
			return attribute.getStringValue();
		}
		return null;
	}

	@Override
	public String getNexusVersion() {
		IAttribute attribute = getAttribute(
				NXConstants.ROOT_NEXUS_VERSION_LABEL);
		if (attribute != null) {
			return attribute.getStringValue();
		}
		return null;
	}

	@Override
	public INXentry getDefaultEntry() {
		return getFirstEntry();
	}

	@Override
	public ISignal getDefaultSignal() {
		INXentry entry = getDefaultEntry();
		if (entry == null) {
			return null;
		}
		return entry.getSignal();
	}

	@Override
	public INXdata getDefaultData() {
		INXentry entry = getDefaultEntry();
		if (entry == null) {
			return null;
		}
		return entry.getData();
	}

	@Override
	public NXentry findCurrentEntry() {
		return (NXentry) getDefaultEntry();
	}
}
