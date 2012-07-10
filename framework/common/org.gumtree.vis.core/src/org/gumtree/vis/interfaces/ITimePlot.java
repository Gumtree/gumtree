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

import org.jfree.data.time.TimeSeries;


/**
 * @author nxi
 *
 */
public interface ITimePlot extends IPlot {

	public abstract void addTimeSeriesSet(ITimeSeriesSet timeSeriesSet);
	
	public abstract void removeTimeSeriesSet(ITimeSeriesSet timeSeriesSet);
	
	public void setPaused(boolean isPaused);
	
	public boolean isPaused();
	
//	public void setSelectedDataset(IDataset dataset);
//	public void setSelectedDataset(IDataset dataset, int seriesIndex);
	
	public void setSelectedSeries(String seriesKey);
	
	public void setSelectedSeries(TimeSeries series);
	
	public abstract void addTimeSeries(TimeSeries series);
	
	public abstract void removeTimeSeries(String seriesKey);
	
	public abstract void removeTimeSeries(TimeSeries series);
	
	public abstract void clear();
	
	public abstract void clearSeries(String seriesKey);
	
	
}
