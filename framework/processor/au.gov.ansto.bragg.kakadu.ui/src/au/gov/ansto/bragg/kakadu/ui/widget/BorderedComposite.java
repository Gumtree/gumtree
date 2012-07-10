/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author Danil Klimontov (dak)
 */
public class BorderedComposite extends Composite {
	
	private Color borderColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
	private int borderSize = 1;

	public BorderedComposite(Composite parent, int style) {
		super(parent, style);
		initBorderedComposite();
	}

	private void initBorderedComposite() {
		addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
		});

	}

	/**
	 * Paint the Label's border.
	 */
	private void paintBorder(GC gc, Rectangle r) {
		Display disp= getDisplay();
		
		gc.setForeground(borderColor);
		gc.setLineWidth(borderSize);
		gc.drawRectangle(r.x, r.y, r.width-1, r.height-1);

/*		Color c1 = null;
		Color c2 = null;
		c1 = disp.getSystemColor(SWT.COLOR_GREEN);
		c2 = disp.getSystemColor(SWT.COLOR_RED);
		
//		int style = getStyle();
//		if ((style & SWT.SHADOW_IN) != 0) {
//			c1 = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
//			c2 = disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
//		}
//		if ((style & SWT.SHADOW_OUT) != 0) {		
//			c1 = disp.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
//			c2 = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
//		}
			
		if (c1 != null && c2 != null) {
			gc.setLineWidth(1);
			drawBevelRect(gc, r.x, r.y, r.width-1, r.height-1, c1, c2);
		}
*/
	}

	/**
	 * Draw a rectangle in the given colors.
	 */
	private static void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
		gc.setForeground(bottomright);
		gc.drawLine(x+w, y,   x+w, y+h);
		gc.drawLine(x,   y+h, x+w, y+h);
		
		gc.setForeground(topleft);
		gc.drawLine(x, y, x+w-1, y);
		gc.drawLine(x, y, x,     y+h-1);
	}

	protected void onPaint(PaintEvent event) {
		Rectangle rect = getClientArea();
		if (rect.width == 0 || rect.height == 0) return;
		
		GC gc = event.gc;

		// draw border
		int style = getStyle();
		if ((style & SWT.SHADOW_IN) != 0 || (style & SWT.SHADOW_OUT) != 0) {
		}
		paintBorder(gc, rect);

	}

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		redraw();
	}

	public int getBorderSize() {
		return borderSize;
	}

	public void setBorderSize(int borderSize) {
		this.borderSize = borderSize;
		redraw();
	}

	
	
}
