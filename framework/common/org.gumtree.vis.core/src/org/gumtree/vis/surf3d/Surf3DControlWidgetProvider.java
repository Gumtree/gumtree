/**
 * 
 */
package org.gumtree.vis.surf3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.freehep.j3d.plot.Plot3D.ColorTheme;
import org.freehep.j3d.plot.RenderStyle;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.ISurf3D;
import org.gumtree.vis.io.ExporterManager;
import org.gumtree.vis.swt.IPlotControlWidgetProvider;

/**
 * @author nxi
 *
 */
public class Surf3DControlWidgetProvider implements IPlotControlWidgetProvider {

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
		exportDataToolItem.setImage(StaticValues.getImage("icons/table_export_16x16.png"));
		controlItems.add(exportDataToolItem);
		final Menu exporterListMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		exportDataToolItem.setData(exporterListMenu);
		List<IExporter> exporters = ExporterManager.get3DExporters();
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
		final SelectionListener exportListener = new SelectionListener() {
			
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
		};
		exportDataToolItem.addSelectionListener(exportListener);

		final ToolItem colorScaleToolItem = new ToolItem (toolBar, SWT.DROP_DOWN);
		colorScaleToolItem.setToolTipText("Change color scale");
		colorScaleToolItem.setImage(StaticValues.getImage("icons/color_scale_16x16.png"));
		controlItems.add(colorScaleToolItem);
		final Menu colorScaleMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		colorScaleToolItem.setData(colorScaleMenu);
		final ColorScale currentScale = ((ISurf3D) getPlot()).getColorScale();
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
					if (colorScale != ((ISurf3D) getPlot()).getColorScale()) {
						((ISurf3D) getPlot()).setColorScale(colorScale);
						getPlot().updatePlot();
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		final SelectionListener colorScaleListener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ColorScale currentScale = ((ISurf3D) getPlot()).getColorScale();
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
		};
		colorScaleToolItem.addSelectionListener(colorScaleListener);

		
		final ToolItem ColorThemeToolItem = new ToolItem (toolBar, SWT.DROP_DOWN);
		ColorThemeToolItem.setToolTipText("Change plot background");
		ColorThemeToolItem.setImage(StaticValues.getImage("icons/reset_color_scale_16x16.png"));
		controlItems.add(ColorThemeToolItem);
		final Menu colorThemeMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		ColorThemeToolItem.setData(colorThemeMenu);
		final ColorTheme currentTheme = ((ISurf3D) getPlot()).getColorTheme();
		for (final ColorTheme theme : ColorTheme.values()) {
			final MenuItem themeItem = new MenuItem(colorThemeMenu, SWT.RADIO);
			themeItem.setText(theme.name());
			if (currentTheme == theme) {
				themeItem.setSelection(true);
			} else {
				themeItem.setSelection(false);
			}
			themeItem.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (theme != ((ISurf3D) getPlot()).getColorTheme()) {
						((ISurf3D) getPlot()).setColorTheme(theme);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		final SelectionListener colorThemeListener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ColorTheme currentTheme = ((ISurf3D) getPlot()).getColorTheme();
				for (final MenuItem item : colorThemeMenu.getItems()) {
					if (currentTheme.name().equals(item.getText())) {
						item.setSelection(true);
					} else {
						item.setSelection(false);
					}
				}
				Rectangle rect = ColorThemeToolItem.getBounds ();
				Point pt = new Point (rect.x, rect.y + rect.height);
				pt = ColorThemeToolItem.getParent().toDisplay (pt);
				colorThemeMenu.setLocation (pt.x, pt.y);
				colorThemeMenu.setVisible (true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		ColorThemeToolItem.addSelectionListener(colorThemeListener);
		
		
		final ToolItem renderStyleToolItem = new ToolItem (toolBar, SWT.DROP_DOWN);
		renderStyleToolItem.setToolTipText("Change render style");
		renderStyleToolItem.setImage(StaticValues.getImage("icons/blue_lego_16x16.png"));
		controlItems.add(renderStyleToolItem);
		final Menu renderStyleMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		renderStyleToolItem.setData(renderStyleMenu);
		final RenderStyle currentStyle = ((ISurf3D) getPlot()).getRenderStyle();
		for (final RenderStyle style : RenderStyle.values()) {
			final MenuItem styleItem = new MenuItem(renderStyleMenu, SWT.RADIO);
			styleItem.setText(style.name());
			if (currentStyle == style) {
				styleItem.setSelection(true);
			} else {
				styleItem.setSelection(false);
			}
			styleItem.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (style != ((ISurf3D) getPlot()).getRenderStyle()) {
						((ISurf3D) getPlot()).setRenderStyle(style);
						getPlot().updatePlot();
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		final SelectionListener renderStyleListener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				final RenderStyle currentStyle = ((ISurf3D) getPlot()).getRenderStyle();
				for (final MenuItem item : renderStyleMenu.getItems()) {
					if (currentStyle.name().equals(item.getText())) {
						item.setSelection(true);
					} else {
						item.setSelection(false);
					}
				}
				Rectangle rect = renderStyleToolItem.getBounds ();
				Point pt = new Point (rect.x, rect.y + rect.height);
				pt = renderStyleToolItem.getParent().toDisplay (pt);
				renderStyleMenu.setLocation (pt.x, pt.y);
				renderStyleMenu.setVisible (true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		renderStyleToolItem.addSelectionListener(renderStyleListener);

		
		ToolItem outsideBoxToolItem = new ToolItem (toolBar, SWT.PUSH);
		outsideBoxToolItem.setToolTipText("Toggle coordinate on/off");
		outsideBoxToolItem.setImage(StaticValues.getImage("icons/box_opened_16x16.png"));
		controlItems.add(outsideBoxToolItem);
		outsideBoxToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						((ISurf3D) getPlot()).toggleOutsideBoxEnabled();
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		
		ToolItem resetCentreToolItem = new ToolItem (toolBar, SWT.PUSH);
		resetCentreToolItem.setToolTipText("Reset centre");
		resetCentreToolItem.setImage(StaticValues.getImage("icons/move_blue_16x16.png"));
		controlItems.add(resetCentreToolItem);
		resetCentreToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						((ISurf3D) getPlot()).resetCenter();
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
//		ToolItem resetOrientationItem = new ToolItem (toolBar, SWT.PUSH);
//		resetOrientationItem.setToolTipText("Reset orientation");
//		resetOrientationItem.setImage(getImage("icons/compassR_16x16.png"));
//		controlItems.add(resetOrientationItem);
//		resetOrientationItem.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				Thread newThread = new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						((ISurf3D) getPlot()).resetOrientation();
//					}
//				});
//				newThread.start();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
		
		ToolItem resetZoomToolItem = new ToolItem (toolBar, SWT.PUSH);
		resetZoomToolItem.setToolTipText("Reset camera distance");
		resetZoomToolItem.setImage(StaticValues.getImage("icons/reset_zoom_16x16.png"));
		controlItems.add(resetZoomToolItem);
		resetZoomToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						((ISurf3D) getPlot()).resetZoomScale();
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		return controlItems;
	}

//	private Image getImage(String path) {
//		return new Image(Display.getDefault(), getClass().getClassLoader().getResourceAsStream(path));
//	}

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
