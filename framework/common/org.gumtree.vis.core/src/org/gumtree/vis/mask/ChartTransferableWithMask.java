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
package org.gumtree.vis.mask;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.jfree.chart.JFreeChart;


/**
 * @author nxi
 *
 */
public class ChartTransferableWithMask implements Transferable {

    /** The data flavor. */
    final DataFlavor imageFlavor = new DataFlavor(
            "image/x-java-image; class=java.awt.Image", "Image");    
    private final static Font maskNameFont = new Font("Serif", Font.ITALIC, 12);
    /** The chart. */
    private JFreeChart chart;

    /** The width of the chart on the clipboard. */
    private int width;

    /** The height of the chart on the clipboard. */
    private int height;


    LinkedHashMap<AbstractMask, Color> masks;
    private LinkedHashMap<Shape, Color> shapeMap;
    private LinkedHashMap<Rectangle2D, String> textContentMap;
	private Rectangle2D dataArea;
	
	/**
	 * @param chart
	 * @param width
	 * @param height
	 */
	public ChartTransferableWithMask(JFreeChart chart, int width, int height) {
		this(chart, width, height, true);
	}

	/**
	 * @param chart
	 * @param width
	 * @param height
	 * @param cloneData
	 */
	public ChartTransferableWithMask(JFreeChart chart, int width, int height,
			boolean cloneData) {
        try {
            this.chart = (JFreeChart) chart.clone();
        }
        catch (CloneNotSupportedException e) {
            this.chart = chart;
        }
        this.width = width;
        this.height = height;
	}

	public ChartTransferableWithMask(JFreeChart chart, int width, int height, 
			Rectangle2D dataArea, LinkedHashMap<AbstractMask, Color> masks, 
			LinkedHashMap<Shape, Color> shapeMap) {
		this(chart, width, height);
		this.dataArea = dataArea;
		this.masks = masks;
		this.shapeMap = shapeMap;
	}
	
    public ChartTransferableWithMask(JFreeChart chart, int width,
			int height, Rectangle2D dataArea,
			LinkedHashMap<AbstractMask, Color> masks,
			LinkedHashMap<Shape, Color> shapeMap,
			LinkedHashMap<Rectangle2D, String> textContents) {
		this(chart, width, height, dataArea, masks, shapeMap);
		this.textContentMap = textContents;
	}

	/**
     * Returns the data flavors supported.
     * 
     * @return The data flavors supported.
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {this.imageFlavor};
    }

    /**
     * Returns <code>true</code> if the specified flavor is supported.
     *
     * @param flavor  the flavor.
     *
     * @return A boolean.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return this.imageFlavor.equals(flavor);
    }

    @Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
        BufferedImage image; 
        if (this.imageFlavor.equals(flavor)) {
        	image =  chart.createBufferedImage(width, height);
        }
        else {
            throw new UnsupportedFlavorException(flavor);
        }
        Graphics2D g2 = image.createGraphics();
        drawMasksInDataArea(g2, dataArea != null ? dataArea : 
        	new Rectangle2D.Double(0, 0, width, height), masks, chart);
        ChartMaskingUtilities.drawShapes(g2, dataArea, shapeMap, chart);
        ChartMaskingUtilities.drawText(g2, dataArea, textContentMap, chart);
        g2.dispose();
        return image;
	}
	
    protected void drawMasksInDataArea(Graphics2D g2, Rectangle2D dataArea, 
    		LinkedHashMap<AbstractMask, Color> masks, JFreeChart chart) {
    	ChartMaskingUtilities.drawMasks(g2, dataArea, masks, null, chart);
    }

    
//    private void drawMaskRectangle(Graphics2D g2) {
//    	if (masks != null) {
//    		for (RectangleMask mask : masks) {
//    			g2.setPaint(mask.getFillColor());
//    			Rectangle2D translatedMask = translateChartRectangle(mask);
//    			if (translatedMask == null || translatedMask.isEmpty()) {
//    				continue;
//    			}
//    			if (mask.isEllipse()) {
//    				drawEllipseMask(g2, translatedMask);
//    			} else {
//    				g2.fill(translatedMask);
//    			}
//    			drawMaskName(g2, translatedMask, mask.getName(), mask.isEllipse());
//    		}
//    	}
//    }
//    
//    private void drawEllipseMask(Graphics2D g2, Rectangle2D mask) {
//    	Ellipse2D ellipse = new Ellipse2D.Double(mask.getMinX(), mask.getMinY(), 
//    			mask.getWidth(), mask.getHeight());
//    	g2.fill(ellipse);
//	}
//
//    private void drawMaskName(Graphics2D g2, Shape shape, String name, boolean isEllipse) {
//    	if (name == null) {
//    		return;
//    	}
//    	Rectangle2D rec = shape.getBounds2D();
//    	int size = name.length();
//    	if (rec.getHeight() < 20 || rec.getWidth() < 6 * size + 10) {
//    		return;
//    	}
//    	Point2D fontLocation = new Point2D.Double();
//    	if (isEllipse) {
//    		fontLocation.setLocation(rec.getCenterX() - size * 3, rec.getMinY() + 15);
//    	} else {
//    		fontLocation.setLocation(rec.getMinX() + 10, rec.getMinY() + 15);
//    	}
//    	g2.setPaint(Color.black);
//    	g2.setFont(maskNameFont);
//    	g2.drawString(name, (int) fontLocation.getX(), (int) fontLocation.getY());
//    }
//
//    public Rectangle2D translateChartRectangle(Rectangle2D rectangle) {
//    	Point2D start = new Point2D.Double(rectangle.getMinX(), rectangle.getMinY());
//    	Point2D end = new Point2D.Double(rectangle.getMaxX(), rectangle.getMaxY());
//    	Rectangle2D screenArea = new Rectangle2D.Double(0, 0, width, height);
//    	Point2D screenStart = translateChartPoint(start, screenArea);
//    	Point2D screenEnd = translateChartPoint(end, screenArea);
//    	return new Rectangle2D.Double(
//    			Math.min(screenStart.getX(), screenEnd.getX()), 
//    			Math.min(screenStart.getY(), screenEnd.getY()), 
//    			Math.abs(screenStart.getX() - screenEnd.getX()), 
//    			Math.abs(screenStart.getY() - screenEnd.getY()));
//    }
//
//    public Point2D translateChartPoint(Point2D point, Rectangle2D screenArea) {
//    	XYPlot plot = chart.getXYPlot();
//    	boolean isDomainInverted = plot.getDomainAxis().isInverted();
//    	boolean isRangeInverted = plot.getRangeAxis().isInverted();
//    	Range domainSection = plot.getDomainAxis().getRange();
//    	Range rangeSection = plot.getRangeAxis().getRange();
//    	double x, y;
//    	if (!isDomainInverted) {
//    		x = screenArea.getMinX() + (point.getX() - domainSection.getLowerBound()) 
//    				/ domainSection.getLength() * screenArea.getWidth();
//    	} else {
//    		x = screenArea.getMinX() + (domainSection.getUpperBound() - point.getX()) 
//    				/ domainSection.getLength() * screenArea.getWidth();
//    	}
//    	if (!isRangeInverted) {
//    		y = screenArea.getMinY() + (rangeSection.getUpperBound() - point.getY()) 
//					/ rangeSection.getLength() * screenArea.getHeight();    		
//    	} else {
//    		y = screenArea.getMinY() + (point.getY() - rangeSection.getLowerBound()) 
//					/ rangeSection.getLength() * screenArea.getHeight();
//    	}
////    	Insets insets = getInsets();
////    	return new Point2D.Double((x - insets.left) / this.scaleX, (y - insets.top) / this.scaleY);
//    	return new Point2D.Double(x, y);
//    }
}
