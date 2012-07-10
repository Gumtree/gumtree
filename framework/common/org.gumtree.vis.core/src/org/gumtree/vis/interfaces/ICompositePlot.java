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

import java.util.List;

import org.jfree.chart.JFreeChart;

/**
 * @author nxi
 *
 */
public interface ICompositePlot extends IPlot {

	public abstract void setLayout(int rows, int columns);
	
	public void setLayout(int rows, int columns, 
			int horizontalGap, int verticalGap);
	
	public abstract void addPlot(IPlot plot);
	
	public abstract void removePlot(IPlot plot);
	
	public abstract void addPlots(List<IPlot> plots);
	
	public abstract List<IPlot> getPlotList();
	
	public abstract void clear();
	
//	public abstract void pack();
	
	public abstract List<JFreeChart> getChartList(); 
}
