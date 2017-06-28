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
package org.gumtree.vis.awt.time;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.gumtree.vis.awt.JChartPanel;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.dataset.DatasetUtils;
import org.gumtree.vis.dataset.DatasetUtils.ExportFormat;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.interfaces.ITimePlot;
import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.gumtree.vis.listener.XYChartMouseEvent;
import org.gumtree.vis.mask.ChartMaskingUtilities;
import org.gumtree.vis.plot1d.LogarithmizableAxis;
import org.gumtree.vis.plot1d.Plot1DChartEditor;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * @author nxi
 *
 */
public class TimePlotPanel extends JChartPanel implements ITimePlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6787029085999173665L;
	private static final String TIME_PLOT_WINDOW_SIZE = "timePlot.defaultWindowSize";
	private static final String TIME_PLOT_SHOW_MULTIAXES = "timePlot.showMultiAxes";
	private static final Color axisTraceColor = Color.darkGray;
	private static final Color AXIS_FOCUS_COLOR = Color.black;
	private static final Color AXIS_SHALLOW_COLOR = Color.lightGray;
	private static final Stroke DEFAULT_STROCK = new BasicStroke(1);
	private static final Stroke BOLD_STROCK = new BasicStroke(3f);
	private static final Stroke SHALLOW_STROCK = new BasicStroke(0.2f);
	private static final int seriesSelectionEventMask = InputEvent.CTRL_MASK; 
	private int selectedSeriesIndex = -1;
	private IDataset selectedDataset = null;
	private boolean isPaused = false;
	private boolean showMultiaxes = true;
	private static final String LEGEND_NONE_COMMAND = "legendNone";
	private static final String LEGEND_BOTTOM_COMMAND = "legendBottom";
	private static final String LEGEND_RIGHT_COMMAND = "legendRight";
	private static final String SHOW_MULTI_AXES_COMMAND = "showMultiAxes";
	private JMenu legendMenu;
	private JMenuItem legendNone;
	private JMenuItem legendBottom;
	private JMenuItem legendRight;
	private JMenuItem showMultiAxesMenuItem;

    public static final String UNFOCUS_CURVE_COMMAND = "FOCUS_NONE";
    public static final String FOCUS_ON_COMMAND = "FOCUS_ON";
    public static final String RESET_ALL_CURVE_COMMAND = "RESET_ALL";
    public static final String RESET_CURVE_COMMAND = "RESET_CURVE";

    private JMenu curveManagementMenu;
	private JMenu curveResetMenu;
    
    public static final String TOGGLE_PAUSED_COMMAND = "TOGGLE_PAUSED";
    private JMenuItem pauseMenuItem;

	/**
	 * @param chart
	 */
	public TimePlotPanel(JFreeChart chart) {
		this(
	            chart,
	            StaticValues.PANEL_WIDTH,
	            StaticValues.PANEL_HEIGHT,
	            StaticValues.PANEL_MINIMUM_DRAW_WIDTH,
	            StaticValues.PANEL_MINIMUM_DRAW_HEIGHT,
	            StaticValues.PANEL_MAXIMUM_DRAW_WIDTH,
	            StaticValues.PANEL_MAXIMUM_DRAW_HEIGHT,
	            true,
	            true,  // properties
	            true,  // save
	            true,  // print
	            true,  // zoom
	            true   // tooltips
	        );
	}

	/**
	 * @param chart
	 * @param useBuffer
	 */
	public TimePlotPanel(JFreeChart chart, boolean useBuffer) {
		this(
	            chart,
	            StaticValues.PANEL_WIDTH,
	            StaticValues.PANEL_HEIGHT,
	            StaticValues.PANEL_MINIMUM_DRAW_WIDTH,
	            StaticValues.PANEL_MINIMUM_DRAW_HEIGHT,
	            StaticValues.PANEL_MAXIMUM_DRAW_WIDTH,
	            StaticValues.PANEL_MAXIMUM_DRAW_HEIGHT,
	            useBuffer,
	            true,  // properties
	            true,  // save
	            true,  // print
	            true,  // zoom
	            true   // tooltips
	        );
	}

	/**
	 * @param chart
	 * @param properties
	 * @param save
	 * @param print
	 * @param zoom
	 * @param tooltips
	 */
	public TimePlotPanel(JFreeChart chart, boolean properties, boolean save,
			boolean print, boolean zoom, boolean tooltips) {
		this(chart,
				StaticValues.PANEL_WIDTH,
				StaticValues.PANEL_HEIGHT,
				StaticValues.PANEL_MINIMUM_DRAW_WIDTH,
				StaticValues.PANEL_MINIMUM_DRAW_HEIGHT,
				StaticValues.PANEL_MAXIMUM_DRAW_WIDTH,
				StaticValues.PANEL_MAXIMUM_DRAW_HEIGHT,
				true,
				properties,
				save,
				print,
				zoom,
				tooltips
		);
	}

	/**
	 * @param chart
	 * @param width
	 * @param height
	 * @param minimumDrawWidth
	 * @param minimumDrawHeight
	 * @param maximumDrawWidth
	 * @param maximumDrawHeight
	 * @param useBuffer
	 * @param properties
	 * @param save
	 * @param print
	 * @param zoom
	 * @param tooltips
	 */
	public TimePlotPanel(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean save, boolean print, boolean zoom, boolean tooltips) {
		this(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties,
				true, save, print, zoom, tooltips);
	}

	/**
	 * @param chart
	 * @param width
	 * @param height
	 * @param minimumDrawWidth
	 * @param minimumDrawHeight
	 * @param maximumDrawWidth
	 * @param maximumDrawHeight
	 * @param useBuffer
	 * @param properties
	 * @param copy
	 * @param save
	 * @param print
	 * @param zoom
	 * @param tooltips
	 */
	public TimePlotPanel(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean copy, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties,
				copy, save, print, zoom, tooltips);
		double windowSize = 0;
		try {
			String sizeProperty = System.getProperty(TIME_PLOT_WINDOW_SIZE);
			if (sizeProperty != null) {
				windowSize = Double.valueOf(sizeProperty);
			}
		} catch (Exception e) {
		}
		if (windowSize > 0) {
			getHorizontalAxis().setFixedAutoRange(windowSize * 1000);
		}
		try {
			String multiAxesProperty = System.getProperty(TIME_PLOT_SHOW_MULTIAXES);
			if (multiAxesProperty != null) {
				showMultiaxes = Boolean.valueOf(multiAxesProperty);
			}
		}catch (Exception e) {
		}
	}

	@Override
	public void addTimeSeriesSet(ITimeSeriesSet timeSeriesSet) {
		int validDatasetCount = 0;
		for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
			XYDataset dataset = getXYPlot().getDataset(i);
			if (dataset != null && dataset instanceof ITimeSeriesSet) {
				validDatasetCount ++;
			}
		}
		if (validDatasetCount == 1) {
			int numberOfSeries = getXYPlot().getDataset().getSeriesCount();
			if (numberOfSeries <= 0) {
				getXYPlot().setDataset(0, timeSeriesSet);
				getXYPlot().getRangeAxis().setLabel(timeSeriesSet.getYTitle());
				return;
			}
		}
		int index = getXYPlot().getDatasetCount();
		getXYPlot().setDataset(index, timeSeriesSet);
		final NumberAxis rangeAxis2 = new NumberAxis(timeSeriesSet.getYTitle());
        rangeAxis2.setAutoRangeIncludesZero(false);
        DefaultXYItemRenderer newRenderer = new DefaultXYItemRenderer();
        newRenderer.setBaseShapesVisible(false);
//        newRenderer.setBaseShapesVisible(true);
        getXYPlot().setRenderer(index, newRenderer);
        getXYPlot().setRangeAxis(index, rangeAxis2);
        getXYPlot().mapDatasetToRangeAxis(index, index);
        if (index > 0) {
        	rangeAxis2.setVisible(showMultiaxes);
        }
	}

	@Override
	public void removeTimeSeriesSet(ITimeSeriesSet timeSeriesSet) {
		if (timeSeriesSet == null) {
			return;
		}
		int index = getXYPlot().indexOf(timeSeriesSet);
		if (index == 0) {
			boolean found = false;
			for (int i = 1; i < getXYPlot().getDatasetCount(); i++) {
				XYDataset dataset = getXYPlot().getDataset(i);
				if (dataset != null && dataset instanceof ITimeSeriesSet) {
					found = true;
					getXYPlot().setDataset(0, dataset);
					getXYPlot().setDataset(i, null);
					getXYPlot().setRenderer(0, getXYPlot().getRenderer(i));
					getXYPlot().setRenderer(i, null);
					getXYPlot().setRangeAxis(0, getXYPlot().getRangeAxis(i));
					getXYPlot().setRangeAxis(i, null);
					break;
				}
			}
			if (!found) {
				timeSeriesSet.removeAllSeries();
				getDataset().setYTitle(null);
				getDataset().setYUnits(null);
//				getXYPlot().getRangeAxis().setLabel("");
			}
		} else if (index > 0) {
			getXYPlot().setRenderer(index, null);
			getXYPlot().setRangeAxis(index, null);
			getXYPlot().setDataset(index, null);
		}
		if (selectedDataset == timeSeriesSet) {
			selectSeries(null, -1);
		}
		if (getXYPlot().getDatasetCount() > 0) {
			getXYPlot().getRangeAxis(0).setVisible(true);
		}
		updateLabels();
	}

	public void setSelectedDataset(int datasetIndex) {
		XYDataset dataset = getXYPlot().getDataset(datasetIndex);
		if (dataset instanceof IDataset) {
			setSelectedDataset((IDataset) dataset);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	        Insets insets = getInsets();
	        int x = (int) ((e.getX() - insets.left) / getScaleX());
	        int y = (int) ((e.getY() - insets.top) / getScaleY());

	        EntityCollection entities = getChartRenderingInfo().getEntityCollection();
	        if (entities != null) {
	        	ChartEntity entity  = entities.getEntity(x, y);
	        	Object[] listeners = getListeners(ChartMouseListener.class);
	        	if (getChart() != null) {
	        		XYChartMouseEvent event = new XYChartMouseEvent(getChart(), e, entity);
	        		event.setXY(getChartX(), getChartY());
	        		//        		event.setSeriesIndex(seriesIndex);
	        		for (int i = listeners.length - 1; i >= 0; i -= 1) {
	        			((ChartMouseListener) listeners[i]).chartMouseMoved(event);
	        		}
	        	}
	        }
	    super.mouseMoved(e);
	}
	
	@Override
    public void mouseClicked(MouseEvent event) {

        Insets insets = getInsets();
        int x = (int) ((event.getX() - insets.left) / getScaleX());
        int y = (int) ((event.getY() - insets.top) / getScaleY());

        setAnchor(new Point2D.Double(x, y));
        if (getChart() == null) {
            return;
        }
//        getChart().setNotify(true);  // force a redraw
        // new entity code...
//        if (listeners.length == 0) {
//            return;
//        }

        ChartEntity entity = null;
        if (getChartRenderingInfo() != null) {
            EntityCollection entities = getChartRenderingInfo().getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
                if (entity instanceof XYItemEntity) {
                	XYItemEntity xyEntity = (XYItemEntity) entity;
//                	XYDataset dataset = xyEntity.getDataset();
//                	int item = ((XYItemEntity) entity).getItem();
//                	chartX = dataset.getXValue(xyEntity.getSeriesIndex(), item);
//                	chartY = dataset.getYValue(xyEntity.getSeriesIndex(), item);
//                	Point2D screenPoint = ChartMaskingUtilities.translateChartPoint(
//                			new Point2D.Double(chartX, chartY), getScreenDataArea(), getChart());
//                	if (getHorizontalAxisTrace()) {
//                		horizontalTraceLocation = (int) screenPoint.getX();
//                	}
//                	if (getVerticalAxisTrace()) {
//                		verticalTraceLocation = (int) screenPoint.getY();
//                	}
                	if ((event.getModifiers() & seriesSelectionEventMask) != 0 && 
                			(event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                		XYDataset dataset = xyEntity.getDataset();
                		if (dataset instanceof IDataset) {
                			selectSeries((IDataset) xyEntity.getDataset(), 
                					xyEntity.getSeriesIndex());
                		}
                	} else if ((event.getModifiers() & maskingSelectionMask) == 0 
                			&& (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                		if (selectedSeriesIndex != xyEntity.getSeriesIndex()) {
                			selectSeries(null, -1);
                		}
                	}
                } else {
                	if (selectedSeriesIndex >= 0) {
                		if ((event.getModifiers() & seriesSelectionEventMask) != 0 
                				&& (event.getModifiers() & maskingSelectionMask) == 0 
                    			&& (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                    		selectSeries(null, -1);
                    	}
                 	}
                }
            }
        }
        
        if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        	double chartX = ChartMaskingUtilities.translateScreenX(x, getScreenDataArea(), getChart());
        	int selectedDatasetIndex = 0;
        	if (selectedDataset != null) {
        		selectedDatasetIndex = getXYPlot().indexOf(selectedDataset);
        	}
        	double chartY = ChartMaskingUtilities.translateScreenY(y, getScreenDataArea(), 
        			getChart(), selectedDatasetIndex);
        	setHorizontalTraceLocation(x);
        	setVerticalTraceLocation(y);
        	setChartX(chartX);
        	setChartY(chartY);
        	repaint();
        }
        
        XYChartMouseEvent chartEvent = new XYChartMouseEvent(getChart(), event,
                entity);
        chartEvent.setXY(getChartX(), getChartY());
        Object[] listeners = getListeners(ChartMouseListener.class);
        for (int i = listeners.length - 1; i >= 0; i -= 1) {
            ((ChartMouseListener) listeners[i]).chartMouseClicked(chartEvent);
        }
        System.out.println("chartX = " + getChartX() + ", chartY = " + getChartY());
    }
	
	protected void selectSeries(IDataset dataset, int seriesIndex) {
		if (dataset != null && seriesIndex >= 0) {
//			if (selectedDataset != null && selectedSeriesIndex >= 0 && 
//					(selectedDataset != dataset || selectedSeriesIndex != seriesIndex)) {
//				XYItemRenderer renderer = getChart().getXYPlot().getRendererForDataset(
//						selectedDataset);
//				if (renderer instanceof XYLineAndShapeRenderer) {
//					renderer.setSeriesStroke(selectedSeriesIndex, DEFAULT_STROCK);
//				}
//			}
			
			boolean isSelected = false;
			if (selectedDataset != dataset || selectedSeriesIndex != seriesIndex) {
				XYItemRenderer renderer = getChart().getXYPlot().getRendererForDataset(dataset);
				if (renderer instanceof XYLineAndShapeRenderer) {
					renderer.setSeriesStroke(seriesIndex, BOLD_STROCK);
					isSelected = true;
				}
				ValueAxis axis = getXYPlot().getRangeAxisForDataset(
						getXYPlot().indexOf(dataset));
//				Font titleFont = axis.getLabelFont();
//				axis.setLabelFont(titleFont.deriveFont(Font.BOLD));
				axis.setLabelPaint(AXIS_FOCUS_COLOR);
//				Font tickFont = axis.getTickLabelFont();
//				axis.setTickLabelFont(tickFont.deriveFont(Font.BOLD));
				axis.setTickLabelPaint(AXIS_FOCUS_COLOR);
//				axis.setVisible(true);
			}
			Stroke strokeToSet = isSelected ? SHALLOW_STROCK : DEFAULT_STROCK;
			for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
				IDataset xyDataset = (IDataset) getXYPlot().getDataset(i);
				if (xyDataset == null) {
					continue;
				}
				XYItemRenderer renderer = getXYPlot().getRendererForDataset(
						xyDataset);
				if (renderer != null && renderer instanceof XYLineAndShapeRenderer) {
					for (int j = 0; j < xyDataset.getSeriesCount(); j++) {
						if (dataset != xyDataset || seriesIndex != j) {
							renderer.setSeriesStroke(j, strokeToSet);
						}
					}
				}
				if (dataset != xyDataset) {
					ValueAxis axis = getXYPlot().getRangeAxisForDataset(i);
//					Font titleFont = axis.getLabelFont();
//					axis.setLabelFont(titleFont.deriveFont(Font.PLAIN));
					axis.setLabelPaint(AXIS_SHALLOW_COLOR);
//					Font tickFont = axis.getTickLabelFont();
//					axis.setTickLabelFont(tickFont.deriveFont(Font.PLAIN));
					axis.setTickLabelPaint(AXIS_SHALLOW_COLOR);
//					axis.setVisible(false);
				}
			}
		} else if (selectedDataset != null && selectedSeriesIndex >= 0) {
//			XYItemRenderer renderer = getChart().getXYPlot().getRendererForDataset(
//					selectedDataset);
//			if (renderer instanceof XYLineAndShapeRenderer) {
//				renderer.setSeriesStroke(selectedSeriesIndex, DEFAULT_STROCK);
//			}
			for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
				IDataset xyDataset = (IDataset) getXYPlot().getDataset(i);
				if (xyDataset == null) {
					continue;
				}
				XYItemRenderer renderer = getXYPlot().getRendererForDataset(
						xyDataset);
				if (renderer != null && renderer instanceof XYLineAndShapeRenderer) {
					for (int j = 0; j < xyDataset.getSeriesCount(); j++) {
						renderer.setSeriesStroke(j, DEFAULT_STROCK);
					}
				}
				ValueAxis axis = getXYPlot().getRangeAxisForDataset(i);
//				Font titleFont = axis.getLabelFont();
//				axis.setLabelFont(titleFont.deriveFont(Font.PLAIN));
//				Font tickFont = axis.getTickLabelFont();
//				axis.setTickLabelFont(tickFont.deriveFont(Font.PLAIN));
				axis.setLabelPaint(AXIS_FOCUS_COLOR);
				axis.setTickLabelPaint(AXIS_FOCUS_COLOR);
//				axis.setVisible(true);
			}
		}
		selectedSeriesIndex = seriesIndex;
		selectedDataset = dataset;
		updatePlot();
	}
	
	@Override
    protected void drawToolTipFollower(Graphics2D g2, int x, int y) {
    	Rectangle2D dataArea = getScreenDataArea();
    	if (((int) dataArea.getMinX() <= x) && (x <= (int) dataArea.getMaxX()) && 
    			((int) dataArea.getMinY() <= y) && (y <= (int) dataArea.getMaxY())) {
    		Date date = new Date((long) getChartX());
    		String text = "";
    		SimpleDateFormat format = new SimpleDateFormat("EEE d MMM HH:mm:ss");
    		StringBuffer buffer = new StringBuffer();
    		format.format(date, buffer, new FieldPosition(0));
    		text = buffer.toString();
    		text = "(" + text + String.format(", %.2f)", getChartY());
    		int xLoc = x + 10;
    		int yLoc = y + 20;
    		double width = text.length() * 5.5;
    		double height = 15;
    		if (xLoc + width > dataArea.getMaxX()) {
    			xLoc = (int) (x - width);
    		}
    		if (yLoc + height > dataArea.getMaxY()) {
    			yLoc = (int) (y - height);
    		}
    		
    		Rectangle2D toolTipArea = new Rectangle2D.Double(xLoc, yLoc, 
    				width, height);
    		
    		g2.setColor(Color.white);
    		g2.fill(toolTipArea);
    		g2.setColor(Color.black);
    		g2.drawString(text, xLoc + 3, yLoc + 11);
    	}
    }

	@Override
	public void paintComponent(Graphics g) {
		int datasetIndex = 0;
		if (selectedDataset != null) {
			datasetIndex = getXYPlot().indexOf(selectedDataset);
		}
		Point2D point = ChartMaskingUtilities.translateChartPoint(
				new Point2D.Double(getChartX(), getChartY()), getScreenDataArea(), 
				getChart(), datasetIndex);
		setHorizontalTraceLocation((int) point.getX());
		setVerticalTraceLocation((int) point.getY());
		if (isPaused) {
//			Graphics2D g2 = (Graphics2D) g.create();
//			if (getHorizontalAxisTrace()) {
//				super.drawHorizontalAxisTrace(g2, getHorizontalTraceLocation());
//			}
//			if (getVerticalAxisTrace()) {
//				drawVerticalAxisTrace(g2, getVerticalTraceLocation());
//			}
//			if (isToolTipFollowerEnabled) {
//				drawToolTipFollower(g2, horizontalTraceLocation, verticalTraceLocation);
//			}
//			return;
		} 
		super.paintComponent(g);
	}

	public void setPaused(boolean isPaused) {
		ValueAxis valueaxis = getXYPlot().getDomainAxis();   
		valueaxis.setAutoRange(!isPaused);  
		this.isPaused = isPaused;
	}
	
	public boolean isPaused() {
		return isPaused;
	}
	
	@Override
	public void restoreAutoDomainBounds() {
		super.restoreAutoDomainBounds();
		if (isPaused) {
			setPaused(isPaused);
		}
	}

	public void setSelectedDataset(IDataset dataset, int seriesIndex) {
		selectSeries(dataset, seriesIndex);
	}

	public void setSelectedDataset(IDataset dataset) {
		selectSeries(dataset, 0);
	}
	
	@Override
	public void moveSelectedMask(int keyCode) {
		// do nothing
	}

	@Override
	protected Color getAxisTraceColor() {
		return axisTraceColor;
	}

	@Override
	protected void selectMask(double x, double y) {
		// do nothing
	}

	@Override
	protected int findCursorOnSelectedItem(int x, int y) {
		// do nothing
		return 0;
	}
	
	@Override
	protected JPopupMenu createPopupMenu(boolean properties, boolean copy,
			boolean save, boolean print, boolean zoom) {
		JPopupMenu menu = super.createPopupMenu(properties, copy, save, print, zoom);
		menu.addSeparator();
		legendMenu = new JMenu("Legend Position");
		menu.add(legendMenu);

		legendNone = new JRadioButtonMenuItem("None");
		legendNone.setActionCommand(LEGEND_NONE_COMMAND);
		legendNone.addActionListener(this);
		legendMenu.add(legendNone);

		legendBottom = new JRadioButtonMenuItem("Bottom");
		legendBottom.setActionCommand(LEGEND_BOTTOM_COMMAND);
		legendBottom.addActionListener(this);
		legendMenu.add(legendBottom);

		legendRight = new JRadioButtonMenuItem("Right");
		legendRight.setActionCommand(LEGEND_RIGHT_COMMAND);
		legendRight.addActionListener(this);
		legendMenu.add(legendRight);

		menu.addSeparator();
		curveManagementMenu = new JMenu("Focus on Curve");
        menu.add(curveManagementMenu);
        
        menu.addSeparator();
        showMultiAxesMenuItem = new JCheckBoxMenuItem("Show Multiple Vertical Axes");
        showMultiAxesMenuItem.setActionCommand(SHOW_MULTI_AXES_COMMAND);
        showMultiAxesMenuItem.addActionListener(this);
        menu.add(showMultiAxesMenuItem);
        
        menu.addSeparator();
        curveResetMenu = new JMenu("Reset Curve");
        menu.add(curveResetMenu);
        
        menu.addSeparator();
        pauseMenuItem = new JCheckBoxMenuItem("Paused");
        pauseMenuItem.setActionCommand(TOGGLE_PAUSED_COMMAND);
        pauseMenuItem.addActionListener(this);
        menu.add(pauseMenuItem);
        return menu;
	}
	
	@Override
	protected void displayPopupMenu(int x, int y) {
    	LegendTitle legend = getChart().getLegend();
    	if (legend != null){
    		boolean isVisable = legend.isVisible();
    		RectangleEdge location = legend.getPosition();
    		if (isVisable) {
    			if (location.equals(RectangleEdge.BOTTOM)){
    				legendBottom.setSelected(true);
    				legendNone.setSelected(false);
    				legendRight.setSelected(false);
    			} else if (isVisable && location.equals(RectangleEdge.RIGHT)){
    				legendRight.setSelected(true);
    				legendNone.setSelected(false);
    				legendBottom.setSelected(false);
    			}
    		}else {
    			legendNone.setSelected(true);
    			legendRight.setSelected(false);
    			legendBottom.setSelected(false);
    		}
    	}
		curveManagementMenu.removeAll();
		curveResetMenu.removeAll();
    	if (getXYPlot().getDatasetCount() > 0) {
			curveManagementMenu.setEnabled(true);
			curveResetMenu.setEnabled(true);
			
			JMenuItem focusNoneCurveItem = new JRadioButtonMenuItem();
			focusNoneCurveItem.setText("None");
			focusNoneCurveItem.setActionCommand(UNFOCUS_CURVE_COMMAND);
			focusNoneCurveItem.addActionListener(this);
			curveManagementMenu.add(focusNoneCurveItem);
			
			JMenuItem resetAllCurveItem = new JMenuItem();
			resetAllCurveItem.setText("RESET ALL");
			resetAllCurveItem.setActionCommand(RESET_ALL_CURVE_COMMAND);
			resetAllCurveItem.addActionListener(this);
			curveResetMenu.add(resetAllCurveItem);
			
			boolean isCurveFocused = false;
    		for (int j = 0; j < getXYPlot().getDatasetCount(); j++) {
    			XYDataset dataset = getChart().getXYPlot().getDataset(j);
    			if (dataset != null) {
    				for (int i = 0; i< dataset.getSeriesCount(); i++) {
    					String seriesKey = (String) dataset.getSeriesKey(i);
    					JMenuItem focusOnCurveItem = new JRadioButtonMenuItem();
    					focusOnCurveItem.setText(seriesKey);
    					focusOnCurveItem.setActionCommand(FOCUS_ON_COMMAND 
    							+ "-" + seriesKey);
    					focusOnCurveItem.addActionListener(this);
    					curveManagementMenu.add(focusOnCurveItem);
    					if (dataset == selectedDataset && i == selectedSeriesIndex) {
    						focusOnCurveItem.setSelected(true);
    						isCurveFocused = true;
    					}
    					
    					JMenuItem resetCurveItem = new JMenuItem();
    					resetCurveItem.setText("Reset " + seriesKey);
    					resetCurveItem.setActionCommand(RESET_CURVE_COMMAND 
    							+ "-" + seriesKey);
    					resetCurveItem.addActionListener(this);
    					curveResetMenu.add(resetCurveItem);

    				}
    			}
    		}
			if (!isCurveFocused) {
				focusNoneCurveItem.setSelected(true);
			}
    	} else {
    		curveManagementMenu.setEnabled(false);
    		curveResetMenu.setEnabled(false);
    	}
    	
    	showMultiAxesMenuItem.setSelected(isShowMultiaxes());
    	
    	if (isPaused) {
    		pauseMenuItem.setText("Paused");
    	} else {
    		pauseMenuItem.setText("Click to Pause");
    	}
    	pauseMenuItem.setSelected(isPaused);
    	super.displayPopupMenu(x, y);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (command.equals(LEGEND_NONE_COMMAND)) {
        	getChart().getLegend().setVisible(false);
//        	repaint();
        } else if (command.equals(LEGEND_BOTTOM_COMMAND)) {
        	getChart().getLegend().setVisible(true);
        	getChart().getLegend().setPosition(RectangleEdge.BOTTOM);
//        	repaint();
        } else if (command.startsWith(LEGEND_RIGHT_COMMAND)) {
        	getChart().getLegend().setVisible(true);
        	getChart().getLegend().setPosition(RectangleEdge.RIGHT);
//            repaint();
        } else if (command.equals(UNFOCUS_CURVE_COMMAND)) {
        	selectSeries(null, -1);
//        	repaint();
        } else if (command.startsWith(FOCUS_ON_COMMAND)) {
        	String[] commands = command.split("-", 2);
        	if (commands.length > 1) {
        		selectSeries(commands[1]);
//            	repaint();
        	}
        } else if (command.equals(RESET_ALL_CURVE_COMMAND)) {
        	clear();
        } else if (command.startsWith(RESET_CURVE_COMMAND)) {
        	String[] commands = command.split("-", 2);
        	if (commands.length > 1) {
        		clearSeries(commands[1]);
        	}
        } else if (command.equals(SHOW_MULTI_AXES_COMMAND)){
        	setShowMultiaxes(showMultiAxesMenuItem.isSelected());
        	updateUI();
        } else if (command.equals(TOGGLE_PAUSED_COMMAND)){
        	setPaused(!isPaused);
        } else {
        	super.actionPerformed(event);
        }
		updatePlot();
	}
	
	public void selectSeries(String key) {
		if (key == null) {
			selectSeries(null, -1);
			return;
		}
		for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
			XYDataset dataset = getXYPlot().getDataset(i);
			if (dataset instanceof IDataset) {
				int seriesIndex = dataset.indexOf(key);
				if (seriesIndex >= 0) {
					selectSeries((IDataset) dataset, seriesIndex);
					break;
				}
			}
		}
	}

	@Override
	public void setSelectedSeries(String seriesKey) {
		selectSeries(seriesKey);
	}

	@Override
	public void setSelectedSeries(TimeSeries series) {
		if (series == null) {
			selectSeries(null, -1);
			return;
		}
		for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
			XYDataset dataset = getXYPlot().getDataset(i);
			boolean found = false;
			if (dataset != null && dataset instanceof ITimeSeriesSet) {
				for (int j = 0; j < dataset.getSeriesCount(); j++) {
					if (((ITimeSeriesSet) dataset).getSeries(j) == series) {
						found = true;
						selectSeries((IDataset) dataset, j);
						break;
					}
				}
			}
			if (found) {
				break;
			}
		}
	}

	@Override
	public void addTimeSeries(TimeSeries series) {
		IDataset dataset = getDataset();
		if (dataset instanceof ITimeSeriesSet) {
			((ITimeSeriesSet) dataset).addSeries(series);
		}
	}

	@Override
	public void removeTimeSeries(String seriesKey) {
		if (seriesKey == null) {
			return;
		}
		for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
			XYDataset dataset = getXYPlot().getDataset(i);
			if (dataset != null && dataset instanceof ITimeSeriesSet) {
				int seriesIndex = dataset.indexOf(seriesKey);
				if (seriesIndex >= 0) {
					if (dataset.getSeriesCount() <= 1) {
						removeTimeSeriesSet((ITimeSeriesSet) dataset);
					} else {
						((ITimeSeriesSet) dataset).removeSeries(seriesIndex);
					}
					break;
				}
			}
		}
		if (getXYPlot().getDatasetCount() > 0) {
			getXYPlot().getRangeAxis(0).setVisible(true);
		}
	}

	@Override
	public void removeTimeSeries(TimeSeries series) {
		if (series == null) {
			return;
		}
		for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
			XYDataset dataset = getXYPlot().getDataset(i);
			boolean found = false;
			if (dataset instanceof ITimeSeriesSet) {
				for (int j = 0; j < dataset.getSeriesCount(); j++) {
					if (((ITimeSeriesSet) dataset).getSeries(j) == series) {
						found = true;
						if (dataset.getSeriesCount() <= 1) {
							removeTimeSeriesSet((ITimeSeriesSet) dataset);
						} else {
							((ITimeSeriesSet) dataset).removeSeries(series);
						}
						break;
					}
				}
			}
			if (found) {
				break;
			}
		}
		if (getXYPlot().getDatasetCount() > 0) {
			getXYPlot().getRangeAxis(0).setVisible(true);
		}
	}

	@Override
	public void clear() {
		for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
			XYDataset dataset = getXYPlot().getDataset(i);
			if (dataset != null && dataset instanceof ITimeSeriesSet) {
				for (TimeSeries series : ((ITimeSeriesSet) dataset).getSeries()) {
					series.clear();
				}
			}
		}
		updatePlot();
	}

	@Override
	public void clearSeries(String seriesKey) {
		for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
			XYDataset dataset = getXYPlot().getDataset(i);
			if (dataset != null && dataset instanceof ITimeSeriesSet) {
				int seriesIndex = ((ITimeSeriesSet) dataset).indexOf(seriesKey);
				if (seriesIndex >= 0) {
					((ITimeSeriesSet) dataset).getSeries(seriesIndex).clear();
				}
			}
		}
		updatePlot();
	}
	
	@Override
	public void doEditChartProperties() {
		if (selectedDataset != null && selectedSeriesIndex >= 0) {
			Plot1DChartEditor.setSuggestedSeriesKey((String) selectedDataset.getSeriesKey(
					selectedSeriesIndex));			
		}
		TimePlotChartEditor editor = new TimePlotChartEditor(getChart());
        int result = JOptionPane.showConfirmDialog(this, editor,
                localizationResources.getString("Chart_Properties"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            editor.updateChart(getChart());
            updatePlot();
        }
	}
	
	@Override
	public void doHelp() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void doExport(IExporter exporter) throws IOException {
		
	}
	
	@Override
	public void cleanUp() {
	}

	@Override
	public void setLogarithmEnabled(boolean enabled) {
		ValueAxis axis = getXYPlot().getRangeAxis();
		if (axis instanceof LogarithmizableAxis) {
			((LogarithmizableAxis) axis).setLogarithmic(enabled);
			axis.setAutoRange(true);
		}
	}

	@Override
	public boolean isLogarithmEnabled() {
		ValueAxis axis = getXYPlot().getRangeAxis();
		if (axis instanceof LogarithmizableAxis) {
			return ((LogarithmizableAxis) axis).isLogarithmic();
		}
		return false;
	}
	
	@Override
	public IHelpProvider getHelpProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the showMultiaxes
	 */
	public boolean isShowMultiaxes() {
		return showMultiaxes;
	}

	/**
	 * @param showMultiaxes the showMultiaxes to set
	 */
	public void setShowMultiaxes(boolean showMultiaxes) {
		this.showMultiaxes = showMultiaxes;
		if (getXYPlot().getDatasetCount() > 1) {
			for (int i = 1; i < getXYPlot().getDatasetCount(); i++) {
				ValueAxis axis = getXYPlot().getRangeAxis(i);
				if (axis != null) {
					axis.setVisible(showMultiaxes);
				}
			}
		}
	}
	
	@Override
	protected void saveAsText(BufferedWriter writer) throws IOException {
		int count = 0;
		Date currentDate = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss");
		writer.write("# printed at: " + dateFormat.format(currentDate) + "\n");
		for (int i = 0; i < getXYPlot().getDatasetCount(); i++) {
			ITimeSeriesSet dataset = (ITimeSeriesSet) getXYPlot().getDataset(i);
			if (dataset != null) {
				writer.write("# series_" + (count++) + "\n");
				DatasetUtils.export(dataset, writer, ExportFormat.TIMESERIES);
			}
		}
	}
	
	@Override
	public void zoom(Rectangle2D selection) {
		super.zoom(selection);
		updatePlot();
	}
	
	@Override
	public void restoreAutoBounds() {
		super.restoreAutoBounds();
		updatePlot();
	}
}
