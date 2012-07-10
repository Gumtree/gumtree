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
package org.gumtree.vis.gdm.dataset;

import java.io.Serializable;

import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.jfree.data.general.Series;

/**
 * @author nxi
 *
 */
public class ArraySeries extends Series implements Cloneable, Serializable, IXYErrorSeries {

	private IArray xArray;
	private IArray yArray;
	private IArray eArray;
	private boolean isProtected;
	private boolean isXAvailable = false;
	private double maxX;
	private double maxY;
	private double minX;
	private double minY;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2279297882124402437L;

	/**
	 * @param key
	 */
	public ArraySeries(Comparable key) {
		super(key);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param key
	 * @param description
	 */
	public ArraySeries(Comparable key, String description) {
		super(key, description);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#setData(org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, boolean)
	 */
	public void setData(IArray x, IArray y, IArray error, boolean isProtected)
	throws ShapeNotMatchException {
		if (y == null) {
			throw new ShapeNotMatchException("y can not be null");
		}
		if (x == null) {
			isXAvailable = false;
			yArray = y;
			eArray = error;
		} else {
			isXAvailable = true;
			if (y.getSize() > x.getSize()) {
				throw new ShapeNotMatchException("X axis too small");
			}
			if (error != null && error.getSize() < y.getSize()) {
				throw new ShapeNotMatchException("variance length too small");
			}
			IIndex index = x.getIndex();
			if (x.getDouble(index.set(0)) 
					> x.getDouble(index.set((int) x.getSize() - 1))) {
				xArray = x.getArrayUtils().flip(0).getArray();
				yArray = y.getArrayUtils().flip(0).getArray();
				if (eArray != null) {
					eArray = error.getArrayUtils().flip(0).getArray();
				}
			} else {
				xArray = x;
				yArray = y;
				eArray = error;
			}
		}
		this.isProtected = isProtected;
		if (isProtected) {
			findXRange();
			findYRange();
		}
	}
	
	private void findYRange() {
		maxY = Double.MIN_VALUE;
		minY = Double.MAX_VALUE;
		IArrayIterator iterator = yArray.getIterator();
		while (iterator.hasNext()) {
			double value = iterator.getDoubleNext();
			if (value > maxY) {
				maxY = value;
			}
			if (value < minY) {
				minY = value;
			}
		}
	}

	private void findXRange() {
		if (isXAvailable) {
			maxX = Double.MIN_VALUE;
			minX = Double.MAX_VALUE;
			IArrayIterator iterator = xArray.getIterator();
			while (iterator.hasNext()) {
				double value = iterator.getDoubleNext();
				if (value > maxX) {
					maxX = value;
				}
				if (value < minX) {
					minX = value;
				}
			}		
		} else {
			maxX = getItemCount() - 1;
			minX = 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#setData(org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array)
	 */
	public void setData(IArray x, IArray y, IArray error) throws ShapeNotMatchException {
		setData(x, y, error, false);
	}
	
	
	/* (non-Javadoc)
	 * @see org.jfree.data.general.Series#getItemCount()
	 */
	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getItemCount()
	 */
	@Override
	public int getItemCount() {
		return (int) yArray.getSize();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getX(int)
	 */
	public Number getX(int item) {
		return getXValue(item);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getY(int)
	 */
	public Number getY(int item) {
		return getYValue(item);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getMaxX()
	 */
	public double getMaxX(){
		if (isProtected) {
			return maxX;
		} else {
			if (isXAvailable) {
				return xArray.getArrayMath().getMaximum();
			} else {
				return getItemCount() - 1;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getMinX()
	 */
	public double getMinX(){
		if (isProtected) {
			return minX;
		} else {
			if (isXAvailable) {
				return xArray.getArrayMath().getMinimum();
			} else {
				return 0;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getMaxY()
	 */
	public double getMaxY(){
		if (isProtected) {
			return maxY;
		} else {
			return yArray.getArrayMath().getMaximum();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getMinY()
	 */
	public double getMinY(){
		if (isProtected) {
			return minY;
		} else {
			return yArray.getArrayMath().getMinimum();
		}
	}
	
//	public double getVariance(int item) {
//		return eArray.getDouble(eArray.getIndex().set(item));
//	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getItemFromX(double)
	 */
	public int getItemFromX(double x) {
		if (isXAvailable) {
			IIndex index = xArray.getIndex();
			boolean isAscending = xArray.getDouble(index.set(0)) 
			<= xArray.getDouble(index.set((int) xArray.getSize() - 1));
			if (isAscending) {
				if (x <= xArray.getDouble(index.set(0))) {
					return 0;
				}
				if (x >= xArray.getDouble(index.set((int) xArray.getSize() - 1))) {
					return (int) xArray.getSize() - 1;
				}
			} else {
				if (x >= xArray.getDouble(index.set(0))) {
					return 0;
				}
				if (x <= xArray.getDouble(index.set((int) xArray.getSize() - 1))) {
					return (int) xArray.getSize() - 1;
				}
			}
			return findItem(x, xArray, 0, (int) xArray.getSize() - 1, isAscending);
		} else {
			return (int) x;
		}
	}

	private static int findItem(double value, IArray array, int begin, int end, boolean isAscending) {
		if (begin >= end) {
			return begin;
		}
		IIndex index = array.getIndex();
		if (isAscending) {
			double beginValue = array.getDouble(index.set(0));
			double endValue = array.getDouble(index.set(end));
			double percentage = (value - beginValue) / (endValue - beginValue);
			int estimate = begin + (int) Math.floor(percentage * (end - begin));
//			if (estimate > end) {
//				return end;
//			}
//			if (estimate < begin) {
//				return begin;
//			}
			double newValue = array.getDouble(index.set(estimate));
			if (value == newValue) {
				return estimate;
			} else if (value < newValue) {
				if (estimate - 1 <= begin) {
					return findNearer(value, begin, beginValue, estimate, newValue);
				}
				int lastIndex = estimate;
				double lastValue = newValue;
				for (int i = estimate - 1; i >= begin; i--) {
					double currentValue = array.getDouble(index.set(i));
					if (value > currentValue) {
						return findNearer(value, i, currentValue, lastIndex, lastValue);
					}
					lastIndex = i;
					lastValue = currentValue;
				}
				return begin;
			} else {
				if (estimate + 1 >= end) {
					return findNearer(value, end, endValue, estimate, newValue);
				}
				int lastIndex = estimate;
				double lastValue = newValue;
				for (int i = estimate + 1; i <= end; i++) {
					double currentValue = array.getDouble(index.set(i));
					if (value < currentValue) {
						return findNearer(value, i, currentValue, lastIndex, lastValue);
					}
					lastIndex = i;
					lastValue = currentValue;
				}
				return end;
			}
		} else {
			return 0;
		}
	}
	
	private static int findNearer(double value, int index1, double value1, int index2, double value2) {
		return Math.abs(value1 - value) <= Math.abs(value2 - value) ? index1 : index2;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getYError(int)
	 */
	public double getYError(int item) {
		if (eArray == null) {
			return 0;
		}
		double error = eArray.getDouble(eArray.getIndex().set(item));
		if (Double.isNaN(error)) {
			return 0;
		}
		return error;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.dataset.XYPattern#getMinPositiveValue()
	 */
	public double getMinPositiveValue() {
		double min = Double.POSITIVE_INFINITY;
		IArrayIterator iterator = yArray.getIterator();
		while (iterator.hasNext()) {
			double value = iterator.getDoubleNext();
			if (value > 0 && min > value) {
				min = value;
			}
		}
		if (Double.isInfinite(min)) {
			return Double.NaN;
		}
		return min;
	}
	
	public String getKey() {
		Object object = super.getKey();
		if (object instanceof String) {
			return (String) object;
		} else {
			return object.toString();
		}
	}

	@Override
	public double getXValue(int item) {
		if (isXAvailable) {
			return xArray.getDouble(xArray.getIndex().set(item));
		} else {
			return item;
		}
	}

	@Override
	public double getYValue(int item) {
		return yArray.getDouble(yArray.getIndex().set(item));
	}
	
	public IArray getXArray() {
		return xArray;
	}
	
	public IArray getYArray() {
		return yArray;
	}
	
	public IArray getErrorArray() {
		return eArray;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}
