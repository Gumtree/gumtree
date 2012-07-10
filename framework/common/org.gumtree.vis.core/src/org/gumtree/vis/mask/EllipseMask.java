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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author nxi
 *
 */
public class EllipseMask extends Abstract2DMask {

	private Ellipse2D ellipse = new Ellipse2D.Double();
	
	public EllipseMask(boolean isInclusive) {
		super(isInclusive);
	}

	public EllipseMask(boolean isInclusive, String name) {
		super(isInclusive, name);
	}
	
	public EllipseMask(boolean isInclusive, double x, double y, 
			double width, double height) {
		this(isInclusive);
		ellipse.setFrame(x, y, width, height);
	}
	
	@Override
	public Shape getShape() {
		return ellipse;
	}
	
	@Override
	public Rectangle2D getRectangleFrame() {
		return ellipse.getFrame();
	}
	
	@Override
	public void setShape(Shape shape) {
		this.ellipse = (Ellipse2D) shape;
	}
	
	@Override
	public Abstract2DMask clone() {
		Abstract2DMask mask = new EllipseMask(isInclusive(), getName());
		mask.setRectangleFrame(getRectangleFrame());
		return mask;
	}
	
	@Override
	public void setRectangleFrame(Rectangle2D rectangle) {
		ellipse.setFrame(rectangle);
	}
	
	@Override
	public Point2D getTitleLocation(Rectangle2D availableArea) {
		int size = getName().length();
    	Rectangle2D frame = getRectangleFrame();
//    	Point2D imageTopLeft = new Point2D.Double(availableArea.getMinX(), 
//    			availableArea.getMinY());
//    	if (frame.contains(imageTopLeft)) {
//    		if (ellipse.contains(imageTopLeft)) {
//    			return new Point2D.Double(availableArea.getMinX() + 10, availableArea.getMinY() + 15); 
//    		} else {
//    			return new Point2D.Double(frame.getCenterX() - size * 3, frame.getMinY() + 15);
//    		}
//    	} else {
//    		return new Point2D.Double(frame.getCenterX() - size * 3, frame.getMinY() + 15);
//    	}
		return new Point2D.Double(frame.getCenterX() - size * 3, frame.getMinY() + 15);
	}
}
