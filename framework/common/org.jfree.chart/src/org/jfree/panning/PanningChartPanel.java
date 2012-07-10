package org.jfree.panning;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;

/**
 * A Swing GUI component for displaying a {@link JFreeChart} object.
 * <P>
 * The panel registers with the chart to receive notification of changes to any component of the
 * chart. The chart is redrawn automatically whenever this notification is received.
 */
public class PanningChartPanel
    extends ChartPanel
    implements MouseWheelListener
{

    public static final int MOUSE_ZOOM = 1;
    public static final int MOUSE_PAN = 2;

    private int mouseMode = MOUSE_ZOOM;
    private int lastPanX = -1;
    private int lastPanY = -1;
    private Point2D panStartPoint = null;
    double zoomFactor = 0.9d;

    /**
     * Constructs a panel that displays the specified chart.
     * 
     * @param chart the chart.
     */
    public PanningChartPanel(JFreeChart chart)
    {
        super(chart);
        addMouseWheelListener(this);
    }

    /**
     * Constructs a panel containing a chart.
     * 
     * @param chart the chart.
     * @param useBuffer a flag controlling whether or not an off-screen buffer is used.
     */
    public PanningChartPanel(JFreeChart chart, boolean useBuffer)
    {
        super(chart, useBuffer);
        addMouseWheelListener(this);
    }

    /**
     * Constructs a JFreeChart panel.
     * 
     * @param chart the chart.
     * @param properties a flag indicating whether or not the chart property editor should be
     *            available via the popup menu.
     * @param save a flag indicating whether or not save options should be available via the popup
     *            menu.
     * @param print a flag indicating whether or not the print option should be available via the
     *            popup menu.
     * @param zoom a flag indicating whether or not zoom options should be added to the popup menu.
     * @param tooltips a flag indicating whether or not tooltips should be enabled for the chart.
     */
    public PanningChartPanel(JFreeChart chart, boolean properties, boolean save, boolean print,
        boolean zoom, boolean tooltips)
    {
        super(chart, properties, save, print, zoom, tooltips);
        addMouseWheelListener(this);
    }

    /**
     * Constructs a JFreeChart panel.
     * 
     * @param chart the chart.
     * @param width the preferred width of the panel.
     * @param height the preferred height of the panel.
     * @param minimumDrawWidth the minimum drawing width.
     * @param minimumDrawHeight the minimum drawing height.
     * @param maximumDrawWidth the maximum drawing width.
     * @param maximumDrawHeight the maximum drawing height.
     * @param useBuffer a flag that indicates whether to use the off-screen buffer to improve
     *            performance (at the expense of memory).
     * @param properties a flag indicating whether or not the chart property editor should be
     *            available via the popup menu.
     * @param save a flag indicating whether or not save options should be available via the popup
     *            menu.
     * @param print a flag indicating whether or not the print option should be available via the
     *            popup menu.
     * @param zoom a flag indicating whether or not zoom options should be added to the popup menu.
     * @param tooltips a flag indicating whether or not tooltips should be enabled for the chart.
     */
    public PanningChartPanel(JFreeChart chart, int width, int height, int minimumDrawWidth,
        int minimumDrawHeight, int maximumDrawWidth, int maximumDrawHeight, boolean useBuffer,
        boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips)
    {
        super(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth,
              maximumDrawHeight, useBuffer, properties, save, print, zoom, tooltips);
        addMouseWheelListener(this);
    }

    /**
     * Returns a point based on (x, y) but constrained to be within the bounds of the given
     * rectangle. This method could be moved to JCommon.
     * 
     * @param x the x-coordinate.
     * @param y the y-coordinate.
     * @param area the rectangle (<code>null</code> not permitted).
     * @return A point within the rectangle.
     */
    private Point getPointInRectangle(int x, int y, Rectangle2D area)
    {
        x = (int)Math.max(Math.ceil(area.getMinX()), Math.min(x, Math.floor(area.getMaxX())));
        y = (int)Math.max(Math.ceil(area.getMinY()), Math.min(y, Math.floor(area.getMaxY())));
        return new Point(x, y);
    }

    /**
     * Handles a 'mouse pressed' event.
     * <P>
     * This event is the popup trigger on Unix/Linux. For Windows, the popup trigger is the 'mouse
     * released' event.
     * 
     * @param e The mouse event.
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        if (e.isControlDown())
        {
            this.mouseMode = MOUSE_PAN;

            Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
            if (screenDataArea != null)
            {
                Point point = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
                panStartPoint = point;
                lastPanX = point.x;
                lastPanY = point.y;
            }
        }
        else
        {
            this.mouseMode = MOUSE_ZOOM;
        }
        
        super.mousePressed(e);
    }

    /**
     * Handles a 'mouse dragged' event.
     * 
     * @param e the mouse event.
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {

        if (this.mouseMode == MOUSE_PAN)
        {
            // panStartPoint might be null if the mouse was pressed between
            // two plots.
            if (panStartPoint == null)
            {
                // try to get a new start point if the user drags inside a plot
                // use the first point in the plot as start point
                Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
                if (screenDataArea != null)
                {
                    Point point = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
                    panStartPoint = point;
                }
            }
            else
            {
                move(panStartPoint, lastPanX, lastPanY, e.getX(), e.getY());
            }
            lastPanX = e.getX();
            lastPanY = e.getY();
            return;
        }

        super.mouseDragged(e);
    }

    private void move(Point2D startPoint, int oldX, int oldY, int newX, int newY)
    {

        /*
         * this is selection of move direction. e.g. dragged to right -> what does this mean ? move
         * selection to right or left ? value of -1 is the typical graphic program behaviour
         */

        double moveDirection = -1;

        double diffX = moveDirection * (newX - oldX); // *
        double diffY = -1 * moveDirection * (newY - oldY); // *

        // check for change
        if (diffX == 0 && diffY == 0)
            return;

        try
        {
            getChart().setNotify(false);

            if (getChart().getPlot() instanceof Pannable)
            {
                Pannable panningPlot = (Pannable)getChart().getPlot();
                if (panningPlot.isDomainPannable())
                {
                    if (panningPlot.getOrientation() == PlotOrientation.VERTICAL)
                    {
                        panningPlot.panDomainAxis(diffX, getChartRenderingInfo().getPlotInfo(),
                            startPoint);
                    }
                    else
                    {
                        panningPlot.panDomainAxis(diffY, getChartRenderingInfo().getPlotInfo(),
                            startPoint);
                    }
                }

                if (panningPlot.isRangePannable())
                {
                    if (panningPlot.getOrientation() == PlotOrientation.VERTICAL)
                    {
                        panningPlot.panRangeAxis(diffY, getChartRenderingInfo().getPlotInfo(),
                            startPoint);
                    }
                    else
                    {
                        panningPlot.panRangeAxis(diffX, getChartRenderingInfo().getPlotInfo(),
                            startPoint);
                    }
                }
            }
        }
        finally
        {
            getChart().setNotify(true);
        }

    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        Point2D p = translateScreenToJava2D(e.getPoint());
        // only act in case mouse is in data area
        if (!getChartRenderingInfo().getPlotInfo().getDataArea().contains(p.getX(), p.getY()))
            return;

        /*
         * getWheelRotation < 0 -> wheel moved up (away from user) getWheelRotation > 0 -> wheel
         * moved down (towards the user) take wheel moved away from user for zoom in.
         */
        int increment = e.getWheelRotation() < 0 ? 1 : e.getWheelRotation() > 0 ? -1 : 0;

        boolean oldNotify = getChart().isNotify();

        // do not notify while zooming each axis
        getChart().setNotify(false);

        if (increment > 0)
            zoomIn(p);
        else if (increment < 0)
            zoomOut(p);

        // set the old notify status
        getChart().setNotify(oldNotify);
    }

    /**
     * zooms in.
     * 
     * @param point the Java2D point where the zoom should center
     */
    private void zoomIn(Point2D point)
    {
        // ----------------

        // check if the mouse zoom was originated in a subplot
        int subPlot = getChartRenderingInfo().getPlotInfo().getSubplotIndex(point);

        PlotRenderingInfo plotInfo = (subPlot >= 0 ? getChartRenderingInfo().getPlotInfo()
            .getSubplotInfo(subPlot) : getChartRenderingInfo().getPlotInfo());

        Rectangle2D dataArea = plotInfo.getDataArea();

        // get mouse position in plot (lower left is (0,0))
        double mouseXPxl = point.getX() - dataArea.getX();
        double mouseYPxl = point.getY() - dataArea.getY();

        // relative position in %
        double relPosX = mouseXPxl / dataArea.getWidth();
        double relPosY = 1 - (mouseYPxl / dataArea.getHeight());

        PlotOrientation orientation = getChart().getXYPlot().getOrientation();
        double domainRelPos = (orientation == PlotOrientation.VERTICAL ? relPosX : relPosY);
        double rangeRelPos = (orientation == PlotOrientation.VERTICAL ? relPosY : relPosX);

        for (int di = 0; di < getChart().getXYPlot().getDomainAxisCount(); di++)
        {
            ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis(di);
            if (domainAxis == null)
            {
                continue;
            }
            domainAxis.zoomRange(domainRelPos * (1 - getZoomFactor()), domainRelPos
                + getZoomFactor() * (1 - domainRelPos));
        }

        for (int ri = 0; ri < getChart().getXYPlot().getRangeAxisCount(); ri++)
        {
            ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis(ri);
            if (rangeAxis == null)
            {
                continue;
            }
            rangeAxis.zoomRange(rangeRelPos * (1 - getZoomFactor()), rangeRelPos + getZoomFactor()
                * (1 - rangeRelPos));
        }
    }

    /**
     * zooms out.
     * 
     * @param point the Java2D point where the zoom should center
     */
    private void zoomOut(Point2D point)
    {
        // check if the mouse zoom was originated in a subplot
        int subPlot = getChartRenderingInfo().getPlotInfo().getSubplotIndex(point);

        PlotRenderingInfo plotInfo = (subPlot >= 0 ? getChartRenderingInfo().getPlotInfo()
            .getSubplotInfo(subPlot) : getChartRenderingInfo().getPlotInfo());

        Rectangle2D dataArea = plotInfo.getDataArea();

        // get mouse position in plot (lower left is (0,0))
        double mouseXPxl = point.getX() - dataArea.getX();
        double mouseYPxl = point.getY() - dataArea.getY();

        // relative position in %
        double relPosX = mouseXPxl / dataArea.getWidth();
        double relPosY = 1 - (mouseYPxl / dataArea.getHeight());

        PlotOrientation orientation = getChart().getXYPlot().getOrientation();
        double domainRelPos = (orientation == PlotOrientation.VERTICAL ? relPosX : relPosY);
        double rangeRelPos = (orientation == PlotOrientation.VERTICAL ? relPosY : relPosX);

        for (int di = 0; di < getChart().getXYPlot().getDomainAxisCount(); di++)
        {
            ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis(di);
            if (domainAxis == null)
            {
                continue;
            }
            domainAxis.zoomRange(-1 * domainRelPos * (1 - getZoomFactor()), 1.0
                + (1 - domainRelPos) * (1 - getZoomFactor()));
        }

        for (int ri = 0; ri < getChart().getXYPlot().getRangeAxisCount(); ri++)
        {
            ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis(ri);
            if (rangeAxis == null)
            {
                continue;
            }
            rangeAxis.zoomRange(-1 * rangeRelPos * (1 - getZoomFactor()), 1.0 + (1 - rangeRelPos)
                * (1 - getZoomFactor()));
        }

    }

    public double getZoomFactor()
    {
        return zoomFactor;
    }

    public void setZoomFactor(double zoomFactor)
    {
        this.zoomFactor = zoomFactor;
    }
}
