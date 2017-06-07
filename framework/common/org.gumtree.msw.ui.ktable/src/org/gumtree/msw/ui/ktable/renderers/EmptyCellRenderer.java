package org.gumtree.msw.ui.ktable.renderers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import org.gumtree.msw.ui.ktable.KTableModel;

public class EmptyCellRenderer extends DefaultCellRenderer {

    public EmptyCellRenderer(int style) {
    	super(style);
    }
    
    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableCellRenderer#getOptimalWidth(org.eclipse.swt.graphics.GC, int, int, java.lang.Object, boolean)
     */
    public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
        return 0 + 8;
    }

    /** 
     * A default implementation that paints cells in a way that is more or less
     * Excel-like. Only the cell with focus looks very different.
     * @see de.kupzog.ktable.KTableCellRenderer#drawCell(GC, Rectangle, int, int, Object, boolean, boolean, boolean, KTableModel)
     */
    public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, 
            boolean focus, boolean fixed, boolean clicked, KTableModel model) {

        // draw focus sign:
        if (focus && (m_Style & INDICATION_FOCUS)!=0) {
            gc.setBackground(getBackground());
            gc.fillRectangle(rect);

        	Color borderColor = COLOR_BGROWFOCUS;
        	
        	if ((m_Style & INDICATION_COPYABLE) != 0)
        		BorderPainter.drawCornerRectangle(gc, rect, borderColor, 5);
        	
        	BorderPainter.drawSolidCellLines(gc, new Rectangle(rect.x, rect.y, rect.width, rect.height), borderColor, borderColor, borderColor, borderColor, 2, 1, 2, 1);
         
        } else if (focus && (m_Style & INDICATION_FOCUS_ROW)!=0) {
            rect = drawDefaultSolidCellLine(gc, rect, COLOR_BGROWFOCUS, COLOR_BGROWFOCUS);

            gc.setBackground(COLOR_BGROWFOCUS);
            gc.fillRectangle(rect);

        } else {
            rect = drawDefaultSolidCellLine(gc, rect, COLOR_LINE_LIGHTGRAY, COLOR_LINE_LIGHTGRAY);

            gc.setBackground(getBackground());
            gc.fillRectangle(rect);
        }
        
        if ((m_Style & INDICATION_COMMENT)!=0)
            drawCommentSign(gc, rect);
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
