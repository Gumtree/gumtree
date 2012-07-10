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
package org.gumtree.vis.mask;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.data.Range;

/**
 * @author nxi
 *
 */
public class RangeMask extends AbstractMask {

	private double lower;
	private double upper;
	
	/**
	 * @param isInclusive
	 */
	public RangeMask(boolean isInclusive) {
		super(isInclusive);
	}

	/**
	 * @param isInclusive
	 * @param name
	 */
	public RangeMask(boolean isInclusive, String name) {
		super(isInclusive, name);
	}

	public void setBoundary(double min, double max) {
		this.lower = min;
		this.upper = max;
	}
	
	public double getMin() {
		return lower;
	}
	
	public double getMax() {
		return upper;
	}
	
	public void setMin(double min) {
		this.lower = min;
	}
	
	public void setMax(double max) {
		this.upper = max;
	}
	
	public boolean isEmpty() {
		if (Double.isNaN(lower) || Double.isNaN(upper)) {
			return true;
		}
		return lower >= upper;
	}
	
	public void setRange(Range range) {
		this.lower = range.getLowerBound();
		this.upper = range.getUpperBound();
	}
	
	public Range getRange() {
		return new Range(lower, upper);
	}

	@Override
	public Point2D getTitleLocation(Rectangle2D rectangle) {
		if (rectangle == null) {
			return null;
		}
		return new Point2D.Double(rectangle.getMinX() + 4, rectangle.getMinY() + 15);
	}
}
