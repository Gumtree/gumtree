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
package org.gumtree.vis.swt;

import java.awt.Frame;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.gumtree.vis.awt.PlotFactory;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.IPreview2DDataset;
import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYZDataset;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;

/**
 * @author nxi
 *
 */
public class PlotComposite extends Composite{

	/**
	 * @param parent
	 * @param style
	 */
	private IPlot plot; 
	private Frame frame;
	private MouseWheelListener mouseWheelListener;
	private KeyListener keyListener;
	private ChartMouseListener chartMouseListener;
	
	public PlotComposite(Composite parent, int style) {
		super(parent, SWT.EMBEDDED | style);
//		GridLayoutFactory.fillDefaults().applyTo(this);
		FillLayout layout = new FillLayout(SWT.FILL);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.spacing = 0;
		setLayout(layout);
		frame = SWT_AWT.new_Frame(this);
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (!isDisposed()) {
					if (mouseWheelListener != null) {
						removeMouseWheelListener(mouseWheelListener);
						mouseWheelListener = null;
					}
					if (keyListener != null) {
						removeKeyListener(keyListener);
						keyListener = null;
					}
					if (plot != null) {
						plot.cleanUp();
						if (chartMouseListener != null) {
							plot.removeChartMouseListener(chartMouseListener);
							chartMouseListener = null;
						}
						frame.remove((JPanel) plot);
						frame.dispose();
						plot = null;
					}
				}
			}
		});
	}

	protected void embedPlot(IPlot plot) {
		if (plot instanceof JPanel) {
			if (frame != null)
				frame.add((JPanel) plot);
		} else {
			throw new IllegalArgumentException("must be a chart plot panel");
		}
	}
	
	public void setPlot(final IPlot plot) {
		final IPlot oldPlot = this.plot;
		if (oldPlot != null) {
			oldPlot.cleanUp();
			frame.remove((JPanel) oldPlot);
		}
		this.plot = plot;
		final Composite composite = this;
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				embedPlot(plot);
				removeListeners(oldPlot);
				addListeners();
				composite.pack();
				composite.getParent().layout(true, true);
			}
		});
	}
	
	private void removeListeners(IPlot plot) {
		if (plot != null) {
			removeMouseWheelListener(mouseWheelListener);
			removeKeyListener(keyListener);
			plot.removeChartMouseListener(chartMouseListener);
		}
	}

	private void addListeners() {
		if (plot != null) {
			mouseWheelListener = new MouseWheelListener() {

				@Override
				public void mouseScrolled(MouseEvent event) {
					JPanel panel = null;
					if (plot instanceof JPanel) {
						panel = (JPanel) plot;
					}
					MouseWheelEvent awtEvent = org.gumtree.vis.listener.SWT_AWT.toMouseWheelEvent(
							event, panel);
					plot.processMouseWheelEvent(awtEvent);
				}
			};
			addMouseWheelListener(mouseWheelListener);


			keyListener = new KeyListener() {

				boolean keyPressed = false;

				@Override
				public void keyReleased(KeyEvent event) {
					switch (event.keyCode) {
					case SWT.DEL:
						plot.removeSelectedMask();
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
						plot.moveSelectedMask(event.keyCode);
						break;
					case SWT.ARROW_LEFT:
						plot.moveSelectedMask(event.keyCode);
						break;
					case SWT.ARROW_RIGHT:
						plot.moveSelectedMask(event.keyCode);
						break;
					case SWT.ARROW_DOWN:
						plot.moveSelectedMask(event.keyCode);
						break;
					default:
						break;
					}
					switch (event.stateMask) {
					case SWT.CTRL:
						if (event.keyCode == 'c' || event.keyCode == 'C') {
							if (!keyPressed) {
								plot.doCopy();
							}
						} else if (event.keyCode == 'z' || event.keyCode == 'Z' || 
								event.keyCode == 'r' || event.keyCode == 'R') {
							if (!keyPressed) {
								plot.restoreAutoBounds();
							}
						} else if (event.keyCode == 'p' || event.keyCode == 'P') {
							System.out.println("p pressed");
							if (!keyPressed) {
								Thread newThread = new Thread(new Runnable() {

									@Override
									public void run() {
										plot.createChartPrintJob();
									}
								});
								newThread.start();
							}
						} else if (event.keyCode == 'e' || event.keyCode == 'E') {
							System.out.println("s pressed");
							if (!keyPressed) {
								Thread newThread = new Thread(new Runnable() {

									@Override
									public void run() {
										try {
											plot.doSaveAs();
										} catch (IOException e) {
											handleException(e);
										}
									}
								});
								newThread.start();
							}
						} 
						keyPressed = true;
						break;
					default:
						break;
					}
				}
			};
			addKeyListener(keyListener);

			final Composite composite = this;

			chartMouseListener = new ChartMouseListener() {

				@Override
				public void chartMouseMoved(ChartMouseEvent event) {

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
			};
			plot.addChartMouseListener(chartMouseListener);
		}
	}

	public JFreeChart getChart() {
		if (plot != null) {
			return plot.getChart();
		} else {
			return null;
		}
	}

//	public JChartPanel getPanel() {
//		return plot;
//	}

//	public void setHorizontalAxisTrace(boolean isEnabled) {
//		plot.setHorizontalAxisTrace(isEnabled);
//	}
//	
//	public void setVerticalAxisTrace(boolean isEnabled) {
//		plot.setVerticalAxisTrace(isEnabled);
//	}
//	
//	public void setComponentOrientation(ComponentOrientation orientation) {
//		plot.setComponentOrientation(orientation);
//	}
//	
//	public void setHorizontalZoomable(boolean isZoomable) {
//		plot.setDomainZoomable(isZoomable);
//	}
//	
//	public void setVerticalZoomable(boolean isZoomable) {
//		plot.setRangeZoomable(isZoomable);
//	}
	
//	@Override
//	public void setEnabled(boolean isEnabled) {
////		plot.setEnabled(isEnabled);
//		super.setEnabled(isEnabled);
//	}
	
	@Override
	public void setVisible(boolean visible) {
		if (plot != null) {
			plot.setVisible(visible);
		}
		super.setVisible(visible);
	}
	
	// [GUMTREE-611] Disabled due to compilation error on mac cocoa
//	@Override
//	public void setCursor(Cursor cursor) {
//		plot.setCursor(org.gumtree.vis.listener.SWT_AWT.toAwtCursor(cursor));
//	}
	
	public void setDataset(IDataset dataset) {
		try{
			boolean createNewPlot = false;
			if (plot != null) {
				IDataset oldDataset = plot.getDataset();
				if (oldDataset.getClass() != dataset.getClass()) {
					createNewPlot = true;
				}
			} else {
				createNewPlot = true;
			}
			if (!createNewPlot) {
				plot.setDataset(dataset);
			} else {
				if (plot != null) {
					JPanel panel = (JPanel) plot;
					frame.remove(panel);
				}
				IPlot newPlot = null;
				if (dataset instanceof IXYErrorDataset) {
					newPlot = PlotFactory.createPlot1DPanel((IXYErrorDataset) dataset);
				} else if (dataset instanceof IXYZDataset) {
					newPlot = PlotFactory.createHist2DPanel(((IXYZDataset) dataset));
				} else if (dataset instanceof ITimeSeriesSet) {
					newPlot = PlotFactory.createTimePlotPanel((ITimeSeriesSet) dataset);
				} else if (dataset instanceof IPreview2DDataset) {
					newPlot = PlotFactory.createPreview2DPanel((IPreview2DDataset) dataset);
				}
				setPlot(newPlot);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void set3DDataset(IDataset dataset) {
		try{
			boolean createNewPlot = false;
			if (plot != null) {
				IDataset oldDataset = plot.getDataset();
				if (oldDataset.getClass() != dataset.getClass()) {
					createNewPlot = true;
				}
			} else {
				createNewPlot = true;
			}
			if (!createNewPlot) {
				plot.setDataset(dataset);
			} else {
				if (plot != null) {
					JPanel panel = (JPanel) plot;
					frame.remove(panel);
				}
				IPlot newPlot = null;
				if (dataset instanceof IXYZDataset) {
					newPlot = PlotFactory.createPlot3DPanel((IXYZDataset) dataset);
				} 
				setPlot(newPlot);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public IDataset getDataset() {
		if (plot == null) {
			return null;
		}
		return plot.getDataset();
	}
	

	public IPlot getPlot() {
		return plot;
	}

	@Override
	public void redraw() {
		if (plot != null) {
			plot.repaint();
		}
		super.redraw();
	}

//	public void setBackgroundColor(Color color) {
//		plot.setBackgroundColor(org.gumtree.vis.listener.SWT_AWT.toAwtColor(color));
//	}

	public void handleException(final Exception e) {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (getShell() != null) {
					MessageBox messageBox = new MessageBox(getShell(), 
							SWT.ICON_WARNING | SWT.OK);
					messageBox.setText("Failed to Save");
			        messageBox.setMessage("failed : " + e.getMessage());
			        messageBox.open();
//					MessageDialog.openError(getShell(), "Failed to Save", "failed to save " +
//							"the image: " + e.getMessage());
					
				}
			}
		});
	}
	
	@Override
	public void dispose() {
		if (!isDisposed()) {
			if (mouseWheelListener != null) {
				removeMouseWheelListener(mouseWheelListener);
				mouseWheelListener = null;
			}
			if (keyListener != null) {
				removeKeyListener(keyListener);
				keyListener = null;
			}
			if (plot != null) {
				plot.cleanUp();
				if (chartMouseListener != null) {
					plot.removeChartMouseListener(chartMouseListener);
					chartMouseListener = null;
				}
				frame.remove((JPanel) plot);
				frame.dispose();
				plot = null;
			}
		}
		frame = null;
		super.dispose();
	}
	
	public void clear(){
		if (this.plot != null) {
			if (plot instanceof JPanel) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						frame.remove((JPanel) plot);
						removeListeners(plot);
						plot = null;
						pack();
						getParent().layout(true, true);
						frame.repaint();
					}
				});
			} else {
				throw new IllegalArgumentException("must be a chart plot panel");
			}
		}
	}
	
	@Override
	public void update() {
		super.update();
		frame.repaint();
	}
//	public void setMouseWheelEnabled(boolean isEnabled) {
//		plot.setMouseWheelEnabled(isEnabled);
//	}
	
//	public void setZoomInFactor(double factor) {
//		plot.setZoomInFactor(factor);
//	}
//	
//	public void setZoomOutFactor(double factor) {
//		plot.setZoomOutFactor(factor);
//	}
	
//	public ValueAxis getHorizontalAxis() {
//		return plot.getHorizontalAxis();
//	}

//	public Title getTitle() {
//		return plot.getTitle();
//	}
//
//	public ValueAxis getVerticalAxis() {
//		return plot.getVerticalAxis();
//	}

//
//	public void setHorizontalAxisFlipped(boolean isFlipped) {
//		plot.setHorizontalAxisFlipped(isFlipped);
//	}
//
//	public void setVerticalAxisFlipped(boolean isFlipped) {
//		plot.setVerticalAxisFlipped(isFlipped);
//	}
//	
//	public void restoreAutoBounds(){
//		plot.restoreAutoBounds();
//	}
//	
//	public void restoreHorizontalBounds() {
//		plot.restoreHorizontalBounds();
//	}
//	
//	public void restoreVerticalBounds() {
//		plot.restoreVerticalBounds();
//	}
//	
//	public void zoomInBoth(double x, double y) {
//		plot.zoomInBoth(x, y);
//	}
//
//	public void zoomOutBoth(double x, double y) {
//		plot.zoomInBoth(x, y);
//	}
//
//	public void zoomInHorizontal(double x, double y) {
//		plot.zoomInHorizontal(x, y);
//	}
//
//	public void zoomInVertical(double x, double y) {
//		plot.zoomInVertical(x, y);
//	}
//	
//	public void zoomOutHorizontal(double x, double y) {
//		plot.zoomOutHorizontal(x, y);
//	}
//
//	public void zoomOutVertical(double x, double y) {
//		plot.zoomOutVertical(x, y);
//	}
}
