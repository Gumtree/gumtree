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
package org.gumtree.data.nexus.utils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.INXentry;
import org.gumtree.data.nexus.INXroot;
import org.gumtree.data.nexus.netcdf.NXConstants;
import org.gumtree.data.nexus.netcdf.NXDataset;
import org.slf4j.LoggerFactory;

import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.Variable;

/**
 * Utilities class for NeXus model.
 * 
 * @author nxi
 * 
 */
public final class NexusUtils {

	/**
	 * Hide the default constructor.
	 */
	private NexusUtils() {
	}

	/**
	 * Read a file in a NeXus format.
	 * 
	 * @param uri
	 *            URI link
	 * @return NXroot object
	 * @throws IOException
	 *             failed to open the file
	 */
	public static INXroot readNexusRoot(final URI uri) throws IOException {
		return readNexusRoot(uri.getPath());
	}

	/**
	 * Read a file in a NeXus format.
	 * 
	 * @param uri
	 *            URI link
	 * @return NXDataset object
	 * @throws IOException
	 *             failed to open the file
	 */
	public static INXDataset readNexusDataset(final URI uri) throws IOException {
		return readNexusDataset(uri.getPath());
	}
	
	/**
	 * Read a file in a NeXus format.
	 * 
	 * @param path
	 *            the file path as a String value
	 * @return NXroot object
	 * @throws IOException
	 *             failed to open the file
	 */
	public static INXroot readNexusRoot(final String path) throws IOException {
		INXDataset dataset = new NXDataset(path);
		dataset.open();
		return dataset.getNXroot();
	}

	/**
	 * Read a file in a NeXus format.
	 * 
	 * @param path
	 *            the file path as a String value
	 * @return NXDataset object
	 * @throws IOException
	 *             failed to open the file
	 */
	public static INXDataset readNexusDataset(final String path) throws IOException {
		INXDataset dataset = new NXDataset(path);
		dataset.open();
		return dataset;
	}
	
	/**
	 * Create an instance of NXDataset with a given file path. The dataset is
	 * not open yet. Use {@link INXDataset#open()} before access to the root of
	 * the dataset.
	 * 
	 * @param path
	 *            String value
	 * @return NXDataset object
	 */
	public static INXDataset createNXDatasetInstance(final String path) {
		return new NXDataset(path);
	}

	/**
	 * Create an instance of NXDataset with a given file URI. The dataset is not
	 * open yet. Use {@link INXDataset#open()} before access to the root of the
	 * dataset.
	 * 
	 * @param uri
	 *            URI
	 * @return NXDataset object
	 */
	public static INXDataset createNXDatasetInstance(final URI uri) {
		return new NXDataset(uri.getPath());
	}

	/**
	 * Create a new empty NXroot object. This also create the parent NXDataset
	 * in the memory.
	 * 
	 * @return new NXroot object
	 */
	public static INXroot createNewNXroot() {
		INXDataset dataset = new NXDataset();
		try {
			dataset.open();
		} catch (IOException e) {
			LoggerFactory.getLogger(NXDataset.class).error(
					"can not create empty dataset in memory", e);
		}
		return dataset.getNXroot();
	}

	/**
	 * Check if a GDM Group is a NXentry.
	 * 
	 * @param group
	 *            GDM IGroup object
	 * @return true or false
	 */
	public static boolean isInEntryFormat(final IGroup group) {
		return group.hasAttribute(NXConstants.GLOBAL_NEXUS_CLASS_LABEL,
				NXConstants.ENTRY_NEXUS_CLASS_VALUE);
	}

	/**
	 * Check if a GDM group is a type of NeXus class referenced by a class name.
	 * 
	 * @param group
	 *            GDM IGroup object
	 * @param className
	 *            String value
	 * @return true or false
	 */
	public static boolean isNXclass(final Group group, final String className) {
		Attribute nxAttribute = group
				.findAttribute(NXConstants.GLOBAL_NEXUS_CLASS_LABEL);
		if (nxAttribute != null) {
			if (nxAttribute.getStringValue().equals(className)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a Netcdf Variable is a NXsignal object (has a signal=1
	 * attribute).
	 * 
	 * @param variable
	 *            Netcdf Variable object
	 * @param classValue
	 *            String value of signal attribute
	 * @return true or false
	 */
	public static boolean isNXsignal(final Variable variable,
			final String classValue) {
		Attribute nxAttribute = variable
				.findAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL);
		if (nxAttribute != null) {
			if (nxAttribute.getStringValue().equals(classValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find the axes for the NeXus signal in the Netcdf Group.
	 * 
	 * @param group
	 *            Netcdf Group object
	 * @param dataVariable
	 *            signal in Netcdf Variable type
	 * @return List of Netcdf Variable objects
	 */
	public static List<Variable> findNXaxisList(final Group group,
			final Variable dataVariable) {
		List<Variable> axisList = new ArrayList<Variable>();
		Attribute axesAttribute = dataVariable
				.findAttribute(NXConstants.SIGNAL_AXES_LABEL);
		if (axesAttribute != null) {
			String value = axesAttribute.getStringValue();
			String[] axisNames = value.split(":");
			for (String string : axisNames) {
				Variable axisItem = group.findVariable(string);
				if (axisItem != null) {
					axisList.add(axisItem);
				}
			}
		}
		return axisList;
	}
	
	public static INXdata getNXdata(Object obj) {
		if (obj instanceof INXDataset) {
			return ((INXDataset) obj).getNXroot().getDefaultData();
		}
		if (obj instanceof INXroot) {
			return ((INXroot) obj).getDefaultData();
		}
		if (obj instanceof INXentry) {
			return ((INXentry) obj).getData();
		}
		if (obj instanceof INXdata) {
			return (INXdata) obj;
		}
		return null;
	}
}
