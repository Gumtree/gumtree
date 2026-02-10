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

import java.util.List;

import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcDimension;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDimension;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.INXentry;
import org.gumtree.data.nexus.INXinstrument;
import org.gumtree.data.nexus.INXmonitor;
import org.gumtree.data.nexus.INXnote;
import org.gumtree.data.nexus.INXsample;
import org.gumtree.data.nexus.INXuser;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.utils.NexusUtils;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.VariableDS;

/**
 * Netcdf implementation of NeXus NXentry.
 * @author nxi
 * 
 */
public class NXentry extends NXGroup implements INXentry {

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
	public NXentry(final NcDataset dataset, final NcGroup parent,
			final String shortName, final boolean updateParent) {
		super(dataset, parent, shortName, updateParent);
	}

	/**
	 * Construct an empty NXentry instance under a parent group.
	 * 
	 * @param parent
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 */
	public NXentry(final NcGroup parent, final String shortName) {
		super(parent.getDataset(), parent, shortName, true);
		addStringAttribute(NXConstants.GLOBAL_NEXUS_CLASS_LABEL,
				NXConstants.ENTRY_NEXUS_CLASS_VALUE);
	}

	// /**
	// * @param dataset
	// * @param parent
	// * @param shortName
	// */
	// public NXentry(NcDataset dataset, Group parent, String shortName) {
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
	public NXentry(final Group from, final NcDataset dataset) {
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
			NXGroup nxGroup = null;
			if (NexusUtils.isNXclass(group, 
					NXConstants.DATA_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXdata(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.INSTRUMENT_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXinstrument(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.USER_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXuser(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.SAMPLE_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXsample(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.MONITOR_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXmonitor(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.NOTE_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXnote(group, dataset);
			} else {
				nxGroup = new NXGroup(group, dataset);
			}
			nxGroup.setParent(this);
			groups.add(nxGroup);
		}
		for (Attribute attribute : from.getAttributes()) {
			NXAttribute ncAttribute = new NXAttribute(attribute);
			attributes.addAttribute(ncAttribute);
		}
		if (isRoot()) {
			this.setParent(null);
		}
	}

	/**
	 * Copy constructor from a GDM IGroup object.
	 * 
	 * @param from
	 *            GDM IGroup object
	 */
	public NXentry(final IGroup from) {
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

		for (IGroup g : from.getGroupList()) {
			NcGroup group = (NcGroup) g;
			NXGroup nxGroup = null;
			if (NexusUtils.isNXclass(group, 
					NXConstants.DATA_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXdata(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.INSTRUMENT_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXinstrument(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.USER_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXuser(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.SAMPLE_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXsample(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.MONITOR_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXmonitor(group, dataset);
			} else if (NexusUtils.isNXclass(group,
					NXConstants.NOTE_NEXUS_CLASS_VALUE)) {
				nxGroup = new NXnote(group, dataset);
			} else {
				nxGroup = new NXGroup(group, dataset);
			}
			nxGroup.setParent(this);
			groups.add(nxGroup);
		}

		for (IAttribute attribute : from.getAttributeList()) {
			NXAttribute ncAttribute = new NXAttribute((Attribute) attribute);
			attributes.addAttribute(ncAttribute);
		}
		if (isRoot()) {
			this.setParent(null);
		}
	}

	@Override
	public INXdata getData() {
		for (IGroup group : getGroupList()) {
			if (group instanceof INXdata) {
				return (INXdata) group;
			}
		}
		for (IDataItem item : getDataItemList()) {
			if (item instanceof ISignal || item.hasAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL,
					NXConstants.SIGNAL_CLASS_VALUE)) {
				return new NXdata(this);
			}
		}
		return null;
	}

	@Override
	public INXinstrument getInstrumentGroup() {
		for (IGroup group : getGroupList()) {
			if (group instanceof INXinstrument) {
				return (INXinstrument) group;
			}
		}
		return null;
	}

	@Override
	public INXmonitor getMonitorGroup() {
		for (IGroup group : getGroupList()) {
			if (group instanceof INXmonitor) {
				return (INXmonitor) group;
			}
		}
		return null;
	}

	@Override
	public INXsample getSampleGroup() {
		for (IGroup group : getGroupList()) {
			if (group instanceof INXsample) {
				return (INXsample) group;
			}
		}
		return null;
	}

	@Override
	public ISignal getSignal() {
		INXdata data = getData();
		if (data == null) {
			return null;
		}
		return data.getSignal();
	}

	@Override
	public INXuser getUserGroup() {
		for (IGroup group : getGroupList()) {
			if (group instanceof INXuser) {
				return (INXuser) group;
			}
		}
		return null;
	}

	@Override
	public List<IAxis> getAxes() {
		ISignal signal = getSignal();
		if (signal == null) {
			return null;
		}
		return signal.findAxes();
	}

	@Override
	public INXnote getNoteGroup() {
		for (IGroup group : getGroupList()) {
			if (group instanceof INXnote) {
				return (INXnote) group;
			}
		}
		return null;
	}

	@Override
	public NXentry findCurrentEntry() {
		return this;
	}
}
