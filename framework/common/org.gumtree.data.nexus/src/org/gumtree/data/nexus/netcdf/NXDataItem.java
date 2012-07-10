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
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.nexus.INXDataItem;
import org.gumtree.data.utils.Utilities;

import ucar.nc2.dataset.VariableDS;

/**
 * Netcdf implementation of NeXus DataItem.
 * @author nxi
 * 
 */
public class NXDataItem extends NcDataItem implements INXDataItem {

	private boolean isFlawed = false;
	private List<Integer> flawedList;
	
	/**
	 * Copy constructor from a Netcdf Variable.
	 * 
	 * @param from
	 *            Netcdf VariableDS object
	 */
	public NXDataItem(final VariableDS from) {
		super(from);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Construct a NXDataItem instance under a given parent group.
	 * 
	 * @param group
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 * @param array
	 *            GDM Array object
	 * @throws InvalidArrayTypeException
	 *             invalid array type
	 */
	public NXDataItem(final NcGroup group, final String shortName,
			final IArray array) throws InvalidArrayTypeException {
		super(group, shortName, array);
	}

	/**
	 * Constructor that initialises fields.
	 * 
	 * @param dataset
	 *            NcDataset
	 * @param group
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 * @param array
	 *            GDM Array object
	 * @throws InvalidArrayTypeException
	 *             invalid array type
	 */
	public NXDataItem(final NcDataset dataset, final NcGroup group,
			final String shortName, final IArray array)
			throws InvalidArrayTypeException {
		super(dataset, group, shortName, array);
	}

	@Override
	public void addOneAttribute(final IAttribute att) {
		super.addOneAttribute(att);
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
			if (title == null || title.length() == 0) {
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

	@Override
	public IArray getData(boolean withTolerance) throws IOException {
		if (withTolerance) {
			try {
				return getData();
			} catch (Exception e) {
				IArray array = (new NcFactory()).createArray(getType(), getShape());
				int rank = getRank();
				int[] origin = new int[rank];
				int[] shape = new int[rank];
				System.arraycopy(getShape(), 0, shape, 0, rank);
				int numberOfFrame = shape[0];
				shape[0] = 1;
				for (int i = 0; i < numberOfFrame; i++) {
					try {
						origin[0] = i;
						IArray slice = array.getArrayUtils().section(origin, shape).getArray();
						IArray partArray = getData(origin, shape);
						Utilities.copyTo(partArray, slice, -1);
					} catch (Exception e2) {
						System.out.println("add to flawed list " + i);
						addToFlawedList(i);
					}
				} 
				try {
					setCachedData(array, false);
				} catch (InvalidArrayTypeException e1) {
				}
				return array;
			}
		} else {
			return getData();
		}
	}

	/**
	 * @param isFlawed the isFlawed to set
	 */
	public void setFlawed(boolean isFlawed) {
		this.isFlawed = isFlawed;
	}

	/**
	 * @return the isFlawed
	 */
	public boolean isFlawed() {
		return isFlawed;
	}

	private void addToFlawedList(int index) {
		if (flawedList == null) {
			flawedList = new ArrayList<Integer>();
		}
		flawedList.add(index);
		setFlawed(true);
	}
	
	public List<Integer> getFlawedIndexList() {
		return flawedList;
	}
}
