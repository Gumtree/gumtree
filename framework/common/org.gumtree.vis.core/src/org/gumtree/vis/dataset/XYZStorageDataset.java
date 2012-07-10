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

import org.gumtree.vis.interfaces.IXYZDataset;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 * @author nxi
 *
 */
public class XYZStorageDataset implements IXYZDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8531318441253207858L;

	private List<DatasetChangeListener> datasetListeners = new ArrayList<DatasetChangeListener>();
	private double[] zDouble1D;
	private double[][] zDouble2D;
	private int[] zInt1D;
	private int[][] zInt2D;
	private float[] zFloat1D;
	private float[][] zFloat2D;
	private double[] xDouble;
	private int[] xInt;
	private float[] xFloat;
	private double[] yDouble;
	private int[] yInt;
	private float[] yFloat;
	
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
	private double yMin;
	private double yMax;
	private double zMax;
	private double zMin;
	private DomainOrder domainOrder = DomainOrder.ASCENDING;
	
	/**
	 * 
	 */
	public XYZStorageDataset() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getXBlockSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getYBlockSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getZMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getZMin() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getZTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getZUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setZTitle(String zTitle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setZUnits(String zUnits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getXSize(int series) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getYSize(int series) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Number getZ(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getZValue(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DomainOrder getDomainOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getItemCount(int series) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Number getX(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getXValue(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Number getY(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getYValue(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSeriesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Comparable getSeriesKey(int series) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Comparable seriesKey) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DatasetGroup getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGroup(DatasetGroup group) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getYTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getYUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setXTitle(String xTitle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setXUnits(String xUnits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setYTitle(String yTitle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setYUnits(String yUnits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getXMax() {
		// TODO Auto-generated method stub
		return xMax;
	}

	@Override
	public double getYMin() {
		// TODO Auto-generated method stub
		return yMin;
	}

	@Override
	public double getYMax() {
		// TODO Auto-generated method stub
		return yMax;
	}

	@Override
	public double getXMin() {
		// TODO Auto-generated method stub
		return xMin;
	}

	@Override
	public double getZofXY(int xIndex, int yIndex) {
		// TODO Auto-generated method stub
		return zDouble2D[yIndex][xIndex];
	}


}
