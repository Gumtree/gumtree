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

import org.jfree.data.xy.IntervalXYDataset;

/**
 * @author nxi
 *
 */
public interface IXYErrorDataset extends IntervalXYDataset, IDataset {

	public abstract double getMinPositiveValue();
	
	public abstract double getMinPositiveValue(int series);
	
	public abstract int indexOf(IXYErrorSeries series);
	
	public abstract List<IXYErrorSeries> getSeries();

	public double getYError(int series, int item);
	
	public void addSeries(IXYErrorSeries series);
	
	public void removeSeries(IXYErrorSeries series);
	
	public void addListOfSeries(List<IXYErrorSeries> listOfSeries);
	
	public void removeAllSeries();
	
	public int getItemFromX(int series, double x);

	public void update(IXYErrorSeries series);

}
