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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.interfaces.ICompositePlot;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.interfaces.IHist2D;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.IPlot1D;
import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.CompositePlotTransferable;
import org.gumtree.vis.mask.IMaskEventListener;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.ExtensionFileFilter;

/**
 * @author nxi
 *
 */
public class CompositePanel extends JPanel implements ICompositePlot, 
	ChartMouseListener, Printable{

	public final static int DEFAULT_HORIZONTAL_GAP = 2;
	public final static int DEFAULT_VERTICAL_GAP = 2;
	/**
	 * 
	 */
	private static final long serialVersionUID = -8994728261209590216L;
	private List<IPlot> plotList = new ArrayList<IPlot>();
	private JPanel inner;
	private IPlot focusedPlot;
	private List<ChartMouseListener> listeners = new ArrayList<ChartMouseListener>();
	
	public CompositePanel() {
		setLayout(new BorderLayout());
//		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		createInnerPanel();		
	}
	
	public CompositePanel(int rows, int columns) {
		this();
		inner.setLayout(new GridLayout(rows, columns, 
				DEFAULT_HORIZONTAL_GAP, DEFAULT_VERTICAL_GAP));
	}
	
	private void createInnerPanel() {
		inner = new JPanel();
		add(inner, BorderLayout.CENTER);
	}

	public CompositePanel(int rows, int columns, int horizontalGap, int verticalGap) {
		this();
		inner.setLayout(new GridLayout(rows, columns, 
				horizontalGap, verticalGap));
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#addChartMouseListener(org.jfree.chart.ChartMouseListener)
	 */
	@Override
	public void addChartMouseListener(ChartMouseListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#doCopy()
	 */
	@Override
	public void doCopy() {
//		if (focusedPlot != null) {
//			focusedPlot.doCopy();
//		}
		final Clipboard systemClipboard = 
			Toolkit.getDefaultToolkit().getSystemClipboard();
		Cursor currentCursor = getCursor();
		setCursor(StaticValues.WAIT_CURSOR);
//		Rectangle2D plotArea = getBounds();
		final CompositePlotTransferable selection = new CompositePlotTransferable(this);
		if (selection != null) {
			systemClipboard.setContents(selection, null);
		}
		setCursor(currentCursor);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getChart()
	 */
	@Override
	public JFreeChart getChart() {
		if (focusedPlot != null) {
			return focusedPlot.getChart();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getChartX()
	 */
	@Override
	public double getChartX() {
		//do nothing
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getChartY()
	 */
	@Override
	public double getChartY() {
		//do nothing
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getDataset()
	 */
	@Override
	public IDataset getDataset() {
		//do nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getHorizontalAxis()
	 */
	@Override
	public ValueAxis getHorizontalAxis() {
		//do nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getMasks()
	 */
	@Override
	public List<AbstractMask> getMasks() {
		//do nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getSelectedMask()
	 */
	@Override
	public AbstractMask getSelectedMask() {
		//do nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getTitle()
	 */
	@Override
	public TextTitle getTitle() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getVerticalAxis()
	 */
	@Override
	public ValueAxis getVerticalAxis() {
		//do nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getXYPlot()
	 */
	@Override
	public XYPlot getXYPlot() {
		//do nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#isMouseWheelEnabled()
	 */
	@Override
	public boolean isMouseWheelEnabled() {
		//do nothing
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#isToolTipFollowerEnabled()
	 */
	@Override
	public boolean isToolTipFollowerEnabled() {
		//do nothing
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#moveSelectedMask(int)
	 */
	@Override
	public void moveSelectedMask(int keyCode) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#removeSelectedMask()
	 */
	@Override
	public void removeSelectedMask() {
		if (focusedPlot != null) {
			focusedPlot.removeSelectedMask();
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#restoreAutoBounds()
	 */
	@Override
	public void restoreAutoBounds() {
		if (focusedPlot != null) {
			focusedPlot.restoreAutoBounds();
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#restoreHorizontalBounds()
	 */
	@Override
	public void restoreHorizontalBounds() {
		if (focusedPlot != null) {
			focusedPlot.restoreHorizontalBounds();
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#restoreVerticalBounds()
	 */
	@Override
	public void restoreVerticalBounds() {
		if (focusedPlot != null) {
			focusedPlot.restoreVerticalBounds();
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#saveTo(java.lang.String, java.lang.String)
	 */
	@Override
	public void saveTo(String filename, String fileType) throws IOException {
		int filterIndex = 0;
		if (fileType != null) {
			if (fileType.toLowerCase().contains("png")) {
				filterIndex = 0;
			} else if (fileType.toLowerCase().contains("jpg") || 
					fileType.toLowerCase().contains("jpeg")) {
				filterIndex = 1;
			} 
		}
		if (filterIndex == 0) {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
			BufferedImage chartImage = getImage();
			try {
				ChartUtilities.writeBufferedImageAsPNG(out, chartImage);
			} finally {
				out.close();
			}
		} else if (filterIndex == 1) {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
			BufferedImage chartImage = getImage();
			try{
				ChartUtilities.writeBufferedImageAsJPEG(out, chartImage);
			} finally {
				out.close();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setBackgroundColor(java.awt.Color)
	 */
//	@Override
	public void setBackgroundColor(Color color) {
		inner.setBackground(color);
		setBackground(color);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setDataset(org.jfree.data.xy.XYDataset)
	 */
	@Override
	public void setDataset(IDataset dataset) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setHorizontalAxisFlipped(boolean)
	 */
	@Override
	public void setHorizontalAxisFlipped(boolean isFlipped) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setHorizontalAxisTrace(boolean)
	 */
	@Override
	public void setHorizontalAxisTrace(boolean isEnabled) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setHorizontalZoomable(boolean)
	 */
	@Override
	public void setHorizontalZoomable(boolean isZoomable) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setMouseWheelEnabled(boolean)
	 */
	@Override
	public void setMouseWheelEnabled(boolean isEnabled) {
		for (IPlot plot: plotList) {
			plot.setMouseWheelEnabled(isEnabled);
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setSelectedMask(org.gumtree.vis.mask.AbstractMask)
	 */
	@Override
	public void setSelectedMask(AbstractMask selectedMask) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setToolTipFollowerEnabled(boolean)
	 */
	@Override
	public void setToolTipFollowerEnabled(boolean enabled) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setVerticalAxisFlipped(boolean)
	 */
	@Override
	public void setVerticalAxisFlipped(boolean isFlipped) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setVerticalAxisTrace(boolean)
	 */
	@Override
	public void setVerticalAxisTrace(boolean isEnabled) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setVerticalZoomable(boolean)
	 */
	@Override
	public void setVerticalZoomable(boolean isZoomable) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setZoomInFactor(double)
	 */
	@Override
	public void setZoomInFactor(double factor) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setZoomOutFactor(double)
	 */
	@Override
	public void setZoomOutFactor(double factor) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomInBoth(double, double)
	 */
	@Override
	public void zoomInBoth(double x, double y) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomInHorizontal(double, double)
	 */
	@Override
	public void zoomInHorizontal(double x, double y) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomInVertical(double, double)
	 */
	@Override
	public void zoomInVertical(double x, double y) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomOutBoth(double, double)
	 */
	@Override
	public void zoomOutBoth(double x, double y) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomOutHorizontal(double, double)
	 */
	@Override
	public void zoomOutHorizontal(double x, double y) {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomOutVertical(double, double)
	 */
	@Override
	public void zoomOutVertical(double x, double y) {
		//do nothing
	}

	@Override
	public void processMouseWheelEvent(MouseWheelEvent event) {
		if (focusedPlot != null) {
			focusedPlot.processMouseWheelEvent(event);
		}
	}

	@Override
	public void setLayout(int rows, int columns) {
		inner.setLayout(new GridLayout(rows, columns, 
				DEFAULT_HORIZONTAL_GAP, DEFAULT_VERTICAL_GAP));
	}

	@Override
	public void setLayout(int rows, int columns, 
			int horizontalGap, int verticalGap) {
		inner.setLayout(new GridLayout(rows, columns, horizontalGap, verticalGap));
	}
	
	@Override
	public void addPlot(final IPlot plot) {
		plotList.add(plot);
		if (plot instanceof JPanel) {
			inner.add((JPanel) plot);
		}
		plot.addChartMouseListener(this);
	}

	@Override
	public void addPlots(List<IPlot> plots) {
		for (IPlot plot : plots) {
			addPlot(plot);
		}
	}

	@Override
	public void clear() {
		for (IPlot plot : plotList) {
			if (plot instanceof JPanel) {
				inner.remove((JPanel) plot);
			}
		}
		plotList.clear();
	}

	@Override
	public List<IPlot> getPlotList() {
		return plotList.subList(0, plotList.size());
	}

	@Override
	public void removePlot(IPlot plot) {
		if (plotList.contains(plot)) {
			plotList.remove(plot);
			if (plot instanceof JPanel) {
				inner.remove((JPanel) plot);
			}
			plot.removeChartMouseListener(this);
		}
	}
	
	@Override
	public void repaint() {
		super.repaint();
	}

//	@Override
//	public void pack() {
//		for (IPlot plot : plotList) {
//			if (plot instanceof JChartPanel) {
//				((JChartPanel) plot).updateUI();
//			}
//		}
//		inner.updateUI();
//		updateUI();
//	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		JFreeChart chart = event.getChart();
		for (IPlot plot : plotList) {
			if (plot.getChart() == chart) {
				focusedPlot = plot;
			}
		}
		for (ChartMouseListener listener : listeners) {
			listener.chartMouseClicked(event);
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		for (ChartMouseListener listener : listeners) {
			listener.chartMouseMoved(event);
		}
	}
	
	public void removeChartMouseListener(
			ChartMouseListener listener) {
		listeners.remove(listener);
	}

	@Override
	public BufferedImage getImage() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D gc2 = image.createGraphics();
		gc2.setBackground(Color.white);
		gc2.setPaint(Color.white);
		gc2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
		for (IPlot plot : plotList) {
			if (plot instanceof JPanel) {
				JPanel panel = (JPanel) plot;
				Rectangle2D panelBounds = panel.getBounds();
				Image panelImage = plot.getImage();
				gc2.drawImage(panelImage, (int) panelBounds.getMinX(), 
						(int) panelBounds.getMinY(), panel);
			}
		}
		gc2.dispose();
		return image;
	}
	
	@Override
	public List<JFreeChart> getChartList() {
		List<JFreeChart> chartList = new ArrayList<JFreeChart>();
		for (IPlot plot : plotList) {
			if (plot instanceof ICompositePlot) {
				chartList.addAll(((ICompositePlot) plot).getChartList());
			} else if (plot instanceof IPlot1D || plot instanceof IHist2D) {
				chartList.add(plot.getChart());
			}
		}
		return chartList;
	}

	@Override
	public void createChartPrintJob() {
		setCursor(StaticValues.WAIT_CURSOR);
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
    			setCursor(StaticValues.defaultCursor);
    		}
    	}
    	setCursor(StaticValues.defaultCursor);
	}

    /**
     * Opens a file chooser and gives the user an opportunity to save the chart
     * in PNG format.
     *
     * @throws IOException if there is an I/O error.
     */
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
        ExtensionFileFilter pngFilter = new ExtensionFileFilter("PNG_Image_Files", ".png");
        ExtensionFileFilter jpgFilter = new ExtensionFileFilter("JPG_Image_Files", ".jpg");
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.addChoosableFileFilter(jpgFilter);

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


	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex)
			throws PrinterException {
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
//        Rectangle2D printArea = new Rectangle2D.Double(x, y, screenWidth * overallRatio, 
//        		screenHeight * overallRatio);
        BufferedImage image = getImage();
        g2.drawImage(image, (int) x, (int) y, (int) (screenWidth * overallRatio), 
        		(int) (screenHeight * overallRatio), null);
//        draw(g2, printArea, x, y);
        g2.dispose();
        return PAGE_EXISTS;

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
//        ChartMaskingUtilities.drawMasks(g2, dataArea, 
//        		getMasks(), null, getChart(), overallRatio);
//        plot.getDomainAxis().setLabelFont(domainFont);
//        plot.getRangeAxis().setLabelFont(rangeFont);
//        getChart().getTitle().setFont(titleFont);
//        plot.getDomainAxis().setTickLabelFont(domainScaleFont);
//        plot.getRangeAxis().setTickLabelFont(rangeScaleFont);
//        if (scaleAxis != null) {
//        	scaleAxis.setTickLabelFont(scaleAxisFont);
//        }
//        return PAGE_EXISTS;
	}
	
	@Override
	public void setCursor(Cursor arg0) {
		super.setCursor(arg0);
    	getParent().setCursor(arg0);
	}
	
	@Override
	public void draw(Graphics2D g2, Rectangle2D area, double shiftX, double shiftY) {
		Rectangle2D compositeBounds = getBounds();
		double xRatio = area.getWidth() / compositeBounds.getWidth();
		double yRatio = area.getHeight() / compositeBounds.getHeight();
		for (IPlot plot : plotList) {
			if (plot instanceof JPanel) {
				Rectangle2D plotBounds = ((JPanel) plot).getBounds();
				Rectangle2D newArea = new Rectangle2D.Double(
						area.getMinX() + plotBounds.getMinX() * xRatio, 
						area.getMinY() + plotBounds.getMinY() * yRatio, 
						plotBounds.getWidth() * xRatio, 
						plotBounds.getHeight() * yRatio);
				plot.draw(g2, newArea, shiftX, shiftY);
			}
		}
	}
	
	@Override
	public void updatePlot() {
		for (IPlot plot : plotList) {
			plot.updatePlot();
		}
		repaint();
	}
	
	@Override
	public void updateLabels() {
		for (IPlot plot : plotList) {
			plot.updateLabels();
		}
	}
	
	@Override
	public void addMask(AbstractMask mask) {
		//	do nothing
	}
	
	@Override
	public void addMasks(List<AbstractMask> maskList) {
		//	do nothing	
	}

	@Override
	public boolean isMaskingEnabled() {
		//	do nothing
		return false;
	}

	@Override
	public void removeMask(AbstractMask mask) {
		// do nothing
	}

	@Override
	public void setMaskingEnabled(boolean enabled) {
		// do nothing
		
	}

	@Override
	public boolean isHorizontalAxisFlipped() {
		return false;
	}

	@Override
	public boolean isVerticalAxisFlipped() {
		return false;
	}

	@Override
	public void addMaskEventListener(IMaskEventListener listener) {
		// do nothing
	}

	@Override
	public void removeMaskEventListener(IMaskEventListener listener) {
		// do nothing
	}

	@Override
	public void setAutoUpdate(boolean autoUpdate) {
	}

	@Override
	public boolean isAutoUpdate() {
		return false;
	}

	@Override
	public void doEditChartProperties() {
		// do nothing
	}
	
	@Override
	public void setPlotTitle(String title) {
	}
	
	@Override
	public void doHelp() {
		// do nothing
	}
	
	@Override
	public void doExport(IExporter exporter) throws IOException {
		
	}

	@Override
	public void cleanUp() {
		for (IPlot plot : plotList) {
			plot.cleanUp();
		}
	}

	@Override
	public void setLogarithmEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLogarithmEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IHelpProvider getHelpProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addShape(Shape shape, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeShape(Shape shape) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearShapes() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isShapeEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setShapeEnabled(boolean isShapeEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDomainAxisMarker(double x, int height, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addRangeAxisMarker(double y, int width, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMarker(double x, double y, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeMarker(double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRangeAxisMarker(double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeDomainAxisMarker(double x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearMarkers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearDomainAxisMarkers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearRangeAxisMarkers() {
		// TODO Auto-generated method stub
		
	}
}
