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
import java.awt.Font;

import org.gumtree.vis.awt.time.TimePlotPanel;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.hist2d.ColorPaintScale;
import org.gumtree.vis.hist2d.Hist2DPanel;
import org.gumtree.vis.hist2d.PaintScaleLegend2D;
import org.gumtree.vis.hist2d.Preview2DPanel;
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IPreview2DDataset;
import org.gumtree.vis.interfaces.ISurf3D;
import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYZDataset;
import org.gumtree.vis.plot1d.LogarithmizableAxis;
import org.gumtree.vis.plot1d.Plot1DPanel;
import org.gumtree.vis.surf3d.Surface3DPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * @author nxi
 *
 */
public class PlotFactory {
	
	public static final ChartTheme chartTheme = new DefaultChartTheme("defaultTheme");
	private static final String LOGX_PROPERTY = "plot1D.logX";
	private static final String LOGY_PROPERTY = "plot1D.logY";
	
	public static Plot1DPanel createPlot1DPanel(IXYErrorDataset dataset) {
		
		JFreeChart chart = createXYErrorChart(dataset);
		
		Plot1DPanel panel = new Plot1DPanel(chart);
		panel.setHorizontalAxisTrace(true);
		panel.setVerticalAxisTrace(true);
		panel.setDoubleBuffered(true);
		panel.setFillZoomRectangle(true);
		panel.setZoomAroundAnchor(true);
		panel.setZoomInFactor(StaticValues.defaultZoomInFactor);
		panel.setZoomOutFactor(StaticValues.defaultZoomOutFactor);
		
		return panel;
	}
	
	public static Hist2DPanel createHist2DPanel(IXYZDataset dataset) {
		JFreeChart chart = createXYBlockChart(dataset);
		
		Hist2DPanel panel = new Hist2DPanel(chart);
		panel.setHorizontalAxisTrace(true);
		panel.setVerticalAxisTrace(true);
		panel.setDoubleBuffered(true);
		panel.setFillZoomRectangle(true);
		panel.setZoomAroundAnchor(true);
		panel.setZoomInFactor(StaticValues.defaultZoomInFactor);
		panel.setZoomOutFactor(StaticValues.defaultZoomOutFactor);
		panel.addMouseListener((PaintScaleLegend2D) chart.getSubtitle(0));
		panel.addMouseMotionListener((PaintScaleLegend2D) chart.getSubtitle(0));
		dataset.addChangeListener(panel);
		return panel;
	}
	
	public static Preview2DPanel createPreview2DPanel(IPreview2DDataset dataset) {
		Preview2DPanel panel = new Preview2DPanel(new BorderLayout());
		panel.setDataset(dataset);
		return panel;
	}

	public static TimePlotPanel createTimePlotPanel(ITimeSeriesSet dataset) {
		JFreeChart chart = createTimeChart(dataset);
		TimePlotPanel panel = new TimePlotPanel(chart);
		panel.setHorizontalAxisTrace(true);
		panel.setVerticalAxisTrace(true);
		panel.setDoubleBuffered(true);
		panel.setFillZoomRectangle(true);
		panel.setZoomAroundAnchor(true);
		panel.setZoomInFactor(StaticValues.defaultZoomInFactor);
		panel.setZoomOutFactor(StaticValues.defaultZoomOutFactor);
		return panel;
	}

	public static ISurf3D createPlot3DPanel(IXYZDataset dataset) {
		Surface3DPanel panel = new Surface3DPanel(new BorderLayout());
		panel.setDataset(dataset);
		return panel;
	}

	public static JFreeChart createXYErrorChart(IXYErrorDataset dataset) {

		JFreeChart chart;
		String title = null;
		String xTitle = null;
		String yTitle = null;
		if (dataset != null) {
			title = "";
			if (dataset.getTitle() != null) {
				title = dataset.getTitle();
			}
			xTitle = "";
			if (dataset.getXTitle() != null) {
				xTitle += dataset.getXTitle();
			}
			if (dataset.getXUnits() != null) {
				xTitle += " (" + dataset.getXUnits() + ")";
			}
			yTitle = "";
			if (dataset.getYTitle() != null) {
				yTitle += dataset.getYTitle();
			}
			if (dataset.getYUnits() != null) {
				yTitle += " (" + dataset.getYUnits() + ")";
			}
		} else {
			dataset = new XYErrorDataset();
		}
		chart = createXYLineChart(
				title,
				xTitle,
				yTitle,
				dataset, 
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				true);
		chart.setBackgroundPaint(Color.WHITE);
		final LegendTitle legend = (LegendTitle) chart.getLegend();
		RectangleEdge legendPosition = RectangleEdge.BOTTOM;
		try {
			String legendProperty = "RectangleEdge." + System.getProperty("kuranda1D.legendPosition");
			if (RectangleEdge.BOTTOM.toString().equals(legendProperty))
					legendPosition = RectangleEdge.BOTTOM;
			else if (RectangleEdge.RIGHT.toString().equals(legendProperty))
				legendPosition = RectangleEdge.RIGHT;
			else if (RectangleEdge.LEFT.toString().equals(legendProperty))
				legendPosition = RectangleEdge.LEFT;
			else if (RectangleEdge.TOP.toString().equals(legendProperty))
				legendPosition = RectangleEdge.TOP;
		} catch (Exception e) {
			// TODO: handle exception
		}
		legend.setPosition(legendPosition);
		chart.setBorderVisible(true);
//		ChartUtilities.applyCurrentTheme(chart);
//		chartTheme.apply(chart);

		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
//		plot.setRangeZeroBaselineVisible(false);
//		plot.setDomainZeroBaselineVisible(false);
		ValueAxis rangeAxis = plot.getRangeAxis();
		if (rangeAxis instanceof NumberAxis) {
			((NumberAxis) rangeAxis).setAutoRangeStickyZero(false);
			((NumberAxis) rangeAxis).setAutoRangeIncludesZero(false);
		}
		ValueAxis domainAxis = plot.getDomainAxis();
		if (domainAxis instanceof NumberAxis) {
			((NumberAxis) domainAxis).setAutoRangeStickyZero(false);
			((NumberAxis) domainAxis).setAutoRangeIncludesZero(false);
		}
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
        
		plot.setDomainGridlinesVisible(true);
//        plot.setDomainCrosshairLockedOnData(true);
//        plot.setDomainCrosshairVisible(true);
        plot.setRangeGridlinesVisible(true);
//        plot.setRangeCrosshairLockedOnData(true);
//        plot.setRangeCrosshairVisible(true);

//		xAxis = plot.getDomainAxis();
//        yAxis = plot.getRangeAxis();

        plot.setDataset(dataset);
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		if (renderer instanceof XYErrorRenderer) {
//			((XYLineAndShapeRenderer) renderer).setBaseShapesVisible(true);
			((XYErrorRenderer) renderer).setBaseShapesFilled(true);
			((XYErrorRenderer) renderer).setDrawXError(false);
			((XYErrorRenderer) renderer).setDrawYError(true);
		}

		chart.fireChartChanged();
		return chart;
	}
	
    /**
     * Creates a line chart (based on an {@link XYDataset}) with default
     * settings.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param xAxisLabel  a label for the X-axis (<code>null</code> permitted).
     * @param yAxisLabel  a label for the Y-axis (<code>null</code> permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param orientation  the plot orientation (horizontal or vertical)
     *                     (<code>null</code> NOT permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return The chart.
     */
    protected static JFreeChart createXYLineChart(String title,
                                               String xAxisLabel,
                                               String yAxisLabel,
                                               XYDataset dataset,
                                               PlotOrientation orientation,
                                               boolean legend,
                                               boolean tooltips,
                                               boolean urls) {

        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
//        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        LogarithmizableAxis xAxis = new LogarithmizableAxis(xAxisLabel);
        boolean isLogX = false;
        try {
        	isLogX = Boolean.valueOf(System.getProperty(LOGX_PROPERTY));
        } catch (Exception e) {
		}
        xAxis.setLogarithmic(isLogX);
//        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setAllowNegativesFlag(true);
        xAxis.setLowerMargin(0.02);
        xAxis.setUpperMargin(0.02);
//        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        LogarithmizableAxis yAxis = new LogarithmizableAxis(yAxisLabel);
        boolean isLogY = false;
        try {
        	isLogY = Boolean.valueOf(System.getProperty(LOGY_PROPERTY));
        } catch (Exception e) {
		}
        yAxis.setLogarithmic(isLogY);
        yAxis.setAllowNegativesFlag(true);
        yAxis.setAutoRangeNextLogFlag(false);
        yAxis.setLowerMargin(0.02);
        yAxis.setUpperMargin(0.02);
        XYErrorRenderer renderer = new XYErrorRenderer();
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(false);
        renderer.setDrawYError(true);
//        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(orientation);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        chartTheme.apply(chart);
        return chart;

    }
    
	public static JFreeChart createXYBlockChart(IXYZDataset dataset) {
		NumberAxis xAxis = createXAxis(dataset);
		NumberAxis yAxis = createYAxis(dataset);
		NumberAxis scaleAxis = createScaleAxis(dataset);

		float min = (float) dataset.getZMin();
		float max = (float) dataset.getZMax();
		PaintScale scale = generateRainbowScale(min, max, StaticValues.DEFAULT_COLOR_SCALE);
		XYBlockRenderer renderer = createRender(dataset, scale);
		
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		
		JFreeChart chart = new JFreeChart(dataset.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, plot, false);
//		chart = new JFreeChart(dataset.getTitle(), plot);
		chart.removeLegend();
		chart.setBackgroundPaint(Color.white);

		PaintScale scaleBar = generateRainbowScale(min, max, StaticValues.DEFAULT_COLOR_SCALE);
		PaintScaleLegend legend = createScaleLegend(scale, scaleAxis);
		legend.setSubdivisionCount(ColorScale.DIVISION_COUNT);
//		legend.setStripOutlineVisible(true);
		chart.addSubtitle(legend);
		chart.setBorderVisible(true);
//		ChartUtilities.applyCurrentTheme(chart);
		chartTheme.apply(chart);
		chart.fireChartChanged();
		return chart;
	}

	private static NumberAxis createScaleAxis(IXYZDataset dataset) {
		NumberAxis scaleAxis = new NumberAxis(null);
		scaleAxis.setRange(dataset.getZMin(), dataset.getZMax());
		return scaleAxis;
	}

	private static PaintScaleLegend createScaleLegend(PaintScale scale, NumberAxis scaleAxis) {
		
		PaintScaleLegend legend = new PaintScaleLegend2D(scale, scaleAxis);
		legend.setSubdivisionCount(20);
		legend.setStripOutlineVisible(false);
		legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		legend.setAxisOffset(5);
		RectangleInsets rec = new RectangleInsets(5, 0, 10, 5);
		legend.setMargin(rec);
		RectangleInsets rec2 = new RectangleInsets(4, 0, 22, 2);
		legend.setPadding(rec2);
		legend.setStripWidth(10);
		legend.setPosition(RectangleEdge.RIGHT);
		return legend;
	}

	private static XYBlockRenderer createRender(IXYZDataset dataset, 
			PaintScale scale) {
//		renderer = new Render2D();
		XYBlockRenderer renderer = new XYBlockRenderer();
		renderer.setPaintScale(scale);
		renderer.setBlockHeight(dataset.getYBlockSize());
		renderer.setBlockWidth(dataset.getXBlockSize());
		return renderer;
	}

	private static ColorPaintScale generateRainbowScale(float min, float max, ColorScale colorScale) {
		ColorPaintScale scale = new ColorPaintScale(min, max, colorScale);
		return scale;
	}

	private static NumberAxis createYAxis(IXYZDataset dataset) {
		String title = "";
		String yTitle = dataset.getYTitle();
		if (yTitle != null) {
			title += yTitle;
		}
		String yUnits = dataset.getYUnits();
		if (yUnits != null) {
			title += " (" + yUnits + ")";
		}
		if (title.trim().length() == 0) {
			title = null;
		}
		NumberAxis yAxis = new NumberAxis(title);
		yAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		yAxis.setLowerMargin(0.0);
		yAxis.setUpperMargin(0.0);
		yAxis.setAutoRangeIncludesZero(false);
		return yAxis;
	}

	private static NumberAxis createXAxis(IXYZDataset dataset) {
		String title = "";
		String xTitle = dataset.getXTitle();
		if (xTitle != null) {
			title += xTitle;
		}
		String xUnits = dataset.getXUnits();
		if (xUnits != null) {
			title += " (" + xUnits + ")";
		}
		if (title.trim().length() == 0) {
			title = null;
		}
		NumberAxis xAxis = new NumberAxis(title);
		xAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);
		xAxis.setAutoRangeIncludesZero(false);
		return xAxis;
	}
	
	public static JFreeChart createTimeChart(ITimeSeriesSet timeSeriesSet) {
//		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				timeSeriesSet.getTitle(), null, timeSeriesSet.getYTitle() 
				+ (timeSeriesSet.getYUnits() != null ? " (" + timeSeriesSet.getYUnits() + ")" : ""), 
				timeSeriesSet, 
				true, true, false);   
        XYPlot xyplot = chart.getXYPlot();   
        ValueAxis valueaxis = xyplot.getDomainAxis();   
        valueaxis.setAutoRange(true);
//        valueaxis.setFixedAutoRange(60000D);   
        valueaxis = xyplot.getRangeAxis();   
        valueaxis.setRange(0.0D, 200D);
        valueaxis.setAutoRange(true);
        XYItemRenderer renderer = xyplot.getRenderer();
        if (renderer instanceof XYLineAndShapeRenderer) {
        	((XYLineAndShapeRenderer) renderer).setBaseShapesVisible(false);
        }
        chartTheme.apply(chart);
		Font font = valueaxis.getLabelFont();
		valueaxis.setLabelFont(font.deriveFont(Font.PLAIN));
        return chart;   
	}

    /**
     * Creates and returns a time series chart.  A time series chart is an
     * {@link XYPlot} with a {@link DateAxis} for the x-axis and a
     * {@link NumberAxis} for the y-axis.  The default renderer is an
     * {@link XYLineAndShapeRenderer}.
     * <P>
     * A convenient dataset to use with this chart is a
     * {@link org.jfree.data.time.TimeSeriesCollection}.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param timeAxisLabel  a label for the time axis (<code>null</code>
     *                       permitted).
     * @param valueAxisLabel  a label for the value axis (<code>null</code>
     *                        permitted).
     * @param dataset  the dataset for the chart (<code>null</code> permitted).
     * @param legend  a flag specifying whether or not a legend is required.
     * @param tooltips  configure chart to generate tool tips?
     * @param urls  configure chart to generate URLs?
     *
     * @return A time series chart.
     */
    public static JFreeChart createTimeSeriesChart(String title,
                                                   String timeAxisLabel,
                                                   String valueAxisLabel,
                                                   XYDataset dataset,
                                                   boolean legend,
                                                   boolean tooltips,
                                                   boolean urls) {

        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
        timeAxis.setLowerMargin(0.02);  // reduce the default margins
        timeAxis.setUpperMargin(0.02);
//        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        LogAxis valueAxis = new LogAxis(valueAxisLabel);
//        valueAxis.setAutoRangeIncludesZero(false);  // override default
        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);

        XYToolTipGenerator toolTipGenerator = null;
        if (tooltips) {
            toolTipGenerator
                = StandardXYToolTipGenerator.getTimeSeriesInstance();
        }

        XYURLGenerator urlGenerator = null;
        if (urls) {
            urlGenerator = new StandardXYURLGenerator();
        }

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,
                false);
        renderer.setBaseToolTipGenerator(toolTipGenerator);
        renderer.setURLGenerator(urlGenerator);
        plot.setRenderer(renderer);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, legend);
        ChartFactory.getChartTheme().apply(chart);
        return chart;

    }
}
