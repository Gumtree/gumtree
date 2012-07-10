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
package org.gumtree.vis.dataset;

import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.jfree.data.xy.YIntervalSeries;

/**
 * @author nxi
 *
 */
public class XYErrorSeries extends YIntervalSeries implements IXYErrorSeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4538184846713699678L;

	private double xMin = Double.NaN, xMax = Double.NaN;
	private double yMin = Double.NaN, yMax = Double.NaN;
	private double minPositiveValue = Double.NaN;
	/**
	 * @param key
	 */
	public XYErrorSeries(Comparable key) {
		super(key);
	}

	/**
	 * @param key
	 * @param autoSort
	 * @param allowDuplicateXValues
	 */
//	public XYSigmaSeries(Comparable key, boolean autoSort,
//			boolean allowDuplicateXValues) {
//		super(key, autoSort, allowDuplicateXValues);
//	}

	@Override
	public void add(double x, double y, double yLow, double yHigh) {
		super.add(x, y, yLow, yHigh);
		if (xMin > x || Double.isNaN(xMin)) {
			xMin = x;
		}
		if (xMax < x || Double.isNaN(x)) {
			xMax = x;
		}
		if (yMin > yLow || Double.isNaN(yMin)) {
			yMin = yLow;
		}
		if (yMax < yHigh || Double.isNaN(yMax)) {
			yMax = yHigh;
		}
		if (y > 0 && (y < minPositiveValue || Double.isNaN(minPositiveValue))) {
			minPositiveValue = y;
		}
	}
	
	public void add(double x, double y, double error) {
		add(x, y, y - Math.abs(error), y + Math.abs(error));
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IXYErrorSeries#getItemFromX(double)
	 */
	@Override
	public int getItemFromX(double x) {
		if (x <= getXValue(0)) {
			return 0;
		}
		if (x >= getXValue(getItemCount() - 1)) {
			return getItemCount() - 1;
		}
		
		return getItemFromX(x, 0, getItemCount() - 1);
	}

	private int getItemFromX(double x, int startIndex, int endIndex) {
		double startX = getXValue(startIndex);
		double endX = getXValue(endIndex);
		if (startIndex >= endIndex - 1) {
			return Math.abs(startX - x) < Math.abs(endX - x) ? startIndex : endIndex;
		}
		double percent = (x - startX) / (endX - startX);
		int estimatedIndex = (int) (startIndex + (endIndex - startIndex) * percent);
		double estimated = getXValue(estimatedIndex);
		if (x == estimated) {
			return estimatedIndex;
		} else if (x > estimated) {
			double nextValue;
			for (int i = estimatedIndex; i < endIndex; i++) {
				nextValue = getXValue(i + 1);
				if (x < nextValue) {
					return Math.abs(estimated - x) < Math.abs(nextValue - x) ?
							i : i + 1;
				}
				estimated = nextValue;
			}
		} else {
			double nextValue;
			for (int i = estimatedIndex; i > startIndex ; i--) {
				nextValue = getXValue(i - 1);
				if (x > nextValue) {
					return Math.abs(estimated - x) < Math.abs(nextValue - x) ?
							i : i - 1;
				}
				estimated = nextValue;
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IXYErrorSeries#getMaxX()
	 */
	@Override
	public double getMaxX() {
		return xMax;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IXYErrorSeries#getMaxY()
	 */
	@Override
	public double getMaxY() {
		return yMax;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IXYErrorSeries#getMinPositiveValue()
	 */
	@Override
	public double getMinPositiveValue() {
		return minPositiveValue;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IXYErrorSeries#getMinX()
	 */
	@Override
	public double getMinX() {
		return xMin;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IXYErrorSeries#getMinY()
	 */
	@Override
	public double getMinY() {
		return yMin;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IXYErrorSeries#getY(int)
	 */
	@Override
	public Number getY(int item) {
		return getYValue(item);
	}

	public double getXValue(int index) {
		return getX(index).doubleValue();
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IXYErrorSeries#getYError(int)
	 */
	@Override
	public double getYError(int item) {
		return getYValue(item) - getYLowValue(item);
	}

	public String getKey() {
		Object object = super.getKey();
		if (object instanceof String) {
			return (String) object;
		} else {
			return object.toString();
		}
	}
}
