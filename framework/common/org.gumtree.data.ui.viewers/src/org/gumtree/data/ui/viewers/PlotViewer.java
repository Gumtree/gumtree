/**
 * 
 */
package org.gumtree.data.ui.viewers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.UIManager;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.gumtree.data.ui.viewers.internal.InternalImage;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.db.RemoteTextDbService;
import org.gumtree.vis.awt.PlotFactory;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IHist2D;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.ISurf3D;
import org.gumtree.vis.interfaces.IXYZDataset;
import org.gumtree.vis.swt.IPlotControlWidgetProvider;
import org.gumtree.vis.swt.PlotComposite;
import org.gumtree.vis.swt.PlotUtils;
import org.jfree.chart.plot.PlotOrientation;

/**
 * @author nxi
 *
 */
public class PlotViewer extends Composite {

	private static PlotViewer instance;
	private static Display newDisplay;
	private PlotComposite plotComposite;
	private Menu menu;
	private CoolBar coolbar;
	private CoolItem coolItem;
	private ToolBar controlToolBar;
//	private ToolBar settingToolBar;
	private ToolItem resetPlotToolItem;
	private ToolItem copyToolItem;
	private ToolItem savePictureToolItem;
	private ToolItem printToolItem;
	private ToolItem sendLogToolItem;
	private ToolItem helpToolItem;
	private ToolItem settingsToolItem;
	private ToolItem logarithmToolItem;
	private ToolItem textInputToolItem;
	private ToolItem orientationToolItem;
	private ToolItem switch3DToolItem;
	private List<ToolItem> plotControlItems;
	private IPlotControlWidgetProvider provider;
//	private IPlot plot;
	/**
	 * @param parent
	 * @param style
	 */
	public PlotViewer(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).applyTo(this);
		createToolboxComposite(this);
		createPlotComposite(this);
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (!isDisposed()) {
					if (controlToolBar != null) {
						for (ToolItem item : controlToolBar.getItems()) {
							item.dispose();
						}
						controlToolBar.dispose();
						controlToolBar = null;
					}
				}
				if (coolbar != null) {
					coolbar.dispose();
					coolbar = null;
				}
				if (provider != null) {
					provider.setPlot(null);
					provider = null;
				}
				if (plotControlItems != null) {
					for (ToolItem item : plotControlItems) {
						item.dispose();
					}
					plotControlItems = null;
				}
			}
		});
	}

	private void createPlotComposite(Composite parent) {
		plotComposite = new PlotComposite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(plotComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plotComposite);
	}

	private void createToolboxComposite(Composite parent) {
		coolbar = new CoolBar(parent, SWT.BORDER | SWT.FLAT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(coolbar);
		
		coolItem = new CoolItem(coolbar, SWT.DROP_DOWN);
		controlToolBar = new ToolBar(coolbar, SWT.FLAT);
		coolItem.setControl(controlToolBar);
		
		resetPlotToolItem = new ToolItem(controlToolBar, SWT.PUSH);
		resetPlotToolItem.setToolTipText("Reset from zooming");
		resetPlotToolItem.setImage(InternalImage.REFRESH.getImage());

		copyToolItem = new ToolItem (controlToolBar, SWT.PUSH);
		copyToolItem.setToolTipText("Copy image to clipboard");
		copyToolItem.setImage(InternalImage.COPY.getImage());

		savePictureToolItem = new ToolItem (controlToolBar, SWT.PUSH);
		savePictureToolItem.setToolTipText("Save image");
		savePictureToolItem.setImage(InternalImage.SAVE.getImage());

		printToolItem = new ToolItem (controlToolBar, SWT.PUSH);
		printToolItem.setToolTipText("Print image");
		printToolItem.setImage(InternalImage.PRINT.getImage());

		sendLogToolItem = new ToolItem (controlToolBar, SWT.PUSH);
		sendLogToolItem.setToolTipText("Send to notebook");
		sendLogToolItem.setImage(InternalImage.SENDLOG.getImage());

		helpToolItem = new ToolItem (controlToolBar, SWT.PUSH);
		helpToolItem.setToolTipText("Show help");
		helpToolItem.setImage(InternalImage.HELP.getImage());

		textInputToolItem = new ToolItem (controlToolBar, SWT.CHECK);
		textInputToolItem.setImage(InternalImage.TEXT_INPUT.getImage());
		if (getPlot() != null) {
			textInputToolItem.setSelection(getPlot().isTextInputEnabled());
		}
		if (textInputToolItem.getSelection()) {
			textInputToolItem.setToolTipText("Click to disable text input");
		} else {
			textInputToolItem.setToolTipText("Click to enable text input");
		}

	    ToolItem sepItem = new ToolItem(controlToolBar, SWT.SEPARATOR);
	    sepItem.setWidth(6);
	    
		settingsToolItem = new ToolItem (controlToolBar, SWT.PUSH);
		settingsToolItem.setToolTipText("Settings");
		settingsToolItem.setImage(InternalImage.SETTING.getImage());

		logarithmToolItem = new ToolItem (controlToolBar, SWT.CHECK);
		logarithmToolItem.setToolTipText("Toggle logarithm scale");
		logarithmToolItem.setImage(InternalImage.LOG.getImage());
		if (getPlot() != null) {
			logarithmToolItem.setSelection(getPlot().isLogarithmEnabled());
		}

		orientationToolItem = new ToolItem (controlToolBar, SWT.PUSH);
		orientationToolItem.setToolTipText("Change/Reset orientation");
		orientationToolItem.setImage(InternalImage.ROTATE.getImage());

	    sepItem = new ToolItem(controlToolBar, SWT.SEPARATOR);
	    sepItem.setWidth(6);
	    
	    plotControlItems = addPlotControlWidgets();
//		CoolItem coolItem2 = new CoolItem(coolbar, SWT.DROP_DOWN);
//		settingToolBar = new ToolBar(coolbar, SWT.FLAT | SWT.WRAP);
//		coolItem2.setControl(settingToolBar);

//		Point toolBar2Size = settingToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//		Point coolBar2Size = coolItem2.computeSize(toolBar2Size.x,
//	        toolBar2Size.y);
//	    coolItem2.setSize(coolBar2Size);
//	    coolItem2.addSelectionListener(new CoolBarListener());
//	    coolbar.update();
//	    coolbar.layout();
	    

	    resizeCoolBar();
	    initListeners();
	}

	private void resizeCoolBar() {
	    Point toolBar1Size = controlToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    Point coolBar1Size = coolItem.computeSize(toolBar1Size.x,
	        toolBar1Size.y);
	    coolItem.setSize(coolBar1Size);
	    coolItem.addSelectionListener(new CoolBarListener());
	}

	private List<ToolItem> addPlotControlWidgets() {
	    if (getPlot() instanceof IHist2D || getPlot() instanceof ISurf3D) {
	    	if (switch3DToolItem == null) {
	    		switch3DToolItem = new ToolItem(controlToolBar, SWT.CHECK);
	    		switch3DToolItem.setToolTipText("Toggle 3D view");
	    		switch3DToolItem.setImage(InternalImage.THREE_D.getImage());
	    		switch3DToolItem.setSelection(getPlot() instanceof ISurf3D);
	    		final SelectionListener switchToolItemListener = new SelectionListener() {
	    			private IHist2D plot2D;
	    			@Override
	    			public void widgetSelected(SelectionEvent e) {
	    				if (switch3DToolItem.getSelection()) {
	    						ISurf3D plot3D = PlotFactory.createPlot3DPanel((IXYZDataset) getDataset());
		    					if (getPlot() instanceof IHist2D) {
		    						plot2D = (IHist2D) getPlot();
		    						plot3D.setColorScale(plot2D.getColorScale());
		    						plot3D.setLogarithmEnabled(plot2D.isLogarithmEnabled());
		    						plot3D.updatePlot();
		    					}
	    						plotComposite.setPlot(plot3D);
	    				} else {
	    					if (plot2D == null) {
	    						plot2D = PlotFactory.createHist2DPanel((IXYZDataset) getDataset());
	    					}
    						if (getPlot() instanceof ISurf3D) {
    							plot2D.setColorScale(((ISurf3D) getPlot()).getColorScale());
    							plot2D.setLogarithmEnabled(getPlot().isLogarithmEnabled());
    						}
	    					plotComposite.setPlot(plot2D);
	    				}
	    				getAvailableDisplay().asyncExec(new Runnable() {

	    					@Override
	    					public void run() {
	    						reloadControlWidgets();
	    						resizeCoolBar();
	    						coolbar.layout(true, true);
	    					}
	    				});
	    			}

	    			@Override
	    			public void widgetDefaultSelected(SelectionEvent e) {
	    			}
	    		};
	    		switch3DToolItem.addSelectionListener(switchToolItemListener);
	    	}
//		    toolItems.add(switch3DToolItem);
	    }
		provider = PlotUtils.getPlotControlWidgetProvider(getPlot());
		if (provider != null) {
			provider.setPlot(getPlot());
			List<ToolItem> toolItems = provider.createControlToolItems(controlToolBar);
		    return toolItems;
		}
		return null;
	}

	private void initListeners() {
		resetPlotToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getPlot() == null) {
					return;
				}
				getPlot().restoreAutoBounds();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		logarithmToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getPlot() == null) {
					return;
				}
				if (logarithmToolItem.getSelection()) {
					getPlot().setLogarithmEnabled(true);
				} else {
					getPlot().setLogarithmEnabled(false);
				}
				getPlot().updatePlot();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		textInputToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (textInputToolItem.getSelection()) {
					textInputToolItem.setToolTipText("Click to disable text input");
				} else {
					textInputToolItem.setToolTipText("Click to enable text input");
				}
				if (getPlot() == null) {
					return;
				}
				getPlot().setTextInputEnabled(textInputToolItem.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		copyToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getPlot() == null) {
					return;
				}
				getPlot().doCopy();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		savePictureToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							if (getPlot() == null) {
								return;
							}
							getPlot().doSaveAs();
						} catch (IOException e1) {
							if (plotComposite != null) {
								plotComposite.handleException(e1);
							}
						}
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		printToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						if (getPlot() == null) {
							return;
						}
						getPlot().createChartPrintJob();
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		sendLogToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						if (getPlot() == null) {
							return;
						}
						Image image = getPlot().getImage();
						if (image instanceof BufferedImage) {
							try {
								RemoteTextDbService.getInstance().appendImageEntry("ScriptingPlot", (BufferedImage) image, null);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (RecordsFileException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		helpToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						if (getPlot() == null) {
							return;
						}
						getPlot().doHelp();
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		settingsToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						if (getPlot() == null) {
							return;
						}
						getPlot().doEditChartProperties();
					}
				});
				newThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		orientationToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getPlot() == null) {
					return;
				}
				if (getPlot() instanceof ISurf3D) {
					((ISurf3D) getPlot()).resetOrientation();
				} else {
					if (getPlot().getXYPlot() == null) {
						return;
					}
					PlotOrientation orientation = getPlot().getXYPlot().getOrientation();
					if (orientation == PlotOrientation.HORIZONTAL) {
						orientation = PlotOrientation.VERTICAL;
					} else {
						orientation = PlotOrientation.HORIZONTAL;
					}
					getPlot().getXYPlot().setOrientation(orientation);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	class CoolBarListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (event.detail == SWT.ARROW) {
				ToolBar toolBar = (ToolBar) ((CoolItem) event.widget).getControl();
				final ToolItem[] buttons = toolBar.getItems();

				if (menu != null) {
					menu.dispose();
				}
				menu = new Menu(coolbar);
				boolean buttonStart = false;
				for (int loopIndex = 0; loopIndex < buttons.length; loopIndex++) {
					Rectangle buttonBounds = buttons[loopIndex].getBounds();
					Rectangle barBounds = coolbar.getBounds();
					if (buttonBounds.x < barBounds.width - 36 - buttonBounds.width) {
						continue;
					}
					if ((buttons[loopIndex].getStyle() & SWT.SEPARATOR) != 0) {
						if (buttonStart) {
							new MenuItem(menu, SWT.SEPARATOR);
						}
					} else if ((buttons[loopIndex].getStyle() & SWT.DROP_DOWN) != 0) {
						MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
						menuItem.setImage(buttons[loopIndex].getImage());
						menuItem.setText(buttons[loopIndex].getToolTipText());
						Object data = buttons[loopIndex].getData();
						if (data instanceof Menu) {
							Menu oldMenu = (Menu) data;
							Menu newMenu = new Menu(menu);
							for (final MenuItem item : oldMenu.getItems()) {
								MenuItem newItem = new MenuItem(newMenu, item.getStyle());
								newItem.setText(item.getText());
								newItem.setSelection(item.getSelection());
								newItem.addSelectionListener(new SelectionListener() {
									
									@Override
									public void widgetSelected(SelectionEvent e) {
										item.setSelection(!item.getSelection());
										item.notifyListeners(SWT.Selection, null);
									}
									
									@Override
									public void widgetDefaultSelected(SelectionEvent e) {
									}
								});
							}
							menuItem.setMenu(newMenu);
						}
						buttonStart = true;
					} else if ((buttons[loopIndex].getStyle() & SWT.CHECK) != 0) {
						MenuItem checkItem = new MenuItem(menu, SWT.CHECK);
						checkItem.setImage(buttons[loopIndex].getImage());
						checkItem.setText(buttons[loopIndex].getToolTipText());
						checkItem.setSelection(buttons[loopIndex].getSelection());
						final int index = loopIndex;
						checkItem.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								buttons[index].setSelection(!buttons[index].getSelection());
								buttons[index].notifyListeners(SWT.Selection, new Event());
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						});
						buttonStart = true;
					}
					else {
						MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
						menuItem.setImage(buttons[loopIndex].getImage());
						menuItem.setText(buttons[loopIndex].getToolTipText());
						final int index = loopIndex;
						menuItem.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								buttons[index].notifyListeners(SWT.Selection, new Event());
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						});
						buttonStart = true;
					}
				}

				Point menuPoint = coolbar.toDisplay(new Point(event.x,
						event.y));
				menu.setLocation(menuPoint.x, menuPoint.y);
				menu.setVisible(true);
			}
		}
	}

	public IPlot getPlot() {
		if (plotComposite != null) {
			return plotComposite.getPlot();
		} 
		return null;
	}

	
	public static PlotViewer openInNewShell(final IDataset dataset) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(
					        UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Display display = getNewDisplay();
				Shell shell = new Shell(display);
				shell.setText("Plot Viewer");
				GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(shell);
				shell.setSize(800, 640);

				// SWT version
				instance = new PlotViewer(shell, SWT.NONE);
				GridDataFactory.fillDefaults().grab(true, true).indent(0, 0).applyTo(instance);
				instance.setDataset(dataset);
//				instance.layout(true, true);
				shell.open();
				display.sleep();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
				display.dispose();

			}
		});
		thread.start();
		int sleepTime = 0;
		while (instance == null && sleepTime < 5000) {
			try {
				Thread.sleep(200);
				sleepTime += 200;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		PlotViewer ins = instance;
		instance = null;
		return ins;
	}
	
	public static Display getNewDisplay() {
		if (newDisplay == null || newDisplay.isDisposed()) {
			newDisplay = new Display();
		}
		return newDisplay;
	}
	
	public Display getAvailableDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			return Display.getDefault();
		}
		return display;
	}
	
	public void setDataset(final IDataset dataset) {
		IPlot oldPlot = getPlot();
		if (plotComposite == null) {
			createPlotComposite(getParent());
		}
		plotComposite.setDataset(dataset);
		IPlot newPlot = getPlot();
		if (oldPlot != newPlot) {
			getAvailableDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					reloadControlWidgets();
					resizeCoolBar();
					coolbar.layout(true, true);
				}
			});
		}
	}
	
	private void reloadControlWidgets() {
		if (plotControlItems != null) {
			for (ToolItem item : plotControlItems) {
				item.dispose();
			}
		}
		if (provider != null) {
			provider.setPlot(null);
		}
		if (!(getPlot() instanceof IHist2D || getPlot() instanceof ISurf3D)) {
			if (switch3DToolItem != null) {
				switch3DToolItem.dispose();
				switch3DToolItem = null;
			}
		}
		plotControlItems = addPlotControlWidgets();
	}

	public IDataset getDataset() {
		if (plotComposite == null) {
			return null;
		}
		return plotComposite.getDataset();
	}

	public PlotComposite getPlotComposite() {
		return plotComposite;
	}


	public CoolBar getCoolbar() {
		return coolbar;
	}

	public ToolBar getControlToolBar() {
		return controlToolBar;
	}


	public ToolItem getResetPlotToolItem() {
		return resetPlotToolItem;
	}

	public ToolItem getCopyToolItem() {
		return copyToolItem;
	}

	public ToolItem getSavePictureToolItem() {
		return savePictureToolItem;
	}

	public ToolItem getPrintToolItem() {
		return printToolItem;
	}


	public ToolItem getSettingsToolItem() {
		return settingsToolItem;
	}


	public ToolItem getLogarithmToolItem() {
		return logarithmToolItem;
	}


	public CoolItem getCoolItem() {
		return coolItem;
	}

	@Override
	public void dispose() {
		if (plotComposite != null) {
			plotComposite.dispose();
			plotComposite = null;
		}
		if (!isDisposed()) {
			if (controlToolBar != null && !controlToolBar.isDisposed()) {
				for (ToolItem item : controlToolBar.getItems()) {
					item.dispose();
				}
				controlToolBar.dispose();
			}
			if (plotControlItems != null) {
				for (ToolItem item : plotControlItems) {
					if (!item.isDisposed()) {
						item.dispose();
					}
				}
				plotControlItems = null;
			}
			controlToolBar = null;
			if (coolbar != null && !coolbar.isDisposed()) {
				coolbar.dispose();
			}
			coolbar = null;
			if (provider != null) {
				provider.setPlot(null);
				provider = null;
			}
		}
		if (this == instance) {
			instance = null;
		}
		super.dispose();
	}
}
