package org.jfree.panning;

import java.awt.geom.Point2D;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;


public interface Pannable
{
    /**
     * Evaluates if the domain axis can be panned.
     * 
     * @return <code>true</code> if the domain axis is pannable.
     */
    public boolean isDomainPannable();

    /**
     * Evaluates if the range axis can be panned.
     * 
     * @return <code>true</code> if the range axis is pannable.
     */
    public boolean isRangePannable();

    public PlotOrientation getOrientation();

    /**
     * Pans the range axis by <var>panRange</var> pixels.
     * 
     * @param panRange the number of pixels to be panned
     * @param info the plot info
     * @param source the source point where the pan action started.
     */
    public void panRangeAxis(double panRange, PlotRenderingInfo info, Point2D source);

    /**
     * Pans the range axis by <var>panRange</var> pixels.
     * 
     * @param panRangePercent the range given as percentage of the axis range 
     * @param info the plot info
     * @param source the source point where the pan action started.
     */
    public void panRangeAxisByAxisRelation(double panRangePercent, PlotRenderingInfo info, Point2D source);
    
    
    /**
     * Pans the domain axis by <var>panRange</var> pixels.
     * 
     * @param panRange the number of pixels to be panned
     * @param info the plot info
     * @param source the source point where the pan action started.
     */
    public void panDomainAxis(double panRange, PlotRenderingInfo info, Point2D source);

    /**
     * Pans the domain axis by <var>panRange</var> pixels.
     * 
     * @param panRangePercent the range given as percentage of the axis range 
     * @param info the plot info
     * @param source the source point where the pan action started.
     */
    public void panDomainAxisByAxisRelation(double panRangePercent, PlotRenderingInfo info, Point2D source);

}
