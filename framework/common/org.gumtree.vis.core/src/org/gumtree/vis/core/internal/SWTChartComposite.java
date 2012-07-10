/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package org.gumtree.vis.core.internal;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.title.LegendTitle;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.experimental.swt.SWTGraphics2D;
import org.jfree.experimental.swt.SWTUtils;
import org.jfree.ui.RectangleEdge;

/**
 * @author nxi
 * Created on 12/03/2009
 */
public class SWTChartComposite extends ChartComposite{

	public static final String COPY_COMMAND = "COPY";
	public static final String LEGEND_BOTTOM_COMMAND = "LEGEND_BUTTOM";
	public static final String LEGEND_RIGHT_COMMAND = "LEGEND_RIGHT";
	public static final String LEGEND_NONE_COMMAND = "LEGEND_NONE";
	private double rangeUpperBound = 1;
	private double rangeLowerBound = 0;
	private MenuItem copyMenuItem;
	private MenuItem legendRightMenuItem;
	private MenuItem legendButtomMenuItem;
	private MenuItem legendNoneMenuItem;
	
	/**
	 * @param comp
	 * @param style
	 */
	public SWTChartComposite(Composite comp, int style) {
		super(comp, style);
	}

	/**
	 * @param comp
	 * @param style
	 * @param chart
	 */
	public SWTChartComposite(Composite comp, int style, JFreeChart chart) {
		super(comp, style, chart);
	}

	/**
	 * @param comp
	 * @param style
	 * @param chart
	 * @param useBuffer
	 */
	public SWTChartComposite(Composite comp, int style, JFreeChart chart,
			boolean useBuffer) {
		super(comp, style, chart, useBuffer);
	}

	/**
	 * @param comp
	 * @param style
	 * @param chart
	 * @param properties
	 * @param save
	 * @param print
	 * @param zoom
	 * @param tooltips
	 */
	public SWTChartComposite(Composite comp, int style, JFreeChart chart,
			boolean properties, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(comp, style, chart, properties, save, print, zoom, tooltips);
	}

	/**
	 * @param comp
	 * @param style
	 * @param jfreechart
	 * @param width
	 * @param height
	 * @param minimumDrawW
	 * @param minimumDrawH
	 * @param maximumDrawW
	 * @param maximumDrawH
	 * @param usingBuffer
	 * @param properties
	 * @param save
	 * @param print
	 * @param zoom
	 * @param tooltips
	 */
	public SWTChartComposite(Composite comp, int style, JFreeChart jfreechart,
			int width, int height, int minimumDrawW, int minimumDrawH,
			int maximumDrawW, int maximumDrawH, boolean usingBuffer,
			boolean properties, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(comp, style, jfreechart, width, height, minimumDrawW,
				minimumDrawH, maximumDrawW, maximumDrawH, usingBuffer,
				properties, save, print, zoom, tooltips);
//		setZoomInFactor(0.5d);
//		setZoomOutFactor(1/0.5d);
	}

    /**
     * Creates a print job for the chart.
     */
	@Override
    public void createChartPrintJob() {
		final PrinterJob job = PrinterJob.getPrinterJob();
		PageFormat pf = job.defaultPage();
		pf.setOrientation(PageFormat.LANDSCAPE);
//		PageFormat pf2 = job.pageDialog(pf);
		PageFormat pf2 = job.defaultPage();
		pf2.setOrientation(PageFormat.LANDSCAPE);
		if (pf2 != pf) {
			Printable print = new MyPrintable();
			job.setPrintable(print, pf2);
			if (job.printDialog()) {
				getDisplay().asyncExec(new Runnable(){

					public void run() {
						try {
							job.print();
						}
						catch (PrinterException e) {
							MessageBox messageBox = new MessageBox(
									getShell(), SWT.OK | SWT.ICON_ERROR);
							messageBox.setMessage(e.getMessage());
							messageBox.open();
						}		
					}});
			}
		}
        
 
        
//		PrintDialog dialog = new PrintDialog(getShell());
//		// Prompts the printer dialog to let the user select a printer.
//		PrinterData printerData = dialog.open();
//
//		if (printerData == null) // the user cancels the dialog
//			return;
//		// Loads the printer.
//		final Printer printer = new Printer(printerData);
//		getDisplay().asyncExec(new Runnable(){
//
//			public void run() {
//				print(printer);
//				printer.dispose();				
//			}});
    }
	
	protected void print(Printer printer){
		org.eclipse.swt.graphics.Rectangle clientArea = printer.getClientArea();
		org.eclipse.swt.graphics.Rectangle trim = printer.computeTrim(0, 0, 0, 0);
		Point dpi = printer.getDPI();
		int leftMargin = dpi.x + trim.x; // one inch from left side of paper
		int rightMargin = clientArea.width - dpi.x + trim.x + trim.width; // one inch from right side of paper
		int topMargin = dpi.y + trim.y; // one inch from top edge of paper
		int bottomMargin = clientArea.height - dpi.y + trim.y + trim.height; // one inch from bottom edge of paper
//		printer.startJob("Plot");
		GC gc = new GC(printer);
		Font font = new Font(getDisplay(), "Courier", 10, SWT.NORMAL);
		FontData fontData = font.getFontData()[0];
		Font printerFont = new Font(printer, fontData.getName(), fontData.getHeight(), fontData.getStyle());
		gc.setFont(printerFont);
//		printer.startPage();
		try {
			paint(gc, leftMargin, topMargin, rightMargin, bottomMargin);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox messageBox = new MessageBox(
                  getShell(), SWT.OK | SWT.ICON_ERROR);
           messageBox.setMessage(e.getMessage());
           messageBox.open();
		} finally{
			gc.dispose();
			font.dispose();
			printerFont.dispose();
			System.out.println("printer job finished");
			printer.endPage();
			printer.endJob();

			printer.dispose();
		}
				
	}
	
	public void paint(GC gc, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
		Transform t = new Transform(gc.getDevice());
		gc.getTransform(t);

		float[] elements = new float[6];
		t.getElements(elements);
		Transform t2 = new Transform(gc.getDevice(), elements);

		// Apply transformation for printer resolution
		Point printerDPI = gc.getDevice().getDPI();
		Point screenDPI = getDisplay().getDPI();
		float scaleX = printerDPI.x / screenDPI.x * 1.5f;
		float scaleY = printerDPI.y / screenDPI.y * 1.5f;
		t2.scale(scaleX, scaleY);
		gc.setTransform(t2);

		
		SWTGraphics2D graphics2D = new SWTGraphics2D(gc);
		
		BufferedImage bufferedImage
        = getChart().createBufferedImage((int) (getBounds().width ), 
        		(int) (getBounds().height ), null);
		ImageData imageData = SWTUtils.convertToSWT(bufferedImage);

		try{
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("D:/tools/quokka/resAwt.png")));
		EncoderUtil.writeBufferedImage(bufferedImage, ImageFormat.PNG, out);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
//		Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
		
//		getChart().draw(graphics2D,  new Rectangle((int) (leftMargin/scaleX), (int) (topMargin/scaleY), 
//				(int) (getBounds().width/scaleX), (int) (getBounds().height/scaleY)));
		// Decrease rendering size
//		Graphics2D g2d = (Graphics2D) graphics2D.create();
//		GraphicsConfiguration configuration = g2d.getDeviceConfiguration();
//		Image awtImage = configuration.createCompatibleImage(
//				getBounds().width, getBounds().height,
//                Transparency.TRANSLUCENT);
//		ImageData imageData = SWTUtils.convertAWTImageToSWT(awtImage);
//		org.eclipse.swt.graphics.Image swtImage = new org.eclipse.swt.graphics.Image(
//				getDisplay(), imageData);

		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {imageData};
		int fileFormat = SWT.IMAGE_PNG;
		imageLoader.save("D:/tools/quokka/resSwt.png", fileFormat);
//		double imageSizeFactor =
//            Math.min(
//              1,
//              (margin.right - margin.left)
//                * 1.0
//                / (dpiScaleFactorX * imageWidth));
//          imageSizeFactor =
//            Math.min(
//              imageSizeFactor,
//              (margin.bottom - margin.top)
//                * 1.0

//        gc.drawImage(swtImage, 0, 0, swtImage.getBounds().width, swtImage.getBounds().height,
//                leftMargin, topMargin,
//                (int) printerDPI.x,
//                (int) printerDPI.y);
// 		getChart().draw(graphics2D, new Rectangle((int) (x/scaleX), (int) (y/scaleY), 
//				(int) (getBounds().width/scaleX), (int) (getBounds().height/scaleY)));
		graphics2D.dispose();
		
		// Restore original scaling
//		gc.setTransform(t);
		System.out.println("chart draw finished");
	}

	class MyPrintable implements Printable {
		
		public int print(Graphics g, PageFormat pf, int pageIndex) {
			if (pageIndex != 0)
				return NO_SUCH_PAGE;
			Graphics2D g2 = (Graphics2D) g;
//			g2.setFont(new java.awt.Font("Serif", java.awt.Font.PLAIN, 36));
//			g2.setPaint(Color.black);
//			g2.drawString("www.java2s.com", 100, 100);
//			Rectangle2D outline = new Rectangle2D.Double(pf.getImageableX(), pf.getImageableY(), pf
//					.getImageableWidth(), pf.getImageableHeight());
//			g2.draw(outline);
//			BufferedImage bufferedImage
//	        = getChart().createBufferedImage((int) pf.getImageableWidth(), 
//	        		(int) pf.getImageableHeight(), null);
//			try{
//				OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("D:/tools/quokka/resAwt.png")));
//				EncoderUtil.writeBufferedImage(bufferedImage, ImageFormat.PNG, out);
//			}catch (Exception e) {
//				e.printStackTrace();
//			}
			// g2.drawImage(bufferedImage, null, (int) pf.getImageableX(), (int) pf.getImageableY());

			double x = pf.getImageableX();
			double y = pf.getImageableY();
			double w = pf.getImageableWidth();
			double h = pf.getImageableHeight();
			getChart().draw(g2, new Rectangle2D.Double(x, y, w, h), null,
					null);

			return PAGE_EXISTS;
		}
	}
	
    public int print(Graphics g, PageFormat pf, int pageIndex) {

        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) g;
        double x = pf.getImageableX();
        double y = pf.getImageableY();
        double w = pf.getImageableWidth();
        double h = pf.getImageableHeight();
        getChart().draw(g2, new Rectangle2D.Double(x, y, w, h), null,
                null);
        return PAGE_EXISTS;

    }
    
    @Override
    protected Menu createPopupMenu(boolean properties, boolean save,
            boolean print, boolean zoom) {
    	Menu result = super.createPopupMenu(properties, save, print, zoom);
    	
    	LegendTitle legend = getChart().getLegend();
    	if (legend != null){
    		boolean isVisable = legend.isVisible();
    		RectangleEdge location = legend.getPosition();
    		
    		new MenuItem(result, SWT.SEPARATOR);
    		Menu legendMenu = new Menu(result);
    		MenuItem legendMenuGroup = new MenuItem(result, SWT.CASCADE);
    		legendMenuGroup.setMenu(legendMenu);
    		legendMenuGroup.setText("Legend");
    		legendButtomMenuItem = new MenuItem(legendMenu, SWT.RADIO);
    		legendButtomMenuItem.setText("Bottom");
    		legendButtomMenuItem.setData(LEGEND_BOTTOM_COMMAND);
    		legendButtomMenuItem.addSelectionListener(this);
    		if (isVisable && location.equals(RectangleEdge.BOTTOM)){
    			legendButtomMenuItem.setSelection(true);
    		}

    		legendRightMenuItem = new MenuItem(legendMenu, SWT.RADIO);
    		legendRightMenuItem.setText("Right");
    		legendRightMenuItem.setData(LEGEND_RIGHT_COMMAND);
    		legendRightMenuItem.addSelectionListener(this);
    		if (isVisable && location.equals(RectangleEdge.RIGHT)){
    			legendRightMenuItem.setSelection(true);
    		}

//    		new MenuItem(legendMenu, SWT.SEPARATOR);
    		legendNoneMenuItem = new MenuItem(legendMenu, SWT.RADIO);
    		legendNoneMenuItem.setText("None");
    		legendNoneMenuItem.setData(LEGEND_NONE_COMMAND);
    		legendNoneMenuItem.addSelectionListener(this);
    		if (!isVisible()){
    			legendNoneMenuItem.setSelection(true);
    		}
    	}
    	new MenuItem(result, SWT.SEPARATOR);
    	copyMenuItem = new MenuItem(result, SWT.NONE);
    	copyMenuItem.setText("Copy");
    	copyMenuItem.setData(COPY_COMMAND);
    	copyMenuItem.addSelectionListener(this);
    	return result;
    }
    
    @Override
    public void restoreAutoRangeBounds() {
        ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
        if (rangeAxis instanceof LogarithmicAxis){
        	super.restoreAutoRangeBounds();
        }else{
			double marginRate = 0.05;
//			double upperRange = getChart().getXYPlot().getRangeAxis().getUpperBound();
//			double lowerRange = getChart().getXYPlot().getRangeAxis().getLowerBound();
			double margin = (rangeUpperBound - rangeLowerBound) * marginRate;
//			double margin = Math.abs(getChart().getXYPlot().getDataset() - rangeAxis.getLowerBound()) * marginRate;
			rangeAxis.setRange(rangeLowerBound - margin, rangeUpperBound + margin);
        }
    }
    
    @Override
    public void widgetSelected(SelectionEvent e) {
    	super.widgetSelected(e);
    	String command = (String) ((MenuItem) e.getSource()).getData();
        if (command.equals(COPY_COMMAND)) {
            copyToClipboard();
        }
        if (command.equals(LEGEND_BOTTOM_COMMAND)) {
            setLegend(LEGEND_BOTTOM_COMMAND);
        }
        if (command.equals(LEGEND_RIGHT_COMMAND)) {
            setLegend(LEGEND_RIGHT_COMMAND);
        }
        if (command.equals(LEGEND_NONE_COMMAND)) {
            setLegend(LEGEND_NONE_COMMAND);
        }
    }

	private void setLegend(String legendPosition) {
		LegendTitle legend = getChart().getLegend();
		if (legend != null){
			if (legendPosition.equals(LEGEND_BOTTOM_COMMAND)){
				legend.setVisible(true);
				legend.setPosition(RectangleEdge.BOTTOM);
			} else if (legendPosition.equals(LEGEND_RIGHT_COMMAND)){
				legend.setVisible(true);
				legend.setPosition(RectangleEdge.RIGHT);
			}
			else
				legend.setVisible(false);
		}
	}

	public void copyToClipboard() {
		BufferedImage bufferedImage = getChart().createBufferedImage((int) (getBounds().width ), 
        		(int) (getBounds().height ), null);
		copyToClipboard(bufferedImage);
	}

	public static void copyToClipboard(BufferedImage image) {
		    ImageSelection imageSelection = new ImageSelection(image);
		    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageSelection, null);
		  }

	public static class ImageSelection implements Transferable{
	// the Image object which will be housed by the ImageSelection
		private BufferedImage image;

		public ImageSelection(BufferedImage image) {
			this.image = image;
		}

		// Returns the supported flavors of our implementation
		public DataFlavor[] getTransferDataFlavors() 
		{
			return new DataFlavor[] {DataFlavor.imageFlavor};
		}

		// Returns true if flavor is supported
		public boolean isDataFlavorSupported(DataFlavor flavor) 
		{
			return DataFlavor.imageFlavor.equals(flavor);
		}

		// Returns Image object housed by Transferable object
		public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException,IOException 
		{
			if (!DataFlavor.imageFlavor.equals(flavor)) 
			{
				throw new UnsupportedFlavorException(flavor);
			}
			// else return the payload
			return image;
		}
	}

	/**
	 * @param rangeUpperBound the rangeUpperBound to set
	 */
	public void setRangeUpperBound(double rangeUpperBound) {
		this.rangeUpperBound = rangeUpperBound;
	}

	/**
	 * @param rangeLowerBound the rangeLowerBound to set
	 */
	public void setRangeLowerBound(double rangeLowerBound) {
		this.rangeLowerBound = rangeLowerBound;
	}

}
