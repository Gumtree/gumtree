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

import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IArrayIterator;

import ucar.ma2.IndexIterator;

/**
 * An iterator for GDM IArray object.
 * 
 * @author nxi
 * 
 */
public class NcArrayIterator implements IArrayIterator {

	/**
	 * Wrapper of Netcdf IndexIterator.
	 */
	private IndexIterator iterator = null;

	/**
	 * Wrapper constructor.
	 * 
	 * @param indexIterator
	 *            Netcdf IndexIterator
	 */
	public NcArrayIterator(final IndexIterator indexIterator) {
		this.iterator = indexIterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public double getDoubleNext() {
		return iterator.getDoubleNext();
	}

	@Override
	public void setDoubleCurrent(double val) {
		iterator.setDoubleCurrent(val);
	}

	@Override
	public float getFloatNext() {
		return iterator.getFloatNext();
	}

	@Override
	public void setFloatCurrent(float val) {
		iterator.setFloatCurrent(val);
	}

	@Override
	public long getLongNext() {
		return iterator.getLongNext();
	}

	@Override
	public void setLongCurrent(long val) {
		iterator.setLongCurrent(val);
	}

	@Override
	public int getIntNext() {
		return iterator.getIntNext();
	}

	@Override
	public void setIntCurrent(int val) {
		iterator.setIntCurrent(val);
	}

	@Override
	public short getShortNext() {
		return iterator.getShortNext();
	}

	@Override
	public void setShortCurrent(short val) {
		iterator.setShortCurrent(val);
	}

	@Override
	public byte getByteNext() {
		return iterator.getByteNext();
	}

	@Override
	public void setByteCurrent(byte val) {
		iterator.setByteCurrent(val);
	}

	@Override
	public char getCharNext() {
		return iterator.getCharNext();
	}

	@Override
	public void setCharCurrent(char val) {
		iterator.setCharCurrent(val);
	}

	@Override
	public boolean getBooleanNext() {
		return iterator.getBooleanNext();
	}

	@Override
	public void setBooleanCurrent(boolean val) {
		iterator.setBooleanCurrent(val);
	}

	@Override
	public Object getObjectNext() {
		return iterator.getObjectNext();
	}

	@Override
	public void setObjectCurrent(Object val) {
		iterator.setObjectCurrent(val);
	}

	@Override
	public IArrayIterator next() {
		getObjectNext();
		return this;
	}
	
	@Override
	public int[] getCounter() {
		return iterator.getCurrentCounter();
	}

	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

}
