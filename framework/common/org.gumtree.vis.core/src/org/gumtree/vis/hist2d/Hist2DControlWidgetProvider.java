/**
 * 
 */
package org.gumtree.vis.hist2d;

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
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IHist2D;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.io.ExporterManager;
import org.gumtree.vis.swt.IPlotControlWidgetProvider;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.PaintScaleLegend;

/**
 * @author nxi
 *
 */
public class Hist2DControlWidgetProvider implements IPlotControlWidgetProvider {

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
		controlItems.add(exportDataToolItem);
		final Menu exporterListMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		exportDataToolItem.setData(exporterListMenu);
		List<IExporter> exporters = ExporterManager.get2DExporters();
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
						((Hist2DPanel) getPlot()).doEditMaskProperties();
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final ToolItem colorScaleToolItem = new ToolItem (toolBar, SWT.DROP_DOWN);
		colorScaleToolItem.setToolTipText("Change color scale");
		colorScaleToolItem.setImage(getImage("icons/color_scale_16x16.png"));
		controlItems.add(colorScaleToolItem);
		final Menu colorScaleMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		colorScaleToolItem.setData(colorScaleMenu);
		XYItemRenderer renderer = getPlot().getChart().getXYPlot().getRenderer();
		ColorScale currentScale = null;
		if (renderer instanceof XYBlockRenderer) {
			PaintScale paintScale = ((XYBlockRenderer) renderer).getPaintScale();
			if (paintScale instanceof ColorPaintScale) {
				currentScale = ((ColorPaintScale) paintScale).getColorScale();
			}
		}
		for (final ColorScale colorScale : ColorScale.values()) {
			final MenuItem scaleItem = new MenuItem(colorScaleMenu, SWT.RADIO);
			scaleItem.setText(colorScale.name());
			if (currentScale == colorScale) {
				scaleItem.setSelection(true);
			} else {
				scaleItem.setSelection(false);
			}
			scaleItem.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					for (Object object : getPlot().getChart().getSubtitles()) {
						if (object instanceof PaintScaleLegend) {
							PaintScale scale = ((PaintScaleLegend) object).getScale();
							if (scale instanceof ColorPaintScale) {
								((ColorPaintScale) scale).setColorScale(
										colorScale);
								XYItemRenderer renderer = getPlot().getChart().getXYPlot().getRenderer();
								if (renderer instanceof XYBlockRenderer) {
									((XYBlockRenderer) renderer).setPaintScale(scale);
								}
							}
						}
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		colorScaleToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				XYItemRenderer renderer = getPlot().getChart().getXYPlot().getRenderer();
				ColorScale currentScale = null;
				if (renderer instanceof XYBlockRenderer) {
					PaintScale paintScale = ((XYBlockRenderer) renderer).getPaintScale();
					if (paintScale instanceof ColorPaintScale) {
						currentScale = ((ColorPaintScale) paintScale).getColorScale();
					}
				}
				for (final MenuItem item : colorScaleMenu.getItems()) {
					if (currentScale.name().equals(item.getText())) {
						item.setSelection(true);
					} else {
						item.setSelection(false);
					}
				}
				Rectangle rect = colorScaleToolItem.getBounds ();
				Point pt = new Point (rect.x, rect.y + rect.height);
				pt = colorScaleToolItem.getParent().toDisplay (pt);
				colorScaleMenu.setLocation (pt.x, pt.y);
				colorScaleMenu.setVisible (true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		ToolItem resetColorScaleToolItem = new ToolItem (toolBar, SWT.PUSH);
		resetColorScaleToolItem.setToolTipText("Reset color scale range");
		resetColorScaleToolItem.setImage(getImage("icons/reset_color_scale_16x16.png"));
		controlItems.add(resetColorScaleToolItem);
		resetColorScaleToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				PaintScaleLegend legend = ((IHist2D) getPlot()).getPaintScaleLegend();
				if (legend != null && legend.getScale() instanceof ColorPaintScale) {
					((ColorPaintScale) legend.getScale()).resetBoundPercentage();
					getPlot().updatePlot();
				}
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
