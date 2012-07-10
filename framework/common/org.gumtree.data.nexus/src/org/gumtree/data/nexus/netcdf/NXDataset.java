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

import java.io.IOException;

import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXroot;

import ucar.nc2.dataset.NetcdfDataset;

/**
 * Netcdf implementation of NeXus file.
 * @author nxi
 * 
 */
public class NXDataset extends NcDataset implements INXDataset {

	/**
	 * Default constructor.
	 */
	public NXDataset() {
		super();
	}

	/**
	 * Construct a NXDataset instance with a given file location.
	 * 
	 * @param location
	 *            String value
	 */
	public NXDataset(final String location) {
		super(location);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Copy constructor from Netcdf dataset.
	 * 
	 * @param netcdfDataset
	 *            Netcdf dataset
	 * @throws IOException
	 *             I/O error
	 */
	public NXDataset(final NetcdfDataset netcdfDataset) throws IOException {
		super(netcdfDataset);
	}

	@Override
	public void open() throws IOException {
		if (netcdfDataset == null) {
			if (getLocation() != null) {
				netcdfDataset = NetcdfDataset.openDataset(getLocation());
				createRootGroup();
			} else {
				netcdfDataset = new NetcdfDataset();
				makeNewRootGroup();
			}
		}

	}

	/**
	 * Make a new root group, which is in NXroot type.
	 */
	protected void makeNewRootGroup() {
		setRootGroup(new NXroot(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.data.nexus.INXDataset#getNXroot()
	 */
	@Override
	public INXroot getNXroot() {
		return (INXroot) getRootGroup();
	}

	@Override
	protected void createRootGroup() {
		rootGroup = new NXroot(netcdfDataset.getRootGroup(), this);
	}

	@Override
	public NXroot getRootGroup() {
		return (NXroot) super.getRootGroup();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		NXDataset newDataset = new NXDataset();
		newDataset.setLocation(getLocation());
		newDataset.setRootGroup(new NXroot(getNXroot()));
		return newDataset;
	}
	
	@Override
	public String getTitle() {
		INXroot root = getNXroot();
		if (root == null) {
			return super.getTitle();
		} else {
			return root.getTitle();
		}
	}
	
	@Override
	public void setTitle(String title) {
		INXroot root = getNXroot();
		if (root == null) {
			super.setTitle(title);
		} else {
			root.setTitle(title);
		}
	}
}
