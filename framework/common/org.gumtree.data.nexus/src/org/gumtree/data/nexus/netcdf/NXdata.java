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
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.IVariance;
import org.gumtree.data.nexus.utils.NexusUtils;

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
public class NXdata extends NXGroup implements INXdata {

	/**
	 * Constructor that initialise necessary fields.
	 * 
	 * @param dataset
	 *            NcDataset holder
	 * @param parent
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 * @param updateParent
	 *            if the parent know this child group
	 */
	public NXdata(final NcDataset dataset, final NcGroup parent,
			final String shortName, final boolean updateParent) {
		super(dataset, parent, shortName, updateParent);
	}

	/**
	 * Create a NXdata instance in a parent group.
	 * 
	 * @param parent
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 */
	public NXdata(final NcGroup parent, final String shortName) {
		super(parent.getDataset(), parent, shortName, false);
		addStringAttribute(NXConstants.GLOBAL_NEXUS_CLASS_LABEL,
				NXConstants.DATA_NEXUS_CLASS_VALUE);
	}

	/**
	 * @param dataset
	 * @param parent
	 * @param shortName
	 */
	// public NXdata(NcDataset dataset, Group parent, String shortName) {
	// super(dataset, parent, shortName);
	// }

	/**
	 * Copy constructor from a Netcdf Group object.
	 * 
	 * @param from
	 *            Netcdf Group object
	 * @param dataset
	 *            parent dataset
	 */
	public NXdata(final Group from, final NcDataset dataset) {
		this(dataset, null, from.getShortName(), false);
		List<Variable> variableList = new ArrayList<Variable>(from
				.getVariables());
		// Collections.copy(variableList, from.getVariables());
		Variable dataVariable = findNXsignal(variableList);
		if (dataVariable != null) {
			NXsignal signal = new NXsignal((VariableDS) dataVariable);
			signal.setParent(this);
			signal.setDataset(dataset);
			variables.add(signal);
			variableList.remove(dataVariable);
			List<Variable> axisList = NexusUtils.findNXaxisList(from,
					dataVariable);
			for (Variable axis : axisList) {
				NXaxis nxAxis = new NXaxis((VariableDS) axis);
				nxAxis.setParent(this);
				nxAxis.setDataset(dataset);
				variables.add(nxAxis);
			}
			variableList.removeAll(axisList);
			Variable variance = findVariance(variableList);
			if (variance != null) {
				NXvariance nxVariance = new NXvariance((VariableDS) variance);
				nxVariance.setParent(this);
				nxVariance.setDataset(dataset);
				variables.add(nxVariance);
				variableList.remove(variance);
			}
		}
		for (Variable variable : variableList) {
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
	 * Copy constructor from another GDM IGroup object.
	 * 
	 * @param from
	 *            GDM IGroup object
	 */
	public NXdata(final IGroup from) {
		this((NcDataset) from.getDataset(), (NcGroup) from.getParentGroup(),
				from.getShortName(), true);
		for (IDataItem dataItem : from.getDataItemList()) {
			NXDataItem nxDataItem;
			if (NexusUtils.isNXsignal((NcDataItem) dataItem,
					NXConstants.SIGNAL_CLASS_VALUE)) {
				nxDataItem = new NXsignal((NcDataItem) dataItem);
			} else if (dataItem instanceof NXaxis){
				nxDataItem = new NXaxis((NcDataItem) dataItem);
			} else if (dataItem instanceof NXvariance){
				nxDataItem = new NXvariance((NcDataItem) dataItem);
			} else {
				nxDataItem = new NXDataItem((NcDataItem) dataItem);
			}
			nxDataItem.setParent(this);
			variables.add(nxDataItem);
		}
		for (IDimension dimension : from.getDimensionList()) {
			if (dimension instanceof NcDimension) {
				addDimension((NcDimension) dimension);
			}
		}

		for (IGroup g : from.getGroupList()) {
			NcGroup group = (NcGroup) g;
			NXGroup nxGroup = new NXGroup(group, dataset);
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
	public List<IAxis> getAxisList() {
		ISignal signal = getSignal();
		return signal.findAxes();
	}

	@Override
	public ISignal getSignal() {
		for (IDataItem dataItem : getDataItemList()) {
			if (dataItem instanceof ISignal) {
				return (ISignal) dataItem;
			}
		}
		return null;
	}

	/**
	 * Find the signal from a list of Netcdf variables in the NXdata group.
	 * 
	 * @param variableList
	 *            a list of Netcdf Variable objects
	 * @return Netcdf Variable
	 */
	private Variable findNXsignal(final List<?> variableList) {
		for (Object object : variableList) {
			if (object instanceof VariableDS) {
				Variable variable = (VariableDS) object;
				if (NexusUtils.isNXsignal(variable,
						NXConstants.SIGNAL_CLASS_VALUE)) {
					return variable;
				}
			}
		}
		return null;
	}

	/**
	 * Find the signal from a list of Netcdf variables in the NXdata group.
	 * 
	 * @param variableList
	 *            a list of Netcdf Variable objects
	 * @return Netcdf Variable
	 */
	private Variable findVariance(final List<?> variableList) {
		for (Object object : variableList) {
			if (object instanceof VariableDS) {
				Variable variable = (VariableDS) object;
				if (NexusUtils.isNXsignal(variable,
						NXConstants.VARANCE_CLASS_VALUE)) {
					return variable;
				}
			}
		}
		return null;
	}
	
	@Override
	public void setSignal(ISignal signal) {
		ISignal currentSignal = getSignal();
		if (currentSignal != null) {
			removeDataItem(signal);
		}
		addDataItem(signal);
	}

	@Override
	public void setAxes(List<IAxis> axes) {
		if (axes == null || axes.size() == 0) {
			return;
		}
		String axesAttrValue = "";
		for (IAxis axis : axes) {
			axesAttrValue += axis.getShortName() + ":";
		}
		if (axes.size() > 0) {
			axesAttrValue = axesAttrValue.substring(0, 
					axesAttrValue.length() - 1);
		}
		ISignal signal = getSignal();
		if (signal != null) {
			signal.addStringAttribute(NXConstants.SIGNAL_AXES_LABEL, 
					axesAttrValue);
		}
	}

	@Override
	public IVariance getVariance() {
		for (IDataItem dataItem : getDataItemList()) {
			if (dataItem instanceof IVariance) {
				return (IVariance) dataItem;
			}
		}
		return null;
	}

	@Override
	public void setVariance(IVariance variance) {
		IVariance currentVariance = getVariance();
		if (currentVariance != null) {
			removeDataItem(variance);
		}
		addDataItem(variance);
	}

	@Override
	public void setMultipleAxes(IAxis... axes) {
		List<IAxis> axisList = new ArrayList<IAxis>();
		for (IAxis axis : axes) {
			axisList.add(axis);
		}
		setAxes(axisList);
	}

}
