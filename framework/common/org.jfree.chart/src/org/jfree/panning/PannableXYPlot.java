package org.jfree.panning;

import java.awt.geom.Point2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

public class PannableXYPlot
    extends XYPlot
    implements Pannable
{
    private boolean domainPannable = true;
    private boolean rangePannable = true;

    public PannableXYPlot()
    {
        super();
    }

    public PannableXYPlot(XYDataset dataset, ValueAxis domainAxis, ValueAxis rangeAxis,
        XYItemRenderer renderer)
    {
        super(dataset, domainAxis, rangeAxis, renderer);
    }

    /**
     * Returns if the domain axis is pannable.
     * 
     * @return <code>true</code> if domain axis is pannable, otherwise <code>false</code>
     */
    public boolean isDomainPannable()
    {
        return domainPannable;
    }

    public void setDomainPannable(boolean pannable)
    {
        this.domainPannable = pannable;
    }

    /**
     * Returns if the range axis is pannable.
     * 
     * @return <code>true</code> if range axis is pannable, otherwise <code>false</code>
     */
    public boolean isRangePannable()
    {
        return rangePannable;
    }

    public void setRangePannable(boolean pannable)
    {
        this.rangePannable = pannable;
    }

    public void panRangeAxis(double panRange, PlotRenderingInfo info, Point2D source)
    {
        double fullRange = (getOrientation() == PlotOrientation.HORIZONTAL ? info.getDataArea()
            .getWidth() : info.getDataArea().getHeight());

        for (int i = 0; i < getRangeAxisCount(); i++)
        {
            ValueAxis rangeAxis = getRangeAxis(i);

            double rangeMove = (fullRange != 0 ? Math.abs(rangeAxis.getLowerBound()
                - rangeAxis.getUpperBound())
                * panRange / fullRange : 0);

            rangeAxis.setRange(rangeAxis.getLowerBound() + rangeMove, rangeAxis.getUpperBound()
                + rangeMove);
        }
    }

    public void panDomainAxis(double panRange, PlotRenderingInfo info, Point2D source)
    {
        double fullRange = (getOrientation() == PlotOrientation.HORIZONTAL ? info.getDataArea()
            .getHeight() : info.getDataArea().getWidth());

        for (int i = 0; i < getDomainAxisCount(); i++)
        {
            ValueAxis domainAxis = getDomainAxis(i);

            double rangeMove = (fullRange != 0 ? Math.abs(domainAxis.getLowerBound()
                - domainAxis.getUpperBound())
                * panRange / fullRange : 0);

            domainAxis.setRange(domainAxis.getLowerBound() + rangeMove, domainAxis.getUpperBound()
                + rangeMove);
        }
    }

    public void panDomainAxisByAxisRelation(double panRangePercent, PlotRenderingInfo info,
        Point2D source)
    {
        double fullRange = (getOrientation() == PlotOrientation.HORIZONTAL ? info.getDataArea()
            .getWidth() : info.getDataArea().getHeight());
        double panRange = fullRange * panRangePercent;
        panDomainAxis(panRange, info, source);
    }

    public void panRangeAxisByAxisRelation(double panRangePercent, PlotRenderingInfo info,
        Point2D source)
    {
        double fullRange = (getOrientation() == PlotOrientation.HORIZONTAL ? info.getDataArea()
            .getWidth() : info.getDataArea().getHeight());
        double panRange = fullRange * panRangePercent;
        panRangeAxis(panRange, info, source);
    }

}
