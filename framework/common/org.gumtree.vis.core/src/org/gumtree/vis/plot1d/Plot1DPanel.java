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
package org.gumtree.vis.plot1d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.eclipse.swt.SWT;
import org.gumtree.vis.awt.JChartPanel;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.dataset.DatasetUtils;
import org.gumtree.vis.dataset.DatasetUtils.ExportFormat;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.interfaces.IPlot1D;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.listener.XYChartMouseEvent;
import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.ChartMaskingUtilities;
import org.gumtree.vis.mask.RangeMask;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ExtensionFileFilter;
import org.jfree.ui.RectangleEdge;

/**
 * @author nxi
 *
 */
public class Plot1DPanel extends JChartPanel implements IPlot1D {

	/**
	 * 
	 */
	public static final Color MASK_INCLUSIVE_COLOR = new Color(0, 220, 0, 30);
	public static final Color MASK_EXCLUSIVE_COLOR = new Color(0, 0, 220, 30);
	private static final long serialVersionUID = -5212815744540142881L;
	private static final String LEGEND_NONE_COMMAND = "legendNone";
	private static final String LEGEND_BOTTOM_COMMAND = "legendBottom";
	private static final String LEGEND_RIGHT_COMMAND = "legendRight";
	private static final Stroke DEFAULT_STROCK = new BasicStroke(1);
	private static final Stroke BOLD_STROCK = new BasicStroke(2f);
	private static final int seriesSelectionEventMask = InputEvent.CTRL_MASK; 
//    private static final Cursor MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    private static final Color axisTraceColor = Color.darkGray;
	private JMenu legendMenu;
	private JMenuItem legendNone;
	private JMenuItem legendBottom;
	private JMenuItem legendRight;
    private int selectedSeriesIndex = -1;
    private double chartError;
    
    /** Remove the selected mask command. */
    public static final String UNFOCUS_CURVE_COMMAND = "FOCUS_NONE";
    public static final String FOCUS_ON_COMMAND = "FOCUS_ON";
    private static int minMaskWidth = 4;
    private double maskPoint = Double.NaN;
    private RangeMask currentMaskRectangle = null;
//    private List<RectangleMask> exclusiveMaskList;
//    private RangeMask selectedMask;
    private double maskMovePoint;
//    private JMenu maskManagementMenu;
    private JMenu curveManagementMenu;
//    private JMenuItem removeSelectedMaskMenuItem;
    
	/**
	 * @param chart
	 */
	public Plot1DPanel(JFreeChart chart) {
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
	public Plot1DPanel(JFreeChart chart, boolean useBuffer) {
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
	public Plot1DPanel(JFreeChart chart, boolean properties, boolean save,
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
	public Plot1DPanel(JFreeChart chart, int width, int height,
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
	public Plot1DPanel(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean copy, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties,
				copy, save, print, zoom, tooltips);
		createMaskColors(false);
		String horizontalMarginProperty = System.getProperty(StaticValues.HORIZONTAL_MARGIN_PROPERTY);
		if (horizontalMarginProperty != null) {
			float horizontalMargin = 0.05f;
			try {
				horizontalMargin = Float.valueOf(horizontalMarginProperty);
				getHorizontalAxis().setLowerMargin(horizontalMargin);
				getHorizontalAxis().setUpperMargin(horizontalMargin);
			} catch (Exception e) {
			}
		}
		
	}

//	@Override
//	public void paintComponent(Graphics g) {
////		long time = System.currentTimeMillis();
//		super.paintComponent(g);
//		Graphics2D g2 = (Graphics2D) g.create();
////		ChartMaskingUtilities.drawDomainMask(g2, getScreenDataArea(), maskList, 
////				selectedMask, getChart());
//		ChartMaskingUtilities.drawMasks(g2, getScreenDataArea(), getMasks(), 
//				selectedMask, getChart());
//		if (getHorizontalAxisTrace()) {
//			drawHorizontalAxisTrace(g2, horizontalTraceLocation);
//		}
//		if (getVerticalAxisTrace()) {
//			drawVerticalAxisTrace(g2, verticalTraceLocation);
//		}
//		if (isToolTipFollowerEnabled) {
//			drawToolTipFollower(g2, horizontalTraceLocation, verticalTraceLocation);
//		}
////		System.out.println("refreshing cost " + (System.currentTimeMillis() - time) + " ms");
//	}
	
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
        
//        this.removeSelectedMaskMenuItem = new JMenuItem();
//        this.removeSelectedMaskMenuItem.setActionCommand(REMOVE_SELECTED_MASK_COMMAND);
//        this.removeSelectedMaskMenuItem.addActionListener(this);
//        menu.addSeparator();
//        menu.add(removeSelectedMaskMenuItem);
//        maskManagementMenu = new JMenu("Mask Management");
//        menu.add(maskManagementMenu);

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
    	XYDataset dataset = getChart().getXYPlot().getDataset();
    	curveManagementMenu.removeAll();
    	if (dataset.getSeriesCount() > 0) {
    		curveManagementMenu.setEnabled(true);
    		JMenuItem focusNoneCurveItem = new JRadioButtonMenuItem();
    		focusNoneCurveItem.setText("None");
    		focusNoneCurveItem.setActionCommand(UNFOCUS_CURVE_COMMAND);
    		focusNoneCurveItem.addActionListener(this);
    		curveManagementMenu.add(focusNoneCurveItem);
    		boolean isCurveFocused = false;
    		for (int i = 0; i< dataset.getSeriesCount(); i++) {
    			String seriesKey = (String) dataset.getSeriesKey(i);
    			JMenuItem focusOnCurveItem = new JRadioButtonMenuItem();
    			focusOnCurveItem.setText(seriesKey);
    			focusOnCurveItem.setActionCommand(FOCUS_ON_COMMAND 
    					+ "-" + seriesKey);
    			focusOnCurveItem.addActionListener(this);
        		curveManagementMenu.add(focusOnCurveItem);
        		if (i == selectedSeriesIndex) {
        			focusOnCurveItem.setSelected(true);
        			isCurveFocused = true;
        		}
    		}
    		if (!isCurveFocused) {
    			focusNoneCurveItem.setSelected(true);
    		}
    	} else {
    		curveManagementMenu.setEnabled(false);
    	}
//        addMaskMenu(x, y);
		super.displayPopupMenu(x, y);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (command.equals(LEGEND_NONE_COMMAND)) {
        	getChart().getLegend().setVisible(false);
        	repaint();
        } else if (command.equals(LEGEND_BOTTOM_COMMAND)) {
        	getChart().getLegend().setVisible(true);
        	getChart().getLegend().setPosition(RectangleEdge.BOTTOM);
        	repaint();
        } else if (command.startsWith(LEGEND_RIGHT_COMMAND)) {
        	getChart().getLegend().setVisible(true);
        	getChart().getLegend().setPosition(RectangleEdge.RIGHT);
            repaint();
        } else if (command.equals(UNFOCUS_CURVE_COMMAND)) {
        	selectSeries(-1);
        	repaint();
        } else if (command.startsWith(FOCUS_ON_COMMAND)) {
        	String[] commands = command.split("-", 2);
        	if (commands.length > 1) {
        		selectSeries(commands[1]);
            	repaint();
        	}
        } else {
        	super.actionPerformed(event);
        }
	}
	
	@Override
	public void doEditChartProperties() {
		showPropertyEditor(0);
	}
	
	private void showPropertyEditor(int tabIndex) {
		XYDataset dataset = getChart().getXYPlot().getDataset();
		if (selectedSeriesIndex >= 0 && selectedSeriesIndex < dataset.getSeriesCount()) {
			Plot1DChartEditor.setSuggestedSeriesKey((String) dataset.getSeriesKey(
					selectedSeriesIndex));			
		}
		Plot1DChartEditor editor = new Plot1DChartEditor(getChart(), this);
		editor.getTabs().setSelectedIndex(tabIndex);
        int result = JOptionPane.showConfirmDialog(this, editor,
                localizationResources.getString("Chart_Properties"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            editor.updateChart(getChart());
        }
	}
	
	public void doEditMaskProperties() {
		showPropertyEditor(2);
	}
	
    /**
     * Receives notification of mouse clicks on the panel. These are
     * translated and passed on to any registered {@link ChartMouseListener}s.
     *
     * @param event  Information about the mouse event.
     */
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

//        if ((event.getModifiers() & maskingSelectionMask) != 0) {
        if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        	selectMask(ChartMaskingUtilities.translateScreenX(x, 
        			getScreenDataArea(), getChart()), Double.NaN);
        	repaint();
        }
        if ((event.getModifiers() & seriesSelectionEventMask) == 0){
        	if (getSelectedMask() != null && (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 
        			&& !getSelectedMask().getRange().contains(ChartMaskingUtilities.translateScreenX(x, 
                			getScreenDataArea(), getChart()))) {
        		selectMask(Double.NaN, Double.NaN);
        	}
        	repaint();
        } else {
        	selectMask(ChartMaskingUtilities.translateScreenX(x, 
        			getScreenDataArea(), getChart()), Double.NaN);
        	repaint();
        }
        
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
                		selectSeries(xyEntity.getSeriesIndex());
                	} else if ((event.getModifiers() & maskingSelectionMask) == 0 
                			&& (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                		if (selectedSeriesIndex != xyEntity.getSeriesIndex()) {
                			selectSeries(-1);
                		}
                	}
                } else {
                	if (selectedSeriesIndex >= 0) {
                		if ((event.getModifiers() & seriesSelectionEventMask) != 0 
                				&& (event.getModifiers() & maskingSelectionMask) == 0 
                    			&& (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                    		selectSeries(-1);
                    	}
                 	}
                }
            }
        }
        XYChartMouseEvent chartEvent = new XYChartMouseEvent(getChart(), event,
                entity);
        chartEvent.setXY(getChartX(), getChartY());
        Object[] listeners = getListeners(ChartMouseListener.class);
        for (int i = listeners.length - 1; i >= 0; i -= 1) {
            ((ChartMouseListener) listeners[i]).chartMouseClicked(chartEvent);
        }
//        System.out.println("chartX = " + getChartX() + ", chartY = " + getChartY());
    }
	
	@Override
	public void mouseMoved(MouseEvent e) {
	        Insets insets = getInsets();
	        int x = (int) ((e.getX() - insets.left) / getScaleX());
	        int y = (int) ((e.getY() - insets.top) / getScaleY());

	        ChartEntity entity = null;
	        if (getChartRenderingInfo() != null) {
	            EntityCollection entities = getChartRenderingInfo().getEntityCollection();
	            if (entities != null) {
	                entity = entities.getEntity(x, y);
//	                boolean isDirty = false;
	                int seriesIndex = -1;
	                if (selectedSeriesIndex >= 0) {
//	                	double xInChart = ChartMaskingUtilities.translateScreenX(x, 
//	                			getScreenDataArea(), getChart());
//	                	XYDataset dataset = getChart().getXYPlot().getDataset();
//	                	PatternDataset patternDataset = (PatternDataset) dataset;
//	                	int itemIndex = patternDataset.getItemFromX(selectedSeriesIndex, xInChart);
//	                	if (itemIndex < patternDataset.getItemCount(selectedSeriesIndex)) {
//	                		chartX = patternDataset.getXValue(selectedSeriesIndex, itemIndex);
//	                		chartY = patternDataset.getYValue(selectedSeriesIndex, itemIndex);
//	                		Point2D axisTrace = ChartMaskingUtilities.translateChartPoint(
//	                				new Point2D.Double(chartX, chartY), getScreenDataArea(), getChart());
//	                		horizontalTraceLocation = (int) axisTrace.getX();
//	                		verticalTraceLocation = (int) axisTrace.getY();
//	                		if (getHorizontalAxisTrace() || getVerticalAxisTrace() || isToolTipFollowerEnabled) {
//	                			repaint();
//	                		}
//	                		seriesIndex = selectedSeriesIndex;
//	                		isDirty = true;
//	                	}
	                	seriesIndex = followDomainTrace(selectedSeriesIndex, x);
	                	if (seriesIndex >= 0 && (getHorizontalAxisTrace() || getVerticalAxisTrace() 
	                			|| isToolTipFollowerEnabled())) {
                			repaint();
                		}
	                } else if (getChart().getXYPlot().getSeriesCount() == 1){
//	                	int seriesIndex0 = 0;
//	                	double xInChart = ChartMaskingUtilities.translateScreenX(x, 
//	                			getScreenDataArea(), getChart());
//	                	XYDataset dataset = getChart().getXYPlot().getDataset();
//	                	PatternDataset patternDataset = (PatternDataset) dataset;
//	                	int itemIndex = patternDataset.getItemFromX(seriesIndex0, xInChart);
//	                	if (itemIndex < patternDataset.getItemCount(seriesIndex0)) {
//	                		chartX = patternDataset.getXValue(seriesIndex0, itemIndex);
//	                		chartY = patternDataset.getYValue(seriesIndex0, itemIndex);
//	                		Point2D axisTrace = ChartMaskingUtilities.translateChartPoint(
//	                				new Point2D.Double(chartX, chartY), getScreenDataArea(), getChart());
////	                		if (getScreenDataArea().contains(axisTrace)) {
//	                		horizontalTraceLocation = (int) axisTrace.getX();
//	                		verticalTraceLocation = (int) axisTrace.getY();
//	                		if (getHorizontalAxisTrace() || getVerticalAxisTrace() || isToolTipFollowerEnabled) {
//	                			repaint();
//	                		}
//	                		seriesIndex = seriesIndex0;
//	                		isDirty = true;
////		                	}
//	                	}
	                	seriesIndex = followDomainTrace(0, x);
	                	if (seriesIndex >= 0 && (getHorizontalAxisTrace() || getVerticalAxisTrace() 
	                			|| isToolTipFollowerEnabled())) {
                			repaint();
                		}
	                } else if (entity instanceof XYItemEntity) {
	                	XYItemEntity xyEntity = (XYItemEntity) entity;
	                	XYDataset dataset = xyEntity.getDataset();
	                	int item = ((XYItemEntity) entity).getItem();
	                	seriesIndex = xyEntity.getSeriesIndex();
	                	double chartX = dataset.getXValue(seriesIndex, item);
	                	double chartY = dataset.getYValue(seriesIndex, item);
	                	Point2D screenPoint = ChartMaskingUtilities.translateChartPoint(
	                			new Point2D.Double(chartX, chartY), getScreenDataArea(), getChart());
	                	setChartX(chartX);
	                	setChartY(chartY);
	                	if (dataset instanceof IXYErrorDataset) {
		                	setChartError(((IXYErrorDataset) dataset).getYError(seriesIndex, item));
		                	
	                	}
	                	if (getHorizontalAxisTrace()) {
	                		setHorizontalTraceLocation((int) screenPoint.getX());
	                	}
	                	if (getVerticalAxisTrace()) {
	                		setVerticalTraceLocation((int) screenPoint.getY());
	                	}
	                	if (getHorizontalAxisTrace() || getVerticalAxisTrace() 
	                			|| isToolTipFollowerEnabled()) {
	                		repaint();
	                	}
//	                	isDirty = true;
	                }
	                if (seriesIndex >= 0) {
	                	Object[] listeners = getListeners(ChartMouseListener.class);
	                	if (getChart() != null) {
	                		XYChartMouseEvent event = new XYChartMouseEvent(getChart(), e, entity);
	                		event.setXY(getChartX(), getChartY());
	                		event.setSeriesIndex(seriesIndex);
	                		for (int i = listeners.length - 1; i >= 0; i -= 1) {
	                			((ChartMouseListener) listeners[i]).chartMouseMoved(event);
	                		}
	                	}
	                }

	            }
//	        	getChart().handleClick(x, y, getChartRenderingInfo());
	        }

	    super.mouseMoved(e);
	        // we can only generate events if the panel's chart is not null
	        // (see bug report 1556951)
	}
	
    @Override
	public void mousePressed(MouseEvent e) {
        int mods = e.getModifiers();
//        if (isMaskingEnabled() && (mods & maskingKeyMask) != 0) {
        if (isMaskingEnabled()) {
        	// Prepare masking service.
        	int cursorType = findSelectedMask(e.getX(), e.getY());
        	if (cursorType == Cursor.DEFAULT_CURSOR && (mods & maskingKeyMask) != 0) {
        		Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
        		if (screenDataArea != null) {
        			this.maskPoint = getPointInRectangle(e.getX(), e.getY(),
        					screenDataArea).getX();
        		} else {
        			this.maskPoint = Double.NaN;
        		}
        	} else {
        		if (cursorType == Cursor.MOVE_CURSOR){
//        			this.maskMovePoint = translateScreenToChart(
//        					translateScreenToJava2D(e.getPoint())).getX();
        			Insets insets = getInsets();
        			this.maskMovePoint = ChartMaskingUtilities.translateScreenX(
                    		(e.getX() - insets.left) / getScaleX(), 
                    		getScreenDataArea(), getChart());
        		}
        		setMaskDragIndicator(cursorType);
        	}
        }
        if (getMaskDragIndicator() == Cursor.DEFAULT_CURSOR){
        	super.mousePressed(e);
        }
	}
    
	@Override
	public void mouseDragged(MouseEvent e) {
        Insets insets = getInsets();
        int x = (int) ((e.getX() - insets.left) / getScaleX());
        int y = (int) ((e.getY() - insets.top) / getScaleY());

        ChartEntity entity = null;
        EntityCollection entities = null;
        if (getChartRenderingInfo() != null) {
            entities = getChartRenderingInfo().getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
//                boolean isDirty = false;
                int seriesIndex = -1;
                if (selectedSeriesIndex >= 0) {
//                	double xInChart = ChartMaskingUtilities.translateScreenX(x, 
//                			getScreenDataArea(), getChart());
//                	XYDataset dataset = getChart().getXYPlot().getDataset();
//                	PatternDataset patternDataset = (PatternDataset) dataset;
//                	int itemIndex = patternDataset.getItemFromX(selectedSeriesIndex, xInChart);
//                	if (itemIndex < patternDataset.getItemCount(selectedSeriesIndex)) {
//                		chartX = patternDataset.getXValue(selectedSeriesIndex, itemIndex);
//                		chartY = patternDataset.getYValue(selectedSeriesIndex, itemIndex);
//                		Point2D axisTrace = ChartMaskingUtilities.translateChartPoint(
//                				new Point2D.Double(chartX, chartY), getScreenDataArea(), getChart());
//                		horizontalTraceLocation = (int) axisTrace.getX();
//                		verticalTraceLocation = (int) axisTrace.getY();
//                		seriesIndex = selectedSeriesIndex;
//                		isDirty = true;
//                	}
                	seriesIndex = followDomainTrace(selectedSeriesIndex, x);
                } else if (getChart().getXYPlot().getSeriesCount() == 1){
//                	int seriesIndex0 = 0;
//                	double xInChart = ChartMaskingUtilities.translateScreenX(x, 
//                			getScreenDataArea(), getChart());
//                	XYDataset dataset = getChart().getXYPlot().getDataset();
//                	PatternDataset patternDataset = (PatternDataset) dataset;
//                	int itemIndex = patternDataset.getItemFromX(seriesIndex0, xInChart);
//                	if (itemIndex < patternDataset.getItemCount(seriesIndex0)) {
//                		chartX = patternDataset.getXValue(seriesIndex0, itemIndex);
//                		chartY = patternDataset.getYValue(seriesIndex0, itemIndex);
//                		Point2D axisTrace = ChartMaskingUtilities.translateChartPoint(
//                				new Point2D.Double(chartX, chartY), getScreenDataArea(), getChart());
//                		horizontalTraceLocation = (int) axisTrace.getX();
//                		verticalTraceLocation = (int) axisTrace.getY();
//                		seriesIndex = seriesIndex0;
//                		isDirty = true;
//                	}
                	seriesIndex = followDomainTrace(0, x);
                } else if (entity instanceof XYItemEntity) {
                	XYItemEntity xyEntity = (XYItemEntity) entity;
                	XYDataset dataset = xyEntity.getDataset();
                	int item = ((XYItemEntity) entity).getItem();
                	seriesIndex = xyEntity.getSeriesIndex();
                	double chartX = dataset.getXValue(seriesIndex, item);
                	double chartY = dataset.getYValue(seriesIndex, item);
                	Point2D screenPoint = ChartMaskingUtilities.translateChartPoint(
                			new Point2D.Double(chartX, chartY), getScreenDataArea(), getChart());
                	setChartX(chartX);
                	setChartY(chartY);
                	if (dataset instanceof IXYErrorDataset) {
	                	setChartError(((IXYErrorDataset) dataset).getYError(seriesIndex, item));
	                	
                	}
                	if (getHorizontalAxisTrace()) {
                		setHorizontalTraceLocation((int) screenPoint.getX());
                	}
                	if (getVerticalAxisTrace()) {
                		setVerticalTraceLocation((int) screenPoint.getY());
                	}
//                	isDirty = true;
                }
                if (seriesIndex >= 0) {
                	Object[] listeners = getListeners(ChartMouseListener.class);
                	if (getChart() != null) {
                		XYChartMouseEvent event = new XYChartMouseEvent(getChart(), e, entity);
                		event.setXY(getChartX(), getChartY());
                		event.setSeriesIndex(seriesIndex);
                		for (int i = listeners.length - 1; i >= 0; i -= 1) {
                			((ChartMouseListener) listeners[i]).chartMouseMoved(event);
                		}
                	}
                }

            }
        }
        
//        if (isMaskingEnabled() && (e.getModifiers() & maskingKeyMask) != 0) {
        if (isMaskingEnabled() && (e.getModifiers() & maskingKeyMask) == 0) {
        	int cursorType = findSelectedMask(e.getX(), e.getY());
        	setCursor(Cursor.getPredefinedCursor(cursorType));
        } else if (getCursor() != defaultCursor) {
        	setCursor(defaultCursor);
        }
        
        if (getMaskDragIndicator() != Cursor.DEFAULT_CURSOR && this.getSelectedMask() != null
        		&& (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        	changeSelectedMask(e);
        } else if (isMaskingEnabled() && (e.getModifiers() & maskingKeyMask) != 0) {
        	// Do masking service
        	// if no initial masking point was set, ignore dragging...
        	makeNewMask(e);
        } else {
        	super.mouseDragged(e);
        }
	}
	
	private void changeSelectedMask(MouseEvent e) {
		// TODO Auto-generated method stub
		Point2D screenPoint = translateScreenToJava2D(e.getPoint());
    	//TODO: resize the mask
		Rectangle2D screenArea = getScreenDataArea();
		if (screenArea.contains(screenPoint)) {
//    			Point2D chartPoint = translateScreenToChart(screenPoint);

			changeSelectedMask(ChartMaskingUtilities.translateScreenX(
					screenPoint.getX(), getScreenDataArea(), getChart()));
			repaint();
		}
	}

	private void makeNewMask(MouseEvent e) {
		Point2D screenPoint = translateScreenToJava2D(e.getPoint());
    	if (Double.isNaN(maskPoint) || Math.abs(screenPoint.getX() - maskPoint) < minMaskWidth) {
    		return;
    	}
    	Graphics2D g2 = (Graphics2D) getGraphics();

    	// erase the previous zoom rectangle (if any).  We only need to do
    	// this is we are using XOR mode, which we do when we're not using
    	// the buffer (if there is a buffer, then at the end of this method we
    	// just trigger a repaint)
    	if (!isDoubleBuffered()) {
//    		drawZoomRectangle(g2, true);
    		ChartMaskingUtilities.drawMasks(g2, getScreenDataArea(), getMaskMap(), 
    				getSelectedMask(), getChart());
    	}

//    	boolean hZoom = false;
//    	boolean vZoom = false;
//    	if (this.orientation == PlotOrientation.HORIZONTAL) {
//    		hZoom = this.rangeZoomable;
//    		vZoom = this.domainZoomable;
//    	}
//    	else {
//    		hZoom = this.domainZoomable;
//    		vZoom = this.rangeZoomable;
//    	}
    	Rectangle2D scaledDataArea = getScreenDataArea();
//    			(int) this.maskPoint.getX(), (int) this.maskPoint.getY());
    	// Working on the current mask. Only create one new mask per drag-drawing.
    	if (currentMaskRectangle == null) {
        	boolean isInclusive = (e.getModifiers() & maskingExclusiveMask) == 0;
        	currentMaskRectangle = new RangeMask(isInclusive);
//        	currentMaskRectangle.setFillColor(getNextMaskColor(isInclusive));
//        	getMasks().add(currentMaskRectangle);
        	addMask(currentMaskRectangle);
    	}
    	// selected rectangle shouldn't extend outside the data area...
    	double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
    	// Update the current mask.
    	Insets insets = getInsets();
        double startX = ChartMaskingUtilities.translateScreenX(
        		(maskPoint - insets.left) / getScaleX(), 
        		getScreenDataArea(), getChart());
        double endX = ChartMaskingUtilities.translateScreenX(
        		(xmax - insets.left) / getScaleX(), 
        		getScreenDataArea(), getChart());
        updateCurrentDomainMask(startX, endX);
    	// Draw the new zoom rectangle...
    	if (isDoubleBuffered()) {
    		repaint();
    	}
    	else {
    		// with no buffer, we use XOR to draw the rectangle "over" the
    		// chart...
    		ChartMaskingUtilities.drawMasks(g2, getScreenDataArea(), getMaskMap(), 
    				getSelectedMask(), getChart());
    	}
    	g2.dispose();		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (currentMaskRectangle != null) {
        	// reset masking service.
        	maskPoint = Double.NaN;
        	currentMaskRectangle = null;
        } else {
        	super.mouseReleased(e);
        }
		setMaskDragIndicator(Cursor.DEFAULT_CURSOR);
        this.maskMovePoint = Double.NaN;
	}
	
	public void selectSeries(int seriesIndex) {
		if (selectedSeriesIndex != seriesIndex && seriesIndex >= 0) {
			XYItemRenderer renderer = getChart().getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				if (selectedSeriesIndex >= 0) {
					renderer.setSeriesStroke(selectedSeriesIndex, DEFAULT_STROCK);
				}
				renderer.setSeriesStroke(seriesIndex, BOLD_STROCK);
				selectedSeriesIndex = seriesIndex;
			}
		} else if (seriesIndex < 0 && selectedSeriesIndex >= 0) {
       		XYItemRenderer renderer = getChart().getXYPlot().getRenderer();
    		if (renderer instanceof XYLineAndShapeRenderer) {
    			renderer.setSeriesStroke(selectedSeriesIndex, DEFAULT_STROCK);
    			selectedSeriesIndex = -1;
    		}
		}
	}
	
	public void selectSeries(String key) {
		if (key == null) {
			selectSeries(-1);
			return;
		}
		XYDataset dataset = getChart().getXYPlot().getDataset();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			if (dataset.getSeriesKey(i).equals(key)) {
				selectSeries(i);
				break;
			}
		}
	}

	public int getSelectedCurveIndex() {
		if (selectedSeriesIndex >= 0) {
			XYDataset dataset = getChart().getXYPlot().getDataset();
			if (dataset.getSeriesCount()> selectedSeriesIndex){
				return selectedSeriesIndex;
			}
		}
		return -1;
	}
	
	protected int findSelectedMask(int x, int y) {
		if (getSelectedMask() != null && !getSelectedMask().isEmpty()) {
			Rectangle2D screenArea = getScreenDataArea();
			Rectangle2D maskArea = ChartMaskingUtilities.getDomainMaskFrame(getSelectedMask(), 
					getScreenDataArea(), getChart());
			Rectangle2D intersect = screenArea.createIntersection(maskArea);
			Point2D point = new Point2D.Double(x, y);
			double minX = maskArea.getMinX();
			double maxX = maskArea.getMaxX();
			double minY = maskArea.getMinY();
			double width = maskArea.getWidth();
			double height = maskArea.getHeight();
			if (!intersect.isEmpty() && screenArea.contains(point)) {
				if (width > 8) {
					Rectangle2D center = new Rectangle2D.Double(minX + 4, minY, 
							width - 8, height);
					if (screenArea.createIntersection(center).contains(point)) {
						return Cursor.MOVE_CURSOR;
					}
				}
				Rectangle2D west = new Rectangle2D.Double(minX - 4, minY, 
						width < 8 ? width / 2 + 4 : 8, height);
				if (screenArea.createIntersection(west).contains(point)) {
					return Cursor.W_RESIZE_CURSOR;
				}
				Rectangle2D east = new Rectangle2D.Double(maxX - (width < 8 ? width / 2 : 4), 
						minY, width < 8 ? width / 2 + 4 : 8, height);
				if (screenArea.createIntersection(east).contains(point)) {
					return Cursor.E_RESIZE_CURSOR;
				}
			}
		}
		return Cursor.DEFAULT_CURSOR;
	}
	
    private void changeSelectedMask(double point) {
    	switch (getMaskDragIndicator()) {
		case Cursor.MOVE_CURSOR:
			moveMask(point);
			break;
		case Cursor.W_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getDomainAxis().isInverted()) {
				changeMaskXMax(point);
			} else {
				changeMaskXMin(point);
			}
			break;
		case Cursor.E_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getDomainAxis().isInverted()) {
				changeMaskXMin(point);
			} else {
				changeMaskXMax(point);
			}
			break;
		default:
			break;
		}
	}
    
    private void moveMask(double point) {
		if (maskMovePoint != Double.NaN && getSelectedMask() != null) {
			Range range = getSelectedMask().getRange();
			getSelectedMask().setBoundary(range.getLowerBound() + point - maskMovePoint, 
					range.getUpperBound() + point - maskMovePoint);
			maskMovePoint = point;
			fireMaskUpdateEvent(getSelectedMask());
		}
	}
    
	private void changeMaskXMax(double x) {
		Range range = getSelectedMask().getRange();
		getSelectedMask().setBoundary(Math.min(range.getLowerBound(), x), 
				Math.max(range.getLowerBound(), x));
		fireMaskUpdateEvent(getSelectedMask());
	}
	
	private void changeMaskXMin(double x) {
		Range range = getSelectedMask().getRange();
		getSelectedMask().setBoundary(Math.min(range.getUpperBound(), x), 
				Math.max(range.getUpperBound(), x));
		fireMaskUpdateEvent(getSelectedMask());
	}
	
	private void updateCurrentDomainMask(double startX, double endX) {
		if (currentMaskRectangle != null) {
			currentMaskRectangle.setMin(Math.min(startX, endX));
			currentMaskRectangle.setMax(Math.max(startX, endX));
			fireMaskUpdateEvent(currentMaskRectangle);
		}
	}
	
	public void selectMask(double chartX, double chartY) {
		if (Double.isNaN(chartX)) {
			setSelectedMask(null);
			return;
		}
		if (getSelectedMask() == null) {
			for (AbstractMask mask : getMasks()) {
				if (mask instanceof RangeMask) {
				if (((RangeMask) mask).getRange().contains(chartX)) {
					setSelectedMask(mask);
					break;
				}
				}
			}
		} else {
			RangeMask newSelection = null;
			if (getMasks().contains(getSelectedMask())) {
				int index = getMasks().indexOf(getSelectedMask());
				for (int i = index + 1; i < getMasks().size(); i++) {
					RangeMask mask = (RangeMask) getMasks().get(i);
					if (mask.getRange().contains(chartX)) {
						newSelection = mask;
						break;
					}
				}
				if (newSelection == null) {
					for (int i = 0; i < index; i++) {
						RangeMask mask = (RangeMask) getMasks().get(i);
						if (mask.getRange().contains(chartX)) {
							newSelection = mask;
							break;
						}
					}
				}
				setSelectedMask(newSelection);
			} else {
				setSelectedMask(null);
				selectMask(chartX, chartY);
			}
		}
//		if (getSelectedMask() != null)
//			System.out.println("selected mask: x[" + getSelectedMask().getMin() + ", " 
//			                                     + getSelectedMask().getMax() + "]");
	}
	
 
    private int followDomainTrace(int seriesIndex, int domainLocation) {
    	if (seriesIndex >= getDataset().getSeriesCount()) {
    		return -1;
    	}
    	double xInChart = ChartMaskingUtilities.translateScreenX(domainLocation, 
    			getScreenDataArea(), getChart());
    	XYDataset dataset = getChart().getXYPlot().getDataset();
    	IXYErrorDataset patternDataset = (IXYErrorDataset) dataset;
    	int itemIndex = patternDataset.getItemFromX(seriesIndex, xInChart);
    	if (itemIndex >= 0 && itemIndex < patternDataset.getItemCount(seriesIndex)) {
    		double chartX = patternDataset.getXValue(seriesIndex, itemIndex);
    		double chartY = patternDataset.getYValue(seriesIndex, itemIndex);
    		Point2D axisTrace = ChartMaskingUtilities.translateChartPoint(
    				new Point2D.Double(chartX, chartY), getScreenDataArea(), getChart());
    		setChartX(chartX);
    		setChartY(chartY);
    		if (dataset instanceof IXYErrorDataset) {
            	setChartError(((IXYErrorDataset) dataset).getYError(seriesIndex, itemIndex));
            	
        	}
    		setHorizontalTraceLocation((int) axisTrace.getX());
    		setVerticalTraceLocation((int) axisTrace.getY());
    		if (getHorizontalAxisTrace() || getVerticalAxisTrace() 
    				|| isToolTipFollowerEnabled()) {
    			repaint();
    		}
    		return seriesIndex;
    	}
    	return -1;
    }
    
	@Override
    public void moveSelectedMask(int direction) {
		if (getSelectedMask() == null) {
			return;
		}
		Range range = getSelectedMask().getRange();
		switch (direction) {
		case SWT.ARROW_LEFT:
			getSelectedMask().setRange(Range.shift(range, - getBinWidth()));
			fireMaskUpdateEvent(getSelectedMask());
			break;
		case SWT.ARROW_RIGHT:
			getSelectedMask().setRange(Range.shift(range, getBinWidth()));
			fireMaskUpdateEvent(getSelectedMask());
			break;
		default:
			break;
		}
		repaint();
	}

	private double getBinWidth() {
		double binWidth = 0;
		try {
			ValueAxis axis = getChart().getXYPlot().getDomainAxis();
			double max = axis.getUpperBound();
			double min = axis.getLowerBound();
			binWidth = (max - min) / StaticValues.DOMAIN_MASK_SHIFT_RESOLUTION;
			if (axis.isInverted()) {
				binWidth = - binWidth;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return binWidth;
	}

	@Override
	protected Color getAxisTraceColor() {
		return axisTraceColor;
	}

	@Override
	public RangeMask getSelectedMask() {
		return (RangeMask) super.getSelectedMask();
	}

	private int getCurveIndex(IXYErrorSeries pattern) {
		XYDataset dataset = getDataset();
		if (dataset instanceof IXYErrorDataset) {
			return ((IXYErrorDataset) dataset).indexOf(pattern);
		}
		return -1;
	}
	
	@Override
	public Color getCurveColor(IXYErrorSeries pattern) {
		int index = getCurveIndex(pattern);
		System.err.println("the curve index is " + index);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				return (Color) ((XYLineAndShapeRenderer) renderer).getSeriesPaint(index); 
			}
		}
		return null;
	}

	@Override
	public boolean isCurveMarkerFilled(IXYErrorSeries pattern) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				return ((XYLineAndShapeRenderer) renderer).getSeriesShapesFilled(index); 
			}
		}
		return false;
	}

	@Override
	public Shape getCurveMarkerShape(IXYErrorSeries pattern) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				return ((XYLineAndShapeRenderer) renderer).getSeriesShape(index); 
			}
		}
		return null;
	}

	@Override
	public Stroke getCurveStroke(IXYErrorSeries pattern) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				return ((XYLineAndShapeRenderer) renderer).getSeriesStroke(index); 
			}
		}
		return null;
	}

	@Override
	public boolean isCurveVisible(IXYErrorSeries pattern) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				return ((XYLineAndShapeRenderer) renderer).isSeriesVisible(index); 
			}
		}
		return false;
	}

	@Override
	public boolean isErrorBarEnabled() {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYErrorRenderer) {
			return ((XYErrorRenderer) renderer).getDrawYError();
		}
		return false;
	}

	@Override
	public boolean isLogarithmXEnabled() {
		ValueAxis axis = getXYPlot().getDomainAxis();
		if (axis instanceof LogarithmizableAxis) {
			return ((LogarithmizableAxis) axis).isLogarithmic();
		}
		return false;
	}

	@Override
	public boolean isLogarithmYEnabled() {
		ValueAxis axis = getXYPlot().getRangeAxis();
		if (axis instanceof LogarithmizableAxis) {
			return ((LogarithmizableAxis) axis).isLogarithmic();
		}
		return false;
	}

	@Override
	public boolean isMarkerEnabled() {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYLineAndShapeRenderer) {
			return ((XYLineAndShapeRenderer) renderer).getBaseShapesVisible(); 
		}
		return false;
	}

	@Override
	public void setCurveColor(IXYErrorSeries pattern, Color color) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				((XYLineAndShapeRenderer) renderer).setSeriesPaint(index, color); 
			}
		}
	}

	@Override
	public void setCurveMarkerFilled(IXYErrorSeries pattern, boolean filled) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				((XYLineAndShapeRenderer) renderer).setSeriesShapesFilled(index, filled); 
			}
		}
	}

	@Override
	public void setCurveMarkerShape(IXYErrorSeries pattern, MarkerShape shape) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				((XYLineAndShapeRenderer) renderer).setSeriesShape(index, shape.getShape()); 
			}
		}
	}

	@Override
	public void setCurveMarkerVisible(IXYErrorSeries pattern, boolean isMarkerVisible) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(index, 
						isMarkerVisible); 
			}
		}
	}
	
	@Override
	public void setCurveStroke(IXYErrorSeries pattern, float stroke) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				((XYLineAndShapeRenderer) renderer).setSeriesStroke(index, 
						new BasicStroke(stroke)); 
			}
		}
	}

	@Override
	public void setCurveVisible(IXYErrorSeries pattern, boolean visible) {
		int index = getCurveIndex(pattern);
		if (index >= 0) {
			XYItemRenderer renderer = getXYPlot().getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				((XYLineAndShapeRenderer) renderer).setSeriesVisible(index, visible); 
			}
		}
	}

	@Override
	public void setErrorBarEnabled(boolean enabled) {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYErrorRenderer) {
			((XYErrorRenderer) renderer).setDrawYError(enabled);
		}
	}

	@Override
	public void setLogarithmXEnabled(boolean enabled) {
		ValueAxis axis = getXYPlot().getDomainAxis();
		if (axis instanceof LogarithmizableAxis) {
			((LogarithmizableAxis) axis).setLogarithmic(enabled);
			axis.setAutoRange(true);
		}
	}

	@Override
	public void setLogarithmYEnabled(boolean enabled) {
		ValueAxis axis = getXYPlot().getRangeAxis();
		if (axis instanceof LogarithmizableAxis) {
			((LogarithmizableAxis) axis).setLogarithmic(enabled);
			axis.setAutoRange(true);
		}
	}

	@Override
	public void setMarkerEnabled(boolean enabled) {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYLineAndShapeRenderer) {
			((XYLineAndShapeRenderer) renderer).setBaseShapesVisible(enabled);
			for (int i = 0; i < getXYPlot().getSeriesCount(); i ++) {
				((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(i, enabled);
			}
		}
	}

	@Override
	public Stroke getErrorBarStroke() {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYErrorRenderer) {
			return ((XYErrorRenderer) renderer).getErrorStroke();
		}
		return null;
	}

	@Override
	public void setErrorBarStroke(float stroke) {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYErrorRenderer) {
			((XYErrorRenderer) renderer).setErrorStroke(new BasicStroke(stroke));
		}
	}
	
	@Override
    protected void drawToolTipFollower(Graphics2D g2, int x, int y) {
    	Rectangle2D dataArea = getScreenDataArea();
    	if (((int) dataArea.getMinX() <= x) && (x <= (int) dataArea.getMaxX()) && 
    			((int) dataArea.getMinY() <= y) && (y <= (int) dataArea.getMaxY())) {
    		String text = String.format("(%.2f, %.2f", getChartX(), 
    				getChartY());
    		boolean isErrorEnabled = false;
    		XYItemRenderer renderer = getXYPlot().getRenderer();
    		if (renderer instanceof XYErrorRenderer) {
    			isErrorEnabled = ((XYErrorRenderer) 
    					renderer).getDrawYError();
    		}
    		if (isErrorEnabled) {
    			text += String.format(" \u00B1%.2f", getChartError());
    		}
    		text += ")";
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
    		
    		g2.setColor(Color.lightGray);
    		g2.fill(toolTipArea);
    		g2.setColor(Color.black);
    		g2.drawString(text, xLoc + 3, yLoc + 11);
    	}
    }

	@Override
	public void setSelectedSeries(int seriesIndex) {
		selectSeries(seriesIndex);
	}

	/**
	 * @param chartError the chartError to set
	 */
	public void setChartError(double chartError) {
		this.chartError = chartError;
	}

	/**
	 * @return the chartError
	 */
	public double getChartError() {
		return chartError;
	}

	@Override
	public void addMask(AbstractMask mask) {
		if (mask instanceof RangeMask) {
			super.addMask(mask);
		}
	}
	
	@Override
	public void doHelp() {
		showPropertyEditor(6);
	}

	@Override
    public void doExport(IExporter exporter) throws IOException {
		boolean isMultipleSeries = getDataset().getSeriesCount() > 1;
        JFileChooser fileChooser = new JFileChooser();
        String currentDirectory = System.getProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL);
        if (currentDirectory != null) {
        	File savePath = new File(currentDirectory);
        	if (savePath.exists() && savePath.isDirectory()) {
        		fileChooser.setCurrentDirectory(savePath);
        	}
        }
    	String fileExtension = exporter.getExtensionName();
        ExtensionFileFilter extensionFilter = new ExtensionFileFilter(exporter.toString(), "." + fileExtension);
        fileChooser.addChoosableFileFilter(extensionFilter);
        if (isMultipleSeries) {
        	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
        	String filename = fileChooser.getSelectedFile().getPath();
    		int confirm = JOptionPane.YES_OPTION;
    		File selectedFile;
        	if (isMultipleSeries) {
        		selectedFile = new File(filename);
        		if (!selectedFile.exists()) {
        			selectedFile.mkdirs();
        		}
            	exporter.export(selectedFile, getDataset());
            	System.setProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL, 
            				fileChooser.getSelectedFile().getAbsolutePath());
        	} else {
        		if (!filename.toLowerCase().endsWith("." + fileExtension)) {
        			filename = filename + "." + fileExtension;
        		}
        		selectedFile = new File(filename);
        		if (selectedFile.exists()) {
        			confirm = JOptionPane.showConfirmDialog(this, selectedFile.getName() + " exists, overwrite?", 
        					"Confirm Overwriting", JOptionPane.YES_NO_OPTION);
        		} else {
        			selectedFile.createNewFile();
        		}
            	if (confirm == JOptionPane.YES_OPTION) {
            		exporter.export(selectedFile, getDataset());
            		System.setProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL, 
            				fileChooser.getSelectedFile().getParent());
            	}
        	}
        }
    }

	@Override
	public void setLogarithmEnabled(boolean enabled) {
		setLogarithmYEnabled(enabled);
	}

	@Override
	public boolean isLogarithmEnabled() {
		return isLogarithmYEnabled();
	}
	
	@Override
	public IHelpProvider getHelpProvider() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void saveAsText(BufferedWriter writer) throws IOException{
		IDataset dataset = getDataset();
		if (dataset != null) {
			DatasetUtils.export((IXYErrorDataset) dataset, writer, ExportFormat.XYSIGMA);
		}
	}

}
