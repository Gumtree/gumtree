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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.gumtree.vis.hist2d.Hist2DPanel;
import org.gumtree.vis.plot1d.Plot1DPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

/**
 * @author nxi
 *
 */
public class ChartMaskingUtilities {

    private final static Font maskNameFont = new Font("Serif", Font.ITALIC, 10);
    private final static int maskDragPointHalfWidth = 2;
    private final static int maskDragPointWidth = 4;

    public static Rectangle2D getMaskFramework(AbstractMask mask, 
    		Rectangle2D imageArea, JFreeChart chart) {
    	if (mask instanceof RangeMask) {
    		return getDomainMaskFrame((RangeMask) mask, imageArea, chart);
    	} else if (mask instanceof Abstract2DMask) {
    		return translateChartRectangle((Abstract2DMask) mask, imageArea, chart).getRectangleFrame();
    	} else {
    		throw new IllegalArgumentException("must be Range mask or 2D mask");
    	}
    }
    
    public static Abstract2DMask translateChartRectangle(Abstract2DMask mask, 
    		Rectangle2D imageArea, JFreeChart chart) {
    	Rectangle2D bound = mask.getRectangleFrame();
    	Point2D start = new Point2D.Double(bound.getMinX(), bound.getMinY());
    	Point2D end = new Point2D.Double(bound.getMaxX(), bound.getMaxY());
    	Point2D screenStart = translateChartPoint(start, imageArea, chart);
    	Point2D screenEnd = translateChartPoint(end, imageArea, chart);
    	Abstract2DMask imageMask = mask.clone();
    	imageMask.setRectangleFrame(new Rectangle2D.Double(
    			Math.min(screenStart.getX(), screenEnd.getX()), 
    			Math.min(screenStart.getY(), screenEnd.getY()), 
    			Math.abs(screenStart.getX() - screenEnd.getX()), 
    			Math.abs(screenStart.getY() - screenEnd.getY())));
    	return imageMask;
    }
    
    public static Shape translateChartShape(Shape shape, Rectangle2D imageArea, JFreeChart chart) {
    	if (shape instanceof Line2D) {
    		Line2D line = (Line2D) shape;
    		double length = line.getP1().distance(line.getP2());
    		if (length == 0){
    			Point2D point = line.getP1();
    			Point2D newPoint = ChartMaskingUtilities.translateChartPoint(point, imageArea, chart);
    			Shape oShape = ShapeUtilities.createDiagonalCross(5f, 0.2f);
//    			Shape oShape = ShapeUtilities.createRegularCross(3f, 0.5f);
    			Shape newShape = ShapeUtilities.createTranslatedShape(oShape, newPoint.getX(), newPoint.getY());
    			return newShape;
    		} else if (length < 1e-6) {
    			if (line.getP1().getX() == line.getP2().getX()) {
    				double newX = ChartMaskingUtilities.translateChartPoint(line.getP1(), imageArea, chart).getX();
    				Line2D newLine = new Line2D.Double(newX, imageArea.getMinY(), newX, imageArea.getMaxY());
    				return newLine;
    			} else {
    				double newY = ChartMaskingUtilities.translateChartPoint(line.getP1(), imageArea, chart).getY();
    				Line2D newLine = new Line2D.Double(imageArea.getMinX(), newY, imageArea.getMaxX(), newY);
    				return newLine;
    			}
    		}
    		Line2D newShape = (Line2D) line.clone();
    		Point2D newP1 = translateChartPoint(line.getP1(), imageArea, chart);
    		Point2D newP2 = translateChartPoint(line.getP2(), imageArea, chart);
    		newShape.setLine(newP1, newP2);
    		return newShape;
    	} else if (shape instanceof RectangularShape) {
    		RectangularShape rect = (RectangularShape) shape;
    		RectangularShape newShape = (RectangularShape) rect.clone();
    		Rectangle2D bound = rect.getBounds2D();
    		Point2D start = new Point2D.Double(bound.getMinX(), bound.getMinY());
        	Point2D end = new Point2D.Double(bound.getMaxX(), bound.getMaxY());
        	Point2D screenStart = translateChartPoint(start, imageArea, chart);
        	Point2D screenEnd = translateChartPoint(end, imageArea, chart);
        	newShape.setFrame(new Rectangle2D.Double(
        			Math.min(screenStart.getX(), screenEnd.getX()), 
        			Math.min(screenStart.getY(), screenEnd.getY()), 
        			Math.abs(screenStart.getX() - screenEnd.getX()), 
        			Math.abs(screenStart.getY() - screenEnd.getY())));
        	return newShape;
    	} else {
    		return shape;
    	}
    }
    
    public static Rectangle2D getDomainMaskFrame(RangeMask mask, 
    		Rectangle2D imageArea, JFreeChart chart) {
    	XYPlot plot = chart.getXYPlot();
//    	boolean isDomainInverted = plot.getDomainAxis().isInverted();
//    	Range domainRange = plot.getDomainAxis().getRange();
//    	Range imageRange = new Range(imageArea.getMinX(), imageArea.getMaxX());
//    	Range dataRange = translateDomainRange(mask.getRange(), 
//    			imageRange, domainRange, isDomainInverted);
    	double lowerData = plot.getDomainAxis().valueToJava2D(
    			mask.getMin(), imageArea, RectangleEdge.BOTTOM);
    	double upperData = plot.getDomainAxis().valueToJava2D(
    			mask.getMax(), imageArea, RectangleEdge.BOTTOM);
    	return new Rectangle2D.Double(Math.min(lowerData, upperData), imageArea.getMinY(), 
    			Math.abs(upperData - lowerData), imageArea.getHeight());
    }
    
    public static Point2D translateChartPoint(Point2D point, Rectangle2D imageArea, JFreeChart chart) {
    	XYPlot plot = chart.getXYPlot();
    	double x, y;

    	ValueAxis domainAxis = plot.getDomainAxis();
    	ValueAxis rangeAxis = plot.getRangeAxis();
    	
    	x = domainAxis.valueToJava2D(point.getX(), imageArea, RectangleEdge.BOTTOM);
    	y = rangeAxis.valueToJava2D(point.getY(), imageArea, RectangleEdge.LEFT);
    	
    	return new Point2D.Double(x, y);
    }
    
    public static Point2D translateChartPoint(Point2D point, Rectangle2D imageArea, 
    		JFreeChart chart, int rangeAxisIndex) {
    	XYPlot plot = chart.getXYPlot();
    	double x, y;

    	ValueAxis domainAxis = plot.getDomainAxis();
    	ValueAxis rangeAxis;
    	if (rangeAxisIndex < 0 || rangeAxisIndex >= plot.getRangeAxisCount()) {
    		rangeAxis = plot.getRangeAxis();
    	} else {
    		rangeAxis = plot.getRangeAxis(rangeAxisIndex);
    	}
    	x = domainAxis.valueToJava2D(point.getX(), imageArea, RectangleEdge.BOTTOM);
    	y = rangeAxis.valueToJava2D(point.getY(), imageArea, RectangleEdge.LEFT);
    	
    	return new Point2D.Double(x, y);
    }
    
//    public static Range translateDomainRange(Range dataRange, Range imageRange, Range plotRange, 
//    		boolean isInverted) {
//    	double lower, upper;
//    	if (!isInverted) {
//    		lower = imageRange.getLowerBound() + (dataRange.getLowerBound() - plotRange.getLowerBound()) 
//    				/ plotRange.getLength() * imageRange.getLength();
//    		upper = imageRange.getLowerBound() + (dataRange.getUpperBound() - plotRange.getLowerBound()) 
//					/ plotRange.getLength() * imageRange.getLength();
//    	} else {
//    		lower = imageRange.getLowerBound() + (plotRange.getUpperBound() - dataRange.getUpperBound()) 
//					/ plotRange.getLength() * imageRange.getLength();
//    		upper = imageRange.getLowerBound() + (plotRange.getUpperBound() - dataRange.getLowerBound()) 
//    				/ plotRange.getLength() * imageRange.getLength();
//    	}
////    	Insets insets = getInsets();
////    	return new Point2D.Double((x - insets.left) / this.scaleX, (y - insets.top) / this.scaleY);
//    	return new Range(lower, upper);
//    }
    
    public static double translateScreenX(double screenX, Rectangle2D imageArea, JFreeChart chart) {
//    	XYPlot plot = chart.getXYPlot();
//    	boolean isDomainInverted = plot.getDomainAxis().isInverted();
//    	Range domainSection = plot.getDomainAxis().getRange();
		ValueAxis axis = chart.getXYPlot().getDomainAxis();

		return axis.java2DToValue(screenX, imageArea, RectangleEdge.BOTTOM);
//    	if (isDomainInverted) {
//    		return domainSection.getUpperBound() - (screenX - imageArea.getMinX()) 
//    				/ imageArea.getWidth() * domainSection.getLength();
//    	} else {
//    		return (screenX - imageArea.getMinX()) / imageArea.getWidth() 
//    				* domainSection.getLength() + domainSection.getLowerBound();
//    	}
    }

    public static double translateScreenY(double screenY, Rectangle2D imageArea, 
    		JFreeChart chart, int rangeAxisIndex) {
    	ValueAxis rangeAxis;
    	if (rangeAxisIndex < 0 || rangeAxisIndex >= chart.getXYPlot().getRangeAxisCount()) {
    		rangeAxis = chart.getXYPlot().getRangeAxis();
    	} else {
    		rangeAxis = chart.getXYPlot().getRangeAxis(rangeAxisIndex);
    	}
		return rangeAxis.java2DToValue(screenY, imageArea, RectangleEdge.LEFT);
    }
    
    public static double translateChartY(double chartY, Rectangle2D imageArea, JFreeChart chart) {
    	XYPlot plot = chart.getXYPlot();
    	boolean isRangeInverted = plot.getRangeAxis().isInverted();
    	Range rangeSection = plot.getRangeAxis().getRange();
    	if (!isRangeInverted) {
    		return imageArea.getMinY() + (rangeSection.getUpperBound() - chartY) 
					/ rangeSection.getLength() * imageArea.getHeight();    		
    	} else {
    		return imageArea.getMinY() + (chartY - rangeSection.getLowerBound()) 
					/ rangeSection.getLength() * imageArea.getHeight();
    	}
    }
    /**
     * Draws mask rectangle (if present).
     * The drawing is performed in XOR mode, therefore
     * when this method is called twice in a row,
     * the second call will completely restore the state
     * of the canvas.
     *
     * @param g2 the graphics device.
     * @param xor  use XOR for drawing?
     */
    private static void drawMaskRectangle(Graphics2D g2, Rectangle2D imageArea, 
    		Abstract2DMask mask, Abstract2DMask selectedMask, JFreeChart chart,
    		double fontSizeRate, Color fillColor) {
    	g2.clip(imageArea);
    	g2.setPaint(fillColor);
    	Abstract2DMask translatedMask = translateChartRectangle(mask, 
    			imageArea, chart);
    	if (translatedMask == null || translatedMask.getRectangleFrame().isEmpty()) {
    		return;
    	}
    	drawMask(g2, translatedMask.getShape());
    	if (mask == selectedMask) {
    		drawMaskBoarder(g2, translatedMask);
    	}
    	drawMaskName(g2, translatedMask, imageArea, fontSizeRate);
    }
    
    private static void drawMask(Graphics2D g2, Shape mask) {
    	g2.fill(mask);
	}

	private static void drawMaskBoarder(Graphics2D g2, Abstract2DMask mask) {
        g2.setPaint(Color.orange);
        g2.setStroke(new BasicStroke(1));
        Rectangle2D frame = mask.getRectangleFrame();
        g2.draw(frame);
        Rectangle2D dragPoint = new Rectangle2D.Double(frame.getMinX() - maskDragPointHalfWidth, 
        		frame.getMinY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getCenterX() - maskDragPointHalfWidth, 
        		frame.getMinY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMaxX() - maskDragPointHalfWidth, 
        		frame.getMinY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMaxX() - maskDragPointHalfWidth, 
        		frame.getCenterY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMinX() - maskDragPointHalfWidth, 
        		frame.getCenterY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMinX() - maskDragPointHalfWidth, 
        		frame.getMaxY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getCenterX() - maskDragPointHalfWidth, 
        		frame.getMaxY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMaxX() - maskDragPointHalfWidth, 
        		frame.getMaxY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        Color fillColor = new Color(250, 250, 50, 10);
        g2.setPaint(fillColor);
        g2.fill(mask.getShape());
	}

	public static void drawMaskBoarder(Graphics2D g2, Rectangle2D frame) {
        g2.setPaint(Color.orange);
        g2.setStroke(new BasicStroke(1));
        g2.draw(frame);
        Rectangle2D dragPoint = new Rectangle2D.Double(frame.getMinX() - maskDragPointHalfWidth, 
        		frame.getMinY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getCenterX() - maskDragPointHalfWidth, 
        		frame.getMinY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMaxX() - maskDragPointHalfWidth, 
        		frame.getMinY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMaxX() - maskDragPointHalfWidth, 
        		frame.getCenterY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMinX() - maskDragPointHalfWidth, 
        		frame.getCenterY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMinX() - maskDragPointHalfWidth, 
        		frame.getMaxY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getCenterX() - maskDragPointHalfWidth, 
        		frame.getMaxY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        dragPoint.setRect(frame.getMaxX() - maskDragPointHalfWidth, 
        		frame.getMaxY() - maskDragPointHalfWidth, maskDragPointWidth, maskDragPointWidth);
        g2.fill(dragPoint);
        Color fillColor = new Color(250, 250, 50, 30);
        g2.setPaint(fillColor);
        g2.fill(frame);
	}
	
    private static void drawMaskName(Graphics2D g2, AbstractMask mask, Rectangle2D imageArea, 
    		double fontSizeRate) {
    	if (mask.getName() == null) {
    		return;
    	}
   		Point2D fontLocation = mask.getTitleLocation(imageArea);
   		g2.setPaint(Color.black);
   		Font currentFont = g2.getFont();
   		g2.setFont(currentFont.deriveFont((float) 
   				(maskNameFont.getSize() * fontSizeRate)).deriveFont(Font.ITALIC));
   		g2.drawString(mask.getName(), (int) fontLocation.getX(), (int) fontLocation.getY());
   		g2.setFont(currentFont);
    }

    /**
     * Writes a chart to an output stream in JPEG format. This method allows
     * you to pass in a {@link ChartRenderingInfo} object, to collect
     * information about the chart dimensions/entities.  You will need this
     * info if you want to create an HTML image map.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  the chart rendering info (<code>null</code> permitted).
     * @param shapeMap 
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsJPEG(File file, JFreeChart chart,
            int width, int height, ChartRenderingInfo info, Rectangle2D imageArea, 
            LinkedHashMap<AbstractMask, Color> maskList, LinkedHashMap<Shape, Color> shapeMap)
            throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("Null 'file' argument.");
        }
        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        BufferedImage image = chart.createBufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB, info);
        Graphics2D g2 = image.createGraphics();
        drawMasks(g2, imageArea, maskList, null, chart);
        drawShapes(g2, imageArea, shapeMap, chart);
        g2.dispose();
        try{
        	EncoderUtil.writeBufferedImage(image, ImageFormat.JPEG, out);
        } finally {
        	out.close();
        }
    }
    
    /**
     * Writes a chart to an output stream in PNG format.  This method allows
     * you to pass in a {@link ChartRenderingInfo} object, to collect
     * information about the chart dimensions/entities.  You will need this
     * info if you want to create an HTML image map.
     *
     * @param out  the output stream (<code>null</code> not permitted).
     * @param chart  the chart (<code>null</code> not permitted).
     * @param width  the image width.
     * @param height  the image height.
     * @param info  carries back chart rendering info (<code>null</code>
     *              permitted).
     * @param shapeMap 
     * @param encodeAlpha  encode alpha?
     * @param compression  the PNG compression level (0-9).
     *
     * @throws IOException if there are any I/O errors.
     */
    public static void writeChartAsPNG(File file, JFreeChart chart,
            int width, int height, ChartRenderingInfo info, 
            Rectangle2D imageArea, LinkedHashMap<AbstractMask, Color> maskList, 
            LinkedHashMap<Shape, Color> shapeMap) 
    throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("Null 'file' argument.");
        }
        if (chart == null) {
            throw new IllegalArgumentException("Null 'chart' argument.");
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        BufferedImage chartImage = chart.createBufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB, info);
        Graphics2D g2 = chartImage.createGraphics();
        drawMasks(g2, imageArea, maskList, null, chart);
        drawShapes(g2, imageArea, shapeMap, chart);
        g2.dispose();
        try{
        	ChartUtilities.writeBufferedImageAsPNG(out, chartImage);
        } finally {
        	out.close();
        }
    }
    
	public static void drawMasks(Graphics2D g2,
			Rectangle2D imageArea, LinkedHashMap<AbstractMask, Color> maskList,
			AbstractMask selectedMask, JFreeChart chart) {
		drawMasks(g2, imageArea, maskList, selectedMask, chart, 1);
	}
	
	public static void drawMasks(Graphics2D g2,
			Rectangle2D imageArea, LinkedHashMap<AbstractMask, Color> maskList,
			AbstractMask selectedMask, JFreeChart chart, double fontSizeRate) {
		for (Entry<AbstractMask, Color> maskEntry : maskList.entrySet()) {
			AbstractMask mask = maskEntry.getKey();
			if (mask instanceof Abstract2DMask) {
				Color fillColor = mask.isInclusive() ? 
						Hist2DPanel.MASK_INCLUSIVE_COLOR : Hist2DPanel.MASK_EXCLUSIVE_COLOR;
				drawMaskRectangle(g2, imageArea, (Abstract2DMask) mask, 
						(Abstract2DMask) selectedMask, chart, fontSizeRate, 
						fillColor);
			} else if (mask instanceof RangeMask) {
//				Color fillColor = maskEntry.getValue();
				Color fillColor = mask.isInclusive() ? 
						Plot1DPanel.MASK_INCLUSIVE_COLOR : Plot1DPanel.MASK_EXCLUSIVE_COLOR;
				drawDomainMask(g2, imageArea, (RangeMask) mask, 
						(RangeMask) selectedMask, chart, fontSizeRate, fillColor);
			}
		}
	}
	
	private static void drawDomainMask(Graphics2D g2, Rectangle2D imageArea, 
			RangeMask mask, RangeMask selectedMask, JFreeChart chart,
			double fontSizeRate, Color fillColor) {
		g2.clip(imageArea);
		g2.setPaint(fillColor);
		Rectangle2D translatedRectangle = getDomainMaskFrame(mask, imageArea, chart);
		if (translatedRectangle == null || translatedRectangle.isEmpty()) {
			return;
		}
		drawMask(g2, translatedRectangle);
		if (mask == selectedMask) {
			drawMaskBoarder(g2, translatedRectangle);
		}
		drawMaskName(g2, mask, translatedRectangle, fontSizeRate);
	}

	public static void drawShapes(Graphics2D g2, Rectangle2D imageArea,
			LinkedHashMap<Shape, Color> shapeList, JFreeChart chart) {
		for (Entry<Shape, Color> shapeEntry : shapeList.entrySet()) {
			Shape shape = shapeEntry.getKey();
			Color color = shapeEntry.getValue();
			drawShape(g2, imageArea, shape, color, chart);
		}
	}

	public static void drawShapes(Graphics2D g2, Rectangle2D imageArea,
			Shape shape, JFreeChart chart) {
		Color color = Color.CYAN;
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(2f));
		drawShape(g2, imageArea, shape, color, chart);
		g2.setStroke(oldStroke);
	}
	
	public static void drawShape(Graphics2D g2, Rectangle2D imageArea,
			Shape shape, Color color, JFreeChart chart) {
		g2.clip(imageArea);
    	g2.setPaint(color);
    	Shape newShape = translateChartShape(shape, imageArea, chart);
    	if (shape == null) {
    		return;
    	}
    	g2.draw(newShape);
	}

}
