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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * @author nxi
 *
 */
public class RectangleMask extends Abstract2DMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Rectangle2D rectangle = new Rectangle2D.Double();
	
	public RectangleMask(boolean isInclusive, String name) {
		super(isInclusive, name);
	}
	
	public RectangleMask(boolean isInclusive) {
		super(isInclusive);
	}

	public RectangleMask(boolean isInclusive, double x, double y, 
			double width, double height) {
		this(isInclusive);
		rectangle.setFrame(x, y, width, height);
	}

	@Override
	public Rectangle2D getRectangleFrame() {
		return rectangle;
	}
	
	@Override
	public Shape getShape() {
		return rectangle;
	}
	
	@Override
	public void setShape(Shape shape) {
		rectangle = (Rectangle2D) shape;
	}
	
	@Override
	public void setRectangleFrame(Rectangle2D rectangle) {
		this.rectangle.setRect(rectangle);
	}
	
	@Override
	public Abstract2DMask clone() {
		Abstract2DMask mask = new RectangleMask(isInclusive(), getName());
		mask.setRectangleFrame(getRectangleFrame());
		mask.setName(getName());
		return mask;
	}
	
	@Override
	public Point2D getTitleLocation(Rectangle2D availableArea) {
		if (rectangle == null) {
			return null;
		}
//		Rectangle2D innerArea = rectangle.createIntersection(availableArea);
//		return new Point2D.Double(innerArea.getMinX() + 10, innerArea.getMinY() + 15);
		return new Point2D.Double(rectangle.getMinX() + 10, rectangle.getMinY() + 15);
	}
}
