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

import org.jfree.data.xy.XYDataset;

/**
 * @author nxi
 *
 */
public interface IDataset extends XYDataset {

	/**
	 * @return the xTitle
	 */
	public abstract String getXTitle();

	/**
	 * @param xTitle the xTitle to set
	 */
	public abstract void setXTitle(String xTitle);

	/**
	 * @return the yTitle
	 */
	public abstract String getYTitle();

	/**
	 * @param yTitle the yTitle to set
	 */
	public abstract void setYTitle(String yTitle);

	/**
	 * @return the xUnits
	 */
	public abstract String getXUnits();

	/**
	 * @param xUnits the xUnits to set
	 */
	public abstract void setXUnits(String xUnits);

	/**
	 * @return the yUnits
	 */
	public abstract String getYUnits();

	/**
	 * @param yUnits the yUnits to set
	 */
	public abstract void setYUnits(String yUnits);

	/**
	 * @return the title
	 */
	public abstract String getTitle();

	/**
	 * @param title the title to set
	 */
	public abstract void setTitle(String title);
}
