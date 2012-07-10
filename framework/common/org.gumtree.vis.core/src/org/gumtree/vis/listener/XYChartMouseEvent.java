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
package org.gumtree.vis.listener;

import java.awt.event.MouseEvent;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;

/**
 * @author nxi
 *
 */
public class XYChartMouseEvent extends ChartMouseEvent {


	/**
	 * 
	 */
	private static final long serialVersionUID = 446291075809275729L;

	private double x;
	private double y;
	private int seriesIndex;
	
	public XYChartMouseEvent(JFreeChart chart, MouseEvent trigger,
			ChartEntity entity) {
		super(chart, trigger, entity);
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	public void setXY(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setSeriesIndex(int index) {
		this.seriesIndex = index;
	}
	
	public int getSeriesIndex() {
		return seriesIndex;
	}
}
