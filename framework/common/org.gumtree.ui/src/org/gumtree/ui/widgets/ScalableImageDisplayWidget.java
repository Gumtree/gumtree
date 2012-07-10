/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.ui.widgets;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class ScalableImageDisplayWidget extends ImageDisplayWidget {

	private enum Mode {
		INITIAL, NORMAL, ZOOM_SELECTION, ZOOM
	}
	
	private int width;
	
	private int height;
	
	// Scaling
	private float zoomx = 1.0f;
	
	private float zoomy = 1.0f;
	
	private float scalex = 0.0f;
	
	private float scaley = 0.0f;
	
	// Left upper corner plot coordinate that attached to the control origin (0, 0)
	private Point plotOrigin = new Point(0, 0);
	
	private Point zoomStart;
	
	private Mode mode = Mode.INITIAL;
	
	private RectangleFigure selectionMask;

	private FigureCanvas plotCanvas;
	
	public ScalableImageDisplayWidget(Composite parent, int style) {
		super(parent, style);
	}

	protected void widgetDispose() {
		selectionMask = null;
		plotCanvas = null;
		super.dispose();
	}
	
	protected Composite createImageArea() {
		setLayout(new FillLayout());
		Composite imageArea = getToolkit().createComposite(this);
		imageArea.setLayout(new FillLayout());
		return imageArea;
	}
	
	protected void paintImage(Composite parent, Image image, boolean reset) {
		if (reset) {
			mode = Mode.INITIAL;
		}
		
		width = image.getImageData().width;
		height = image.getImageData().height;
		
		plotCanvas = new FigureCanvas(parent, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		final ImageFigure imageFigure = new ImageFigure(image, PositionConstants.NORTH | PositionConstants.WEST) {
			protected void paintFigure(Graphics graphics) {
				scalex = getSize().width / (float) width;
				scaley = getSize().height / (float) height;
				graphics.setInterpolation(SWT.NONE);
				float totalZoomX = zoomx * scalex;
				float totalZoomY = zoomy * scaley;
				graphics.scale(totalZoomX, totalZoomY);
				graphics.translate(-1 * plotOrigin.x, -1 * (width - 1 - plotOrigin.y));
				super.paintFigure(graphics);
			}
		};
		imageFigure.setLayoutManager(new XYLayout());
		plotCanvas.setContents(imageFigure);
		
		plotCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (mode == Mode.ZOOM_SELECTION) {
					if (selectionMask != null) {
						try {
							imageFigure.remove(selectionMask);
						} catch (Exception ex) {
							// It may out of sync
						}
						selectionMask = null;
					}
					selectionMask = new RectangleFigure() {
						protected void fillShape(Graphics graphics) {
							int originalAlpha = graphics.getAlpha();
							graphics.setAlpha(128);
							super.fillShape(graphics);
							graphics.setAlpha(originalAlpha);
						}
					};
					selectionMask.setLineStyle(Graphics.LINE_DASHDOT);
					selectionMask.setBackgroundColor(ColorConstants.white);
					imageFigure.add(selectionMask, new Rectangle(zoomStart.x, zoomStart.y, e.x - zoomStart.x, e.y - zoomStart.y));
				}
			}			
		});
		
		plotCanvas.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				mode = Mode.ZOOM_SELECTION;
				zoomStart = new Point(e.x, e.y);
			}
			public void mouseUp(MouseEvent e) {
				if (mode == Mode.ZOOM_SELECTION) {
					if (selectionMask != null) {
						imageFigure.remove(selectionMask);
						selectionMask = null;

						// Assume only move to right bottom
						Point start = translateToPlot(zoomStart.x, zoomStart.y);
						Point end = translateToPlot(e.x, e.y);
						if (end.x >= width) {
							end.x = width;
						}
						if (end.y < 0) {
							end.y = 0;
						}
						zoomx = width / (float) (end.x - start.x);
						zoomy = height / (float) (start.y - end.y);
						plotOrigin = start;
						plotCanvas.redraw();
					}
					mode = Mode.NORMAL;
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				redrawPlot();
			}
		});
		
		if (mode.equals(Mode.INITIAL)) {
			redrawPlot();
		}
	}

	private void redrawPlot() {
		zoomx = 1.0f;
		zoomy = 1.0f;
		plotOrigin = new Point(0, width - 1);
		plotCanvas.redraw();
		mode = Mode.NORMAL;
	}
	
	private Point translateToPlot(int x, int y) {
		// TODO: zoomx != 0
		int xp = (int) Math.floor(x / (zoomx * scalex)) + plotOrigin.x;
		int yp = plotOrigin.y - (int) Math.floor(y / (zoomy * scaley));
		return new Point(xp, yp);
	}
	
}
