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
package org.gumtree.vis.hist2d;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eclipse.swt.SWT;
import org.gumtree.vis.awt.JChartPanel;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.dataset.DatasetUtils;
import org.gumtree.vis.dataset.DatasetUtils.ExportFormat;
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.interfaces.IHist2D;
import org.gumtree.vis.interfaces.IXYZDataset;
import org.gumtree.vis.listener.XYZChartMouseEvent;
import org.gumtree.vis.mask.Abstract2DMask;
import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.ChartMaskingUtilities;
import org.gumtree.vis.mask.EllipseMask;
import org.gumtree.vis.mask.RectangleMask;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.Title;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.ExtensionFileFilter;

/**
 * @author nxi
 *
 */
public class Hist2DPanel extends JChartPanel implements IHist2D, DatasetChangeListener {

	/**
	 * 
	 */
	public static final Color MASK_INCLUSIVE_COLOR = new Color(0, 220, 0, 75);
	public static final Color MASK_EXCLUSIVE_COLOR = new Color(220, 220, 220, 75);
	private static final long serialVersionUID = -6844570933617261485L;
	private static final String RESET_COLOR_SCALE_COMMAND = "resetColorScale";
    /**
     * Masking parameters 
     */
//    private final static int numberOfMaskColors = 8;
    private static final Color axisTraceColor = Color.cyan;
    /** Remove the selected mask command. */
    private Point2D maskPoint = null;
    private JMenuItem resetColorScaleMenuItem;
    private Abstract2DMask currentMaskRectangle = null;
//    private List<RectangleMask> exclusiveMaskList;
    private Point2D maskMovePoint;
	private double chartZ = Double.NaN;

//    private static final Cursor defaultCursor = Cursor.getPredefinedCursor(
//    		Cursor.DEFAULT_CURSOR);
//    private static final Cursor westResizeCursor = Cursor.getPredefinedCursor(
//    		Cursor.W_RESIZE_CURSOR);
//    private static final Cursor eastResizeCursor = Cursor.getPredefinedCursor(
//    		Cursor.E_RESIZE_CURSOR);
//    private static final Cursor northResizeCursor = Cursor.getPredefinedCursor(
//    		Cursor.N_RESIZE_CURSOR);
//    private static final Cursor southResizeCursor = Cursor.getPredefinedCursor(
//    		Cursor.S_RESIZE_CURSOR);
//    private static final Cursor northwestResizeCursor = Cursor.getPredefinedCursor(
//    		Cursor.NW_RESIZE_CURSOR);
//    private static final Cursor northeastResizeCursor = Cursor.getPredefinedCursor(
//    		Cursor.NE_RESIZE_CURSOR);
//    private static final Cursor southwestResizeCursor = Cursor.getPredefinedCursor(
//    		Cursor.SW_RESIZE_CURSOR);
//    private static final Cursor southeastResizeCursor = Cursor.getPredefinedCursor(
//    		Cursor.SE_RESIZE_CURSOR);
//    private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
//    private static final Cursor MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

	/**
	 * @param chart
	 */
	public Hist2DPanel(JFreeChart chart) {
		 this(
		            chart,
		            StaticValues.PANEL_WIDTH,
		            StaticValues.PANEL_HEIGHT,
		            StaticValues.PANEL_MINIMUM_DRAW_WIDTH,
		            StaticValues.PANEL_MINIMUM_DRAW_HEIGHT,
		            StaticValues.PANEL_MAXIMUM_DRAW_WIDTH,
		            StaticValues.PANEL_MAXIMUM_DRAW_HEIGHT,
		            DEFAULT_BUFFER_USED,
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
	public Hist2DPanel(JFreeChart chart, boolean useBuffer) {
		this(chart, StaticValues.PANEL_WIDTH,
	            StaticValues.PANEL_HEIGHT,
	            StaticValues.PANEL_MINIMUM_DRAW_WIDTH,
	            StaticValues.PANEL_MINIMUM_DRAW_HEIGHT,
	            StaticValues.PANEL_MAXIMUM_DRAW_WIDTH,
	            StaticValues.PANEL_MAXIMUM_DRAW_HEIGHT, useBuffer,
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
	public Hist2DPanel(JFreeChart chart, boolean properties, boolean save,
			boolean print, boolean zoom, boolean tooltips) {
		this(chart,
				StaticValues.PANEL_WIDTH,
	            StaticValues.PANEL_HEIGHT,
	            StaticValues.PANEL_MINIMUM_DRAW_WIDTH,
	            StaticValues.PANEL_MINIMUM_DRAW_HEIGHT,
	            StaticValues.PANEL_MAXIMUM_DRAW_WIDTH,
	            StaticValues.PANEL_MAXIMUM_DRAW_HEIGHT,
				DEFAULT_BUFFER_USED,
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
	public Hist2DPanel(JFreeChart chart, int width, int height,
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
	public Hist2DPanel(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean copy, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties,
				copy, save, print, zoom, tooltips);
		String showColorScaleProperty = System.getProperty(StaticValues.SHOW_COLORSCALE_PROPERTY);
		if (showColorScaleProperty != null) {
			boolean showColorScale = true;
			try{
				showColorScale = Boolean.valueOf(showColorScaleProperty);
				chart.setShowSubtitle(showColorScale);
			}catch (Exception e) {
			}
		}
		createMaskColors(true);
	}

//	@Override
//	public void actionPerformed(ActionEvent event) {
//		String command = event.getActionCommand();
//		if (command.equals(REMOVE_SELECTED_MASK_COMMAND)) {
//        	removeSelectedMask();
//        	repaint();
//        } else if (command.equals(DESELECT_MASK_COMMAND)) {
//        	selectMask(null);
//        	repaint();
//        } else if (command.startsWith(SELECT_MASK_COMMAND)) {
//        	String[] commands = command.split("-", 2);
//        	if (commands.length > 1) {
//        		selectMask(commands[1]);
//            	repaint();
//        	}
//        } else {
//        	super.actionPerformed(event);
//        }
//	}
	
	private void changeMaskXMax(double x) {
		Rectangle2D frame = getSelectedMask().getRectangleFrame();
		getSelectedMask().setRectangleFrame(new Rectangle2D.Double(
				Math.min(frame.getMinX(), x), frame.getMinY(), 
				Math.abs(frame.getMinX() - x), frame.getHeight()));
	}
	
	private void changeMaskXMin(double x) {
		Rectangle2D frame = getSelectedMask().getRectangleFrame();
		getSelectedMask().setRectangleFrame(new Rectangle2D.Double(
				Math.min(frame.getMaxX(), x), frame.getMinY(), 
				Math.abs(frame.getMaxX() - x), frame.getHeight()));
	}
	
	private void changeMaskYMax(double y) {
		Rectangle2D frame = getSelectedMask().getRectangleFrame();
		getSelectedMask().setRectangleFrame(new Rectangle2D.Double(frame.getMinX(), 
				Math.min(frame.getMinY(), y), frame.getWidth(), 
				Math.abs(frame.getMinY() - y)));	
	}

	private void changeMaskYMin(double y) {
		Rectangle2D frame = getSelectedMask().getRectangleFrame();
		getSelectedMask().setRectangleFrame(new Rectangle2D.Double(frame.getMinX(), 
				Math.min(frame.getMaxY(), y), frame.getWidth(), 
				Math.abs(frame.getMaxY() - y)));
	}
	
    private void changeSelectedMask(Point2D point) {
    	switch (getMaskDragIndicator()) {
		case Cursor.MOVE_CURSOR:
			moveMask(point);
			break;
		case Cursor.W_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getDomainAxis().isInverted()) {
				changeMaskXMax(point.getX());
			} else {
				changeMaskXMin(point.getX());
			}
			break;
		case Cursor.E_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getDomainAxis().isInverted()) {
				changeMaskXMin(point.getX());
			} else {
				changeMaskXMax(point.getX());
			}
			break;
		case Cursor.N_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getRangeAxis().isInverted()) {
				changeMaskYMin(point.getY());
			} else {
				changeMaskYMax(point.getY());
			}
			break;
		case Cursor.S_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getRangeAxis().isInverted()) {
				changeMaskYMax(point.getY());
			} else {
				changeMaskYMin(point.getY());
			}
			break;
		case Cursor.NW_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getDomainAxis().isInverted()) {
				changeMaskXMax(point.getX());
			} else {
				changeMaskXMin(point.getX());
			}
			if (((XYPlot) getChart().getPlot()).getRangeAxis().isInverted()) {
				changeMaskYMin(point.getY());
			} else {
				changeMaskYMax(point.getY());
			}			
			break;
		case Cursor.NE_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getDomainAxis().isInverted()) {
				changeMaskXMin(point.getX());
			} else {
				changeMaskXMax(point.getX());
			}
			if (((XYPlot) getChart().getPlot()).getRangeAxis().isInverted()) {
				changeMaskYMin(point.getY());
			} else {
				changeMaskYMax(point.getY());
			}
			break;
		case Cursor.SW_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getDomainAxis().isInverted()) {
				changeMaskXMax(point.getX());
			} else {
				changeMaskXMin(point.getX());
			}
			if (((XYPlot) getChart().getPlot()).getRangeAxis().isInverted()) {
				changeMaskYMax(point.getY());
			} else {
				changeMaskYMin(point.getY());
			}
			break;
		case Cursor.SE_RESIZE_CURSOR:
			if (((XYPlot) getChart().getPlot()).getDomainAxis().isInverted()) {
				changeMaskXMin(point.getX());
			} else {
				changeMaskXMax(point.getX());
			}
			if (((XYPlot) getChart().getPlot()).getRangeAxis().isInverted()) {
				changeMaskYMax(point.getY());
			} else {
				changeMaskYMin(point.getY());
			}
			break;
		default:
			break;
		}
    	fireMaskUpdateEvent(getSelectedMask());
	}
    
//    @Override
//    public void createChartPrintJob() {
//    	setCursor(WAIT_CURSOR);
//    	PrinterJob job = PrinterJob.getPrinterJob();
//    	setCursor(WAIT_CURSOR);
//    	PageFormat pf = job.defaultPage();
//    	PageFormat pf2 = job.pageDialog(pf);
//    	if (pf2 != pf) {
//    		job.setPrintable(this, pf2);
//    		try {
//    			job.print();
//    		}
//    		catch (PrinterException e) {
//    			JOptionPane.showMessageDialog(this, e);
//    		} finally {
//    			setCursor(defaultCursor);
//    		}
//    	}
//    	setCursor(defaultCursor);
//    }

//    @Override
//    protected JPopupMenu createPopupMenu(boolean properties, boolean copy,
//    		boolean save, boolean print, boolean zoom) {
//    	JPopupMenu result = super.createPopupMenu(properties, copy, save, print, zoom);
//        this.removeSelectedMaskMenuItem = new JMenuItem();
//        this.removeSelectedMaskMenuItem.setActionCommand(REMOVE_SELECTED_MASK_COMMAND);
//        this.removeSelectedMaskMenuItem.addActionListener(this);
//        result.addSeparator();
//        result.add(removeSelectedMaskMenuItem);
//        maskManagementMenu = new JMenu("Mask Management");
//        result.add(maskManagementMenu);
//        return result;
//    }
    
//    private void createMaskColors() {
//    	inclusiveMaskColor = new Color[numberOfMaskColors];
//    	exclusiveMaskColor = new Color[numberOfMaskColors];
//    	int interval = 155 / numberOfMaskColors;
//    	for (int i = 0; i < numberOfMaskColors; i++) {
//    		int value = 255 - i * interval;
//    		inclusiveMaskColor[i] = new Color(0, value, 0, 100);
//    		exclusiveMaskColor[i] = new Color(value, value, value, 100);
//    	}
//	}

//    @Override
//    protected void displayPopupMenu(int x, int y) {
//        if (this.removeSelectedMaskMenuItem != null) {
//        	boolean isRemoveMenuEnabled = false;
//        	if (this.selectedMask != null) {
//        		Abstract2DMask screenMask = ChartMaskingUtilities.translateChartRectangle(
//        				selectedMask, getScreenDataArea(), getChart());
//        		if (screenMask.getShape().contains(x, y)) {
//        			isRemoveMenuEnabled = true;
//        		}
//        	}
//        	this.removeSelectedMaskMenuItem.setEnabled(isRemoveMenuEnabled);
//        	if (isRemoveMenuEnabled) {
//        		removeSelectedMaskMenuItem.setVisible(true);
//        		removeSelectedMaskMenuItem.setText("Remove " + selectedMask.getName());
//        	} else {
//        		//        		removeSelectedMaskMenuItem.setText("Mask Management");
//        		removeSelectedMaskMenuItem.setVisible(false);
//        	}
//        }
//        maskManagementMenu.removeAll();
//        if (maskList.size() > 0) {
//        	maskManagementMenu.setEnabled(true);
//        	JMenuItem selectNoneMaskItem = new JRadioButtonMenuItem();
//        	selectNoneMaskItem.setText("Select None");
//        	selectNoneMaskItem.setActionCommand(DESELECT_MASK_COMMAND);
//        	selectNoneMaskItem.addActionListener(this);
//        	maskManagementMenu.add(selectNoneMaskItem);
//        	boolean isInShade = false;
//        	for (Abstract2DMask mask : maskList) {
//        		Abstract2DMask screenMask = ChartMaskingUtilities.translateChartRectangle(
//        				mask, getScreenDataArea(), getChart());
//        		if (screenMask.getShape().contains(x, y)) {
//        			JMenuItem selectMaskItem = new JRadioButtonMenuItem();
//        			selectMaskItem.setText("Select " + mask.getName());
//        			selectMaskItem.setActionCommand(SELECT_MASK_COMMAND 
//        					+ "-" + mask.getName());
//        			if (mask == selectedMask) {
//        				selectMaskItem.setSelected(true);
//        			}
//        			selectMaskItem.addActionListener(this);
//        			maskManagementMenu.add(selectMaskItem);
//        			isInShade = true;
//        		}
//        	}
//        	if (isInShade) {
//        		if (selectedMask == null) {
//        			selectNoneMaskItem.setSelected(true);
//        		}
//        	} else {
//        		for (Abstract2DMask mask : maskList) {
//        			JMenuItem selectMaskItem = new JRadioButtonMenuItem();
//        			selectMaskItem.setText("Select " + mask.getName());
//        			selectMaskItem.setActionCommand(SELECT_MASK_COMMAND 
//        					+ "-" + mask.getName());
//        			if (mask == selectedMask) {
//        				selectMaskItem.setSelected(true);
//        			}
//        			selectMaskItem.addActionListener(this);
//        			maskManagementMenu.add(selectMaskItem);
//        		}
//        		selectNoneMaskItem.setSelected(selectedMask == null);
//        	}
//        } else {
//        	maskManagementMenu.setEnabled(false);
//        }
//    	super.displayPopupMenu(x, y);
//    }
    
//	/**
//     * Copies the current chart to the system clipboard.
//     * 
//     * @since 1.0.13
//     */
//	@Override
//    public void doCopy() {
//        final Clipboard systemClipboard
//                = Toolkit.getDefaultToolkit().getSystemClipboard();
//        Rectangle2D screenArea = getScreenDataArea();
//        final ChartTransferableWithMask selection = new ChartTransferableWithMask(
//        		getChart(), getWidth(), getHeight(), screenArea, maskList);
//        Cursor currentCursor = getCursor();
//        setCursor(WAIT_CURSOR);
//        systemClipboard.setContents(selection, null);
//        setCursor(currentCursor);
//    }

    private void showPropertyEditor(int tabIndex) {
    	Hist2DChartEditor editor = new Hist2DChartEditor(getChart(), this);
    	editor.getTabs().setSelectedIndex(tabIndex);
        int result = JOptionPane.showConfirmDialog(this, editor,
                localizationResources.getString("Chart_Properties"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            editor.updateChart(getChart());
        }
    }
    
	@Override
	public void doEditChartProperties() {
		showPropertyEditor(0);
	}
	
	public void doEditMaskProperties() {
		showPropertyEditor(1);
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
//						int filterIndex = fileDialog.getFilterIndex();
//						if (filterIndex == 0) {
//							if (!filename.endsWith(".png")) {
//								filename = filename + ".png";
//							} 
//							try {
//				           		ChartMaskingUtilities.writeChartAsPNG(new File(filename), getChart(), 
//			            				getWidth(), getHeight(), null, getScreenDataArea(), 
//			            				getMasks());
//						} catch (IOException e) {
//								handleException(e);
//							}
//						} else if (filterIndex == 1) {
//							if (!filename.endsWith(".jpg")) {
//								filename = filename + ".jpg";
//							}
//							try {
//			            		ChartMaskingUtilities.writeChartAsJPEG(new File(filename), getChart(),
//			            				getWidth(), getHeight(), null, getScreenDataArea(), getMasks());
//							} catch (IOException e) {
//								handleException(e);
//							}
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
//		});
//	}

    
	protected int findSelectedMask(int x, int y) {
        if (getSelectedMask() != null && !getSelectedMask().getRectangleFrame().isEmpty()) {
        	Rectangle2D screenArea = getScreenDataArea();
        	Rectangle2D maskArea = ChartMaskingUtilities.translateChartRectangle(getSelectedMask(), 
        			getScreenDataArea(), getChart()).getRectangleFrame();
        	Rectangle2D intersect = screenArea.createIntersection(maskArea);
        	Point2D point = new Point2D.Double(x, y);
        	double minX = maskArea.getMinX();
        	double maxX = maskArea.getMaxX();
        	double minY = maskArea.getMinY();
        	double maxY = maskArea.getMaxY();
        	double width = maskArea.getWidth();
        	double height = maskArea.getHeight();
        	if (!intersect.isEmpty() && screenArea.contains(point)) {
//        		if (y > minY && y < maxY) {
//        			if (minX > screenArea.getMinX() + 1 
//        					&& minX < screenArea.getMaxX() - 1) {
//        					if (x > minX - 4 && x < minX + (width < 8 ? width / 2 : 4)) {
//        						return Cursor.W_RESIZE_CURSOR;
//        					} 
//        			}
//        			if (maxX > screenArea.getMinX() + 1 
//        					&& maxX < screenArea.getMaxX() - 1) {
//        					if (x > maxX - (width < 8 ? width / 2 : 4) && x < maxX + 4) {
//        						return Cursor.E_RESIZE_CURSOR;
//        					} 
//        			}
//        		}
        		if (height > 8 && width > 8) {
        			Rectangle2D center = new Rectangle2D.Double(minX + 4, minY + 4, 
        					width - 8, height - 8);
        			if (screenArea.createIntersection(center).contains(point)) {
        				return Cursor.MOVE_CURSOR;
        			}
        		}
        		if (height > 8) {
        			Rectangle2D west = new Rectangle2D.Double(minX - 4, minY + 4, 
        					width < 8 ? width / 2 + 4 : 8, height - 8);
        			if (screenArea.createIntersection(west).contains(point)) {
        				return Cursor.W_RESIZE_CURSOR;
        			}
        			Rectangle2D east = new Rectangle2D.Double(maxX - (width < 8 ? width / 2 : 4), 
        					minY + 4, width < 8 ? width / 2 + 4 : 8, height - 8);
        			if (screenArea.createIntersection(east).contains(point)) {
        				return Cursor.E_RESIZE_CURSOR;
        			}
        		}
        		if (width > 8) {
        			Rectangle2D north = new Rectangle2D.Double(minX + 4, minY - 4, 
        					width - 8, height < 8 ? height / 2 + 4 : 8);
        			if (screenArea.createIntersection(north).contains(point)) {
        				return Cursor.N_RESIZE_CURSOR;
        			}
        			Rectangle2D south = new Rectangle2D.Double(minX + 4,
        					maxY - (height < 8 ? height / 2 : 4), 
        					width - 8, height < 8 ? height / 2 + 4 : 8);
        			if (screenArea.createIntersection(south).contains(point)) {
        				return Cursor.S_RESIZE_CURSOR;
        			}
        		}
        		Rectangle2D northwest = new Rectangle2D.Double(minX - 4, minY - 4,
        				width < 8 ? width / 2 + 4 : 8, height < 8 ? height / 2 + 4 : 8);
        		if (screenArea.createIntersection(northwest).contains(point)) {
        			return Cursor.NW_RESIZE_CURSOR;
        		}
        		Rectangle2D northeast = new Rectangle2D.Double(maxX - (width < 8 ? width / 2 : 4), 
        				minY - 4, width < 8 ? width / 2 + 4 : 8, height < 8 ? height / 2 + 4 : 8);
        		if (screenArea.createIntersection(northeast).contains(point)) {
        			return Cursor.NE_RESIZE_CURSOR;
        		}
        		Rectangle2D southwest = new Rectangle2D.Double(minX - 4, 
        				maxY - (height < 8 ? height / 2 : 4),
        				width < 8 ? width / 2 + 4 : 8, height < 8 ? height / 2 + 4 : 8);
        		if (screenArea.createIntersection(southwest).contains(point)) {
        			return Cursor.SW_RESIZE_CURSOR;
        		}
        		Rectangle2D southeast = new Rectangle2D.Double(maxX - (width < 8 ? width / 2 : 4), 
        				maxY - (height < 8 ? height / 2 : 4),
        				width < 8 ? width / 2 + 4 : 8, height < 8 ? height / 2 + 4 : 8);
        		if (screenArea.createIntersection(southeast).contains(point)) {
        			return Cursor.SE_RESIZE_CURSOR;
        		}
        	}
//        	System.out.println("intersect X:[" + intersect.getMinX() + ", " + 
//        			(intersect.getMinX() + intersect.getWidth()) + 
//        			"], Y:[" + intersect.getMinY() + ", " + 
//        			(intersect.getMinY() + intersect.getHeight()) +
//        			"], x=" + point.getX() + ", y=" + point.getY() + 
//        			" " + intersect.contains(point));
        }
        return Cursor.DEFAULT_CURSOR;
	}

//	private Color getNextMaskColor(boolean isInclusive){
//    	Color[] colorSeries = isInclusive ? inclusiveMaskColor : exclusiveMaskColor;
//    	for (int i = 0; i < numberOfMaskColors; i++) {
//    		boolean isUsed = false;
//    		for (Abstract2DMask mask : maskList) {
//    			if (colorSeries[i].equals(mask.getFillColor())) {
//    				isUsed = true;
//    				break;
//    			}
//    		}
//    		if (!isUsed) {
//    			return colorSeries[i];
//    		}
//    	}
//    	Color lastColor = null;
//    	for (int i = maskList.size() - 1; i >= 0; i--) {
//    		Abstract2DMask mask = maskList.get(i);
//    		if (mask.isInclusive() == isInclusive) {
//    			lastColor = maskList.get(i).getFillColor();
//    			break;
//    		}
//    	}
//    	int nextColorIndex = 0;
//    	for (int i = 0; i < numberOfMaskColors; i++) {
//    		if (colorSeries[i].equals(lastColor)) {
//    			nextColorIndex = i + 1;
//    			if (nextColorIndex >= numberOfMaskColors) {
//    				nextColorIndex = 0;
//    			}
//    		}
//    	}
//    	return colorSeries[nextColorIndex];
//    }
    
//	public boolean isChartPointInScreen(Point point) {
//    	XYPlot plot = getChart().getXYPlot();
//    	Range domainSection = plot.getDomainAxis().getRange();
//    	Range rangeSection = plot.getDomainAxis().getRange();
//    	return domainSection.contains(point.x) && rangeSection.contains(point.y);
//    }
    
    /**
     * Receives notification of mouse clicks on the panel. These are
     * translated and passed on to any registered {@link ChartMouseListener}s.
     *
     * @param event  Information about the mouse event.
     */
    public void mouseClicked(MouseEvent event) {

        Insets insets = getInsets();
        int x = (int) ((event.getX() - insets.left) / getScaleX());
        int y = (int) ((event.getY() - insets.top) / getScaleY());

        double chartX = 0;
        double chartY = 0;
        double chartZ = 0;
        setAnchor(new Point2D.Double(x, y));
        if (getChart() == null) {
            return;
        }
        
//        this.chart.setNotify(true);  // force a redraw
        // new entity code...
//        Object[] listeners = this.chartMouseListeners.getListeners(
//                ChartMouseListener.class);
        
        ChartEntity entity = null;
        if (getChartRenderingInfo() != null) {
            EntityCollection entities = getChartRenderingInfo().getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
            }
        }
        
        if (entity instanceof XYItemEntity) {
        	IDataset dataset = (IDataset) ((XYItemEntity) entity).getDataset();
        	int item = ((XYItemEntity) entity).getItem();
        	chartX = dataset.getXValue(0, item);
        	chartY = dataset.getYValue(0, item);
        	chartZ = ((XYZDataset) dataset).getZValue(0, item);
//        	System.out.println("px=" + x + ", py=" + y);
//        	System.out.println("x=" + chartX + ", y=" + chartY + ", z=" + chartZ);
        	//        Point2D trsPoint = translateChartPoint(new Point2D.Double(chartX, chartY));
        	//        System.out.println("tx=" + trsPoint.getX() + ", y=" + trsPoint.getY());
//            if ((event.getModifiers() & maskingSelectionMask) != 0) {
//            	selectMask(chartX, chartY);
//            	repaint();
//            } else {
//            	if (getSelectedMask() != null && (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 
//            			&& !getSelectedMask().getShape().contains(chartX, chartY)) {
//            		selectMask(null);
//            		repaint();
//            	}
//            }
        	if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        		selectMask(chartX, chartY);
        		repaint();
        	}
        }
        
        Object[] listeners = getListeners(ChartMouseListener.class);
        
        XYZChartMouseEvent chartEvent = new XYZChartMouseEvent(getChart(), event,
                entity);
        chartEvent.setXYZ(chartX, chartY, chartZ);
        for (int i = listeners.length - 1; i >= 0; i -= 1) {
            ((ChartMouseListener) listeners[i]).chartMouseClicked(chartEvent);
        }
    }
    
    /**
     * Handles a 'mouse dragged' event.
     *
     * @param e  the mouse event.
     */
    public void mouseDragged(MouseEvent e) {

   		setHorizontalTraceLocation(e.getX());
   		setVerticalTraceLocation(e.getY());
    	
        Insets insets = getInsets();
        int x = (int) ((e.getX() - insets.left) / getScaleX());
        int y = (int) ((e.getY() - insets.top) / getScaleY());

        EntityCollection entities = null;
        ChartEntity entity = null;
        if (getChartRenderingInfo() != null) {
            entities = getChartRenderingInfo().getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
            }
        }
    	if (entity instanceof XYItemEntity) {
        	IDataset dataset = (IDataset) ((XYItemEntity) entity).getDataset();
        	int item = ((XYItemEntity) entity).getItem();
        	setChartX(dataset.getXValue(0, item));
        	setChartY(dataset.getYValue(0, item));
        	setChartZ(((XYZDataset) dataset).getZValue(0, item));
        }

//        if (isMaskingEnabled() && (e.getModifiers() & maskingKeyMask) != 0) {
    	if (isMaskingEnabled()) {
        	int cursorType = findSelectedMask(e.getX(), e.getY());
        	setCursor(Cursor.getPredefinedCursor(cursorType));
        } else if (getCursor() != defaultCursor) {
        	setCursor(defaultCursor);
        }
        
        // we can only generate events if the panel's chart is not null
        // (see bug report 1556951)
        Object[] listeners = getListeners(ChartMouseListener.class);
        if (getChart() != null) {
            XYZChartMouseEvent event = new XYZChartMouseEvent(getChart(), e, entity);
            event.setXYZ(getChartX(), getChartY(), getChartZ());
            for (int i = listeners.length - 1; i >= 0; i -= 1) {
                ((ChartMouseListener) listeners[i]).chartMouseMoved(event);
            }
        }
        if (getMaskDragIndicator() != Cursor.DEFAULT_CURSOR && getSelectedMask() != null 
        		&& (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        	changeSelectedMask(e, entities);
        } else if (isMaskingEnabled() && (e.getModifiers() & maskingKeyMask) != 0) {
        	makeNewMask(e, entities);
        } else {
        	super.mouseDragged(e);
        }
    }
    
    private void changeSelectedMask(MouseEvent e, EntityCollection entities) {
    	// Do masking service
    	// if no initial masking point was set, ignore dragging...
    	Rectangle2D screenArea = getScreenDataArea();
    	Point2D screenPoint = translateScreenToJava2D(e.getPoint());
//    	System.out.println("screen point is [" + screenPoint.getX() + ", " +
//    			screenPoint.getY() + "]");
    	if (screenArea.contains(screenPoint)) {
    		Point2D chartPoint = translateScreenToChart(screenPoint);
    		if (chartPoint != null) {
//    			System.out.println("chart point is [" + chartPoint.getX() + ", " +
//    					chartPoint.getY() + "]");
    			changeSelectedMask(chartPoint);
    			repaint();
    		}
    	}
	}

	private void makeNewMask(MouseEvent e, EntityCollection entities) {
    	if (this.maskPoint == null) {
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
    	Rectangle2D scaledDataArea = getScreenDataArea(
    			(int) this.maskPoint.getX(), (int) this.maskPoint.getY());
    	// Working on the current mask. Only create one new mask per drag-drawing.
    	if (currentMaskRectangle == null) {
        	boolean isInclusive = (e.getModifiers() & maskingExclusiveMask) == 0;
        	boolean isEllipse = (e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0;
        	if (isEllipse) {
        		currentMaskRectangle = new EllipseMask(isInclusive);
        	} else {
        		currentMaskRectangle = new RectangleMask(isInclusive);
        	}
//        	currentMaskRectangle.setFillColor(getNextMaskColor(isInclusive));
//        	getMasks().add(currentMaskRectangle);
        	addMask(currentMaskRectangle);
    	}
    	// selected rectangle shouldn't extend outside the data area...
    	double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
    	double ymax = Math.min(e.getY(), scaledDataArea.getMaxY());
    	// Update the current mask.
        ChartEntity startEntity = null;
        ChartEntity endEntity = null;
        boolean isMaskUpdated = false;
        if (entities != null) {
//            EntityCollection entities = this.info.getEntityCollection();
//            if (entities != null) {
        	Insets insets = getInsets();
        	double screenX = (maskPoint.getX() - insets.left) / getScaleX();
        	double screenY = (maskPoint.getY() - insets.top) / getScaleY();
        	startEntity = entities.getEntity(screenX, screenY);

        	screenX = (xmax - insets.left) / getScaleX();
        	screenY = (ymax - insets.top) / getScaleY();
        	if (screenX >= scaledDataArea.getMaxX()) {
        		screenX = scaledDataArea.getMaxX() - 0.001;
        	}
        	if (screenY >= scaledDataArea.getMaxY()) {
        		screenY = scaledDataArea.getMaxY() - 0.001;
        	}
        	endEntity = entities.getEntity(screenX, screenY);
//        	System.out.println("Try to update mask");
        	if (startEntity instanceof XYItemEntity && endEntity instanceof XYItemEntity) {
        		isMaskUpdated = updateCurrentMaskRectangle((XYItemEntity) startEntity, 
        				(XYItemEntity) endEntity);
        	}
//            }
        }
        if (!isMaskUpdated) {
        	currentMaskRectangle.setRectangleFrame(new Rectangle2D.Double(maskPoint.getX(), 
        			this.maskPoint.getY(), xmax - this.maskPoint.getX(), 
        			ymax - this.maskPoint.getY()));
        }
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
    /**
     * Implementation of the MouseMotionListener's method.
     *
     * @param e  the event.
     */
    public void mouseMoved(MouseEvent e) {
    	if (getHorizontalAxisTrace()) {
    		setHorizontalTraceLocation(e.getX());
    	}
    	if (getVerticalAxisTrace()) {
    		setVerticalTraceLocation(e.getY());
    	}
    	
        Insets insets = getInsets();
        int x = (int) ((e.getX() - insets.left) / getScaleX());
        int y = (int) ((e.getY() - insets.top) / getScaleY());

        ChartEntity entity = null;
        if (getChartRenderingInfo() != null) {
            EntityCollection entities = getChartRenderingInfo().getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
            }
        }
    	if (entity instanceof XYItemEntity) {
        	IDataset dataset = (IDataset) ((XYItemEntity) entity).getDataset();
        	int item = ((XYItemEntity) entity).getItem();
        	setChartX(dataset.getXValue(0, item));
        	setChartY(dataset.getYValue(0, item));
        	setChartZ(((XYZDataset) dataset).getZValue(0, item));
        }
    	
    	if (getHorizontalAxisTrace() || getVerticalAxisTrace() || isToolTipFollowerEnabled()) {
    		repaint();
    	}
    	
//        if ((e.getModifiers() & maskingKeyMask) != 0) {
//        	int cursorType = findSelectedMask(e.getX(), e.getY());
//        	setCursor(Cursor.getPredefinedCursor(cursorType));
//        } else if (getCursor() != defaultCursor) {
//        	setCursor(defaultCursor);
//        }
//        
        // we can only generate events if the panel's chart is not null
        // (see bug report 1556951)
        Object[] listeners = getListeners(ChartMouseListener.class);
        
        if (getChart() != null) {
            XYZChartMouseEvent event = new XYZChartMouseEvent(getChart(), e, entity);
            event.setXYZ(getChartX(), getChartY(), getChartZ());
            for (int i = listeners.length - 1; i >= 0; i -= 1) {
                ((ChartMouseListener) listeners[i]).chartMouseMoved(event);
            }
        }
        super.mouseMoved(e);

    }

    @Override
	public void mousePressed(MouseEvent e) {
//        int mods = e.getModifiers();
//        if (isMaskingEnabled() && (mods & maskingKeyMask) != 0) {
        if (isMaskingEnabled()) {
        	// Prepare masking service.
        	int cursorType = findSelectedMask(e.getX(), e.getY());
        	if (cursorType == Cursor.DEFAULT_CURSOR) {
        		Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
        		if (screenDataArea != null) {
        			this.maskPoint = getPointInRectangle(e.getX(), e.getY(),
        					screenDataArea);
        		} else {
        			this.maskPoint = null;
        		}
        	} else {
        		if (cursorType == Cursor.MOVE_CURSOR){
        			Point2D point = translateScreenToChart(
        					translateScreenToJava2D(e.getPoint()));
        			if (point != null) {
        				this.maskMovePoint = point;
        			}
        		}
        		setMaskDragIndicator(cursorType);
        	}
        } 
        if (getMaskDragIndicator() == Cursor.DEFAULT_CURSOR){
        	if (e.getX() < getScreenDataArea().getMaxX()) {
        		super.mousePressed(e);
        	}
        }
	}

    /**
     * Handles a 'mouse released' event.  On Windows, we need to check if this
     * is a popup trigger, but only if we haven't already been tracking a zoom
     * rectangle.
     *
     * @param e  information about the event.
     */
    public void mouseReleased(MouseEvent e) {
    	if (currentMaskRectangle != null) {
        	// reset masking service.
        	maskPoint = null;
        	currentMaskRectangle = null;
    	} else {
//        } else if (getScreenDataArea().contains(e.getPoint())){
        	super.mouseReleased(e);
        }
        setMaskDragIndicator(Cursor.DEFAULT_CURSOR);
        this.maskMovePoint = null;
    }
    
    private void moveMask(Point2D point) {
		if (maskMovePoint != null && getSelectedMask() != null) {
			Rectangle2D frame = getSelectedMask().getRectangleFrame();
			getSelectedMask().setRectangleFrame(new Rectangle2D.Double(frame.getMinX() 
					+ point.getX() - maskMovePoint.getX(), frame.getMinY() 
					+ point.getY() - maskMovePoint.getY(), frame.getWidth(), 
					frame.getHeight()));
			maskMovePoint = point;
			fireMaskUpdateEvent(getSelectedMask());
		}
	}
    
    @Override
    public void moveSelectedMask(int direction) {
    	Abstract2DMask selectedMask = getSelectedMask();
		if (selectedMask == null) {
			return;
		}
		double blockWidth = 0;
		double blockHeight = 0;
		try {
			XYBlockRenderer render = (XYBlockRenderer) ((XYPlot) 
					getChart().getPlot()).getRenderer();
			blockWidth = render.getBlockWidth();
			blockHeight = render.getBlockHeight();
			if (getChart().getXYPlot().getDomainAxis().isInverted()) {
				blockWidth = - blockHeight;
			}
			if (getChart().getXYPlot().getRangeAxis().isInverted()) {
				blockHeight = - blockHeight;
			}
		}catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Rectangle2D frame = selectedMask.getRectangleFrame();
		switch (direction) {
		case SWT.ARROW_UP:
			selectedMask.setRectangleFrame(new Rectangle2D.Double(frame.getMinX(), 
					frame.getMinY() + blockHeight, frame.getWidth(), frame.getHeight()));
			break;
		case SWT.ARROW_LEFT:
			selectedMask.setRectangleFrame(new Rectangle2D.Double(frame.getMinX() - blockWidth, 
					frame.getMinY(), frame.getWidth(), frame.getHeight()));
			break;
		case SWT.ARROW_RIGHT:
			selectedMask.setRectangleFrame(new Rectangle2D.Double(frame.getMinX() + blockWidth, 
					frame.getMinY(), frame.getWidth(), frame.getHeight()));
			break;
		case SWT.ARROW_DOWN:
			selectedMask.setRectangleFrame(new Rectangle2D.Double(frame.getMinX(), 
					frame.getMinY() - blockHeight, frame.getWidth(), frame.getHeight()));
			break;
		default:
			break;
		}
		repaint();
		fireMaskUpdateEvent(getSelectedMask());
	}

    @Override
    protected void drawToolTipFollower(Graphics2D g2, int x, int y) {
    	Rectangle2D dataArea = getScreenDataArea();
    	if (((int) dataArea.getMinX() < x) && (x < (int) dataArea.getMaxX()) && 
    			((int) dataArea.getMinY() < y) && (y < (int) dataArea.getMaxY())) {
    		String text = String.format("(%.2f, %.2f, %.2f)", getChartX(), getChartY(), 
    				getChartZ());
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
    		
    		Rectangle2D toolTipArea = new Rectangle2D.Double(xLoc, yLoc, width, height);
    		g2.setColor(Color.white);
    		g2.fill(toolTipArea);
    		g2.setColor(Color.black);
    		g2.drawString(text, xLoc + 3, yLoc + 11);
    	}
    }
    
//	@Override
//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		Graphics2D g2 = (Graphics2D) g.create();
//        ChartMaskingUtilities.drawMaskRectangle(g2, getScreenDataArea(), 
//        		maskList, selectedMask, getChart());
//        if (getHorizontalAxisTrace()) {
//        	drawHorizontalAxisTrace(g2, horizontalTraceLocation);
//        }
//        if (getVerticalAxisTrace()) {
//        	drawVerticalAxisTrace(g2, verticalTraceLocation);
//        }
//        if (isToolTipFollowerEnabled) {
//        	drawToolTipFollower(g2, horizontalTraceLocation, verticalTraceLocation);
//        }
//        g2.dispose();
//	}

// 	/**
//     * Prints the chart on a single page.
//     *
//     * @param g  the graphics context.
//     * @param pf  the page format to use.
//     * @param pageIndex  the index of the page. If not <code>0</code>, nothing
//     *                   gets print.
//     *
//     * @return The result of printing.
//     */
//    @Override
//    public int print(Graphics g, PageFormat pf, int pageIndex) {
//
//        if (pageIndex != 0) {
//            return NO_SUCH_PAGE;
//        }
//        Graphics2D g2 = (Graphics2D) g;
//        double x = pf.getImageableX();
//        double y = pf.getImageableY();
//        double w = pf.getImageableWidth();
//        double h = pf.getImageableHeight();
//        double screenWidth = getWidth();
//        double screenHeight = getHeight();
//        double widthRatio = w / screenWidth;
//        double heightRatio = h / screenHeight;
//        double overallRatio = 1;
//        overallRatio = widthRatio < heightRatio ? widthRatio : heightRatio;
//        Rectangle2D printArea = new Rectangle2D.Double(x, y, screenWidth * overallRatio, 
//        		screenHeight * overallRatio);
//        XYPlot plot = (XYPlot) getChart().getPlot();
//        Font domainFont = plot.getDomainAxis().getLabelFont();
//        int domainSize = domainFont.getSize();
//        Font rangeFont = plot.getRangeAxis().getLabelFont();
//        int rangeSize = rangeFont.getSize();
//        Font titleFont = getChart().getTitle().getFont();
//        int titleSize = titleFont.getSize();
//        Font domainScaleFont = plot.getDomainAxis().getTickLabelFont();
//        int domainScaleSize = domainScaleFont.getSize();
//        Font rangeScaleFont = plot.getRangeAxis().getTickLabelFont();
//        int rangeScaleSize = rangeScaleFont.getSize();
//        plot.getDomainAxis().setLabelFont(domainFont.deriveFont(
//        		(float) (domainSize * overallRatio)));
//        plot.getRangeAxis().setLabelFont(rangeFont.deriveFont(
//        		(float) (rangeSize * overallRatio)));
//        getChart().getTitle().setFont(titleFont.deriveFont(
//        		(float) (titleSize * overallRatio)));
//        plot.getDomainAxis().setTickLabelFont(domainScaleFont.deriveFont(
//        		(float) (domainScaleSize * overallRatio)));
//        plot.getRangeAxis().setTickLabelFont(rangeScaleFont.deriveFont(
//        		(float) (rangeScaleSize * overallRatio)));
//        
//        Rectangle2D chartArea = (Rectangle2D) printArea.clone();
//        getChart().getPadding().trim(chartArea);
//        AxisUtilities.trimTitle(chartArea, g2, getChart().getTitle(), getChart().getTitle().getPosition());
//        
//        Axis scaleAxis = null;
//        Font scaleAxisFont = null;
//        int scaleAxisFontSize = 0;
//        for (Object object : getChart().getSubtitles()) {
//        	Title title = (Title) object;
//        	if (title instanceof PaintScaleLegend) {
//        		scaleAxis = ((PaintScaleLegend) title).getAxis();
//        		scaleAxisFont = scaleAxis.getTickLabelFont();
//        		scaleAxisFontSize = scaleAxisFont.getSize();
//        		scaleAxis.setTickLabelFont(scaleAxisFont.deriveFont(
//        				(float) (scaleAxisFontSize * overallRatio)));
//        	}
//        	AxisUtilities.trimTitle(chartArea, g2, title, title.getPosition());
//        }
//        AxisSpace axisSpace = AxisUtilities.calculateAxisSpace(
//        		getChart().getXYPlot(), g2, chartArea);
//        Rectangle2D dataArea = axisSpace.shrink(chartArea, null);
//        getChart().getXYPlot().getInsets().trim(dataArea);
//        getChart().getXYPlot().getAxisOffset().trim(dataArea);
//        
////        Rectangle2D screenArea = getScreenDataArea();
////        Rectangle2D visibleArea = getVisibleRect();
////        Rectangle2D printScreenArea = new Rectangle2D.Double(screenArea.getMinX() * overallRatio + x, 
////        		screenArea.getMinY() * overallRatio + y, 
////        		printArea.getWidth() - visibleArea.getWidth() + screenArea.getWidth(), 
////        		printArea.getHeight() - visibleArea.getHeight() + screenArea.getHeight());
//
//        getChart().draw(g2, printArea, getAnchor(), null);
//        ChartMaskingUtilities.drawMaskRectangle(g2, dataArea, 
//        		maskList, null, getChart(), overallRatio);
//        plot.getDomainAxis().setLabelFont(domainFont);
//        plot.getRangeAxis().setLabelFont(rangeFont);
//        getChart().getTitle().setFont(titleFont);
//        plot.getDomainAxis().setTickLabelFont(domainScaleFont);
//        plot.getRangeAxis().setTickLabelFont(rangeScaleFont);
//        if (scaleAxis != null) {
//        	scaleAxis.setTickLabelFont(scaleAxisFont);
//        }
//        return PAGE_EXISTS;
//
//    }

//    public void removeSelectedMask() {
//    	if (selectedMask != null) {
//    		maskList.remove(selectedMask);
//    		selectedMask = null;
//    		repaint();
//    	}
//	}

//    private void selectMask(String maskName) {
//    	if (maskName == null) {
//    		selectedMask = null;
//    	} else {
//    		for (Abstract2DMask mask : maskList) {
//    			if (maskName.equals(mask.getName())) {
//    				selectedMask = mask;
//    				break;
//    			}
//    		}
//    	}
//    }
    
    protected void selectMask(double chartX, double chartY) {
    	Abstract2DMask selectedMask = getSelectedMask();
    	if (selectedMask == null) {
    		for (AbstractMask mask : getMasks()) {
    			if (mask instanceof Abstract2DMask) {
    				if (((Abstract2DMask) mask).getShape().contains(
    						chartX, chartY)) {
    					setSelectedMask(mask);
    					break;
    				}
    			}
    		}
    	} else {
    		Abstract2DMask newSelection = null;
    		if (getMasks().contains(selectedMask)) {
    			int index = getMasks().indexOf(selectedMask);
    			for (int i = index + 1; i < getMasks().size(); i++) {
    				AbstractMask mask = getMasks().get(i);
    				if (mask instanceof Abstract2DMask) {
    					if (((Abstract2DMask) mask).getShape().contains(
    							chartX, chartY)) {
    						newSelection = (Abstract2DMask) mask;
    						break;
    					}
    				}
    			}
    			if (newSelection == null) {
    				for (int i = 0; i < index; i++) {
    					AbstractMask mask = getMasks().get(i);
    					if (mask instanceof Abstract2DMask) {
    						if (((Abstract2DMask) mask).getShape().contains(
    								chartX, chartY)) {
    							newSelection = (Abstract2DMask) mask;
    							break;
    						}
    					}
    				}
    			}
    			setSelectedMask(newSelection);
    		} else {
    			selectedMask = null;
    			selectMask(chartX, chartY);
    		}
    	}
//    	if (selectedMask != null)
//    		System.out.println("selected mask: x[" + selectedMask.x + ", " 
//    				+ (selectedMask.x + selectedMask.width) + "] y[" + 
//    				selectedMask.y + ", " + (selectedMask.y + selectedMask.height));
	}
    
//    private Point2D translateScreenToChart(Point2D point) {
//        EntityCollection entities = getChartRenderingInfo().getEntityCollection();
//        ChartEntity entity = entities.getEntity(point.getX(), point.getY());
//        if (entity instanceof XYItemEntity) {
//        	XYDataset dataset = ((XYItemEntity) entity).getDataset();
//        	int item = ((XYItemEntity) entity).getItem();
//        	double chartX = dataset.getXValue(0, item);
//        	double chartY = dataset.getYValue(0, item);
////        	double chartZ = ((XYZDataset) dataset).getZValue(0, item);
//        	return new Point2D.Double(chartX, chartY);
//        }
//        return point;
//	}
    
    private boolean updateCurrentMaskRectangle(XYItemEntity startEntity,
    		XYItemEntity endEntity) {
    	if (currentMaskRectangle != null) {
    		if (startEntity != null && endEntity != null) {
    			IDataset startDataset = (IDataset) startEntity.getDataset();
    			IDataset endDataset = (IDataset) endEntity.getDataset();
    			if (startDataset instanceof XYZDataset && endDataset instanceof XYZDataset) {
    				XYZDataset start = (XYZDataset) startDataset;
    				XYZDataset end = (XYZDataset) endDataset;
    				double xStart = start.getXValue(0, startEntity.getItem());
    				double yStart = start.getYValue(0, startEntity.getItem());
    				double xEnd = end.getXValue(0, endEntity.getItem());
    				double yEnd = end.getYValue(0, endEntity.getItem());
    				currentMaskRectangle.setRectangleFrame(new Rectangle2D.Double(
    						Math.min(xStart, xEnd), Math.min(yStart, yEnd), 
    						Math.abs(xStart - xEnd), Math.abs(yStart - yEnd)));
//    				System.out.println("[" + xStart + ", " + yStart + ", " + xEnd + ", " + yEnd);
    				fireMaskUpdateEvent(currentMaskRectangle);
    				return true;
    			}
    		}
    	}
    	return false;
	}

    @Override
    public Abstract2DMask getSelectedMask() {
    	return (Abstract2DMask) super.getSelectedMask();
    }

	/**
	 * @return the chartZ
	 */
	public double getChartZ() {
		return chartZ;
	}

	/**
	 * @param chartZ the chartZ to set
	 */
	protected void setChartZ(double chartZ) {
		this.chartZ = chartZ;
	}

	@Override
	protected Color getAxisTraceColor() {
		return axisTraceColor;
	}

	@Override
	public ColorScale getColorScale() {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYBlockRenderer) {
			PaintScale scale = ((XYBlockRenderer) renderer).getPaintScale();
			if (scale instanceof ColorPaintScale) {
				return ((ColorPaintScale) scale).getColorScale();
			}
		}
		return StaticValues.DEFAULT_COLOR_SCALE;
	}

	@Override
	public boolean isLogarithmScaleEnabled() {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYBlockRenderer) {
			PaintScale scale = ((XYBlockRenderer) renderer).getPaintScale();
			if (scale instanceof ColorPaintScale) {
				return ((ColorPaintScale) scale).isLogScale();
			}
		}
		return false;
	}

	@Override
	public void setColorScale(ColorScale colorScale) {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYBlockRenderer) {
			PaintScale scale = ((XYBlockRenderer) renderer).getPaintScale();
			if (scale instanceof ColorPaintScale) {
				((ColorPaintScale) scale).setColorScale(colorScale);
			}
		}
	}

	@Override
	public void setLogarithmScaleEnabled(boolean enabled) {
		XYItemRenderer renderer = getXYPlot().getRenderer();
		if (renderer instanceof XYBlockRenderer) {
			PaintScale scale = ((XYBlockRenderer) renderer).getPaintScale();
			if (scale instanceof ColorPaintScale) {
				((ColorPaintScale) scale).setLogScale(enabled);
				updatePlot();
			}
		}
	}
    
	@Override
	public PaintScaleLegend getPaintScaleLegend() {
		for (Object object : getChart().getSubtitles()) {
        	Title title = (Title) object;
        	if (title instanceof PaintScaleLegend) {
        		return (PaintScaleLegend) title;
        	}
		}
		return null;
	}
	
	@Override
	public void updatePaintScaleLegend() {
		PaintScaleLegend legend = getPaintScaleLegend();
		double max = ((IXYZDataset) getDataset()).getZMax();
		double min = ((IXYZDataset) getDataset()).getZMin();
		if (legend != null) {
			((ColorPaintScale) legend.getScale()).setLowerBound(min);
			((ColorPaintScale) legend.getScale()).setUpperBound(max);
			legend.getAxis().setLowerBound(min);
			legend.getAxis().setUpperBound(max);
		}
	}
	
	@Override
	protected JPopupMenu createPopupMenu(boolean properties, boolean copy,
			boolean save, boolean print, boolean zoom) {
		JPopupMenu menu = super.createPopupMenu(properties, copy, save, print, zoom);
        
        this.resetColorScaleMenuItem = new JMenuItem("Reset Color Scale");
        this.resetColorScaleMenuItem.setActionCommand(RESET_COLOR_SCALE_COMMAND);
        this.resetColorScaleMenuItem.addActionListener(this);
        menu.addSeparator();
        menu.add(resetColorScaleMenuItem);
        return menu;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (command.equals(RESET_COLOR_SCALE_COMMAND)) {
			PaintScaleLegend legend = getPaintScaleLegend();
			if (legend != null && legend.getScale() instanceof ColorPaintScale) {
				((ColorPaintScale) legend.getScale()).resetBoundPercentage();
				updatePlot();
			}
        } else {
        	super.actionPerformed(event);
        }
	}
	
	
	@Override
	public void updatePlot() {
		updatePaintScaleLegend();
		super.updatePlot();
	}
	
	@Override
	public void setDataset(IDataset dataset) {
		if (getDataset() != null) {
			dataset.removeChangeListener(this);
		}
		super.setDataset(dataset);
		XYBlockRenderer renderer = (XYBlockRenderer) getXYPlot().getRenderer();
		renderer.setBlockHeight(((IXYZDataset) dataset).getYBlockSize());
		renderer.setBlockWidth(((IXYZDataset) dataset).getXBlockSize());
		PaintScaleLegend legend = getPaintScaleLegend();
		if (legend != null) {
			((ColorPaintScale) legend.getScale()).resetBoundPercentage();
		}
		dataset.addChangeListener(this);
	}
	
	@Override
	public void addMask(AbstractMask mask) {
		if (mask instanceof Abstract2DMask) {
			super.addMask(mask);
		}
	}

	@Override
	public void doHelp() {
		showPropertyEditor(5);
	}
	
	@Override
    public void doExport(IExporter exporter) throws IOException {

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

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
        	String filename = fileChooser.getSelectedFile().getPath();
//        	String selectedDescription = fileChooser.getFileFilter().getDescription();
        	if (!filename.toLowerCase().endsWith("." + fileExtension)) {
        		filename = filename + "." + fileExtension;
        	}
        	File selectedFile = new File(filename);
        	int confirm = JOptionPane.YES_OPTION;
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

	@Override
	public void datasetChanged(DatasetChangeEvent event) {
		XYBlockRenderer renderer = (XYBlockRenderer) getXYPlot().getRenderer();
		renderer.setBlockHeight(((IXYZDataset) getDataset()).getYBlockSize());
		renderer.setBlockWidth(((IXYZDataset) getDataset()).getXBlockSize());
		getXYPlot().configureDomainAxes();
		getXYPlot().configureRangeAxes();
//		PaintScaleLegend legend = getPaintScaleLegend();
//		if (legend != null) {
//			((ColorPaintScale) legend.getScale()).resetBoundPercentage();
//		}
	}

	@Override
	public void setLogarithmEnabled(boolean enabled) {
		setLogarithmScaleEnabled(enabled);
	}
	
	@Override
	public boolean isLogarithmEnabled() {
		return isLogarithmScaleEnabled();
	}
	
	@Override
	public IHelpProvider getHelpProvider() {
		return new Hist2DHelpProvider();
	}

	@Override
	protected void saveAsText(BufferedWriter writer) throws IOException {
		IDataset dataset = getDataset();
		if (dataset != null) {
			DatasetUtils.export((IXYZDataset) dataset, writer, ExportFormat.XYZ);
		}
	}

}
