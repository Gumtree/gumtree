/******************************************************************************
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong - initial API and implementation
 *    Paul Hathaway - extensions December 2008
 ******************************************************************************/
package org.gumtree.data.impl.netcdf;

import org.gumtree.data.IFactory;
import org.gumtree.data.exception.BackupException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.impl.math.NcArrayMath;
import org.gumtree.data.impl.utils.NcArrayUtils;
import org.gumtree.data.impl.utils.Register;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.math.IArrayMath;
import org.gumtree.data.utils.IArrayUtils;

import ucar.ma2.MAMath;
import ucar.ma2.Section;

/**
 * Netcdf implementation of GDM Array.
 * 
 * @author nxi
 * 
 */
public class NcArray implements IArray {

	/**
	 * Use Netcdf Array as data storage.
	 */
	private ucar.ma2.Array array = null;
	/**
	 * Use this id to register the Array at the data manager.
	 */
	private long registerId = 0;
	/**
	 * The dirty flag.
	 */
	private boolean isDirty = true;
	/**
	 * The lock flag.
	 */
	private boolean isLocked = false;
	
	private IArrayMath arrayMath;

	/**
	 * Constructor from a Netcdf array storage.
	 * 
	 * @param netcdfArray
	 *            Netcdf Array object
	 */
	public NcArray(final ucar.ma2.Array netcdfArray) {
		this.array = netcdfArray;
		registerId = Register.getInstance().getArrayRegisterId(this);
		IFactory factory = new NcFactory();
		arrayMath = new NcArrayMath(this, factory);
		// if (registerId != 0)
		// Register.getInstance().registerNewArray(this);
	}

	@Override
	public IArrayMath getArrayMath() {
		return arrayMath;
	}

	@Override
	public IArrayUtils getArrayUtils() {
		return new NcArrayUtils(this);
	}

	@Override
	public void setDirty(boolean dirty) {
		isDirty = dirty;
	}

	/**
	 * Get the register id for data management.
	 * 
	 * @return long value
	 * @see org.gumtree.data.interfaces.IArray#getRegisterId()
	 */
	@Override
	public long getRegisterId() {
		return registerId;
	}

	/**
	 * Get the data storage.
	 * 
	 * @return Netcdf Array object
	 */
	public ucar.ma2.Array getArray() {
		if (array != null) {
			return array;
		} else {
			int time = 0;
			final int sleepTimeMilisec = 100;
			while (isLocked) {
				try {
					Thread.sleep(sleepTimeMilisec);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				time += sleepTimeMilisec;
				if (time > Register.LOCK_TIMEOUT) {
					break;
				}
			}
			array = Register.getInstance().getNetcdfArray(registerId);
			isDirty = false;
			return array;
		}
	}

	/**
	 * Release the storage by clear the Netcdf array.
	 * 
	 * @throws BackupException
	 *             can not release because it is not reload-able.
	 * @see org.gumtree.data.interfaces.IArray#releaseStorage()
	 */
	@Override
	public void releaseStorage() throws BackupException {
		if (isDirty) {
			System.out.println("write data to backup file");
			if (Register.getInstance().backupArray(registerId, this)) {
				array = null;
			}
		} else {
			array = null;
		}
	}

	/**
	 * Lock the Array so that it can not be changed.
	 * 
	 * @see org.gumtree.data.interfaces.IArray#lock()
	 */
	@Override
	public final void lock() {
		isLocked = true;
	}

	/**
	 * Unlock the Array so that it can be changed.
	 * 
	 * @see org.gumtree.data.interfaces.IArray#unlock()
	 */
	@Override
	public void unlock() {
		isLocked = false;
	}

	/**
	 * Check the dirty flag.
	 * 
	 * @return true or false
	 * @see org.gumtree.data.interfaces.IArray#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Get the shape of the Array.
	 * 
	 * @return array of integer
	 * @see org.gumtree.data.interfaces.IArray#getShape()
	 */
	@Override
	public int[] getShape() {
		return getArray().getShape();
	}

	/**
	 * Make a deep copy of the Array. The return object has a new data storage.
	 * 
	 * @return new NcArray object
	 * @see org.gumtree.data.interfaces.IArray#copy()
	 */
	@Override
	public NcArray copy() {
		return new NcArray(getArray().copy());
	}

	@Override
	public IArray copy(boolean data) {
		// [ANSTO][Tony][2011-09-02] We do not support lightweight copy.
		// So regardless the data parameter, we always perform deep copy.
		// TODO: Make lightweight copy available
		return copy();
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @return boolean value
	 * @see org.gumtree.data.interfaces.IArray#getBoolean(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public boolean getBoolean(final IIndex ima) {
		return getArray().getBoolean(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @return byte value
	 * @see org.gumtree.data.interfaces.IArray#getByte(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public final byte getByte(final IIndex ima) {
		return getArray().getByte(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @return char value
	 * @see org.gumtree.data.interfaces.IArray#getChar(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public char getChar(final IIndex ima) {
		return getArray().getChar(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @return double value
	 * @see org.gumtree.data.interfaces.IArray#getDouble(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public double getDouble(final IIndex ima) {
		return getArray().getDouble(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * @return Class instance
	 * @see org.gumtree.data.interfaces.IArray#getElementType()
	 */
	@Override
	public Class<?> getElementType() {
		return getArray().getElementType();
	}

	/**
	 * @param ima
	 *            the IIndex object
	 * @return float value
	 * @see org.gumtree.data.interfaces.IArray#getFloat(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public float getFloat(final IIndex ima) {
		return getArray().getFloat(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * @return IIndex object
	 * @see org.gumtree.data.interfaces.IArray#getIndex()
	 */
	@Override
	public IIndex getIndex() {
		return new NcIndex(getArray().getIndex());
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @return integer value
	 * @see org.gumtree.data.interfaces.IArray#getInt(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public int getInt(final IIndex ima) {
		return getArray().getInt(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * Get long value.
	 * 
	 * @param ima
	 *            IIndex object
	 * @return long value
	 * @see org.gumtree.data.interfaces.IArray#getLong(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public long getLong(final IIndex ima) {
		return getArray().getLong(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @return generic object
	 * @see org.gumtree.data.interfaces.IArray#getObject(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public Object getObject(final IIndex ima) {
		return getArray().getObject(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * @return integer value
	 * @see org.gumtree.data.interfaces.IArray#getRank()
	 */
	@Override
	public int getRank() {
		return getArray().getRank();
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @return short value
	 * @see org.gumtree.data.interfaces.IArray#getShort(org.gumtree.data.interfaces.IIndex)
	 */
	@Override
	public short getShort(final IIndex ima) {
		return getArray().getShort(((NcIndex) ima).getNetcdfIndex());
	}

	/**
	 * @return long value
	 * @see org.gumtree.data.interfaces.IArray#getSize()
	 */
	@Override
	public long getSize() {
		return getArray().getSize();
	}

	/**
	 * @return generic object
	 * @see org.gumtree.data.interfaces.IArray#getStorage()
	 */
	@Override
	public Object getStorage() {
		return getArray().getStorage();
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            boolean value
	 * @see org.gumtree.data.interfaces.IArray#setBoolean(org.gumtree.data.interfaces.IIndex,
	 *      boolean)
	 */
	@Override
	public void setBoolean(final IIndex ima, final boolean value) {
		getArray().setBoolean(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            byte value
	 * @see org.gumtree.data.interfaces.IArray#setByte(org.gumtree.data.interfaces.IIndex,
	 *      byte)
	 */
	@Override
	public void setByte(final IIndex ima, final byte value) {
		getArray().setByte(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            char value
	 * @see org.gumtree.data.interfaces.IArray#setChar(org.gumtree.data.interfaces.IIndex,
	 *      char)
	 */
	@Override
	public void setChar(final IIndex ima, final char value) {
		getArray().setChar(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            double value
	 * @see org.gumtree.data.interfaces.IArray#setDouble(org.gumtree.data.interfaces.IIndex,
	 *      double)
	 */
	@Override
	public void setDouble(final IIndex ima, final double value) {
		getArray().setDouble(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * Set double value to all values of the Array.
	 * 
	 * @param value
	 *            double value
	 * @return this
	 */
	@Override
	public IArray setDouble(final double value) {
		MAMath.setDouble(getArray(), value);
		isDirty = true;
		return this;
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            float value
	 * @see org.gumtree.data.interfaces.IArray#setFloat(org.gumtree.data.interfaces.IIndex,
	 *      float)
	 */
	@Override
	public void setFloat(final IIndex ima, final float value) {
		getArray().setFloat(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            integer value
	 * @see org.gumtree.data.interfaces.IArray#setInt(org.gumtree.data.interfaces.IIndex,
	 *      int)
	 */
	@Override
	public void setInt(final IIndex ima, final int value) {
		getArray().setInt(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            long value
	 * @see org.gumtree.data.interfaces.IArray#setLong(org.gumtree.data.interfaces.IIndex,
	 *      long)
	 */
	@Override
	public void setLong(final IIndex ima, final long value) {
		getArray().setLong(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            generic object
	 * @see org.gumtree.data.interfaces.IArray#setObject(org.gumtree.data.interfaces.IIndex,
	 *      java.lang.Object)
	 */
	@Override
	public void setObject(final IIndex ima, final Object value) {
		getArray().setObject(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * @param ima
	 *            IIndex object
	 * @param value
	 *            short value
	 * @see org.gumtree.data.interfaces.IArray#setShort(org.gumtree.data.interfaces.IIndex,
	 *      short)
	 */
	@Override
	public void setShort(final IIndex ima, final short value) {
		getArray().setShort(((NcIndex) ima).getNetcdfIndex(), value);
		isDirty = true;
	}

	/**
	 * @return String value
	 * @see org.gumtree.data.interfaces.IArray#shapeToString()
	 */
	@Override
	public String shapeToString() {
		return getArray().shapeToString();
	}

	/**
	 * @param dim
	 *            integer value
	 * @param value
	 *            integer value
	 * @return IArray object
	 * @see org.gumtree.data.interfaces.IArray#slice(int, int)
	 */
	public IArray slice(final int dim, final int value) {
		return new NcArray(getArray().slice(dim, value));
	}

	/**
	 * @return String value
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (getArray() == null) {
			return null;
		}
		String string = getArray().toString();
		if (string.length() > 1 && string.charAt(string.length() - 1) == ' ') {
			return string.substring(0, string.length() - 1);
		}
		return string;
	}

	/**
	 * @return IArrayIterator object
	 * @see org.gumtree.data.interfaces.IArray#getIterator()
	 */
	@Override
	public IArrayIterator getIterator() {
		return new NcArrayIterator(getArray().getIndexIterator());
	}

	/**
	 * @param reference
	 *            array of integer
	 * @param range
	 *            array of integer
	 * @return IArrayIterator object
	 * @throws InvalidRangeException
	 *             invalid range
	 * @see org.gumtree.data.interfaces.IArray#getRegionIterator(int[], int[])
	 */
	@Override
	public IArrayIterator getRegionIterator(final int[] reference,
			final int[] range) throws InvalidRangeException {
		IArrayIterator iterator = null;
		try {
			// List<Range> regionList = Range.factory(reference, range);
			Section section = new Section(reference, range);
			iterator = new NcArrayIterator(getArray().getRangeIterator(
					section.getRanges()));
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException("out of range");
		}
		return iterator;
	}

	@Override
	public final ISliceIterator getSliceIterator(final int rank)
			throws ShapeNotMatchException, InvalidRangeException {
		return new NcSliceIterator(this, rank);
	}

	@Override
	public boolean equals(final Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj == null || !(obj instanceof IArray)) {
			return false;
		}
		IArray array1 = (IArray) obj;
		if (getRank() != array1.getRank()) {
			return false;
		}
		if (getElementType() != array1.getElementType()) {
			return false;
		}
		if (getSize() != array1.getSize()) {
			return false;
		}
		int[] currentShape = getShape();
		int[] newShape = array1.getShape();
		for (int i = 0; i < newShape.length; i++) {
			if (currentShape[i] != newShape[i]) {
				return false;
			}
		}
		IArrayIterator currentIterator = getIterator();
		IArrayIterator newIterator = array1.getIterator();
		while (currentIterator.hasNext()) {
			if (currentIterator.getObjectNext().equals(
					newIterator.getObjectNext())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public void setIndex(IIndex index) {
		try {
			// [ANSTO][Tony][2011-08-29] Downcast long array to int array
			long[] longStride = index.getStride();
			int[] stride = new int[longStride.length];
			for (int i = 0; i < stride.length; i++) {
				stride[i] = (int) longStride[i];
			}
			array = array.section(index.getOrigin(), index.getShape(), stride);
		} catch (ucar.ma2.InvalidRangeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

}
