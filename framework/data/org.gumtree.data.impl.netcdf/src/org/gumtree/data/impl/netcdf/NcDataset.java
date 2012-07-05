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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.exception.WriterException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.impl.io.NcHdfWriter;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Netcdf implementation of IDataset.
 * 
 * @author nxi
 * 
 */
public class NcDataset implements IDataset {

	/**
	 * String value.
	 */
	private String location;
	/**
	 * Netcdf dataset wrapper.
	 */
	protected NetcdfDataset netcdfDataset;
	/**
	 * Root group.
	 */
	protected NcGroup rootGroup = null;
	/**
	 * 
	 */
	private boolean isSyncronized = false;

	/**
	 * Default constructor.
	 */
	public NcDataset() {
		super();
	}

	/**
	 * Return the netcdf dataset core.
	 * 
	 * @return Netcdf Dataset object
	 */
	public NetcdfFile getNetcdfDataset() {
		return netcdfDataset;
	}

	/**
	 * Constructor with the location of the dataset.
	 * 
	 * @param locationString
	 *            String value
	 */
	public NcDataset(final String locationString) {
		this.location = locationString;
	}

	/**
	 * Wrapper constructor from an NetcdfDataset.
	 * 
	 * @param dataset
	 *            Netcdf dataset
	 * @throws IOException
	 *             I/O error
	 */
	public NcDataset(final NetcdfDataset dataset) throws IOException {
		this.location = dataset.getLocation();
		this.netcdfDataset = dataset;
		createRootGroup();
	}

	/**
	 * Open an dataset file at a given location.
	 * 
	 * @param location
	 *            String value
	 * @return NcDataset object
	 * @throws IOException
	 *             I/O exception
	 */
	public static NcDataset open(final String location) throws IOException {
		return new NcDataset(NetcdfDataset.openDataset(location));
	}

	@Override
	public NcGroup getRootGroup() {
		return rootGroup;
	}

	@Override
	public void close() throws IOException {
		if (netcdfDataset != null) {
			netcdfDataset.close();
			netcdfDataset = null;
		}
		rootGroup = null;
	}

	@Override
	public String getLocation() {
		// if (netcdfDataset != null) {
		// return netcdfDataset.getLocation();
		// } else {
		// return location;
		// }
		return location;
	}

	@Override
	public String getTitle() {
		if (netcdfDataset != null) {
			return netcdfDataset.getTitle();
		} else {
			File file = new File(location);
			return file.getName();
		}
	}

	// public boolean isClosed() {
	// return netcdfDataset.isClosed();
	// }

	@Override
	public void open() throws IOException {
		// return new NcDataset(NetcdfDataset.openDataset(
		// netcdfDataset.getLocation()));
		if (netcdfDataset == null) {
			if (location != null) {
				netcdfDataset = NetcdfDataset.openDataset(location);
			} else {
				netcdfDataset = new NetcdfDataset();
			}
			createRootGroup();
		}
	}

	/**
	 * Routine to create the root group.
	 */
	protected void createRootGroup() {
		rootGroup = new NcGroup(netcdfDataset.getRootGroup(), this);
	}

	@Override
	public void setLocation(final String locationString) {
		this.location = locationString;
		if (netcdfDataset != null) {
			netcdfDataset.setLocation(locationString);
		}
	}

	@Override
	public void setTitle(final String title) {
		if (netcdfDataset != null) {
			netcdfDataset.setTitle(title);
		}
	}

	@Override
	public boolean sync() throws IOException {
		return netcdfDataset.sync();
	}

	/**
	 * Create an empty dataset with a given location.
	 * 
	 * @param location
	 *            String value
	 * @return NcDataset object
	 * @throws Exception
	 *             any error
	 */
	public static NcDataset createDataset(final String location)
			throws Exception {
		return new NcDataset(location);
	}

	/**
	 * Create an Dataset instance with empty location property.
	 * 
	 * @return new NcDataset object
	 * @throws IOException
	 *             I/O error
	 */
	public static NcDataset createDataset() throws IOException {
		return new NcDataset();
	}

	@Override
	public void save() throws WriterException {
		try {
			readAll(getRootGroup());
			netcdfDataset.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WriterException(e);
		}
		if (location == null || location.trim().length() == 0) {
			throw new WriterException("failed to write to null file");
		}
		NcHdfWriter hdfWriter = new NcHdfWriter(new File(location));
		hdfWriter.open();
		hdfWriter.writeToRoot(getRootGroup(), true);
		hdfWriter.close();
	}

	@Override
	public void saveTo(String location) throws WriterException {
		try {
			readAll(getRootGroup());
//			netcdfDataset.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WriterException(e);
		}
		if (location == null || location.trim().length() == 0) {
			throw new WriterException("failed to write to null file");
		}
		NcHdfWriter hdfWriter = new NcHdfWriter(new File(location));
		hdfWriter.open();
		hdfWriter.writeToRoot(getRootGroup(), true);
		hdfWriter.close();
	}
	
	@Override
	public void save(IContainer object) throws WriterException {
		try {
			System.out.println("save called");
			readAll(getRootGroup());
			netcdfDataset.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WriterException(e);
		}
		if (location == null || location.trim().length() == 0) {
			throw new WriterException("failed to write to null file");
		}
		NcHdfWriter hdfWriter = new NcHdfWriter(new File(getLocation()));
		hdfWriter.open();
		if (object instanceof IGroup) {
			IGroup group = (IGroup) object;
			if (group.isRoot()) {
				hdfWriter.writeToRoot(group, true);
			} else {
				hdfWriter.writeGroup(group.getParentGroup().getName(), 
						group, true);
			}
		} else if (object instanceof IDataItem) {
			IDataItem item = (IDataItem) object;
			hdfWriter.writeDataItem(item.getParentGroup().getName(), 
					item, true);
		} 
//		hdfWriter.close();
	}

	@Override
	public void save(String parentPath, IAttribute attribute) 
	throws WriterException {
		try {
			readAll(getRootGroup());
			netcdfDataset.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WriterException(e);
		}
		if (location == null || location.trim().length() == 0) {
			throw new WriterException("failed to write to null file");
		}
		NcHdfWriter hdfWriter = new NcHdfWriter(new File(getLocation()));
		hdfWriter.open();
		hdfWriter.writeAttribute(parentPath, attribute, true);
		hdfWriter.close();
	}
	
	private void readAll(IGroup group) throws IOException {
		for (IDataItem item : group.getDataItemList()) {
			item.getData();
		}
		for (IGroup subgroup : group.getGroupList()) {
			readAll(subgroup);
		}
	}

	@Override
	public void writeNcML(final OutputStream os, final String uri)
			throws IOException {
		if (!isSyncronized) {
			copyToNetcdfDataset();
			isSyncronized = true;
		}
		netcdfDataset.writeNcML(os, uri);
	}

	/**
	 * Copy the GDM dataset to a NetcdfDataset instance.
	 */
	private void copyToNetcdfDataset() {
		Group netcdfRootGroup = netcdfDataset.getRootGroup();
		for (Group group : rootGroup.getGroups()) {
			netcdfRootGroup
					.addGroup(((NcGroup) group).enhance(netcdfRootGroup));
		}
		for (Attribute attribute : rootGroup.getAttributes()) {
			netcdfRootGroup.addAttribute(attribute);
		}
		for (Dimension dimension : rootGroup.getDimensions()) {
			netcdfRootGroup.addDimension(dimension);
		}
		for (Variable variable : rootGroup.getVariables()) {
			netcdfRootGroup.addVariable(((NcDataItem) variable)
					.enhance(netcdfRootGroup));
		}
	}

	@Override
	public boolean isOpen() {
		return netcdfDataset == null;
	}

	/**
	 * Set the root of the dataset.
	 * 
	 * @param root
	 *            NcGroup object
	 */
	protected void setRootGroup(final NcGroup root) {
		rootGroup = root;
	}
	
	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

	// TODO [SOLEIL][clement]
	// Add those two methods to fit new mechanism of dictionary.
	// The ILogicalGroup, that can be seen as root, is a specific browsing system
	// that uses IKey to iterate searches
	// TODO END
	@Override
	public ILogicalGroup getLogicalRoot() {
		// TODO Auto-generated method stub
		return null;
	}
}
