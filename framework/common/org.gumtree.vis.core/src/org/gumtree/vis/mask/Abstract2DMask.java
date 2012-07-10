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

import java.awt.Shape;
import java.awt.geom.Rectangle2D;


/**
 * @author nxi
 *
 */
public abstract class Abstract2DMask extends AbstractMask{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3426432794021173993L;

	
	public Abstract2DMask(boolean isInclusive) {
		super(isInclusive);
	}

	public Abstract2DMask(boolean isInclusive, String name) {
		super(isInclusive, name);
	}
	
	public abstract void setShape(Shape shape);
	
	public abstract Shape getShape();
	
	public abstract Rectangle2D getRectangleFrame();
	
	public abstract Abstract2DMask clone();
	
	public abstract void setRectangleFrame(Rectangle2D rectangle);
	
	public void setMinX(double minX) {
		Rectangle2D oldRect = getRectangleFrame();
		double maxX = oldRect.getMaxX();
		double minY = oldRect.getMinY();
		double height = oldRect.getHeight();
		double width = Math.max(minX, maxX) - minX;
		setRectangleFrame(new Rectangle2D.Double(
				minX, minY, width, height));
	}

	public void setMaxX(double maxX) {
		Rectangle2D oldRect = getRectangleFrame();
		double minX = Math.min(oldRect.getMinX(), maxX);
		double minY = oldRect.getMinY();
		double height = oldRect.getHeight();
		double width = maxX - minX;
		setRectangleFrame(new Rectangle2D.Double(
				minX, minY, width, height));
	}

	public void setMinY(double minY) {
		Rectangle2D oldRect = getRectangleFrame();
		double minX = oldRect.getMinX();
		double maxY = oldRect.getMaxY();
		double height = Math.max(minY, maxY) - minY;
		double width = oldRect.getWidth();
		setRectangleFrame(new Rectangle2D.Double(
				minX, minY, width, height));
	}

	public void setMaxY(double maxY) {
		Rectangle2D oldRect = getRectangleFrame();
		double minX = oldRect.getMinX();
		double minY = Math.min(oldRect.getMinY(), maxY);
		double height = maxY - minY;
		double width = oldRect.getWidth();
		setRectangleFrame(new Rectangle2D.Double(
				minX, minY, width, height));
	}

	public double getMinX() {
		return getRectangleFrame().getMinX();
	}
	
	public double getMaxX() {
		return getRectangleFrame().getMaxX();
	}
	
	public double getMinY() {
		return getRectangleFrame().getMinY();
	}
	
	public double getMaxY() {
		return getRectangleFrame().getMaxY();
	}
}
