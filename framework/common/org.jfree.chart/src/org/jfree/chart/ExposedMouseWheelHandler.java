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
package org.jfree.chart;

import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.io.Serializable;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;

/**
 * @author nxi
 *
 */
public class ExposedMouseWheelHandler  implements MouseWheelListener, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2792509866433533040L;
	private static final int SLOW_ZOOM_MASK = InputEvent.CTRL_MASK;
	
    /** The chart panel. */
    private ChartPanel chartPanel;

    /** The zoom factor. */
    double zoomFactor;
    double slowerZoomFactor;

	/**
	 * @param chartPanel
	 */
	public ExposedMouseWheelHandler(ChartPanel chartPanel) {
		this.chartPanel = chartPanel;
        this.zoomFactor = 0.10;
        this.slowerZoomFactor = 0.02;
        this.chartPanel.addMouseWheelListener(this);
	}

	@Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        JFreeChart chart = chartPanel.getChart();
        if (chart == null) {
            return;
        }
        Plot plot = chart.getPlot();
        if (plot instanceof Zoomable) {
            Zoomable zoomable = (Zoomable) plot;
            handleZoomable(zoomable, e);
        }
        // TODO:  here we could handle non-zoomable plots in interesting
        // ways (for example, the wheel could rotate a PiePlot or just zoom
        // in on the whole panel).
    }
    
    private void handleZoomable(Zoomable zoomable, MouseWheelEvent e) {
        Plot plot = (Plot) zoomable;
        ChartRenderingInfo info = this.chartPanel.getChartRenderingInfo();
        PlotRenderingInfo pinfo = info.getPlotInfo();
        Point2D p = this.chartPanel.translateScreenToJava2D(e.getPoint());
        if (!pinfo.getDataArea().contains(p)) {
            return;
        }
        int clicks = e.getWheelRotation();
        int direction = 0;
        if (clicks > 0) {
            direction = -1;
        }
        else if (clicks < 0) {
            direction = 1;
        }

        boolean old = plot.isNotify();

        // do not notify while zooming each axis
        plot.setNotify(false);
        double realZoomFactor = this.zoomFactor;
        if ((e.getModifiers() & SLOW_ZOOM_MASK) != 0) {
        	realZoomFactor = slowerZoomFactor;
        }
        double increment = 1.0 + realZoomFactor;
        if (direction > 0) {
            zoomable.zoomDomainAxes(increment, pinfo, p, true);
            zoomable.zoomRangeAxes(increment, pinfo, p, true);
        }
        else if (direction < 0) {
            zoomable.zoomDomainAxes(1.0 / increment, pinfo, p, true);
            zoomable.zoomRangeAxes(1.0 / increment, pinfo, p, true);
        }
        // set the old notify status
        plot.setNotify(old);

    }

    /**
     * Returns the current zoom factor.  The default value is 0.10 (ten
     * percent).
     *
     * @return The zoom factor.
     *
     * @see #setZoomFactor(double)
     */
    public double getZoomFactor() {
        return this.zoomFactor;
    }

    /**
     * Sets the zoom factor.
     *
     * @param zoomFactor  the zoom factor.
     *
     * @see #getZoomFactor()
     */
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

	/**
	 * @return the slowerZoomFactor
	 */
	public double getSlowerZoomFactor() {
		return slowerZoomFactor;
	}

	/**
	 * @param slowerZoomFactor the slowerZoomFactor to set
	 */
	public void setSlowerZoomFactor(double slowerZoomFactor) {
		this.slowerZoomFactor = slowerZoomFactor;
	}
    
    
}
