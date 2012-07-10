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
import java.util.List;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.impl.netcdf.NcArray;
import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXDataItem;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.INXentry;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.IVariance;
import org.gumtree.data.nexus.netcdf.NXConstants;
import org.gumtree.data.nexus.netcdf.NXDataItem;
import org.gumtree.data.nexus.netcdf.NXDataset;
import org.gumtree.data.nexus.netcdf.NXaxis;
import org.gumtree.data.nexus.netcdf.NXdata;
import org.gumtree.data.nexus.netcdf.NXentry;
import org.gumtree.data.nexus.netcdf.NXsignal;
import org.gumtree.data.nexus.netcdf.NXvariance;

/**
 * @author nxi
 *
 */
public class NexusFactory extends NcFactory{

	public NexusFactory() {
	}
	
	public INXDataset createNXDataset() {
		return new NXDataset();
	}
	
	public INXentry createNXentry(IGroup parent, String shortName, INXdata nxData) {
		INXentry entry = new NXentry((NcGroup) parent, shortName);
		if (nxData != null) {
			entry.addSubgroup(nxData);
		}
		return entry;
	}
	
	public INXdata createNXdata(IGroup parent, String shortName) {
		if (parent == null) {
			try {
				parent = createGroup("temp");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		if (shortName == null) {
			shortName = "temp";
		}
		return new NXdata((NcGroup) parent, shortName);
	}
	
	public ISignal createNXsignal(IGroup parent, String shortName, IArray array) 
	throws InvalidArrayTypeException, IOException {
		if (parent == null) {
			parent = createGroup("temp");
		} 
		if (shortName == null) {
			shortName = "temp";
		}
		return new NXsignal((NcGroup) parent, shortName, array);
	}
	
	public IVariance createNXvariance(IGroup parent, IArray array) 
	throws InvalidArrayTypeException, IOException {
		if (parent == null) {
			parent = createGroup("temp");
		} 
		IVariance variance = new NXvariance((NcGroup) parent, 
				NXConstants.VARANCE_CLASS_VALUE, array);
		parent.addDataItem(variance);
		return variance;
	}
	
	public INXdata createNXdata(IGroup parent, String shortName, 
			ISignal signal, List<IAxis> axes) throws IOException {
		if (parent == null) {
			parent = createGroup("temp");
		} 
		if (shortName == null) {
			shortName = "temp";
		}
		INXdata data = new NXdata((NcGroup) parent, shortName);
		data.setSignal(signal);
		data.setAxes(axes);
		return data;
	}
	
	public IAxis createNXaxis(IGroup parent, String shortName, IArray array)
	throws InvalidArrayTypeException, IOException {
		if (parent == null) {
			parent = createGroup("temp");
		} 
		if (shortName == null) {
			shortName = "index";
		}
		IAxis axis = new NXaxis((NcGroup) parent, shortName, array);
		parent.addDataItem(axis);
		return axis;
	}
	
	public INXDataItem copyToNXDataItem(IDataItem dataItem) {
		return new NXDataItem((NcDataItem) dataItem);
	}
	
	public INXDataItem createNXDataItem(IGroup parent, String shortName, IArray array) 
	throws InvalidArrayTypeException {
		if (parent == null) {
			try {
				return new NXDataItem((NcGroup) createGroup("temp"), shortName,
						(NcArray) array);
			} catch (IOException e) {
				throw new InvalidArrayTypeException("IO exception");
			}
		}
		return new NXDataItem((NcGroup) parent, shortName, (NcArray) array);
	}
}
