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
package org.gumtree.vis.gdm.dataset;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.vis.interfaces.IXYZDataset;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 * @author nxi
 *
 */
public class Hist2DDataset implements IXYZDataset {

	private List<DatasetChangeListener> datasetListeners = new ArrayList<DatasetChangeListener>();
	private IArray zArray;
	private IArray xArray;
	private IArray yArray;
	private IIndex zIndex;
	private IIndex xIndex;
	private IIndex yIndex;
	private int sizex;
	private int sizey;
	private boolean isXAvailable;
	private boolean isYAvailable;
	private String xTitle;
	private String yTitle;
	private String zTitle;
	private String xUnits;
	private String yUnits;
	private String zUnits;
	private String title;
	private double xMin;
	private double xMax;
	private double xBlockSize = 1;
	private double yMin;
	private double yMax;
	private double yBlockSize = 1;
	private double zMax;
	private double zMin;
	private DomainOrder domainOrder = DomainOrder.ASCENDING;
	private DomainOrder rangeOrder = DomainOrder.ASCENDING;
	
	public void setData(IArray x, IArray y, IArray z) throws ShapeNotMatchException {
		if (z == null) {
			return;
		}
		if (z.getRank() != 2) {
			throw new ShapeNotMatchException("not a 2D array");
		}
		xArray = x;
		yArray = y;
		zArray = z;
		sizex = z.getShape()[1];
		sizey = z.getShape()[0];
		if (x != null) {
			if (x.getSize() < sizex) {
				throw new ShapeNotMatchException("X axis too small");
			}
			xIndex = x.getIndex();
			if (x.getDouble(xIndex.set(0)) > x.getDouble(xIndex.set(
					(int) x.getSize() - 1))) {
				domainOrder = DomainOrder.DESCENDING;
			} else {
				domainOrder = DomainOrder.ASCENDING;
			} 
			if (x.getSize() == sizex) {
				xMin = x.getArrayMath().getMinimum();
				xMax = x.getArrayMath().getMaximum();
				if (sizex > 1) {
					xBlockSize = (xMax - xMin) / (sizex - 1);
				} else {
					xBlockSize = xMax - xMin;
				}
			} 
			else if (x.getSize() == sizex + 1) {
				double max = x.getArrayMath().getMaximum();
				double min = x.getArrayMath().getMinimum();
				xBlockSize = (max - min) / sizex;
				xMin = min + xBlockSize / 2;
				xMax = max - xBlockSize / 2;
			} else {
				if (domainOrder == DomainOrder.ASCENDING) {
					xMin = x.getDouble(xIndex.set(0));
					xMax = x.getDouble(xIndex.set(sizex));
					xBlockSize = (xMax - xMin) / sizex;
				} else {
					xMin = x.getDouble(xIndex.set(sizex));
					xMax = x.getDouble(xIndex.set(0));
					xBlockSize = (xMax - xMin) / sizex;
				}
			}
			isXAvailable = true;
		} else {
			isXAvailable = false;
		}
		
		if (y != null) {
			if (y.getSize() < sizey) {
				throw new ShapeNotMatchException("Y axis too small");
			}
			yIndex = y.getIndex();
			if (y.getDouble(yIndex.set(0)) > y.getDouble(yIndex.set(
					(int) y.getSize() - 1))) {
				rangeOrder = DomainOrder.DESCENDING;
			} else {
				rangeOrder = DomainOrder.ASCENDING;
			} 
			if (y.getSize() == sizey) {
				yMin = y.getArrayMath().getMinimum();
				yMax = y.getArrayMath().getMaximum();
				if (sizey > 1) {
					yBlockSize = (yMax - yMin) / (sizey - 1);
				} else {
					yBlockSize = yMax - yMin;
				}
			} 
			else if (y.getSize() == sizey + 1) {
				double max = y.getArrayMath().getMaximum();
				double min = y.getArrayMath().getMinimum();
				yBlockSize = (max - min) / sizey;
				yMin = min + yBlockSize / 2;
				yMax = max - yBlockSize / 2;
			} else {
				if (rangeOrder == DomainOrder.ASCENDING) {
					yMin = y.getDouble(yIndex.set(0));
					yMax = y.getDouble(yIndex.set(sizey));
					yBlockSize = (yMax - yMin) / sizey;
				} else {
					yMin = y.getDouble(yIndex.set(sizey));
					yMax = y.getDouble(yIndex.set(0));
					yBlockSize = (yMax - yMin) / sizey;
				}
			}
			isYAvailable = true;
		} else {
			isYAvailable = false;
		}

//		if (y != null) {
//			if (y.getSize() < sizey) {
//				throw new ShapeNotMatchException("Y axis too small");
//			}
//			yIndex = y.getIndex();
//			yMin = y.getArrayMath().getMinimum();
//			yMax = y.getArrayMath().getMaximum();
//			isYAvailable = true;
//			if (y.getDouble(yIndex.set(0)) > y.getDouble(yIndex.set(
//					(int) y.getSize() - 1))) {
//				rangeOrder = DomainOrder.DESCENDING;
//			} else {
//				rangeOrder = DomainOrder.ASCENDING;
//			}
//		} else {
//			isYAvailable = false;
//		}
		if (z != null) {
			zIndex = z.getIndex();
			zMin = z.getArrayMath().getMinimum();
			zMax = z.getArrayMath().getMaximum();
		}
		notifyDatasetChanged(this);
	}
	
	public void setTitles(String xTitle, String yTitle, String zTitle) {
		this.xTitle = xTitle;
		this.yTitle = yTitle;
		this.zTitle = zTitle;
	}
	
	public void setUnits(String xUnits, String yUnits, String zUnits) {
		this.xUnits = xUnits;
		this.yUnits = yUnits;
		this.zUnits = zUnits;
	}
	
	public int getXIndex(int item) {
		if (domainOrder == DomainOrder.ASCENDING) {
			return item / sizey;
		} else {
			return sizex - 1 - item / sizey;
		}

	}
	
	public int getYIndex(int item) {
		if (rangeOrder == DomainOrder.ASCENDING) {
			return item - item / sizey * sizey;
		} else {
			return sizey - 1 - item + item / sizey * sizey;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYZDataset#getZ(int, int)
	 */
	@Override
	public Number getZ(int series, int item) {
		return new Double(getZValue(series, item));
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYZDataset#getZValue(int, int)
	 */
	@Override
	public double getZValue(int series, int item) {
		zIndex.set(getYIndex(item), getXIndex(item));
		return zArray.getDouble(zIndex);
	}

	@Override
	public double getZofXY(int xIndex, int yIndex) {
		zIndex.set(yIndex, xIndex);
		return zArray.getDouble(zIndex);
	}
	
	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getDomainOrder()
	 */
	@Override
	public DomainOrder getDomainOrder() {
		return domainOrder;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	@Override
	public int getItemCount(int series) {
//		return (int) zArray.getSize();
		return sizex * sizey;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	@Override
	public Number getX(int series, int item) {
//		int xItem = getXIndex(item);
//		if (isXAvailable) {
//			try{
//				double value = new Double(xArray.getDouble(xIndex.set(xItem)));
//				return value;
//			}catch (Exception e) {
//				System.out.println(item + " " + xItem);
//				e.printStackTrace();
//			}
//		}
//		return xItem;
		return new Double(getXValue(series, item)); 
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getXValue(int, int)
	 */
	@Override
	public double getXValue(int series, int item) {
		int xItem = getXIndex(item);
		if (isXAvailable) {
//			return xArray.getDouble(xIndex.set(xItem));
			return xMin + getXBlockSize() * xItem;
		}
		return (double) xItem;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	@Override
	public Number getY(int series, int item) {
		return new Double(getYValue(series, item));
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getYValue(int, int)
	 */
	@Override
	public double getYValue(int series, int item) {
		int yItem = getYIndex(item);
		if (isYAvailable) {
//			return yArray.getDouble(yIndex.set(yItem));
			return yMin + getYBlockSize() * yItem;
		}
		return (double) yItem; 
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#getSeriesCount()
	 */
	@Override
	public int getSeriesCount() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#getSeriesKey(int)
	 */
	@Override
	public Comparable getSeriesKey(int series) {
		if (title != null) {
			return title;
		}
		if (zTitle != null) {
			return zTitle;
		}
		return zArray.getRegisterId();
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#indexOf(java.lang.Comparable)
	 */
	@Override
	public int indexOf(Comparable seriesKey) {
		return 0;
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
		// TODO Auto-generated method stub
		datasetListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#setGroup(org.jfree.data.general.DatasetGroup)
	 */
	@Override
	public void setGroup(DatasetGroup group) {
		// TODO Auto-generated method stub

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
	 * @return the zTitle
	 */
	public String getZTitle() {
		return zTitle;
	}

	/**
	 * @param zTitle the zTitle to set
	 */
	public void setZTitle(String zTitle) {
		this.zTitle = zTitle;
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
	 * @return the zUnits
	 */
	public String getZUnits() {
		return zUnits;
	}

	/**
	 * @param zUnits the zUnits to set
	 */
	public void setZUnits(String zUnits) {
		this.zUnits = zUnits;
	}

	public double getXMin(){
		if (isXAvailable) {
//			return xArray.getMinimum();
			return xMin;
		}
		return 0;
	}
	
	public double getXMax(){
		if (isXAvailable) {
//			return xArray.getMaximum();
			return xMax;
		}
		return sizex;
	}
	
	public double getYMin(){
		if (isYAvailable) {
//			return yArray.getMinimum();
			return yMin;
		}
		return 0;
	}
	
	public double getYMax(){
		if (isYAvailable) {
//			return yArray.getMaximum();
			return yMax;
		}
		return sizey;
	}

	public double getZMin(){
//		return zArray.getMinimum();
		return zMin;
	}
	
	public double getZMax(){
//		return zArray.getMaximum();
		return zMax;
	}
	
	public double getXBlockSize(){
		if (sizex > 1) {
//			return Math.abs(xArray.getDouble(xIndex.set(1)) - xArray.getDouble(xIndex.set(0)));
			if (isXAvailable) {
//				return Math.abs(xArray.getDouble(xIndex.set(sizex - 1)) 
//						- xArray.getDouble(xIndex.set(0))) / sizex;
				return xBlockSize;
			} else {
				return 1;
			}
		}
		return 1;
	}
	
	public double getYBlockSize(){
		return yBlockSize;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		if (title != null) {
			return title;
		}
		if (zTitle != null) {
			return zTitle;
		}
		return "";
	}
	
	public void update() {
		sizex = zArray.getShape()[1];
		sizey = zArray.getShape()[0];
		if (isXAvailable) {
			IArray x = xArray;
//			if (x.getSize() < sizex) {
//				throw new ShapeNotMatchException("X axis too small");
//			}
			xIndex = x.getIndex();
			if (x.getDouble(xIndex.set(0)) > x.getDouble(xIndex.set(
					(int) x.getSize() - 1))) {
				domainOrder = DomainOrder.DESCENDING;
			} else {
				domainOrder = DomainOrder.ASCENDING;
			} 
			if (x.getSize() == sizex) {
				xMin = x.getArrayMath().getMinimum();
				xMax = x.getArrayMath().getMaximum();
				if (sizex > 1) {
					xBlockSize = (xMax - xMin) / (sizex - 1);
				} else {
					xBlockSize = xMax - xMin;
				}
			} 
			else if (x.getSize() == sizex + 1) {
				double max = x.getArrayMath().getMaximum();
				double min = x.getArrayMath().getMinimum();
				xBlockSize = (max - min) / sizex;
				xMin = min + xBlockSize / 2;
				xMax = max - xBlockSize / 2;
			} else {
				if (domainOrder == DomainOrder.ASCENDING) {
					xMin = x.getDouble(xIndex.set(0));
					xMax = x.getDouble(xIndex.set(sizex));
					xBlockSize = (xMax - xMin) / sizex;
				} else {
					xMin = x.getDouble(xIndex.set(sizex));
					xMax = x.getDouble(xIndex.set(0));
					xBlockSize = (xMax - xMin) / sizex;
				}
			}
		} else {
			xMin = 0;
			xMax = sizex - 1;
			xBlockSize = 1;
		}
		
		if (isYAvailable) {
			IArray y = yArray;
//			if (y.getSize() < sizey) {
//				throw new ShapeNotMatchException("Y axis too small");
//			}
			yIndex = y.getIndex();
			if (y.getDouble(yIndex.set(0)) > y.getDouble(yIndex.set(
					(int) y.getSize() - 1))) {
				rangeOrder = DomainOrder.DESCENDING;
			} else {
				rangeOrder = DomainOrder.ASCENDING;
			} 
			if (y.getSize() == sizey) {
				yMin = y.getArrayMath().getMinimum();
				yMax = y.getArrayMath().getMaximum();
				if (sizey > 1) {
					yBlockSize = (yMax - yMin) / (sizey - 1);
				} else {
					yBlockSize = yMax - yMin;
				}
			} 
			else if (y.getSize() == sizey + 1) {
				double max = y.getArrayMath().getMaximum();
				double min = y.getArrayMath().getMinimum();
				yBlockSize = (max - min) / sizey;
				yMin = min + yBlockSize / 2;
				yMax = max - yBlockSize / 2;
			} else {
				if (rangeOrder == DomainOrder.ASCENDING) {
					yMin = y.getDouble(yIndex.set(0));
					yMax = y.getDouble(yIndex.set(sizey));
					yBlockSize = (yMax - yMin) / sizey;
				} else {
					yMin = y.getDouble(yIndex.set(sizey));
					yMax = y.getDouble(yIndex.set(0));
					yBlockSize = (yMax - yMin) / sizey;
				}
			}
		} else {
			yMin = 0;
			yMax = sizey - 1;
			yBlockSize = 1;
		}

		if (zArray != null) {
			zIndex = zArray.getIndex();
			zMin = zArray.getArrayMath().getMinimum();
			zMax = zArray.getArrayMath().getMaximum();
		}
		notifyDatasetChanged(this);
	}
	
	protected void notifyDatasetChanged(Object object) {
		if (datasetListeners.size() > 0) {
			DatasetChangeEvent event = new DatasetChangeEvent(object, this);
			for (DatasetChangeListener listener : datasetListeners) {
				listener.datasetChanged(event);
			}
		}
	}
	
	public IArray getXArray() {
		return xArray;
	}
	
	public IArray getYArray() {
		return yArray;
	}
	
	public IArray getZArray() {
		return zArray;
	}
	
	@Override
	public int getXSize(int series) {
		return sizex;
	}
	
	@Override
	public int getYSize(int series) {
		return sizey;
	}
}
