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
package org.gumtree.vis.plot1d;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.MouseWheelEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.vis.awt.DefaultChartTheme;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.listener.XYChartMouseEvent;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * @author nxi
 *
 */
public class Plot1D extends Composite {

	private static final float defaultZoomInFactor = 0.85f;
	private static final float slowZoomInFactor = 0.95f;
	private static final float defaultZoomOutFactor = 1 / defaultZoomInFactor;
	private static final float slowZoomOutFactor = 1 / slowZoomInFactor;
	private static final ChartTheme chartTheme = new DefaultChartTheme("default2D");
	private Composite parent;
	private XYErrorDataset dataset;
	private ValueAxis xAxis;
	private ValueAxis yAxis;
	private JFreeChart chart;
	private Plot1DPanel panel;
	

	public Plot1D(Composite parent, int style) {
		super(parent, SWT.EMBEDDED | style);
		this.parent = parent;
		FillLayout layout = new FillLayout(SWT.FILL);
		setLayout(layout);
//		GridLayoutFactory.fillDefaults().applyTo(this);
	}

	public void plot() {
		createChart();
		final Frame frame = SWT_AWT.new_Frame(this);
		panel = new Plot1DPanel(chart);
		panel.setHorizontalAxisTrace(true);
		panel.setVerticalAxisTrace(true);
		panel.setDoubleBuffered(true);
		panel.setFillZoomRectangle(true);
		panel.setZoomAroundAnchor(true);
		panel.setZoomInFactor(defaultZoomInFactor);
		panel.setZoomOutFactor(defaultZoomOutFactor);
		frame.add(panel);
		createStatusBar();
		addListeners();
	}
	
	
	private void addListeners() {
		addMouseWheelListener(new MouseWheelListener() {
			
//			int time;
			@Override
			public void mouseScrolled(MouseEvent event) {
//				int currentTime = event.time;
//				if (currentTime - time > 500) {
//					int increment = event.count == 0 ? 0 : event.count > 0 ? 1 : -1;
//					if ((event.stateMask & SWT.CTRL) != 0) {
//						if (increment < 0) {
//							panel.setZoomOutFactor(slowZoomOutFactor);
//							panel.zoomOutBoth(event.x, event.y);
//							panel.setZoomOutFactor(defaultZoomOutFactor);
//						}
//						if (increment > 0) { 
//							panel.setZoomInFactor(slowZoomInFactor);
//							panel.zoomInBoth(event.x, event.y);
//							panel.setZoomInFactor(defaultZoomInFactor);
//						}
//					} else {
//						if (increment < 0) {
//							panel.zoomOutBoth(event.x, event.y);
//						}
//						if (increment > 0) { 
//							panel.zoomInBoth(event.x, event.y);
//						}
//					}
//					time = currentTime;
//				}
				MouseWheelEvent awtEvent = org.gumtree.vis.listener.SWT_AWT.toMouseWheelEvent(
						event, panel);
				panel.processMouseWheelEvent(awtEvent);
			}
		});
		
		addKeyListener(new KeyListener() {
			
			boolean keyPressed = false;
			
			@Override
			public void keyReleased(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.DEL:
					panel.removeSelectedMask();
					break;
				default:
					break;
				}
				switch (event.character) {
				default:
					break;
				}
				keyPressed = false;
			}
			
			@Override
			public void keyPressed(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.ARROW_LEFT:
					panel.moveSelectedMask(event.keyCode);
					break;
				case SWT.ARROW_RIGHT:
					panel.moveSelectedMask(event.keyCode);
					break;
				default:
					break;
				}
				switch (event.stateMask) {
				case SWT.CTRL:
					if (event.keyCode == 'c' || event.keyCode == 'C') {
						if (!keyPressed) {
							panel.doCopy();
						}
					} else if (event.keyCode == 'r' || event.keyCode == 'R') {
						if (!keyPressed) {
							panel.restoreAutoBounds();
						}
					}
					keyPressed = true;
					break;
				default:
					break;
				}
			}
		});
	}

	private void createStatusBar() {
		Composite statusComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(6, false);
		layout.marginLeft = 6;
		layout.marginRight = 6;
		layout.marginTop = 1;
		layout.marginBottom = 1;
		layout.horizontalSpacing = 3;
		layout.verticalSpacing = 1;
		statusComposite.setLayout(layout);
		
		GridData gridData = new GridData(SWT.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		statusComposite.setLayoutData(gridData);
		
		final Label xLabel = new Label(statusComposite, SWT.NONE);
		xLabel.setText("X:");
		gridData = new GridData(SWT.DEFAULT);
		xLabel.setLayoutData(gridData);
//		GridDataFactory.swtDefaults().applyTo(xLabel);
		final Text xText = new Text(statusComposite, SWT.BORDER);
		gridData = new GridData(SWT.FILL);
		gridData.widthHint = 50;
		xText.setLayoutData(gridData);
//		GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).applyTo(xText);
		xText.setEditable(false);
		
		final Label yLabel = new Label(statusComposite, SWT.NONE);
		yLabel.setText("Y:");
		gridData = new GridData(SWT.DEFAULT);
		yLabel.setLayoutData(gridData);
//		GridDataFactory.swtDefaults().applyTo(yLabel);
		final Text yText = new Text(statusComposite, SWT.BORDER);
		gridData = new GridData(SWT.FILL);
		gridData.widthHint = 50;
		yText.setLayoutData(gridData);
//		GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).applyTo(yText);
		yText.setEditable(false);
		
		final Composite composite = this;
		panel.addChartMouseListener(new ChartMouseListener() {
			
			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				if (event instanceof XYChartMouseEvent) {
					final String xString = 
						String.format("%.2f", ((XYChartMouseEvent) event).getX());
						final String yString = 
							String.format("%.2f", ((XYChartMouseEvent) event).getY()); 
//						panel.requestFocus();

						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								xText.setText(xString);
								yText.setText(yString);
								if (!composite.isFocusControl()) {
									composite.setFocus();
								}
							}
						});
				}
			}
			
			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (!composite.isFocusControl()) {
							composite.setFocus();
						}
					}
				});
			}
		});
	}

	/**
	 * @return the dataset
	 */
	public XYErrorDataset getDataset() {
		return dataset;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(XYErrorDataset dataset) {
		
		this.dataset = (XYErrorDataset) dataset;
		if (chart != null) {
			chart.getXYPlot().setDataset(dataset);
		}
	}
	
	
	private void createChart() {

		String title = null;
		String xTitle = null;
		String yTitle = null;
		if (dataset != null) {
			title = dataset.getTitle();
			xTitle = dataset.getXTitle() + (dataset.getXUnits() == null ? "" : 
				" (" + dataset.getXUnits() + ")");
			yTitle = dataset.getYTitle() + (dataset.getYUnits() == null ? "" : 
				" (" + dataset.getYUnits() + ")");
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
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
        
		plot.setDomainGridlinesVisible(true);
//        plot.setDomainCrosshairLockedOnData(true);
//        plot.setDomainCrosshairVisible(true);
        plot.setRangeGridlinesVisible(true);
//        plot.setRangeCrosshairLockedOnData(true);
//        plot.setRangeCrosshairVisible(true);

		xAxis = plot.getDomainAxis();
        yAxis = plot.getRangeAxis();

        plot.setDataset(dataset);
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		if (renderer instanceof XYLineAndShapeRenderer) {
//			((XYLineAndShapeRenderer) renderer).setBaseShapesVisible(true);
			((XYLineAndShapeRenderer) renderer).setBaseShapesFilled(true);
		}
//		XYErrorRenderer errorRenderer = (XYErrorRenderer) chart.getXYPlot().getRenderer();
//		errorRenderer.setDrawXError(true);
//        StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
//        renderer.setPlotLines(true);
//        renderer.setBaseShapesVisible(true);


		chart.fireChartChanged();
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
    public static JFreeChart createXYLineChart(String title,
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
        
        xAxis.setLogarithmic(false);
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setAllowNegativesFlag(true);
        xAxis.setLowerMargin(0.02);
        xAxis.setUpperMargin(0.02);
//        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        LogarithmizableAxis yAxis = new LogarithmizableAxis(yAxisLabel);
        yAxis.setAllowNegativesFlag(true);
        yAxis.setAutoRangeNextLogFlag(false);
        yAxis.setLowerMargin(0.02);
        yAxis.setUpperMargin(0.02);
        yAxis.setLogarithmic(false);
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
}