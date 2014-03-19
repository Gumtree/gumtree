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
package org.gumtree.vis.awt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.ChartMaskingUtilities;
import org.gumtree.vis.mask.ChartTransferableWithMask;
import org.gumtree.vis.mask.IMaskEventListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ExposedMouseWheelHandler;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisUtilities;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ExtensionFileFilter;

/**
 * @author nxi
 *
 */
public abstract class JChartPanel extends ChartPanel implements IPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4383623034527952722L;
    public static final String SELECT_MASK_COMMAND = "SELECT_MASK";
    public static final String DESELECT_MASK_COMMAND = "SELECT_NONE";
	private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    protected static final Cursor defaultCursor = Cursor.getPredefinedCursor(
    		Cursor.DEFAULT_CURSOR);
    public static final String REMOVE_SELECTED_MASK_COMMAND = "REMOVE_SELECTED_MASK";
	public static final String ADD_INTERNAL_MARKER_COMMAND = "ADD_INTERNAL_MARKER";
	public static final String ADD_HORIZONTAL_BAR_COMMAND = "ADD_HORIZONTAL_BAR";
	public static final String ADD_VERTICAL_BAR_COMMAND = "ADD_VERTICAL_BAR";
	public static final String REMOVE_SELECTED_MARKER_COMMAND = "REMOVE_SELECTED_MARKER";
	public static final String CLEAR_ALL_MARKERS_COMMAND = "CLEAR_ALL_MARKERS";
	public static final String CLEAR_INTERNAL_MARKERS_COMMAND = "CLEAR_INTERNAL_MARKERS";
	public static final String CLEAR_DOMAIN_MARKERS_COMMAND = "CLEAR_DOMAIN_MARKERS";
	public static final String CLEAR_RANGE_MARKERS_COMMAND = "CLEAR_RANGE_MARKERS";
    protected static int maskingKeyMask = InputEvent.SHIFT_MASK;
    protected static int maskingExclusiveMask = InputEvent.ALT_MASK;
    protected static int maskingSelectionMask = InputEvent.SHIFT_MASK;
    private boolean autoUpdate = true;

    private boolean isMouseWheelEnabled = true;
    protected Color[] inclusiveMaskColor;
    protected Color[] exclusiveMaskColor;
    private int horizontalTraceLocation;
	private int verticalTraceLocation;
    private boolean isToolTipFollowerEnabled = true;
    private boolean isMaskingEnabled = true;
    private boolean isTextInputEnabled = false;
    private boolean textInputFlag = false;
    private Point2D textInputPoint;
    private String textInputContent;
    private int textInputCursorIndex;
    private Point2D textMovePoint;

	private double chartX;
    private double chartY;
    private int maskDragIndicator = Cursor.DEFAULT_CURSOR;

    //Popup Menu
    private JMenu maskManagementMenu;
    private JMenuItem removeSelectedMaskMenuItem;
    private JMenu markerManagementMenu;
    private JMenuItem internalMarkerMenuItem;
    private JMenuItem horizontalBarMenuItem;
    private JMenuItem verticalBarMenuItem;
    private JMenuItem removeSelectedMarkerMenuItem;
    private JMenuItem clearMarkersMenuItem;
    private JMenuItem clearRangeMarkersMenuItem;
    private JMenuItem clearDomainMarkersMenuItem;
    private JMenuItem clearAllMarkersMenuItem;
    private LinkedHashMap<AbstractMask, Color> maskList;
    private AbstractMask selectedMask;
    private List<IMaskEventListener> maskEventListeners = new ArrayList<IMaskEventListener>();
	private boolean isShapeEnabled = true;
	private boolean isShapeValid = true;
    private LinkedHashMap<Line2D, Color> domainMarkerMap;
    private LinkedHashMap<Line2D, Color> rangeMarkerMap;
    private LinkedHashMap<Line2D, Color> markerMap;
	private LinkedHashMap<Shape, Color> shapeMap;
	private LinkedHashMap<Rectangle2D, String> textContentMap;
	private Rectangle2D selectedTextWrapper;
	private Point2D mouseRightClickLocation;
	private Line2D selectedMarker;
	
    
    
    /**
	 * @param chart
	 */
	public JChartPanel(JFreeChart chart) {
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
	public JChartPanel(JFreeChart chart, boolean useBuffer) {
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
	public JChartPanel(JFreeChart chart, boolean properties, boolean save,
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
	public JChartPanel(JFreeChart chart, int width, int height,
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
	public JChartPanel(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean copy, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties,
				copy, save, print, zoom, tooltips);
		maskList = new LinkedHashMap<AbstractMask, Color>();
		shapeMap = new LinkedHashMap<Shape, Color>();
		domainMarkerMap = new LinkedHashMap<Line2D, Color>();
		rangeMarkerMap = new LinkedHashMap<Line2D, Color>();
		markerMap = new LinkedHashMap<Line2D, Color>();
		textContentMap = new LinkedHashMap<Rectangle2D, String>();
		addMouseWheelListener(new ExposedMouseWheelHandler(this));
	}

	@Override
	public void processMouseWheelEvent(MouseWheelEvent event) {
		if (isMouseWheelEnabled) {
			for (MouseWheelListener listener: getMouseWheelListeners()) {
				listener.mouseWheelMoved(event);
			}
		}
	}
	
	@Override
	public void setMouseWheelEnabled(boolean flag) {
		isMouseWheelEnabled = flag;
	}
	
	@Override
	public boolean isMouseWheelEnabled() {
		return isMouseWheelEnabled;
	}

	public abstract void moveSelectedMask(int keyCode);

	public void setDataset(IDataset dataset) {
		getXYPlot().setDataset(dataset);
	}

	public ValueAxis getHorizontalAxis() {
		return getChart().getXYPlot().getDomainAxis();
	}

	public XYPlot getXYPlot(){
		return getChart().getXYPlot();
	}
	
	public TextTitle getTitle() {
		return getChart().getTitle();
	}

	public ValueAxis getVerticalAxis() {
		return getXYPlot().getRangeAxis();
	}

	public void setHorizontalAxisFlipped(boolean isFlipped) {
		getXYPlot().getDomainAxis().setInverted(isFlipped);
	}

	public void setVerticalAxisFlipped(boolean isFlipped) {
		getXYPlot().getRangeAxis().setInverted(isFlipped);
	}

	public void restoreHorizontalBounds() {
		restoreAutoDomainBounds();
	}

	public void restoreVerticalBounds() {
		restoreAutoRangeBounds();
	}

	public void zoomInHorizontal(double x, double y) {
		zoomInDomain(x, y);
	}

	public void zoomInVertical(double x, double y) {
		zoomInRange(x, y);
	}

	public void zoomOutHorizontal(double x, double y) {
		zoomOutDomain(x, y);
	}

	public void zoomOutVertical(double x, double y) {
		zoomOutRange(x, y);
	}
	
	@Override
	public IDataset getDataset() {
		return (IDataset) getXYPlot().getDataset();
	}

//	@Override
	public void setBackgroundColor(Color color) {
		getChart().setBackgroundPaint(color);
		setBackground(color);
	}

	@Override
	public void setHorizontalZoomable(boolean isZoomable) {
		setDomainZoomable(isZoomable);
	}

	@Override
	public void setVerticalZoomable(boolean isZoomable) {
		setRangeZoomable(isZoomable);
	}

    protected void createMaskColors(boolean inverted) {
    	int numberOfMaskColors = StaticValues.NUMBER_OF_MASK_COLORS;
    	inclusiveMaskColor = new Color[numberOfMaskColors];
    	exclusiveMaskColor = new Color[numberOfMaskColors];
    	int interval = 155 / numberOfMaskColors;
    	for (int i = 0; i < numberOfMaskColors; i++) {
    		int value = 255 - i * interval;
    		if (inverted) {
        		inclusiveMaskColor[i] = new Color(0, value, 0, 75);
    			exclusiveMaskColor[i] = new Color(value, value, value, 75);
    		} else {
        		inclusiveMaskColor[i] = new Color(0, value, 0, 30);
    			exclusiveMaskColor[i] = new Color(0, 0, value, 30);
    		}
    	}
	}
    
	protected Color getNextMaskColor(boolean isInclusive){
    	Color[] colorSeries = isInclusive ? inclusiveMaskColor : exclusiveMaskColor;
    	for (int i = 0; i < StaticValues.NUMBER_OF_MASK_COLORS; i++) {
    		boolean isUsed = false;
    		for (AbstractMask mask : getMasks()) {
    			if (colorSeries[i].equals(maskList.get(mask))) {
    				isUsed = true;
    				break;
    			}
    		}
    		if (!isUsed) {
    			return colorSeries[i];
    		}
    	}
    	Color lastColor = null;
    	for (int i = getMasks().size() - 1; i >= 0; i--) {
    		AbstractMask mask = getMasks().get(i);
    		if (mask.isInclusive() == isInclusive) {
    			lastColor = maskList.get(getMasks().get(i));
    			break;
    		}
    	}
    	int nextColorIndex = 0;
    	for (int i = 0; i < StaticValues.NUMBER_OF_MASK_COLORS; i++) {
    		if (colorSeries[i].equals(lastColor)) {
    			nextColorIndex = i + 1;
    			if (nextColorIndex >= StaticValues.NUMBER_OF_MASK_COLORS) {
    				nextColorIndex = 0;
    			}
    		}
    	}
    	return colorSeries[nextColorIndex];
    }
    
	protected void addMaskMenu(int x, int y) {
	       if (this.removeSelectedMaskMenuItem != null) {
	        	boolean isRemoveMenuEnabled = false;
	        	if (this.selectedMask != null) {
	        		Rectangle2D screenMask = ChartMaskingUtilities.getMaskFramework(
	        				selectedMask, getScreenDataArea(), getChart());
	        		if (screenMask.contains(x, y)) {
	        			isRemoveMenuEnabled = true;
	        		}
	        	}
	        	this.removeSelectedMaskMenuItem.setEnabled(isRemoveMenuEnabled);
	        	if (isRemoveMenuEnabled) {
	        		removeSelectedMaskMenuItem.setVisible(true);
	        		removeSelectedMaskMenuItem.setText("Remove " + selectedMask.getName());
	        	} else {
	        		//        		removeSelectedMaskMenuItem.setText("Mask Management");
	        		removeSelectedMaskMenuItem.setVisible(false);
	        	}
	        }
	        maskManagementMenu.removeAll();
	        if (maskList.size() > 0) {
	        	maskManagementMenu.setEnabled(true);
	        	JMenuItem selectNoneMaskItem = new JRadioButtonMenuItem();
	        	selectNoneMaskItem.setText("Select None");
	        	selectNoneMaskItem.setActionCommand(DESELECT_MASK_COMMAND);
	        	selectNoneMaskItem.addActionListener(this);
	        	maskManagementMenu.add(selectNoneMaskItem);
	        	boolean isInShade = false;
	        	for (AbstractMask mask : maskList.keySet()) {
	        		Rectangle2D screenMask = ChartMaskingUtilities.getMaskFramework(
	        				mask, getScreenDataArea(), getChart());
	        		if (screenMask.contains(x, y)) {
	        			JMenuItem selectMaskItem = new JRadioButtonMenuItem();
	        			selectMaskItem.setText("Select " + mask.getName());
	        			selectMaskItem.setActionCommand(SELECT_MASK_COMMAND 
	        					+ "-" + mask.getName());
	        			if (mask == selectedMask) {
	        				selectMaskItem.setSelected(true);
	        			}
	        			selectMaskItem.addActionListener(this);
	        			maskManagementMenu.add(selectMaskItem);
	        			isInShade = true;
	        		}
	        	}
	        	if (isInShade) {
	        		if (selectedMask == null) {
	        			selectNoneMaskItem.setSelected(true);
	        		}
	        	} else {
	        		for (AbstractMask mask : getMasks()) {
	        			JMenuItem selectMaskItem = new JRadioButtonMenuItem();
	        			selectMaskItem.setText("Select " + mask.getName());
	        			selectMaskItem.setActionCommand(SELECT_MASK_COMMAND 
	        					+ "-" + mask.getName());
	        			if (mask == selectedMask) {
	        				selectMaskItem.setSelected(true);
	        			}
	        			selectMaskItem.addActionListener(this);
	        			maskManagementMenu.add(selectMaskItem);
	        		}
	        		selectNoneMaskItem.setSelected(selectedMask == null);
	        	}
	        } else {
	        	maskManagementMenu.setEnabled(false);
	        }
	}

	@Override
	public void paintComponent(Graphics g) {
//		long time = System.currentTimeMillis();
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
//		ChartMaskingUtilities.drawDomainMask(g2, getScreenDataArea(), maskList, 
//				selectedMask, getChart());
		if (isMaskingEnabled) {
			ChartMaskingUtilities.drawMasks(g2, getScreenDataArea(), maskList, 
					selectedMask, getChart());
		}
		if (isShapeEnabled()) {
			drawShapes(g2);
		}
		if (selectedMarker != null) {
			drawSelectedMarker(g2);
		}
		if (getHorizontalAxisTrace()) {
			drawHorizontalAxisTrace(g2, horizontalTraceLocation);
		}
		if (getVerticalAxisTrace()) {
			drawVerticalAxisTrace(g2, verticalTraceLocation);
		}
		if (isToolTipFollowerEnabled) {
			drawToolTipFollower(g2, horizontalTraceLocation, verticalTraceLocation);
		}
//		if (isTextInputEnabled && textInputFlag && textInputPoint != null){
		drawTextInputBox(g2);
//		}
//		long diff = System.currentTimeMillis() - time;
//		if (diff > 100) {
//			System.out.println("refreshing cost: " + diff);
//		}
	}

	private void drawTextInputBox(Graphics2D g2) {
		if (textInputFlag && textInputPoint != null) {
//			g2.drawChars("Input Text Here".toCharArray(), 1, 60, (int) textInputPoint.getX(), (int) textInputPoint.getY());
			Color oldColor = g2.getColor();
			g2.setColor(Color.BLACK);
			String inputText = textInputContent == null ? "" : textInputContent;
			FontMetrics fm = g2.getFontMetrics();
//			int sWidth;
//			if (textInputCursorIndex == 0 || inputText.length() == 0) {
//				sWidth = 0;
//			} else if (textInputCursorIndex < inputText.length()){
//				sWidth = fm.stringWidth(inputText.substring(0, textInputCursorIndex));
//			} else {
//				sWidth = fm.stringWidth(inputText);
//			}
			
			String[] lines = inputText.split("\n", 100);
			int cursorY = 0;
			int cursorX = 0;
			int charCount = 0;
			int maxWidth = 0;
			int maxHeight = 0;
			for (int i = 0; i < lines.length; i++) {
				g2.drawString(lines[i], (int) textInputPoint.getX() + 3, (int) textInputPoint.getY() - 3 + i * 15);
//				charCount += lines[i].length() + 1;
				if (textInputCursorIndex > charCount && textInputCursorIndex < charCount + lines[i].length() + 1) {
					cursorY = i;
					cursorX = fm.stringWidth(lines[i].substring(0, textInputCursorIndex - charCount));
				} else if (textInputCursorIndex == charCount + lines[i].length() + 1) {
					cursorY = i + 1;
					cursorX = 0;
				}
				charCount += lines[i].length() + 1;
				int lineWidth = fm.stringWidth(lines[i]);
				if (lineWidth > maxWidth) {
					maxWidth = lineWidth;
				}
			}
			maxHeight = 15 * lines.length;
//			g2.drawString(inputText, (int) textInputPoint.getX() + 3, (int) textInputPoint.getY() - 3);
			g2.setColor(Color.MAGENTA);
//			g2.drawString("|", (float) textInputPoint.getX() + 2 + sWidth, (float) textInputPoint.getY() - 3);
			g2.drawLine((int) textInputPoint.getX() + 3 + cursorX, (int) textInputPoint.getY() + (cursorY - 1) * 15, 
					(int) textInputPoint.getX() + 3 + cursorX, (int) textInputPoint.getY() + cursorY * 15);
			g2.setColor(Color.BLACK);
			g2.setColor(oldColor);
			
//			int boxWidth = fm.stringWidth(inputText) + 10;
			if (maxWidth < 100) {
				maxWidth = 100;
			}
			Rectangle2D inputBox = new Rectangle2D.Double(textInputPoint.getX(), textInputPoint.getY() - 15, maxWidth + 8, maxHeight);
//			ChartMaskingUtilities.drawMaskBoarder(g2, inputBox);
	        Color fillColor = new Color(250, 250, 50, 30);
	        g2.setPaint(fillColor);
	        g2.fill(inputBox);
			g2.setColor(Color.ORANGE);
			g2.drawRect((int) textInputPoint.getX(), (int) textInputPoint.getY() - 15, maxWidth + 8, maxHeight);
		}
		if (textContentMap.size() > 0){
			Color oldColor = g2.getColor();
			g2.setColor(Color.BLACK);
			Rectangle2D imageArea = getScreenDataArea();
			for (Entry<Rectangle2D, String> entry : textContentMap.entrySet()) {
				Rectangle2D rect = entry.getKey();
				Point2D screenPoint = ChartMaskingUtilities.translateChartPoint(new Point2D.Double(rect.getX(), rect.getY()), imageArea, getChart());
				String text = entry.getValue();
				if (text == null) {
					continue;
				}
				String[] lines = text.split("\n");
				g2.setColor(Color.BLACK);
				for (int i = 0; i < lines.length; i++) {
					g2.drawString(lines[i], (int) screenPoint.getX() + 3, (int) screenPoint.getY() - 3 + i * 15);
				}
				if (rect == selectedTextWrapper) {
					FontMetrics fm = g2.getFontMetrics();
					int maxWidth = 0;
					int maxHeight = 0;
					for (int i = 0; i < lines.length; i++) {
						int lineWidth = fm.stringWidth(lines[i]);
						if (lineWidth > maxWidth) {
							maxWidth = lineWidth;
						}
					}
					maxHeight = 15 * lines.length;
					if (maxWidth < 100) {
						maxWidth = 100;
					}
					Rectangle2D inputBox = new Rectangle2D.Double(screenPoint.getX(), screenPoint.getY() - 15, maxWidth + 8, maxHeight);
			        Color fillColor = new Color(250, 250, 50, 30);
			        g2.setPaint(fillColor);
			        g2.fill(inputBox);
					g2.setColor(Color.ORANGE);
					g2.drawRect((int) screenPoint.getX(), (int) screenPoint.getY() - 15, maxWidth + 8, maxHeight);

				}
//				g2.drawString(text == null ? "" : text, (int) screenPoint.getX() + 3, (int) screenPoint.getY() - 3);
			}
			g2.setColor(oldColor);
		}
	}

	@Override
	protected void processKeyEvent(KeyEvent e) {
		super.processKeyEvent(e);
	}
	
	/**
     * Draws a vertical line used to trace the mouse position to the horizontal
     * axis.
     *
     * @param g2 the graphics device.
     * @param x  the x-coordinate of the trace line.
     */
    private void drawHorizontalAxisTrace(Graphics2D g2, int x) {

    	Rectangle2D dataArea = getScreenDataArea();
    	if (((int) dataArea.getMinX() < x) && (x < (int) dataArea.getMaxX())) {
    		g2.setPaint(getAxisTraceColor());
    		g2.setStroke(new BasicStroke(0.25f));
        	g2.draw(new Line2D.Float(x,
                    (int) dataArea.getMinY(), x, (int) dataArea.getMaxY()));
    	}
    }

	/**
     * Draws a horizontal line used to trace the mouse position to the vertical
     * axis.
     *
     * @param g2 the graphics device.
     * @param y  the y-coordinate of the trace line.
     */
    private void drawVerticalAxisTrace(Graphics2D g2, int y) {

        Rectangle2D dataArea = getScreenDataArea();
        if (((int) dataArea.getMinY() < y) && (y < (int) dataArea.getMaxY())) {
        	g2.setPaint(getAxisTraceColor());
        	g2.setStroke(new BasicStroke(0.25f));
        	g2.draw(new Line2D.Float((int) dataArea.getMinX(), y, (int) dataArea.getMaxX(),
        			y));
        }
    }

    protected abstract void drawToolTipFollower(Graphics2D g2, int x, int y);

	public List<AbstractMask> getMasks() {
		return new ArrayList<AbstractMask>(maskList.keySet());
	}
	
	protected LinkedHashMap<AbstractMask, Color> getMaskMap() {
		return maskList;
	}
	
	protected abstract Color getAxisTraceColor();
	
	public boolean isToolTipFollowerEnabled() {
		return isToolTipFollowerEnabled;
	}
	
	/**
	 * @return the horizontalTraceLocation
	 */
	protected int getHorizontalTraceLocation() {
		return horizontalTraceLocation;
	}

	/**
	 * @param horizontalTraceLocation the horizontalTraceLocation to set
	 */
	protected void setHorizontalTraceLocation(int horizontalTraceLocation) {
		this.horizontalTraceLocation = horizontalTraceLocation;
	}

	/**
	 * @return the verticalTraceLocation
	 */
	protected int getVerticalTraceLocation() {
		return verticalTraceLocation;
	}

	/**
	 * @param verticalTraceLocation the verticalTraceLocation to set
	 */
	protected void setVerticalTraceLocation(int verticalTraceLocation) {
		this.verticalTraceLocation = verticalTraceLocation;
	}

	@Override
    public void doCopy() {
        final Clipboard systemClipboard
                = Toolkit.getDefaultToolkit().getSystemClipboard();
        Rectangle2D screenArea = getScreenDataArea();
        final ChartTransferableWithMask selection = new ChartTransferableWithMask(
        		getChart(), getWidth(), getHeight(), screenArea, maskList, shapeMap);
        //TODO: the below command take too long to run. 6 seconds for Wombat data. 
        Cursor currentCursor = getCursor();
        setCursor(WAIT_CURSOR);
        systemClipboard.setContents(selection, null);
        setCursor(currentCursor);
    }

//	@Override
//	public void doSaveAs() throws IOException {
//
//		Display.getDefault().asyncExec(new Runnable() {
//			
//			Shell shell;
//			
//			private void handleException(Exception e) {
//				if (shell != null) {
//					MessageDialog.openError(shell, "Failed to Save", "failed to save " +
//							"the image: " + e.getMessage());
//					
//				}
//			}
//			
//			@Override
//			public void run() {
//				try {
//					shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//				}catch (Exception e) {
//					e.printStackTrace();
//				}
//				if (shell != null) {
//					FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
//					String[] extensions = {"*.png", "*.jpg"};
//					String[] typeNames = {"PNG IMAGE FILE",  "JPEG IMAGE FILE"};
//					String systemSavePath = System.getProperty("SYSTEM_SAVE_PATH");
//					if (systemSavePath != null) {
//						fileDialog.setFilterPath(systemSavePath);
//					}
//					fileDialog.setFilterExtensions(extensions);
//					fileDialog.setFilterNames(typeNames);
//					String filename = fileDialog.open();
//
//					if (filename != null) {
////						int filterIndex = fileDialog.getFilterIndex();
////						if (filterIndex == 0) {
////							if (!filename.endsWith(".png")) {
////								filename = filename + ".png";
////							} 
////							try {
////				           		ChartMaskingUtilities.writeChartAsPNG(new File(filename), getChart(), 
////			            				getWidth(), getHeight(), null, getScreenDataArea(), 
////			            				getMasks());
////						} catch (IOException e) {
////								handleException(e);
////							}
////						} else if (filterIndex == 1) {
////							if (!filename.endsWith(".jpg")) {
////								filename = filename + ".jpg";
////							}
////							try {
////			            		ChartMaskingUtilities.writeChartAsJPEG(new File(filename), getChart(),
////			            				getWidth(), getHeight(), null, getScreenDataArea(), getMasks());
////							} catch (IOException e) {
////								handleException(e);
////							}
////						}
//						int filterIndex = fileDialog.getFilterIndex();
//						String fileType = "png";
//						if (filterIndex == 0) {
//							fileType = "png";
//						} else if (filterIndex == 1) {
//							fileType = "jpg";
//						}
//						try {
//							saveTo(filename, fileType);
//						} catch (IOException e) {
//							handleException(e);
//						}
//						System.setProperty("SYSTEM_SAVE_PATH", fileDialog.getFilterPath());
//					}
//				} else {
//					try {
//						superDoSaveAs();
//					} catch (IOException e) {
//						handleException(e);
//					}
//				}
//				
//			}
//
//		});
//	}
	
	@Override
    public void doSaveAs() throws IOException {

        JFileChooser fileChooser = new JFileChooser();
        String currentDirectory = System.getProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL);
        if (currentDirectory != null) {
        	File savePath = new File(currentDirectory);
        	if (savePath.exists() && savePath.isDirectory()) {
        		fileChooser.setCurrentDirectory(savePath);
        	}
        }
        ExtensionFileFilter ascFilter = new ExtensionFileFilter("Text_Files", ".txt");
        ExtensionFileFilter jpgFilter = new ExtensionFileFilter("JPG_Image_Files", ".jpg");
        ExtensionFileFilter pngFilter = new ExtensionFileFilter("PNG_Image_Files", ".png");
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.addChoosableFileFilter(jpgFilter);
        fileChooser.addChoosableFileFilter(ascFilter);
        fileChooser.setFileFilter(jpgFilter);
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
        	String filename = fileChooser.getSelectedFile().getPath();
        	String selectedDescription = fileChooser.getFileFilter().getDescription();
        	String fileExtension = StaticValues.DEFAULT_IMAGE_FILE_EXTENSION;
        	if (selectedDescription.toLowerCase().contains("png")) {
        		fileExtension = "png";
        		if (!filename.toLowerCase().endsWith(".png")) {
        			filename = filename + ".png";
        		}
        	} else if (selectedDescription.toLowerCase().contains("jpg")) {
        		fileExtension = "jpg";
        		if (!filename.toLowerCase().endsWith(".jpg")) {
        			filename = filename + ".jpg";
        		}
        	} else if (selectedDescription.toLowerCase().contains("text")) {
        		fileExtension = "txt";
        		if (!filename.toLowerCase().endsWith(".txt")) {
        			filename = filename + ".txt";
        		}
        	}
        	File selectedFile = new File(filename);
        	int confirm = JOptionPane.YES_OPTION;
        	if (selectedFile.exists()) {
        		confirm = JOptionPane.showConfirmDialog(this, selectedFile.getName() + " exists, overwrite?", 
        				"Confirm Overwriting", JOptionPane.YES_NO_OPTION);
        	}
        	if (confirm == JOptionPane.YES_OPTION) {
        		saveTo(filename, fileExtension);
        		System.setProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL, 
        				fileChooser.getSelectedFile().getParent());
        	}
        }
	}
	
	public void saveTo(String filename, String fileType) 
	throws IOException{
		int filterIndex = 0;
		if (fileType != null) {
			if (fileType.toLowerCase().contains("png")) {
				filterIndex = 0;
			} else if (fileType.toLowerCase().contains("jpg") || 
					fileType.toLowerCase().contains("jpeg")) {
				filterIndex = 1;
			} else if (fileType.toLowerCase().contains("txt")) {
				filterIndex = 2;
			} 
		}
		if (filterIndex == 0) {
			ChartMaskingUtilities.writeChartAsPNG(new File(filename), getChart(), 
					getWidth(), getHeight(), null, getScreenDataArea(), 
					maskList, shapeMap);
		} else if (filterIndex == 1) {
			ChartMaskingUtilities.writeChartAsJPEG(new File(filename), getChart(),
					getWidth(), getHeight(), null, getScreenDataArea(), maskList, 
					shapeMap);
		} else if (filterIndex == 2) {
			FileWriter fw = new FileWriter(filename);
			BufferedWriter writer = new BufferedWriter (fw);
			saveAsText(writer);
			writer.close();
			fw.close();
		}
	}
	
	protected abstract void saveAsText(BufferedWriter writer) throws IOException;
	
	
 	/**
     * Prints the chart on a single page.
     *
     * @param g  the graphics context.
     * @param pf  the page format to use.
     * @param pageIndex  the index of the page. If not <code>0</code>, nothing
     *                   gets print.
     *
     * @return The result of printing.
     */
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {

        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) g;
        double x = pf.getImageableX();
        double y = pf.getImageableY();
        double w = pf.getImageableWidth();
        double h = pf.getImageableHeight();
        double screenWidth = getWidth();
        double screenHeight = getHeight();
        double widthRatio = w / screenWidth;
        double heightRatio = h / screenHeight;
        double overallRatio = 1;
        overallRatio = widthRatio < heightRatio ? widthRatio : heightRatio;
        Rectangle2D printArea = new Rectangle2D.Double(x, y, screenWidth * overallRatio, 
        		screenHeight * overallRatio);
        draw(g2, printArea, 0, 0);
        return PAGE_EXISTS;
    }

    @Override
    public void createChartPrintJob() {
    	setCursor(WAIT_CURSOR);
    	PrinterJob job = PrinterJob.getPrinterJob();
    	PageFormat pf = job.defaultPage();
    	PageFormat pf2 = job.pageDialog(pf);
    	if (pf2 != pf) {
    		job.setPrintable(this, pf2);
    		try {
    			job.print();
    		}
    		catch (PrinterException e) {
    			JOptionPane.showMessageDialog(this, e);
    		} finally {
    			setCursor(defaultCursor);
    		}
    	}
    	setCursor(defaultCursor);
    }
    
    protected void selectMask(String maskName) {
    	if (maskName == null) {
    		selectedMask = null;
    	} else {
    		for (AbstractMask mask : getMasks()) {
    			if (maskName.equals(mask.getName())) {
    				selectedMask = mask;
    				break;
    			}
    		}
    	}
    }
   
    public void removeSelectedMask() {
    	if (selectedMask != null) {
    		maskList.remove(selectedMask);
    		final AbstractMask toRemove = selectedMask;
    		selectedMask = null;
    		repaint();
    		fireMaskRemovalEvent(toRemove);
    	}
	}

    public void addShape(Shape shape, Color color) {
    	shapeMap.put(shape, color);
    }
    
    public void removeShape(Shape shape) {
    	shapeMap.remove(shape);
    }
    
    public void clearShapes() {
    	shapeMap.clear();
    	isShapeValid = true;
    }
    
	@Override
	protected JPopupMenu createPopupMenu(boolean properties, boolean copy,
			boolean save, boolean print, boolean zoom) {
		JPopupMenu menu = super.createPopupMenu(properties, copy, save, print, zoom);
        this.markerManagementMenu = new JMenu("Marker Management");
        this.internalMarkerMenuItem = new JMenuItem("Add Cross Marker Here");
        internalMarkerMenuItem.setActionCommand(ADD_INTERNAL_MARKER_COMMAND);
        internalMarkerMenuItem.addActionListener(this);
        this.horizontalBarMenuItem = new JMenuItem("Add Horizontal Bar");
        horizontalBarMenuItem.setActionCommand(ADD_HORIZONTAL_BAR_COMMAND);
        horizontalBarMenuItem.addActionListener(this);
        this.verticalBarMenuItem = new JMenuItem("Add Vertical Bar");
        verticalBarMenuItem.setActionCommand(ADD_VERTICAL_BAR_COMMAND);
        verticalBarMenuItem.addActionListener(this);
        this.removeSelectedMarkerMenuItem = new JMenuItem("Remove Selected Marker");
        removeSelectedMarkerMenuItem.setActionCommand(REMOVE_SELECTED_MARKER_COMMAND);
        removeSelectedMarkerMenuItem.addActionListener(this);
        this.clearMarkersMenuItem = new JMenuItem("Clear Cross Markers");
        clearMarkersMenuItem.setActionCommand(CLEAR_INTERNAL_MARKERS_COMMAND);
        clearMarkersMenuItem.addActionListener(this);
        this.clearDomainMarkersMenuItem = new JMenuItem("Clear Vertical Markers");
        clearDomainMarkersMenuItem.setActionCommand(CLEAR_DOMAIN_MARKERS_COMMAND);
        clearDomainMarkersMenuItem.addActionListener(this);
        this.clearRangeMarkersMenuItem = new JMenuItem("Clear Horizontal Markers");
        clearRangeMarkersMenuItem.setActionCommand(CLEAR_RANGE_MARKERS_COMMAND);
        clearRangeMarkersMenuItem.addActionListener(this);
        this.clearAllMarkersMenuItem = new JMenuItem("Clear All Markers");
        clearAllMarkersMenuItem.setActionCommand(CLEAR_ALL_MARKERS_COMMAND);
        clearAllMarkersMenuItem.addActionListener(this);

        markerManagementMenu.add(removeSelectedMarkerMenuItem);
        markerManagementMenu.addSeparator();
        markerManagementMenu.add(internalMarkerMenuItem);
        markerManagementMenu.add(clearMarkersMenuItem);
        markerManagementMenu.addSeparator();
        markerManagementMenu.add(horizontalBarMenuItem);
        markerManagementMenu.add(clearRangeMarkersMenuItem);
        markerManagementMenu.addSeparator();
        markerManagementMenu.add(verticalBarMenuItem);
        markerManagementMenu.add(clearDomainMarkersMenuItem);
        markerManagementMenu.addSeparator();
        markerManagementMenu.add(clearAllMarkersMenuItem);
        menu.addSeparator();
        menu.add(markerManagementMenu);
        this.removeSelectedMaskMenuItem = new JMenuItem();
        this.removeSelectedMaskMenuItem.setActionCommand(REMOVE_SELECTED_MASK_COMMAND);
        this.removeSelectedMaskMenuItem.addActionListener(this);
        menu.addSeparator();
        menu.add(removeSelectedMaskMenuItem);
        maskManagementMenu = new JMenu("Mask Management");
        menu.add(maskManagementMenu);

        return menu;
	}
	
	@Override
	protected void displayPopupMenu(int x, int y) {
		
        addMaskMenu(x, y);
        removeSelectedMarkerMenuItem.setEnabled(selectedMarker != null);
		super.displayPopupMenu(x, y);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (command.equals(REMOVE_SELECTED_MASK_COMMAND)) {
        	removeSelectedMask();
        	repaint();
        } else if (command.equals(DESELECT_MASK_COMMAND)) {
        	selectMask(Double.NaN, Double.NaN);
        	repaint();
        } else if (command.equals(ADD_INTERNAL_MARKER_COMMAND)) {
        	addInternalMarker(event);
        	repaint();
        } else if (command.equals(ADD_HORIZONTAL_BAR_COMMAND)) {
        	addHorizontalBar(event);
        	repaint();
        } else if (command.equals(ADD_VERTICAL_BAR_COMMAND)) {
        	addVerticalBar(event);
        	repaint();
        } else if (command.equals(REMOVE_SELECTED_MARKER_COMMAND)) {
        	removeSelectedMarker();
        } else if (command.equals(CLEAR_INTERNAL_MARKERS_COMMAND)) {
        	clearMarkers();
        } else if (command.equals(CLEAR_DOMAIN_MARKERS_COMMAND)) {
        	clearDomainAxisMarkers();
        } else if (command.equals(CLEAR_RANGE_MARKERS_COMMAND)) {
        	clearRangeAxisMarkers();
        } else if (command.equals(CLEAR_ALL_MARKERS_COMMAND)) {
        	clearAllMarkers();
        } else if (command.startsWith(SELECT_MASK_COMMAND)) {
        	String[] commands = command.split("-", 2);
        	if (commands.length > 1) {
        		selectMask(commands[1]);
            	repaint();
        	}
        } else {
        	super.actionPerformed(event);
        }
	}
	
	private void addInternalMarker(ActionEvent event) {
		if (mouseRightClickLocation != null) {
			addMarker(mouseRightClickLocation.getX(), mouseRightClickLocation.getY(), null);
		}
	}

	private void addHorizontalBar(ActionEvent event) {
		if (mouseRightClickLocation != null) {
			addRangeAxisMarker(mouseRightClickLocation.getY(), 0, null);
		}
	}

	private void addVerticalBar(ActionEvent event) {
		if (mouseRightClickLocation != null) {
			addDomainAxisMarker(mouseRightClickLocation.getX(), 0, null);
		}
	}

	protected abstract void selectMask(double x, double y);

	/**
	 * @return the selectedMask
	 */
	public AbstractMask getSelectedMask() {
		return selectedMask;
	}

	/**
	 * @param selectedMask the selectedMask to set
	 */
	public void setSelectedMask(AbstractMask selectedMask) {
		this.selectedMask = selectedMask;
	}

	/**
	 * @return the maskDragIndicator
	 */
	protected int getMaskDragIndicator() {
		return maskDragIndicator;
	}

	/**
	 * @param maskDragIndicator the maskDragIndicator to set
	 */
	protected void setMaskDragIndicator(int maskDragIndicator) {
		this.maskDragIndicator = maskDragIndicator;
	}

	/**
	 * @return the chartX
	 */
	public double getChartX() {
		return chartX;
	}

	/**
	 * @param chartX the chartX to set
	 */
	protected void setChartX(double chartX) {
		this.chartX = chartX;
	}

	/**
	 * @return the chartY
	 */
	public double getChartY() {
		return chartY;
	}

	/**
	 * @param chartY the chartY to set
	 */
	protected void setChartY(double chartY) {
		this.chartY = chartY;
	}
	
	@Override
    public void setCursor(Cursor arg0) {
    	super.setCursor(arg0);
    	getParent().setCursor(arg0);
    }
	
	@Override
	public void mouseMoved(MouseEvent e) {
//        if (isMaskingEnabled() && (e.getModifiers() & maskingKeyMask) != 0) {
		if (selectedTextWrapper == null && isMaskingEnabled()) {
        	int cursorType = findCursorOnSelectedItem(e.getX(), e.getY());
        	setCursor(Cursor.getPredefinedCursor(cursorType));
        } else {
        	Cursor newCursor = defaultCursor;
        	if (selectedTextWrapper != null) {
				Point2D screenXY = ChartMaskingUtilities.translateChartPoint(new Point2D.Double(selectedTextWrapper.getMinX(), 
						selectedTextWrapper.getMinY()), getScreenDataArea(), getChart());
				Rectangle2D screenRect = new Rectangle2D.Double(screenXY.getX(), screenXY.getY() - 15, 
						selectedTextWrapper.getWidth(), selectedTextWrapper.getHeight());
				if (screenRect.contains(e.getX(), e.getY())) {
					newCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
				}
        	}
        	if (newCursor != getCursor()) {
        		setCursor(newCursor);
        	}
        }
		Line2D oldSelection = selectedMarker;
		findSelectedMarker(e.getPoint());
		if (selectedMarker != oldSelection) {
			repaint();
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (selectedTextWrapper != null && textInputPoint != null) {
			moveSelectedText(e);
		} else {
			super.mouseDragged(e);
		}
	}
	
	private void moveSelectedText(MouseEvent e) {
			Point2D screenPoint = translateScreenToJava2D(e.getPoint());
			Rectangle2D screenArea = getScreenDataArea();
			if (screenArea.contains(screenPoint)) {

				if (textMovePoint != null) {
//					Point2D point = translateScreenToChart(translateScreenToJava2D(e.getPoint()));
					double screenX = ChartMaskingUtilities.translateScreenX(e.getX() / getScaleX(), 
		            		getScreenDataArea(), getChart());
					double screenY = ChartMaskingUtilities.translateScreenY(e.getY(), getScreenDataArea(), getChart(), 0);
//					Point2D point = translateScreenToChart(translateScreenToJava2D(e.getPoint()));
					Point2D point = new Point2D.Double(screenX, screenY);
					selectedTextWrapper.setRect(selectedTextWrapper.getMinX() + point.getX() - textMovePoint.getX(), 
							selectedTextWrapper.getMinY() + point.getY() - textMovePoint.getY(), 
							selectedTextWrapper.getWidth(), 
							selectedTextWrapper.getHeight());
					if (point != null) {
						this.textMovePoint = point;
					}
					repaint();
				}
			}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (selectedTextWrapper != null && getCursor() == Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)) {
			Point2D screenXY = ChartMaskingUtilities.translateChartPoint(new Point2D.Double(selectedTextWrapper.getMinX(), selectedTextWrapper.getMinY()), getScreenDataArea(), getChart());
			Rectangle2D screenRect = new Rectangle2D.Double(screenXY.getX(), screenXY.getY() - 15, selectedTextWrapper.getWidth(), selectedTextWrapper.getHeight());
			if (screenRect.contains(e.getX(), e.getY())) {

				double screenX = ChartMaskingUtilities.translateScreenX(e.getX() / getScaleX(), 
						getScreenDataArea(), getChart());
				double screenY = ChartMaskingUtilities.translateScreenY(e.getY(), getScreenDataArea(), getChart(), 0);
				//			Point2D point = translateScreenToChart(translateScreenToJava2D(e.getPoint()));
				Point2D point = new Point2D.Double(screenX, screenY);
				if (point != null) {
					this.textMovePoint = point;
				}
			} else {
				this.textMovePoint = null;
			}
		} else {
			super.mousePressed(e);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.ALT_MASK) != 0) {
			double xNew = ChartMaskingUtilities.translateScreenX(e.getX(), getScreenDataArea(), getChart());
			double yNew = ChartMaskingUtilities.translateScreenY(e.getY(), getScreenDataArea(), getChart(), 0);
			addMarker(xNew, yNew, null);
		} else if (isTextInputEnabled) {
			if (!textInputFlag) {
				boolean newTextEnabled = selectedTextWrapper == null;
				if (selectedTextWrapper != null) {
					Point2D screenXY = ChartMaskingUtilities.translateChartPoint(new Point2D.Double(selectedTextWrapper.getMinX(), selectedTextWrapper.getMinY()), getScreenDataArea(), getChart());
					Rectangle2D screenRect = new Rectangle2D.Double(screenXY.getX(), screenXY.getY() - 15, selectedTextWrapper.getWidth(), selectedTextWrapper.getHeight());
					if (screenRect.contains(e.getX(), e.getY())) {
						Point2D point = e.getPoint();
						String inputText = textContentMap.get(selectedTextWrapper);
						if (inputText == null) {
							inputText = "";
						}
						String[] lines = inputText.split("\n", 100);
						int cursorX = 0;
						int charCount = 0;
						int maxWidth = 0;
						int pickX = -1;
						FontMetrics fm = getGraphics().getFontMetrics();
						for (int i = 0; i < lines.length; i++) {
							int lineWidth = fm.stringWidth(lines[i]);
							if (lineWidth > maxWidth) {
								maxWidth = lineWidth;
							}
						}
						if (maxWidth < 100) {
							maxWidth = 100;
						}
						Point2D screenPoint = ChartMaskingUtilities.translateChartPoint(new Point2D.Double(selectedTextWrapper.getX(), 
								selectedTextWrapper.getY()), getScreenDataArea(), getChart());
						if (point.getX() <= screenPoint.getX() + 11 + maxWidth && point.getY() <= screenPoint.getY() + lines.length * 15 - 15){
							textInputPoint = screenPoint;
							textInputContent = inputText;
							textInputFlag = true;
							textContentMap.remove(selectedTextWrapper);
							selectedTextWrapper = null;
							textInputCursorIndex = 0;
							for (int i = 0; i < lines.length; i++) {
								if (point.getY() > screenPoint.getY() + i * 15 - 15 && point.getY() <= screenPoint.getY() + i * 15){
									cursorX = fm.stringWidth(lines[i]);
									if (point.getX() >= screenPoint.getX() && point.getX() <= screenPoint.getX() + 3 + cursorX) {
										if (point.getX() >= screenPoint.getX() && point.getX() < screenPoint.getX() + 3) { 
											pickX = 0;
										}									
										double lastEnd = screenPoint.getX() + 3;
										for (int j = 0; j < lines[i].length(); j++) {
											int size = fm.stringWidth(lines[i].substring(0, j + 1));
											double newEnd = screenPoint.getX() + 3 + size;
											if (point.getX() >= lastEnd && point.getX() < lastEnd + (newEnd - lastEnd) / 2) {
												pickX = j;
											} else if (point.getX() >= lastEnd + (newEnd - lastEnd) / 2 && point.getX() < newEnd) {
												pickX = j + 1;
											}
											lastEnd = newEnd;
										}
										if (pickX >= 0) {
											textInputCursorIndex = charCount + pickX;
										}
									} else {
										textInputCursorIndex = charCount + lines[i].length();
									}
									break;
								}
								charCount += lines[i].length() + 1;
							}
						}
					}
				}
				selectText(e.getX(), e.getY());
				if (selectedTextWrapper == null && !textInputFlag && newTextEnabled && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
					textInputFlag = true;
					textInputPoint = e.getPoint();
				}
			} else {
				Point2D point = e.getPoint();
				boolean finishInput = false;
//				if (point.getX() < textInputPoint.getX() || point.getY() < textInputPoint.getY() - 15) {
//					finishInput = true;
//				} else {
				String inputText = textInputContent;
				if (inputText == null) {
					inputText = "";
				}
				String[] lines = inputText.split("\n", 100);
				int cursorX = 0;
				int charCount = 0;
				int maxWidth = 0;
				int pickX = -1;
				FontMetrics fm = getGraphics().getFontMetrics();
				for (int i = 0; i < lines.length; i++) {
					int lineWidth = fm.stringWidth(lines[i]);
					if (lineWidth > maxWidth) {
						maxWidth = lineWidth;
					}
				}
				if (maxWidth < 100) {
					maxWidth = 100;
				}
				if (point.getX() > textInputPoint.getX() + 11 + maxWidth || point.getY() > textInputPoint.getY() + lines.length * 15 - 15
						|| point.getX() < textInputPoint.getX() || point.getY() < textInputPoint.getY() - 15){
					finishInput = true;
				} else {
					for (int i = 0; i < lines.length; i++) {
						if (point.getY() > textInputPoint.getY() + i * 15 - 15 && point.getY() <= textInputPoint.getY() + i * 15){
							cursorX = fm.stringWidth(lines[i]);
							if (point.getX() >= textInputPoint.getX() && point.getX() <= textInputPoint.getX() + 3 + cursorX) {
								if (point.getX() >= textInputPoint.getX() && point.getX() < textInputPoint.getX() + 3) { 
									pickX = 0;
								}									
								double lastEnd = textInputPoint.getX() + 3;
								for (int j = 0; j < lines[i].length(); j++) {
									int size = fm.stringWidth(lines[i].substring(0, j + 1));
									double newEnd = textInputPoint.getX() + 3 + size;
									if (point.getX() >= lastEnd && point.getX() < lastEnd + (newEnd - lastEnd) / 2) {
										pickX = j;
									} else if (point.getX() >= lastEnd + (newEnd - lastEnd) / 2 && point.getX() < newEnd) {
										pickX = j + 1;
									}
									lastEnd = newEnd;
								}
								if (pickX >= 0) {
									textInputCursorIndex = charCount + pickX;
								}
							} else {
								textInputCursorIndex = charCount + lines[i].length();
							}
							break;
						}
						charCount += lines[i].length() + 1;
					}
				}
//				}
				
				if (finishInput) {
					if (textInputContent != null && textInputContent.length() > 0) {
						double xNew = ChartMaskingUtilities.translateScreenX(textInputPoint.getX(), getScreenDataArea(), getChart());
						double yNew = ChartMaskingUtilities.translateScreenY(textInputPoint.getY(), getScreenDataArea(), getChart(), 0);
						textContentMap.put(new Rectangle2D.Double(xNew, yNew, maxWidth, lines.length * 15), textInputContent);
					}
					textInputContent = null;
					textInputCursorIndex = 0;
					textInputFlag = false;
				}
			}
		}
	}
	
	private void selectText(double xNew, double yNew) {
		for (Entry<Rectangle2D, String> item : textContentMap.entrySet()) {
			Rectangle2D rect = item.getKey();
			Point2D screenPoint = ChartMaskingUtilities.translateChartPoint(new Point2D.Double(rect.getMinX(), rect.getMinY()), getScreenDataArea(), getChart());
			Rectangle2D screenRect = new Rectangle2D.Double(screenPoint.getX(), screenPoint.getY() - 15, rect.getWidth(), rect.getHeight());
			if (screenRect.contains(xNew, yNew)) {
				if (selectedTextWrapper == rect) {
					selectedTextWrapper = null;
				} else {
					selectedTextWrapper = rect;
					setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					return;
				}
			}
		}
		if (selectedTextWrapper != null) {
			selectedTextWrapper = null;
		}
	}

	private void findSelectedMarker(Point point) {
		Line2D marker = null;
		double distance = Double.MAX_VALUE;
		double cDis;
		for (Entry<Line2D, Color> entry : domainMarkerMap.entrySet()) {
			Line2D line = entry.getKey();
			Point2D p2 = line.getP2();
			double height = p2.getY();
			cDis = Double.MAX_VALUE;
			if (height < 1e-4) {
				double xScr = ChartMaskingUtilities.translateChartPoint(p2, getScreenDataArea(), getChart()).getX();
				cDis = Math.abs(point.getX() - xScr);
			} else {
				Point2D newP2 = ChartMaskingUtilities.translateChartPoint(p2, getScreenDataArea(), getChart());
				if (newP2.getY() < point.getY()) {
					cDis = Math.abs(point.getX() - newP2.getX());
				} else {
					cDis = newP2.distance(point);
				}
			}
			if (cDis <= distance) {
				distance = cDis;
				marker = line;
			}
		}
		for (Entry<Line2D, Color> entry : rangeMarkerMap.entrySet()) {
			Line2D line = entry.getKey();
			Point2D p1 = line.getP1();
			Point2D p2 = line.getP2();
			double width = p2.getX();
			cDis = Double.MAX_VALUE;
			if (width < 1e-4) {
				double yScr = ChartMaskingUtilities.translateChartPoint(p1, getScreenDataArea(), getChart()).getY();
				cDis = Math.abs(point.getY() - yScr);
			} else {
				Point2D newP2 = ChartMaskingUtilities.translateChartPoint(p2, getScreenDataArea(), getChart());
				if (newP2.getX() > point.getX()) {
					cDis = Math.abs(point.getY() - newP2.getY());
				} else {
					cDis = newP2.distance(point);
				}
			}
			if (cDis <= distance) {
				distance = cDis;
				marker = line;
			}
		}
		for (Entry<Line2D, Color> entry : markerMap.entrySet()) {
			Line2D line = entry.getKey();
			Point2D p1 = line.getP1();
			p1 = ChartMaskingUtilities.translateChartPoint(p1, getScreenDataArea(), getChart());
			cDis = p1.distance(point);
			if (cDis <= distance) {
				distance = cDis;
				marker = line;
			}
		}
		if (distance < 5) {
			selectedMarker = marker;
		} else {
			selectedMarker = null;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
			double xNew = ChartMaskingUtilities.translateScreenX(e.getX(), getScreenDataArea(), getChart());
			double yNew = ChartMaskingUtilities.translateScreenY(e.getY(), getScreenDataArea(), getChart(), 0);
			mouseRightClickLocation = new Point2D.Double(xNew, yNew);
		}
		textMovePoint = null;
		super.mouseReleased(e);
	}
	
	protected abstract int findCursorOnSelectedItem(int x, int y);
	
    protected Point2D translateScreenToChart(Point2D point) {
        EntityCollection entities = getChartRenderingInfo().getEntityCollection();
        ChartEntity entity = entities.getEntity(point.getX(), point.getY());
        if (entity instanceof XYItemEntity) {
        	XYDataset dataset = ((XYItemEntity) entity).getDataset();
        	int item = ((XYItemEntity) entity).getItem();
        	double chartX = dataset.getXValue(0, item);
        	double chartY = dataset.getYValue(0, item);
//        	double chartZ = ((XYZDataset) dataset).getZValue(0, item);
        	return new Point2D.Double(chartX, chartY);
        } 
        return null;
	}
    
    /**
     * Returns a point based on (x, y) but constrained to be within the bounds
     * of the given rectangle.  This method could be moved to JCommon.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param area  the rectangle (<code>null</code> not permitted).
     *
     * @return A point within the rectangle.
     */
    protected Point2D getPointInRectangle(int x, int y, Rectangle2D area) {
        double xx = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
        double yy = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
        return new Point2D.Double(xx, yy);
    }
    
    public void setToolTipFollowerEnabled(boolean enabled){
    	isToolTipFollowerEnabled = enabled;
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
    	setHorizontalTraceLocation(-1);
    	setVerticalTraceLocation(-1);
    	repaint();
    	super.mouseExited(e);
    }

    @Override
    public Image getImage() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), 
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
//		gc2.setBackground(Color.white);
		g2.setPaint(Color.white);
		g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
		if (getChart() != null) {
			Image chartImage = getChart().createBufferedImage((int) getWidth(),
					(int) getHeight());
			g2.drawImage(chartImage, 0, 0, this);
			ChartMaskingUtilities.drawMasks(g2, getScreenDataArea(), maskList, 
					null, getChart());
		}
		g2.dispose();
		return image;
    }
    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, 
    		double shiftX, double shiftY) {
//    	g2.setPaint(Color.white);
//		g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
//		if (getChart() != null) {
////			Image chartImage = getChart().createBufferedImage((int) area.getWidth(),
////					(int) area.getHeight());
////			g2.drawImage(chartImage, (int) area.getMinX(), (int) area.getMinY(), 
////					this);
//			getChart().draw(g2, area, getAnchor(), null);
//			ChartMaskingUtilities.drawMasks(g2, getScreenDataArea(), maskList, 
//					null, getChart());
//		}
        double widthRatio = area.getWidth() / getWidth();
        double heightRatio = area.getHeight() / getHeight();
        double overallRatio = 1;
        overallRatio = widthRatio < heightRatio ? widthRatio : heightRatio;

        XYPlot plot = (XYPlot) getChart().getPlot();
        Font domainFont = plot.getDomainAxis().getLabelFont();
        int domainSize = domainFont.getSize();
        Font rangeFont = plot.getRangeAxis().getLabelFont();
        int rangeSize = rangeFont.getSize();
        TextTitle titleBlock = getChart().getTitle();
        Font titleFont = null;
        int titleSize = 0;
        if (titleBlock != null) {
        	titleFont = titleBlock.getFont();
        	titleSize = titleFont.getSize();
        	getChart().getTitle().setFont(titleFont.deriveFont(
        			(float) (titleSize * overallRatio)));
        }
        Font domainScaleFont = plot.getDomainAxis().getTickLabelFont();
        int domainScaleSize = domainScaleFont.getSize();
        Font rangeScaleFont = plot.getRangeAxis().getTickLabelFont();
        int rangeScaleSize = rangeScaleFont.getSize();
        plot.getDomainAxis().setLabelFont(domainFont.deriveFont(
        		(float) (domainSize * overallRatio)));
        plot.getRangeAxis().setLabelFont(rangeFont.deriveFont(
        		(float) (rangeSize * overallRatio)));
        plot.getDomainAxis().setTickLabelFont(domainScaleFont.deriveFont(
        		(float) (domainScaleSize * overallRatio)));
        plot.getRangeAxis().setTickLabelFont(rangeScaleFont.deriveFont(
        		(float) (rangeScaleSize * overallRatio)));
        LegendTitle legend = getChart().getLegend();
        Font legendFont = null;
        int legendFontSize = 0;
        if (legend != null) {
        	legendFont = legend.getItemFont();
        	legendFontSize = legendFont.getSize();
        	legend.setItemFont(legendFont.deriveFont(
        			(float) (legendFontSize * overallRatio)));
        }
        
        Rectangle2D chartArea = (Rectangle2D) area.clone();
        getChart().getPadding().trim(chartArea);
        if (titleBlock != null) {
        	AxisUtilities.trimTitle(chartArea, g2, titleBlock, titleBlock.getPosition());
        }
        
        Axis scaleAxis = null;
        Font scaleAxisFont = null;
        int scaleAxisFontSize = 0;
        for (Object object : getChart().getSubtitles()) {
        	Title title = (Title) object;
        	if (title instanceof PaintScaleLegend) {
        		scaleAxis = ((PaintScaleLegend) title).getAxis();
        		scaleAxisFont = scaleAxis.getTickLabelFont();
        		scaleAxisFontSize = scaleAxisFont.getSize();
        		scaleAxis.setTickLabelFont(scaleAxisFont.deriveFont(
        				(float) (scaleAxisFontSize * overallRatio)));
        	}
        	AxisUtilities.trimTitle(chartArea, g2, title, title.getPosition());
        }
        AxisSpace axisSpace = AxisUtilities.calculateAxisSpace(
        		getChart().getXYPlot(), g2, chartArea);
        Rectangle2D dataArea = axisSpace.shrink(chartArea, null);
        getChart().getXYPlot().getInsets().trim(dataArea);
        getChart().getXYPlot().getAxisOffset().trim(dataArea);
        
//        Rectangle2D screenArea = getScreenDataArea();
//        Rectangle2D visibleArea = getVisibleRect();
//        Rectangle2D printScreenArea = new Rectangle2D.Double(screenArea.getMinX() * overallRatio + x, 
//        		screenArea.getMinY() * overallRatio + y, 
//        		printArea.getWidth() - visibleArea.getWidth() + screenArea.getWidth(), 
//        		printArea.getHeight() - visibleArea.getHeight() + screenArea.getHeight());

        getChart().draw(g2, area, getAnchor(), null);
        ChartMaskingUtilities.drawMasks(g2, dataArea, 
        		maskList, null, getChart(), overallRatio);
        ChartMaskingUtilities.drawShapes(g2, dataArea, 
        		shapeMap, getChart());
        plot.getDomainAxis().setLabelFont(domainFont);
        plot.getRangeAxis().setLabelFont(rangeFont);
        if (titleBlock != null) {
        	titleBlock.setFont(titleFont);
        }
        if (legend != null) {
        	legend.setItemFont(legendFont);
        }
        plot.getDomainAxis().setTickLabelFont(domainScaleFont);
        plot.getRangeAxis().setTickLabelFont(rangeScaleFont);
        if (scaleAxis != null) {
        	scaleAxis.setTickLabelFont(scaleAxisFont);
        }
//        System.out.println("print " + titleBlock.getText() + 
//        		" at [" + area.getX() + ", " + area.getY() + ", " + 
//        		area.getWidth() + ", " + area.getHeight() + "]");
    }
    
    @Override
    public void updatePlot() {
    	setRefreshBuffer(true);
    	repaint();
    }
    
    @Override
    public void updateLabels() {
    	XYPlot xyPlot = getChart().getXYPlot();
    	XYDataset xyDataset = xyPlot.getDataset();
    	if (xyDataset instanceof IDataset) {
    		IDataset dataset = (IDataset) xyDataset;
    		try{
    			String title = "";
    			if (dataset.getXTitle() != null) {
    				title += dataset.getXTitle();
    			}
    			if (dataset.getXUnits() != null) {
    				title += " (" + dataset.getXUnits() + ")";
    			}
    			xyPlot.getDomainAxis().setLabel(title);
    			title = "";
    			if (dataset.getYTitle() != null) {
    				title += dataset.getYTitle();
    			}
    			if (dataset.getYUnits() != null) {
    				title += " (" + dataset.getYUnits() + ")";
    			}
    			xyPlot.getRangeAxis().setLabel(title);
    			title = "";
    			if (dataset.getTitle() != null) {
    				title = dataset.getTitle();
    			}
    			getChart().getTitle().setText(title);
    		} catch (Exception e) {
			}
    	}
    }
    
    @Override
    public void addMask(AbstractMask mask) {
    	Color newColor = getNextMaskColor(mask.isInclusive());
//    	mask.setFillColor(newColor);
    	if (maskList.containsKey(mask)) {
    		return;
    	}
    	maskList.put(mask, newColor);
    	fireMaskCreationEvent(mask);
    }
    
    @Override
    public void addMasks(List<AbstractMask> maskList) {
    	for (AbstractMask mask : maskList) {
    		addMask(mask);
    	}
    }
    
    @Override
    public void setMaskingEnabled(boolean enabled) {
    	isMaskingEnabled = enabled;
    }
    
    @Override
    public boolean isMaskingEnabled() {
    	return isMaskingEnabled;
    }
    
    @Override
    public void removeMask(AbstractMask mask) {
    	if (selectedMask == mask) {
    		selectedMask = null;
    	}
    	if (maskList.containsKey(mask)) {
    		maskList.remove(mask);
    		fireMaskRemovalEvent(mask);
    	}
    }
    
    @Override
    public boolean isHorizontalAxisFlipped() {
    	return getXYPlot().getDomainAxis().isInverted();
    }
    
    @Override
    public boolean isVerticalAxisFlipped() {
    	return getXYPlot().getRangeAxis().isInverted();
    }

    @Override
    public void addMaskEventListener(IMaskEventListener listener) {
    	maskEventListeners.add(listener);
    }
    
    @Override
    public void removeMaskEventListener(IMaskEventListener listener) {
    	maskEventListeners.remove(listener);
    }
    
    public List<IMaskEventListener> getMaskEventListeners() {
    	return maskEventListeners;
    }

	protected void fireMaskUpdateEvent(AbstractMask mask) {
		for (IMaskEventListener listener : maskEventListeners) {
			listener.maskUpdated(mask);
		}
	}
	
	protected void fireMaskCreationEvent(AbstractMask mask) {
		for (IMaskEventListener listener : maskEventListeners) {
			listener.maskAdded(mask);
		}
	}

	protected void fireMaskRemovalEvent(AbstractMask mask) {
		for (IMaskEventListener listener : maskEventListeners) {
			listener.maskRemoved(mask);
		}
	}

	/**
	 * @param autoUpdate the autoUpdate to set
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	/**
	 * @return the autoUpdate
	 */
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	@Override
	public void chartChanged(ChartChangeEvent event) {
		if (autoUpdate) {
			clearShapes();
			makeMarkers();
			super.chartChanged(event);
		}
	}
	
	@Override
	public void setPlotTitle(String title) {
		JFreeChart chart = getChart();
		chart.setTitle(title);
	}
	
	@Override
	public void cleanUp() {
		
	}

	/**
	 * @return the isShapeEnabled
	 */
	public boolean isShapeEnabled() {
		return isShapeEnabled;
	}

	/**
	 * @param isShapeEnabled the isShapeEnabled to set
	 */
	public void setShapeEnabled(boolean isShapeEnabled) {
		this.isShapeEnabled = isShapeEnabled;
		getXYPlot().setNotify(true);
	}

	private void drawShapes(Graphics2D g2) {
		if (!isShapeValid) {
			clearShapes();
			makeMarkers();
		}
		ChartMaskingUtilities.drawShapes(g2, getScreenDataArea(), shapeMap, getChart());
	}
	
	private void drawSelectedMarker(Graphics2D g2) {
		if (selectedMarker != null) {
			if (domainMarkerMap.containsKey(selectedMarker)) {
				Line2D line = convertDomainAxisMarker(selectedMarker);
				ChartMaskingUtilities.drawShapes(g2, getScreenDataArea(), line, getChart());
			} else if (rangeMarkerMap.containsKey(selectedMarker)) {
				Line2D line = convertRangeAxisMarker(selectedMarker);
				ChartMaskingUtilities.drawShapes(g2, getScreenDataArea(), line, getChart());
			} else if (markerMap.containsKey(selectedMarker)) {
				ChartMaskingUtilities.drawShapes(g2, getScreenDataArea(), selectedMarker, getChart());
			} 
		}
	}
	
	@Override
	public void addDomainAxisMarker(double x, int height, Color color) {
		if (color == null) {
			color = Color.BLACK;
		}
		double dHeight = height;
		if (height == 0) {
			dHeight = 1e-9;
		}
		Line2D line = new Line2D.Double(new Point2D.Double(x, 0), new Point2D.Double(x, dHeight));
		domainMarkerMap.put(line, color);
		Line2D newLine = convertDomainAxisMarker(line);
		addShape(newLine, color);
		getXYPlot().setNotify(true);
	}
	
	private Line2D convertDomainAxisMarker(Line2D marker) {
		Line2D newLine = (Line2D) marker.clone();
		Rectangle2D imageArea = getScreenDataArea();
		double maxY = imageArea.getBounds2D().getMaxY();
		if (maxY == 0) {
			isShapeValid = false;
		}
		newLine.setLine(marker.getX1(), ChartMaskingUtilities.translateScreenY(maxY - marker.getY1(), imageArea, getChart(), 0), 
				marker.getX2(), ChartMaskingUtilities.translateScreenY(maxY - marker.getY2(), imageArea, getChart(), 0));
		return newLine;
	}
	
	private void createDomainAxisMarkers() {
		for (Entry<Line2D, Color> entry : domainMarkerMap.entrySet()) {
			Line2D line = entry.getKey();
			Color color = entry.getValue();
			Line2D newLine = convertDomainAxisMarker(line);
			addShape(newLine, color);
		}
	}

	public void clearDomainAxisMarkers() {
		domainMarkerMap.clear();
		getXYPlot().setNotify(true);
	}
	
	@Override
	public void addRangeAxisMarker(double y, int width, Color color) {
		if (color == null) {
			color = Color.BLACK;
		}
		double dWidth = width;
		if (width == 0) {
			dWidth = 1e-9;
		}
		Line2D line = new Line2D.Double(new Point2D.Double(0, y), new Point2D.Double(dWidth, y));
		rangeMarkerMap.put(line, color);
		Line2D newLine = convertRangeAxisMarker(line);
		addShape(newLine, color);
		getXYPlot().setNotify(true);
	}
	
	private Line2D convertRangeAxisMarker(Line2D marker) {
		Line2D newLine = (Line2D) marker.clone();
		Rectangle2D imageArea = getScreenDataArea();
		double minX = imageArea.getBounds2D().getMinX();
		if (imageArea.getBounds2D().getMaxX() == 0) {
			isShapeValid = false;
		}
		newLine.setLine(ChartMaskingUtilities.translateScreenX(minX + marker.getX1(), imageArea, getChart()), marker.getY1(),  
				ChartMaskingUtilities.translateScreenX(minX + marker.getX2(), imageArea, getChart()), marker.getY2());
		return newLine;
	}
	
	private void createRangeAxisMarkers() {
		for (Entry<Line2D, Color> entry : rangeMarkerMap.entrySet()) {
			Line2D line = entry.getKey();
			Color color = entry.getValue();
			Line2D newLine = convertRangeAxisMarker(line);
			addShape(newLine, color);
		}
	}	

	public void clearRangeAxisMarkers() {
		rangeMarkerMap.clear();
		getXYPlot().setNotify(true);
	}
	
	private void makeMarkers() {
		createDomainAxisMarkers();
		createRangeAxisMarkers();
		createMarkers();
	}

	@Override
	public void addMarker(double x, double y, Color color) {
		if (color == null) {
			color = Color.BLACK;
		}
//		Point2D newPoint = ChartMaskingUtilities.translateChartPoint(new Point2D.Double(x, y), getScreenDataArea(), getChart());
//		
//		Shape newShape = ShapeUtilities.createTranslatedShape(shape, newPoint.getX(), newPoint.getY());
		Point2D marker = new Point2D.Double(x, y);
		Line2D point = new Line2D.Double(marker, marker);
		markerMap.put(point, color);
//		Shape newShape = shape.
		addShape(point, color);
		getXYPlot().setNotify(true);
	}
	
	private void createMarkers() {
		for (Entry<Line2D, Color> entry : markerMap.entrySet()) {
			Line2D line = entry.getKey();
			Color color = entry.getValue();
			addShape(line, color);
		}
	}

	public void clearAllMarkers() {
		domainMarkerMap.clear();
		rangeMarkerMap.clear();
		markerMap.clear();
		getXYPlot().setNotify(true);
	}

	public void clearMarkers() {
		markerMap.clear();
		getXYPlot().setNotify(true);
	}

	public void removeSelectedMarker() {
		domainMarkerMap.remove(selectedMarker);
		rangeMarkerMap.remove(selectedMarker);
		markerMap.remove(selectedMarker);
		getXYPlot().setNotify(true);
	}
	
	@Override
	public void removeDomainAxisMarker(double x) {
		for (Entry<Line2D, Color> entry : domainMarkerMap.entrySet()) {
			Line2D line = entry.getKey();
			if (line.getP1().getX() == x) {
				domainMarkerMap.remove(line);
				break;
			}
		}
		getXYPlot().setNotify(true);
	}
	
	@Override
	public void removeRangeAxisMarker(double y) {
		for (Entry<Line2D, Color> entry : rangeMarkerMap.entrySet()) {
			Line2D line = entry.getKey();
			if (line.getP1().getY() == y) {
				rangeMarkerMap.remove(line);
				break;
			}
		}
		getXYPlot().setNotify(true);
	}
	
	@Override
	public void removeMarker(double x, double y) {
		for (Entry<Line2D, Color> entry : markerMap.entrySet()) {
			Line2D line = entry.getKey();
			if (line.getP1().getX() == x && line.getP1().getY() == y) {
				markerMap.remove(line);
				break;
			}
		}
		getXYPlot().setNotify(true);
	}

	/**
	 * @return the isTextInputEnabled
	 */
	public boolean isTextInputEnabled() {
		return isTextInputEnabled;
	}

	/**
	 * @param isTextInputEnabled the isTextInputEnabled to set
	 */
	public void setTextInputEnabled(boolean isTextInputEnabled) {
		
		if (this.isTextInputEnabled && !isTextInputEnabled) {
			if (textInputContent != null && textInputContent.length() > 0) {
				double xNew = ChartMaskingUtilities.translateScreenX(textInputPoint.getX(), getScreenDataArea(), getChart());
				double yNew = ChartMaskingUtilities.translateScreenY(textInputPoint.getY(), getScreenDataArea(), getChart(), 0);
				textContentMap.put(new Rectangle2D.Double(xNew, yNew, 100, 15), textInputContent);
			}
			textInputContent = null;
			textInputCursorIndex = 0;
			textInputFlag = false;
			setSelectedMask(null);
			repaint();
		}
		this.isTextInputEnabled = isTextInputEnabled;
		if (selectedMask != null) {
			setSelectedMask(null);
			repaint();
		}
	}

    public String getTextInputContent() {
		return textInputContent;
	}

	public void setTextInputContent(String textInputContent) {
		this.textInputContent = textInputContent;
	}

	@Override
	public void cancelTextInput() {
		textInputContent = null;
		textInputFlag = false;
		textInputCursorIndex = 0;
	}

	/**
	 * @return the textInputCursorIndex
	 */
	public int getTextInputCursorIndex() {
		return textInputCursorIndex;
	}

	/**
	 * @param textInputCursorIndex the textInputCursorIndex to set
	 */
	public void setTextInputCursorIndex(int textInputCursorIndex) {
		this.textInputCursorIndex = textInputCursorIndex;
	}
	
	public boolean isCurrentlyInputtingText(){
		return textInputFlag;
	}
}
