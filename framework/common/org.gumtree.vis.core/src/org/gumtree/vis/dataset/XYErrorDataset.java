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

import java.util.ArrayList;
import java.util.List;

import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 * @author nxi
 *
 */
public class XYErrorDataset implements IXYErrorDataset {

	private List<IXYErrorSeries> seriesList = new ArrayList<IXYErrorSeries>();
	private List<DatasetChangeListener> datasetListeners = new ArrayList<DatasetChangeListener>();
	private String xTitle;
	private String yTitle;
	private String xUnits;
	private String yUnits;
	private String title;

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getDomainOrder()
	 */
	@Override
	public DomainOrder getDomainOrder() {
		// TODO Auto-generated method stub
		return DomainOrder.ASCENDING;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	@Override
	public int getItemCount(int series) {
		return seriesList.get(series).getItemCount();
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	@Override
	public Number getX(int series, int item) {
		return getXValue(series, item);
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getXValue(int, int)
	 */
	@Override
	public double getXValue(int series, int item) {
		return seriesList.get(series).getXValue(item);
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	@Override
	public Number getY(int series, int item) {
		return getYValue(series, item);
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getYValue(int, int)
	 */
	@Override
	public double getYValue(int series, int item) {
		return seriesList.get(series).getYValue(item);
	}

	public double getYError(int series, int item) {
		return seriesList.get(series).getYError(item);
	}
	
	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#getSeriesCount()
	 */
	@Override
	public int getSeriesCount() {
		return seriesList.size();
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#getSeriesKey(int)
	 */
	@Override
	public Comparable getSeriesKey(int series) {
		return seriesList.get(series).getKey();
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#indexOf(java.lang.Comparable)
	 */
	@Override
	public int indexOf(Comparable seriesKey) {
		for (IXYErrorSeries series : seriesList) {
			if (series.getKey().equals(seriesKey)) {
				return seriesList.indexOf(series);
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#addChangeListener(org.jfree.data.general.DatasetChangeListener)
	 */
	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		datasetListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#getGroup()
	 */
	@Override
	public DatasetGroup getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#removeChangeListener(org.jfree.data.general.DatasetChangeListener)
	 */
	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		datasetListeners.remove(listener);
	}

	protected void notifyDatasetChanged(Object object) {
		if (datasetListeners.size() > 0) {
			DatasetChangeEvent event = new DatasetChangeEvent(object, this);
			for (DatasetChangeListener listener : datasetListeners) {
				listener.datasetChanged(event);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#setGroup(org.jfree.data.general.DatasetGroup)
	 */
	@Override
	public void setGroup(DatasetGroup group) {
		// TODO Auto-generated method stub
	}

	public void addSeries(IXYErrorSeries series) {
		seriesList.add(series);
		notifyDatasetChanged(series);
	}
	
	public void removeSeries(IXYErrorSeries series) {
		seriesList.remove(series);
		notifyDatasetChanged(this);
	}
	
	public void addListOfSeries(List<IXYErrorSeries> listOfSeries) {
		seriesList.addAll(listOfSeries);
		notifyDatasetChanged(listOfSeries);
	}
	
	public void removeAllSeries() {
		seriesList.clear();
		notifyDatasetChanged(null);
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
	
	public int getItemFromX(int series, double x) {
		return seriesList.get(series).getItemFromX(x);
	}

	@Override
	public Number getEndX(int series, int item) {
		return getX(series, item);
	}

	@Override
	public double getEndXValue(int series, int item) {
		return getXValue(series, item);
	}

	@Override
	public Number getEndY(int series, int item) {
		return getEndYValue(series, item);
	}

	@Override
	public double getEndYValue(int series, int item) {
		IXYErrorSeries pattern = seriesList.get(series);
		return pattern.getYValue(item) + pattern.getYError(item);
	}

	@Override
	public Number getStartX(int series, int item) {
		return getX(series, item);
	}

	@Override
	public double getStartXValue(int series, int item) {
		return getXValue(series, item);
	}

	@Override
	public Number getStartY(int series, int item) {
		return getStartYValue(series, item);
	}

	@Override
	public double getStartYValue(int series, int item) {
		IXYErrorSeries pattern = seriesList.get(series);
		return pattern.getYValue(item) - pattern.getYError(item);
	}
	
	public double getMinPositiveValue() {
		double min = Double.POSITIVE_INFINITY;
		for (IXYErrorSeries series : seriesList) {
			double seriesMin = series.getMinPositiveValue();
			if (!Double.isNaN(seriesMin) && min > seriesMin) {
				min = seriesMin;
			}
		}
		if (Double.isInfinite(min)) {
			return Double.NaN;
		}
		return min;
	}
	
	public double getMinPositiveValue(int series) {
		return seriesList.get(series).getMinPositiveValue();
	}
	
	public int indexOf(IXYErrorSeries series) {
		return seriesList.indexOf(series);
	}
	
	public List<IXYErrorSeries> getSeries() {
		List<IXYErrorSeries> patterns = new ArrayList<IXYErrorSeries>();
		patterns.addAll(seriesList);
		return patterns;
	}
	
	public void update(IXYErrorSeries series) {
		notifyDatasetChanged(series);
	}
}
