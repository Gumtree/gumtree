/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package org.gumtree.data.impl.utils;

import java.io.IOException;

import org.gumtree.data.interfaces.IDataItem;

import ucar.ma2.DataType;

/**
 * A register class for GDM Arrays. This class has the logic to control memory
 * usage of arrays. If too many data are loaded at the same time which is likely
 * to use up the JVM memory, it triggers the logic to dump some data into
 * physical storage such as hard drive. So that the memory will be cleaned up.
 * When the data is accessed in the future time, they will be loaded back.
 * 
 * @author nxi
 * @version 0.9 Beta (still under construction, not fully performing yet)
 *          Created on 03/03/2009
 */
public class Register extends org.gumtree.data.utils.Register {

	private static Register my_instance; 
	
	public Register() {
		super();
	}

	public static Register getInstance() {
		if (my_instance == null)
			my_instance = new Register();
		return my_instance;
	}
	
	public ucar.ma2.Array getNetcdfArray(long registerId) {
		// if (backupHandler == null)
		// String filename =
		if (getBackupReader() == null)
			return null;
		IDataItem item = getBackupReader().getRootGroup().getGroup(DATA_GROUP_NAME)
				.getDataItem(String.valueOf(registerId));
		if (item == null) {
			try {
				while (isLocked()) {
					try {
						Thread.sleep(200);
					} catch (Exception e) {
					}
				}
				getBackupReader().sync();
				item = getBackupReader().getRootGroup().getDataItem(
						String.valueOf(registerId));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			if (item == null)
				return null;
		}
		ucar.ma2.Array netcdfArray = null;
		try {
			netcdfArray = ucar.ma2.Array.factory(DataType.getType(item.getType()), item.getShape(), item.getData().getStorage() );
			item.invalidateCache();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return netcdfArray;
	}
}
