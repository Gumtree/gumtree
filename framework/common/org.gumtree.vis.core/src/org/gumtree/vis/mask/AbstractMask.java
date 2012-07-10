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


/**
 * @author nxi
 *
 */
public abstract class AbstractMask {

	/**
	 * 
	 */
	private static int id = 0;

	private boolean isInclusive;
	private String name;
//	private Color fillColor;
	
	public boolean isInclusive() {
		return isInclusive;
	}
	
	public AbstractMask(boolean isInclusive) {
		this.isInclusive = isInclusive;
		setNextName();
	}

	public AbstractMask(boolean isInclusive, String name) {
		this.isInclusive = isInclusive;
		this.name = name;;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

//	/**
//	 * @return the fillColor
//	 */
//	public Color getFillColor() {
//		return fillColor;
//	}

//	/**
//	 * @param fillColor the fillColor to set
//	 */
//	public void setFillColor(Color fillColor) {
//		this.fillColor = fillColor;
//	}
	
	private static int getNextID() {
		return ++id;
	}
	
	private void setNextName() {
		if (isInclusive) {
			setName("I-" + getNextID()); 
		} else {
			setName("E-" + getNextID());
		}
	}
	
	public abstract Point2D getTitleLocation(Rectangle2D availableArea);
	
	public void setInclusive(boolean isInclusive) {
		this.isInclusive = isInclusive;
	}
	
	public String toString() {
		return getName();
	}
}
