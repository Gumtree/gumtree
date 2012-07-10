/*******************************************************************************
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
import org.gumtree.data.interfaces.IIndex;

/**
 * Implementation of GDM IIndex.
 * 
 * @author nxi
 * 
 */
public class NcIndex implements IIndex {

	/**
	 * Wrapping Netcdf Index object.
	 */
	private ucar.ma2.Index ncIndex = null;
	/**
	 * Names of the dimensions.
	 */
	private String[] names;

	/**
	 * Construct a index with the shape information.
	 * 
	 * @param shape
	 *            array of integer
	 */
	public NcIndex(final int[] shape) {
		ncIndex = ucar.ma2.Index.factory(shape);
		names = new String[shape.length];
	}

	// public NcIndex(int[] _shape, int[] _stride) {
	// index = ucar.ma2.Index.factory(_shape);
	// }

	/**
	 * Wrapper constructor.
	 * 
	 * @param index
	 *            Netcdf Index
	 */
	public NcIndex(final ucar.ma2.Index index) {
		this.ncIndex = index;
	}

	@Override
	public NcIndex set(final int[] index) {
		this.ncIndex.set(index);
		return this;
	}

	@Override
	public NcIndex set0(final int v) {
		this.ncIndex.set0(v);
		return this;
	}

	@Override
	public IIndex set1(final int v) {
		this.ncIndex.set1(v);
		return this;
	}

	@Override
	public IIndex set2(final int v) {
		this.ncIndex.set2(v);
		return this;
	}

	@Override
	public IIndex set3(final int v) {
		this.ncIndex.set3(v);
		return this;
	}

	@Override
	public IIndex set4(final int v) {
		this.ncIndex.set4(v);
		return this;
	}

	@Override
	public IIndex set5(final int v) {
		this.ncIndex.set5(v);
		return this;
	}

	@Override
	public IIndex set6(final int v) {
		this.ncIndex.set6(v);
		return this;
	}

	@Override
	public IIndex set(final int v0) {
		this.ncIndex.set(v0);
		return this;
	}

	@Override
	public IIndex set(final int v0, final int v1) {
		this.ncIndex.set(v0, v1);
		return this;
	}

	@Override
	public IIndex set(final int v0, final int v1, final int v2) {
		this.ncIndex.set(v0, v1, v2);
		return this;
	}

	@Override
	public IIndex set(final int v0, final int v1, final int v2, final int v3) {
		this.ncIndex.set(v0, v1, v2, v3);
		return this;
	}

	@Override
	public IIndex set(final int v0, final int v1, final int v2, final int v3,
			final int v4) {
		this.ncIndex.set(v0, v1, v2, v3, v4);
		return this;
	}

	@Override
	public IIndex set(final int v0, final int v1, final int v2, final int v3,
			final int v4, final int v5) {
		this.ncIndex.set(v0, v1, v2, v3, v4, v5);
		return this;
	}

	@Override
	public IIndex set(final int v0, final int v1, final int v2, final int v3,
			final int v4, final int v5, final int v6) {
		this.ncIndex.set(v0, v1, v2, v3, v4, v5, v6);
		return this;
	}

	@Override
	public long currentElement() {
		return ncIndex.currentElement();
	}

	@Override
	public int[] getCurrentCounter() {
		return ncIndex.getCurrentCounter();
	}

	@Override
	public String getIndexName(final int dim) {
		return names[dim];
	}

	@Override
	public long[] getStride() {
		int[] shape = ncIndex.getShape();
		long[] stride = new long[ncIndex.getRank()];
		long product = 1;
		for (int ii = shape.length - 1; ii >= 0; ii--) {
			final int thisDim = shape[ii];
			if (thisDim < 0)
				throw new NegativeArraySizeException();
			stride[ii] = (int) product;
			product *= thisDim;
		}
		return stride;
	}

	@Override
	public int getRank() {
		return ncIndex.getRank();
	}

	@Override
	public int[] getShape() {
		return ncIndex.getShape();
	}

	@Override
	public long getSize() {
		return ncIndex.getSize();
	}

	@Override
	public void setDim(final int dim, final int value) {
		ncIndex.setDim(dim, value);
	}

	@Override
	public void setIndexName(final int dim, final String indexName) {
		// index.setIndexName(dim, indexName);
		names[dim] = indexName;
	}

	@Override
	public String toStringDebug() {
		return ncIndex.toString();
	}

	/**
	 * Expose the Netcdf core.
	 * 
	 * @return Netcdf Index object.
	 */
	public ucar.ma2.Index getNetcdfIndex() {
		return ncIndex;
	}

	/**
	 * Remove all index with length one.
	 * 
	 * @return the new IIndex
	 */
	@Override
	public IIndex reduce() {
		NcIndex c = this;
		for (int ii = 0; ii < getRank(); ii++)
			if (getShape()[ii] == 1) { // do this on the first one you find
				NcIndex newc = c.reduce(ii);
				return newc.reduce(); // any more to do?
			}
		return c;
	}

	/**
	 * Eliminate the specified dimension.
	 * 
	 * @param dim
	 *            dimension to eliminate: must be of length one, else
	 *            IllegalArgumentException
	 * @return the new Array
	 */
	@Override
	public NcIndex reduce(int dim) throws IllegalArgumentException {
		int rank = getRank();
		if ((dim < 0) || (dim >= rank))
			throw new IllegalArgumentException("illegal reduce dim " + dim);
		if (getShape()[dim] != 1)
			throw new IllegalArgumentException("illegal reduce dim " + dim
					+ " : length != 1");

		NcIndex newindex;
		int[] oldshape = getShape();
		int[] newshape = new int[rank];

		int count = 0;
		for (int ii = 0; ii < rank; ii++) {
			if (ii != dim) {
				newshape[count] = oldshape[count];
				count++;
			}
		}
		newindex = new NcIndex(newshape);
		return newindex;

	}

	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

	// [ANSTO][Tony][2011-05-03] To be implemented
	@Override
	public int[] getOrigin() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long lastElement() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOrigin(int[] origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShape(int[] shape) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStride(long[] stride) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IIndex clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
