package org.gumtree.msw.ui.ktable;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.gumtree.msw.ui.Resources;

import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.renderers.BorderPainter;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;

public class CheckableCellRenderer extends DefaultCellRenderer {
    // fields
    private final Image checked;
    private final Image unchecked;
    private final Color vBorderColor;
    private final Color hBorderColor;

    // construction
	public CheckableCellRenderer() {
		this(SWT.NONE);
	}
    public CheckableCellRenderer(int style) {
    	this(style, Resources.IMAGE_BOX_CHECKED, Resources.IMAGE_BOX_UNCHECKED, COLOR_LINE_LIGHTGRAY, COLOR_LINE_LIGHTGRAY);
    }
    public CheckableCellRenderer(int style, Color vBorderColor, Color hBorderColor) {
    	this(style, Resources.IMAGE_BOX_CHECKED, Resources.IMAGE_BOX_UNCHECKED, vBorderColor, hBorderColor);
    }
    public CheckableCellRenderer(int style, Image checked, Image unchecked, Color vBorderColor, Color hBorderColor) {
    	super(style);
    	this.checked = checked;
    	this.unchecked = unchecked;
    	this.vBorderColor = vBorderColor;
    	this.hBorderColor = hBorderColor;
    }
	
	// methods
	@Override
	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
		return checked.getBounds().width;
	}
	@Override
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header, boolean clicked, KTableModel model) {
        gc.setBackground(getBackground());
        gc.fillRectangle(rect);

        if (focus) {
        	Color borderColor = COLOR_BGROWFOCUS;
        	
        	if ((m_Style & INDICATION_COPYABLE) != 0)
        		BorderPainter.drawCornerRectangle(gc, rect, borderColor, 5);
        	
        	BorderPainter.drawSolidCellLines(gc, new Rectangle(rect.x, rect.y, rect.width, rect.height), borderColor, borderColor, borderColor, borderColor, 2, 1, 2, 1);
        }
        else {
        	rect = drawDefaultSolidCellLine(gc, rect, vBorderColor, hBorderColor);
        }
        
		Image img;			
		if ((content == null) || Objects.equals(content, false))
			img = unchecked;
		else
			img = checked;
			
		Rectangle bounds = img.getBounds();
		gc.drawImage(
				img,
				rect.x + (rect.width - bounds.width) / 2, 
				rect.y + (rect.height - bounds.height) / 2);
		
		//if (focus && !clicked)
		//	gc.drawFocus(rect.x, rect.y, rect.width, rect.height);

	}
}
