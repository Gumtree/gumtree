/*
 * Copyright (C) 2004 by Friederich Kupzog Elektronik & Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

Authors: 
Friederich Kupzog,  fkmk@kupzog.de, www.kupzog.de/fkmk
Lorenz Maierhofer, lorenz.maierhofer@logicmindguide.com

*/
package org.gumtree.msw.ui.ktable.renderers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.SWTX;

/**
 * Simply draws a text to the cell.<p>
 * Honored style bits are: 
 * <ul>
 * <li><b>INDICATION_FOCUS</b> colors the cell in a slightly 
 * different way and draws a selection border.</li>
 * <li><b>INDICATION_FOCUS_ROW</b> colors the cell that has 
 * focus dark and its content white.</li>
 * <li><b>INDICATION_COMMENT</b> makes the renderer draw a 
 * small triangle in the upper right corner of the cell.</li>
 * <li><b>SWT.BOLD</b> Makes the renderer draw bold text.</li>
 * <li><b>SWT.ITALIC</b> Makes the renderer draw italic text</li>
 * </ul>
 * 
 * @author Lorenz Maierhofer <lorenz.maierhofer@logicmindguide.com>
 */
public class TextCellRenderer extends DefaultCellRenderer {
	//private IContentToTextConverter converter;

    /**
     * Creates a cellrenderer that prints text in the cell.<p>
     * 
     * <p>
     * @param style 
     * Honored style bits are:<br>
     * - INDICATION_FOCUS makes the cell that has the focus have a different
     *   background color and a selection border.<br>
     * - INDICATION_FOCUS_ROW makes the cell show a selection indicator as it
     *   is often seen in row selection mode. A deep blue background and white content.<br>
     * - INDICATION_COMMENT lets the renderer paint a small triangle to the
     *   right top corner of the cell.<br>
     * - SWT.BOLD Makes the renderer draw bold text.<br>
     * - SWT.ITALIC Makes the renderer draw italic text<br>
     */
    public TextCellRenderer(int style) {
    	super(style);
    }
    /*
    public TextCellRenderer(int style, IContentToTextConverter converter) {
        super(style);
        this.converter = converter;
    }

    // properties
    public IContentToTextConverter getContentToTextConverter() {
    	return converter;
    }
    public void setContentToTextConverter(IContentToTextConverter value) {
    	converter = value;
    }
    */
    
    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableCellRenderer#getOptimalWidth(org.eclipse.swt.graphics.GC, int, int, java.lang.Object, boolean)
     */
    public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
        return SWTX.getCachedStringExtent(gc, content.toString()).x + 8;
    }

    /** 
     * A default implementation that paints cells in a way that is more or less
     * Excel-like. Only the cell with focus looks very different.
     * @see de.kupzog.ktable.KTableCellRenderer#drawCell(GC, Rectangle, int, int, Object, boolean, boolean, boolean, KTableModel)
     */
    public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, 
            boolean focus, boolean fixed, boolean clicked, KTableModel model) {
        applyFont(gc);
        
        /*int topWidth = 1; int bottomWidth=1; int leftWidth=1; int rightWidth=1; 
         rect = drawSolidCellLines(gc, rect, vBorderColor, hBorderColor, 
         topWidth, bottomWidth, leftWidth, rightWidth);
         */

        //String contentStr = converter != null ? converter.toText(content) : content.toString();
        String contentStr = content.toString();
        
        // draw focus sign:
        if (focus && (m_Style & INDICATION_FOCUS)!=0) {
            // draw content:
            //rect = drawDefaultSolidCellLine(gc, rect, COLOR_LINE_LIGHTGRAY, COLOR_LINE_LIGHTGRAY);
            //drawCellContent(gc, rect, converter.ToText(content), null, getForeground(), COLOR_BGFOCUS);
        	//gc.drawFocus(rect.x, rect.y, rect.width, rect.height);
        	
            gc.setBackground(getBackground());
            gc.fillRectangle(rect);

        	Color borderColor = COLOR_BGROWFOCUS;
        	
        	if ((m_Style & INDICATION_COPYABLE) != 0)
        		BorderPainter.drawCornerRectangle(gc, rect, borderColor, 5);
        	
        	BorderPainter.drawSolidCellLines(gc, new Rectangle(rect.x, rect.y, rect.width, rect.height), borderColor, borderColor, borderColor, borderColor, 2, 1, 2, 1);
        	
            drawCellContent(gc, rect, contentStr, null, getForeground(), null);
                        
        } else if (focus && (m_Style & INDICATION_FOCUS_ROW)!=0) {
            rect = drawDefaultSolidCellLine(gc, rect, COLOR_BGROWFOCUS, COLOR_BGROWFOCUS);
            // draw content:
            drawCellContent(gc, rect, contentStr, null, COLOR_FGROWFOCUS, COLOR_BGROWFOCUS);
            
        } else {
            rect = drawDefaultSolidCellLine(gc, rect, COLOR_LINE_LIGHTGRAY, COLOR_LINE_LIGHTGRAY);
            // draw content:
            drawCellContent(gc, rect, contentStr, null, getForeground(), getBackground());
        }
        
        if ((m_Style & INDICATION_COMMENT)!=0)
            drawCommentSign(gc, rect);
        
        resetFont(gc);
    }

    /**
     * @param value If true, the comment sign is painted. Else it is omitted.
     */
	public void setCommentIndication(boolean value) {
	    if (value)
	        m_Style = m_Style | INDICATION_COMMENT;
	    else 
	        m_Style = m_Style & ~INDICATION_COMMENT;
	}
}
