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

import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IRange;

/**
 * Netcdf implementation of GDM Range.
 * 
 * @author nxi Created on 24/03/2009
 */
public class NcRange implements IRange {

	/**
	 * Netcdf Range core.
	 */
	private ucar.ma2.Range range;

	/**
	 * Constructor from a Netcdf Range core.
	 * 
	 * @param netcdfRange
	 *            Netcdf Range instance
	 */
	public NcRange(final ucar.ma2.Range netcdfRange) {
		this.range = netcdfRange;
	}

	/**
	 * Create a range with unit stride.
	 * 
	 * @param first
	 *            first value in range
	 * @param last
	 *            last value in range, inclusive
	 * @throws InvalidRangeException
	 *             elements must be nonnegative, 0 <= first <= last
	 */
	public NcRange(final int first, final int last)
			throws InvalidRangeException {
		try {
			range = new ucar.ma2.Range(null, first, last, 1);
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	/**
	 * Create a range starting at zero, with unit stride.
	 * 
	 * @param length
	 *            number of elements in the Range
	 */
	public NcRange(final int length) {
		range = new ucar.ma2.Range(length);
	}

	/**
	 * Create a named range with unit stride.
	 * 
	 * @param name
	 *            name of Range
	 * @param first
	 *            first value in range
	 * @param last
	 *            last value in range, inclusive
	 * @throws InvalidRangeException
	 *             elements must be nonnegative, 0 <= first <= last
	 */
	public NcRange(final String name, final int first, final int last)
			throws InvalidRangeException {
		try {
			range = new ucar.ma2.Range(name, first, last, 1);
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	/**
	 * Create a range with a specified stride.
	 * 
	 * @param first
	 *            first value in range
	 * @param last
	 *            last value in range, inclusive
	 * @param stride
	 *            stride between consecutive elements, must be > 0
	 * @throws InvalidRangeException
	 *             elements must be nonnegative: 0 <= first <= last, stride > 0
	 */
	public NcRange(final int first, final int last, final int stride)
			throws InvalidRangeException {
		try {
			range = new ucar.ma2.Range(null, first, last, stride);
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	/**
	 * Create a named range with a specified stride.
	 * 
	 * @param name
	 *            name of Range
	 * @param first
	 *            first value in range
	 * @param last
	 *            last value in range, inclusive
	 * @param stride
	 *            stride between consecutive elements, must be > 0
	 * @throws InvalidRangeException
	 *             elements must be nonnegative: 0 <= first <= last, stride > 0
	 */
	public NcRange(final String name, final int first, final int last,
			final int stride) throws InvalidRangeException {
		try {
			range = new ucar.ma2.Range(name, first, last, stride);
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	/**
	 * Copy Constructor.
	 * 
	 * @param r
	 *            copy from here
	 */
	public NcRange(final IRange r) {
		range = new ucar.ma2.Range(((NcRange) r).getNetcdfRange());
	}

	/**
	 * Copy Constructor with name.
	 * 
	 * @param name
	 *            result name
	 * @param r
	 *            copy from here
	 */
	public NcRange(final String name, final IRange r) {
		range = new ucar.ma2.Range(name, ((NcRange) r).getNetcdfRange());
	}

	/**
	 * Get the Netcdf Range core.
	 * 
	 * @return Netcdf Range
	 */
	public ucar.ma2.Range getNetcdfRange() {
		return range;
	}

	@Override
	public IRange compact() throws InvalidRangeException {
		try {
			return new NcRange(range.compact());
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	@Override
	public IRange compose(final IRange r) throws InvalidRangeException {
		try {
			return new NcRange(range.compose(((NcRange) r).getNetcdfRange()));
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	@Override
	public boolean contains(final int i) {
		return range.contains(i);
	}

	@Override
	public int element(long i) throws InvalidRangeException {
		try {
			// [ANSTO][Tony][2011-08-29] Range.element(i) supports int only.
			return range.element((int) i);
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	@Override
	public long first() {
		return range.first();
	}

	@Override
	public int getFirstInInterval(final int start) {
		return range.getFirstInInterval(start);
	}

	@Override
	public String getName() {
		return range.getName();
	}

	@Override
	public long index(final int elem) throws InvalidRangeException {
		try {
			return range.index(elem);
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	@Override
	public IRange intersect(final IRange r) throws InvalidRangeException {
		try {
			return new NcRange(range.intersect(((NcRange) r).getNetcdfRange()));
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	@Override
	public boolean intersects(final IRange r) {
		return range.intersects(((NcRange) r).getNetcdfRange());
	}

	@Override
	public long last() {
		return range.last();
	}

	@Override
	public int length() {
		return range.length();
	}

	// public int max() {
	// return range.max();
	// }
	//
	// public int min() {
	// return range.min();
	// }

	@Override
	public IRange shiftOrigin(final int origin) throws InvalidRangeException {
		try {
			return new NcRange(range.shiftOrigin(origin));
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	@Override
	public long stride() {
		return range.stride();
	}

	@Override
	public IRange union(final IRange r) throws InvalidRangeException {
		try {
			return new NcRange(range.union(((NcRange) r).getNetcdfRange()));
		} catch (ucar.ma2.InvalidRangeException e) {
			throw new InvalidRangeException(e);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof NcRange)) {
			return false;
		}
		return range.equals(((NcRange) o).getNetcdfRange());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

}
