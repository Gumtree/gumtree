/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.renderers.BorderPainter;
import org.gumtree.msw.ui.ktable.renderers.ImageButtonCellRenderer;

/**
 * @author nxi
 *
 */
public class ImageButtonHighlightRenderer extends ImageButtonCellRenderer {

	/**
	 * @param style
	 */
	public ImageButtonHighlightRenderer(int style) {
		super(style);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean fixed,
			boolean clicked, KTableModel model) {
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
        	
            drawCellContent(gc, rect, contentStr, getImage(), getForeground(), null);
                        
        } else if (focus && (m_Style & INDICATION_FOCUS_ROW)!=0) {
            rect = drawDefaultSolidCellLine(gc, rect, COLOR_BGROWFOCUS, COLOR_BGROWFOCUS);
            // draw content:
            drawCellContent(gc, rect, contentStr, getImage(), COLOR_FGROWFOCUS, COLOR_BGROWFOCUS);
            
        } 
//        else if (clicked) {
//            rect = drawDefaultSolidCellLine(gc, rect, COLOR_LINE_LIGHTGRAY, COLOR_LINE_LIGHTGRAY);
//            // draw content:
//            drawCellContent(gc, rect, contentStr, image, COLOR_FGROWFOCUS, COLOR_BGROWFOCUS);
//            
//        } 
        else {
            rect = drawDefaultSolidCellLine(gc, rect, COLOR_LINE_LIGHTGRAY, COLOR_LINE_LIGHTGRAY);
            // draw content:
            drawCellContent(gc, rect, contentStr, getImage(), getForeground(), COLOR_FIXEDHIGHLIGHT);
        }
        
        if ((m_Style & INDICATION_COMMENT)!=0)
            drawCommentSign(gc, rect);
        
        resetFont(gc);
	}
}
