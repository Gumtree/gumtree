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
package org.gumtree.vis.interfaces;

import org.jfree.data.xy.XYZDataset;

/**
 * @author nxi
 *
 */
public interface IXYZDataset extends XYZDataset, IDataset {
	public double getZMin();
	
	public double getZMax();

	public double getXBlockSize();

	public double getYBlockSize();

	public void update();

	public String getZUnits();

	/**
	 * @param zUnits the zUnits to set
	 */
	public void setZUnits(String zUnits);

	public String getZTitle();

	/**
	 * @param zTitle the zTitle to set
	 */
	public void setZTitle(String zTitle);

	public double getXMax();
	
	public double getYMin();
	
	public double getYMax();
	
	public double getXMin();
	
	public double getZofXY(int xIndex, int yIndex);
}
