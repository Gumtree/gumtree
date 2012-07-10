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
package org.gumtree.vis.hist2d;

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
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IXYZDataset;
import org.gumtree.vis.listener.XYZChartMouseEvent;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;


/**
 * @author nxi
 *
 */
public class Hist2D extends Composite {

//	private static final int colorLevels = 256;
	private static final float defaultZoomInFactor = 0.85f;
	private static final float defaultZoomOutFactor = 1 / defaultZoomInFactor;
	private static final ChartTheme defaultChartTheme = new DefaultChartTheme("default2D");
	private Composite parent;
	private IXYZDataset dataset;
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	private NumberAxis scaleAxis;
//	private Render2D renderer;
	private XYBlockRenderer renderer;
	private JFreeChart chart;
	private Hist2DPanel panel;
	
	private ChartTheme chartTheme;

	public Hist2D(Composite parent, int style) {
		super(parent, SWT.EMBEDDED | style);
		this.parent = parent;
		FillLayout layout = new FillLayout(SWT.FILL);
		setLayout(layout);
//		GridLayoutFactory.fillDefaults().applyTo(this);
	}

	public void plot() {
		createChart();
		final Frame frame = SWT_AWT.new_Frame(this);
		panel = new Hist2DPanel(chart);
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
			
//			long time;
			@Override
			public void mouseScrolled(MouseEvent event) {
//				long currentTime = System.currentTimeMillis();
//				if (currentTime - time > 200) {
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
				case SWT.ARROW_UP:
					panel.moveSelectedMask(event.keyCode);
					break;
				case SWT.ARROW_LEFT:
					panel.moveSelectedMask(event.keyCode);
					break;
				case SWT.ARROW_RIGHT:
					panel.moveSelectedMask(event.keyCode);
					break;
				case SWT.ARROW_DOWN:
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
							System.out.println("Do copy");
						}
					} else if (event.keyCode == 'r' || event.keyCode == 'R') {
						if (!keyPressed) {
							panel.restoreAutoBounds();
							System.out.println("reset");
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
		
		final Label zLabel = new Label(statusComposite, SWT.NONE);
		zLabel.setText("Z:");
		gridData = new GridData(SWT.DEFAULT);
		zLabel.setLayoutData(gridData);
//		GridDataFactory.swtDefaults().applyTo(zLabel);
		final Text zText = new Text(statusComposite, SWT.BORDER);
		gridData = new GridData(SWT.FILL);
		gridData.widthHint = 50;
		zText.setLayoutData(gridData);
//		GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).applyTo(zText);
		zText.setEditable(false);
		
		final Composite composite = this;
		panel.addChartMouseListener(new ChartMouseListener() {
			
			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				if (event instanceof XYZChartMouseEvent) {
					final String xString = 
						String.format("%.2f", ((XYZChartMouseEvent) event).getX());
					final String yString = 
						String.format("%.2f", ((XYZChartMouseEvent) event).getY()); 
					final String zString = 
						String.format("%.2f", ((XYZChartMouseEvent) event).getZ());
					panel.requestFocus();
					
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							xText.setText(xString);
							yText.setText(yString);
							zText.setText(zString);
							if (!composite.isFocusControl()) {
								composite.setFocus();
							}
						}
					});
				}
			}
			
			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/**
	 * @return the dataset
	 */
	public XYZDataset getDataset() {
		return dataset;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(IXYZDataset dataset) {
		this.dataset = (IXYZDataset) dataset;
	}
	
	
	private void createChart() {
		createXAxis();
		createYAxis();
		createScaleAxis();

		float min = (float) dataset.getZMin();
		float max = (float) dataset.getZMax();
		PaintScale scale = generateRainbowScale(min, max, ColorScale.Rainbow);
		createRender(scale);
		
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		
		chart = new JFreeChart(dataset.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, plot, false);
//		chart = new JFreeChart(dataset.getTitle(), plot);
		chart.removeLegend();
		chart.setBackgroundPaint(Color.white);

		PaintScale scaleBar = generateRainbowScale(min, max, ColorScale.Rainbow);
		PaintScaleLegend legend = createScaleLegend(scaleBar);
		legend.setSubdivisionCount(ColorScale.DIVISION_COUNT);
		chart.addSubtitle(legend);
		chart.setBorderVisible(true);
//		ChartUtilities.applyCurrentTheme(chart);
		defaultChartTheme.apply(chart);
		chart.fireChartChanged();
	}

	private void createScaleAxis() {
		scaleAxis = new NumberAxis(null);
		scaleAxis.setRange(dataset.getZMin(), dataset.getZMax());
//		scaleAxis.setLabel(null);		
	}

	private PaintScaleLegend createScaleLegend(PaintScale scale) {
		
		PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);
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

	private void createRender(PaintScale scale) {
//		renderer = new Render2D();
		renderer = new XYBlockRenderer();
		renderer.setPaintScale(scale);
		renderer.setBlockHeight(dataset.getYBlockSize());
		renderer.setBlockWidth(dataset.getXBlockSize());
//		XYToolTipGenerator toolTip = new StandardXYZToolTipGenerator();
//		renderer.setBaseToolTipGenerator(toolTip);
	}

	private ColorPaintScale generateRainbowScale(float min, float max, ColorScale colorScale) {
//		LookupPaintScale scale = new LookupPaintScale(min, max, Color.BLACK); //Purple
//		double increment = (max - min) / stepNumber;
//		for (int i = 0; i < stepNumber; i++) {
//			double value = (i * increment ) / (max - min);
//			scale.add(min + i * increment, ColorScale.Rainbow.getColor(value));
//		}
		ColorPaintScale scale = new ColorPaintScale(min, max, colorScale);
		return scale;
	}

	private void createYAxis() {
//		if (yAxis == null) {
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
			yAxis = new NumberAxis(title);
			yAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
			yAxis.setLowerMargin(0.0);
			yAxis.setUpperMargin(0.0);
			yAxis.setAutoRangeIncludesZero(false);
//		}
	}

	private void createXAxis() {
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
		xAxis = new NumberAxis(title);
		xAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);
		xAxis.setAutoRangeIncludesZero(false);
	}
	
	public void applyChartTheme(ChartTheme theme) {
		theme.apply(chart);
		this.chartTheme = theme;
	}
}
