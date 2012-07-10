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
package org.jfree.chart.axis;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Size2D;

/**
 * @author nxi
 *
 */
public class AxisUtilities {

    /**
     * Calculates the space required for all the axes in the plot.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     *
     * @return The required space.
     */
    public static AxisSpace calculateAxisSpace(XYPlot plot, 
    		Graphics2D g2, Rectangle2D plotArea) {
        AxisSpace space = new AxisSpace();
        space = calculateRangeAxisSpace(plot, g2, plotArea, space);
        Rectangle2D revPlotArea = space.shrink(plotArea, null);
        space = calculateDomainAxisSpace(plot, g2, revPlotArea, space);
        return space;
    }
    
    /**
     * Calculates the space required for the range axis/axes.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param space  a carrier for the result (<code>null</code> permitted).
     *
     * @return The required space.
     */
    public static AxisSpace calculateRangeAxisSpace(XYPlot plot, 
    		Graphics2D g2, Rectangle2D plotArea, AxisSpace space) {

        if (space == null) {
            space = new AxisSpace();
        }

        // reserve some space for the range axis...
        if (plot.getFixedRangeAxisSpace() != null) {
            if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                space.ensureAtLeast(plot.getFixedRangeAxisSpace().getTop(),
                        RectangleEdge.TOP);
                space.ensureAtLeast(plot.getFixedRangeAxisSpace().getBottom(),
                        RectangleEdge.BOTTOM);
            }
            else if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                space.ensureAtLeast(plot.getFixedRangeAxisSpace().getLeft(),
                        RectangleEdge.LEFT);
                space.ensureAtLeast(plot.getFixedRangeAxisSpace().getRight(),
                        RectangleEdge.RIGHT);
            }
        }
        else {
            // reserve space for the range axes...
            for (int i = 0; i < plot.getRangeAxisCount(); i++) {
                Axis axis = (Axis) plot.getRangeAxis(i);
                if (axis != null) {
                    RectangleEdge edge = plot.getRangeAxisEdge(i); 
                    space = axis.reserveSpace(g2, plot, plotArea, edge, space);
                }
            }
        }
        return space;

    }
 
    /**
     * Calculates the space required for the domain axis/axes.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param space  a carrier for the result (<code>null</code> permitted).
     *
     * @return The required space.
     */
    public static AxisSpace calculateDomainAxisSpace(XYPlot plot, 
    		Graphics2D g2, Rectangle2D plotArea, AxisSpace space) {

        if (space == null) {
            space = new AxisSpace();
        }

        // reserve some space for the domain axis...
        if (plot.getFixedDomainAxisSpace() != null) {
            if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                space.ensureAtLeast(plot.getFixedDomainAxisSpace().getLeft(),
                        RectangleEdge.LEFT);
                space.ensureAtLeast(plot.getFixedDomainAxisSpace().getRight(),
                        RectangleEdge.RIGHT);
            }
            else if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                space.ensureAtLeast(plot.getFixedDomainAxisSpace().getTop(),
                        RectangleEdge.TOP);
                space.ensureAtLeast(plot.getFixedDomainAxisSpace().getBottom(),
                        RectangleEdge.BOTTOM);
            }
        }
        else {
            // reserve space for the domain axes...
            for (int i = 0; i < plot.getDomainAxisCount(); i++) {
                Axis axis = (Axis) plot.getDomainAxis(i);
                if (axis != null) {
                    RectangleEdge edge = plot.getDomainAxisEdge(i);
                    space = axis.reserveSpace(g2, plot, plotArea, edge, space);
                }
            }
        }

        return space;

    }
    
    public static void trimTitle(Rectangle2D area, Graphics2D g2, Title t, RectangleEdge position) {
    	double ww = area.getWidth();
        double hh = area.getHeight();
    	RectangleConstraint constraint = new RectangleConstraint(ww,
                new Range(0.0, ww), LengthConstraintType.RANGE, hh,
                new Range(0.0, hh), LengthConstraintType.RANGE);
        if (position == RectangleEdge.TOP) {
            Size2D size = t.arrange(g2, constraint);
            area.setRect(area.getX(), Math.min(area.getY() + size.height,
                    area.getMaxY()), area.getWidth(), Math.max(area.getHeight()
                    - size.height, 0));
        }
        else if (position == RectangleEdge.BOTTOM) {
            Size2D size = t.arrange(g2, constraint);
            area.setRect(area.getX(), area.getY(), area.getWidth(),
                    area.getHeight() - size.height);
        }
        else if (position == RectangleEdge.RIGHT) {
            Size2D size = t.arrange(g2, constraint);
            area.setRect(area.getX(), area.getY(), area.getWidth()
                    - size.width, area.getHeight());
        }

        else if (position == RectangleEdge.LEFT) {
            Size2D size = t.arrange(g2, constraint);
            area.setRect(area.getX() + size.width, area.getY(), area.getWidth()
                    - size.width, area.getHeight());
        }
    }
}
