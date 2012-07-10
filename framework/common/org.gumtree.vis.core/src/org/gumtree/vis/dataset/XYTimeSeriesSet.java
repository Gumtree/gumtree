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

import java.util.TimeZone;

import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * @author nxi
 *
 */
public class XYTimeSeriesSet extends TimeSeriesCollection implements
		ITimeSeriesSet {

	private String xTitle;
	private String yTitle;
	private String xUnits;
	private String yUnits;
	private String title;

	/**
	 * 
	 */
	public XYTimeSeriesSet() {
	}

	/**
	 * @param zone
	 */
	public XYTimeSeriesSet(TimeZone zone) {
		super(zone);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param series
	 */
	public XYTimeSeriesSet(TimeSeries series) {
		super(series);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param series
	 * @param zone
	 */
	public XYTimeSeriesSet(TimeSeries series, TimeZone zone) {
		super(series, zone);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the xTitle
	 */
	public String getXTitle() {
		return xTitle;
	}

	/**
	 * @param xTitle the xTitle to set
	 */
	public void setXTitle(String xTitle) {
		this.xTitle = xTitle;
	}

	/**
	 * @return the yTitle
	 */
	public String getYTitle() {
		return yTitle;
	}

	/**
	 * @param yTitle the yTitle to set
	 */
	public void setYTitle(String yTitle) {
		this.yTitle = yTitle;
		fireDatasetChanged();
	}

	/**
	 * @return the xUnits
	 */
	public String getXUnits() {
		return xUnits;
	}

	/**
	 * @param xUnits the xUnits to set
	 */
	public void setXUnits(String xUnits) {
		this.xUnits = xUnits;
	}

	/**
	 * @return the yUnits
	 */
	public String getYUnits() {
		return yUnits;
	}

	/**
	 * @param yUnits the yUnits to set
	 */
	public void setYUnits(String yUnits) {
		this.yUnits = yUnits;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
