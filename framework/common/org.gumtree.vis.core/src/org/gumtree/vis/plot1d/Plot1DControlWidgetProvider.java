/**
 * 
 */
package org.gumtree.vis.plot1d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.io.ExporterManager;
import org.gumtree.vis.swt.IPlotControlWidgetProvider;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;

/**
 * @author nxi
 *
 */
public class Plot1DControlWidgetProvider implements IPlotControlWidgetProvider {

	private IPlot plot;
	/* (non-Javadoc)
	 * @see org.gumtree.vis.swt.IPlotControlWidgetProvider#createControlToolItems(org.eclipse.swt.widgets.ToolBar)
	 */
	@Override
	public List<ToolItem> createControlToolItems(ToolBar toolBar) {
		List<ToolItem> controlItems = new ArrayList<ToolItem>();
		
		final ToolItem exportDataToolItem;
		exportDataToolItem = new ToolItem (toolBar, SWT.DROP_DOWN);
		exportDataToolItem.setToolTipText("Export data in multiple formats");
		exportDataToolItem.setImage(getImage("icons/table_export_16x16.png"));
		final Menu exporterListMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		exportDataToolItem.setData(exporterListMenu);
		controlItems.add(exportDataToolItem);
		List<IExporter> exporters = ExporterManager.get1DExporters();
		for (final IExporter exporter : exporters) {
			final MenuItem scaleItem = new MenuItem(exporterListMenu, SWT.PUSH);
			scaleItem.setText(exporter.toString());
			scaleItem.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					Thread newThread = new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								getPlot().doExport(exporter);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					});
					newThread.start();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		exportDataToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Rectangle rect = exportDataToolItem.getBounds ();
				Point pt = new Point (rect.x, rect.y + rect.height);
				pt = exportDataToolItem.getParent().toDisplay (pt);
				exporterListMenu.setLocation (pt.x, pt.y);
				exporterListMenu.setVisible (true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		ToolItem maskToolItem = new ToolItem (toolBar, SWT.PUSH);
		maskToolItem.setToolTipText("Manage ROI");
		maskToolItem.setImage(getImage("icons/roi_16x16.png"));
		controlItems.add(maskToolItem);
		maskToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						((Plot1DPanel) getPlot()).doEditMaskProperties();
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		ToolItem markerToolItem = new ToolItem (toolBar, SWT.PUSH);
		markerToolItem.setToolTipText("Toggle showing marker");
		markerToolItem.setImage(getImage("icons/marker_add_16x16.png"));
		controlItems.add(markerToolItem);
		markerToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) getPlot().getXYPlot().getRenderer();
				boolean isMarkerVisible = !renderer.getBaseShapesVisible();
    			for (int i = 0; i < getPlot().getXYPlot().getSeriesCount(); i++) {
    				renderer.setSeriesShapesVisible(i, isMarkerVisible);
    			}
    			renderer.setBaseShapesVisible(isMarkerVisible);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		ToolItem errorBarToolItem = new ToolItem (toolBar, SWT.PUSH);
		errorBarToolItem.setToolTipText("Toggle showing error bar");
		errorBarToolItem.setImage(getImage("icons/error_bar2_16x16_l2.png"));
		controlItems.add(errorBarToolItem);
		errorBarToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				XYErrorRenderer renderer = (XYErrorRenderer) getPlot().getXYPlot().getRenderer();
				renderer.setDrawYError(!renderer.getDrawYError());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		ToolItem lineToolItem = new ToolItem (toolBar, SWT.PUSH);
		lineToolItem.setToolTipText("Toggle showing lines");
		lineToolItem.setImage(getImage("icons/chart_line_dot_16x16.png"));
		controlItems.add(lineToolItem);
		lineToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				XYErrorRenderer renderer = (XYErrorRenderer) getPlot().getXYPlot().getRenderer();
				for (int i = 0; i < getPlot().getXYPlot().getSeriesCount(); i++) {
					Boolean isLineVisable = renderer.getSeriesLinesVisible(i);
					if (isLineVisable == null) {
						isLineVisable = renderer.getBaseLinesVisible();
					}
    				renderer.setSeriesLinesVisible(i, !isLineVisable);
    			}
				renderer.setBaseLinesVisible(!renderer.getBaseLinesVisible());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		ToolItem flipXToolItem = new ToolItem (toolBar, SWT.PUSH);
		flipXToolItem.setToolTipText("Flip horizontally");
		flipXToolItem.setImage(getImage("icons/horizontal_flip_16x16_blue.png"));
		controlItems.add(flipXToolItem);
		flipXToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ValueAxis axis = getPlot().getXYPlot().getDomainAxis();
				axis.setInverted(!axis.isInverted());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		ToolItem flipYToolItem = new ToolItem (toolBar, SWT.PUSH);
		flipYToolItem.setToolTipText("Flip vertically");
		flipYToolItem.setImage(getImage("icons/vertical_flip_16x16_blue.png"));
		controlItems.add(flipYToolItem);
		flipYToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ValueAxis axis = getPlot().getXYPlot().getRangeAxis();
				axis.setInverted(!axis.isInverted());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		ToolItem legendToolItem = new ToolItem (toolBar, SWT.PUSH);
		legendToolItem.setToolTipText("Toggle showing legend");
		legendToolItem.setImage(getImage("icons/toggle_legend_16x16.png"));
		controlItems.add(legendToolItem);
		legendToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				LegendTitle legend = getPlot().getChart().getLegend();
				legend.setVisible(!legend.isVisible());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		return controlItems;
	}

	private Image getImage(String path) {
		return new Image(Display.getDefault(), getClass().getClassLoader().getResourceAsStream(path));
	}

	/**
	 * @return the plot
	 */
	public IPlot getPlot() {
		return plot;
	}

	/**
	 * @param plot the plot to set
	 */
	public void setPlot(IPlot plot) {
		this.plot = plot;
	}

}
