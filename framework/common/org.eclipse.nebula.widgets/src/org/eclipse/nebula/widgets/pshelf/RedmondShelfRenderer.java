/*******************************************************************************
 * Copyright (c) 2006 Chris Gross. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: schtoo@schtoo.com(Chris Gross) - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.pshelf;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA 
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p> 
 *
 * @author cgross
 */
public class RedmondShelfRenderer extends AbstractRenderer {

	private Color[] initialColors;	// to dispose created colors
	
	private int textMargin = 2;
	private int margin = 4;
	private PShelf parent;
	private int spacing = 8;
	
	private Font initialFont;
	private Font initialOpenFont;
	
	private Color gradient1;
	private Color gradient2;
	
	private Font font;
	private Font selectedFont;
	
	private Color selectedGradient1;
	private Color selectedGradient2;
	
	private Color hoverGradient1;
	private Color hoverGradient2;
	
	private Color lineColor;
	
	private Color selectedForeground;
	private Color foreground;

	/** 
     * {@inheritDoc}
     */
    public Point computeSize(GC gc, int wHint, int hHint, Object value)
    {
        PShelfItem item = (PShelfItem)value;
        
		int h = 0;
		
		gc.setFont(font);
		
		if (item.getImage() == null){
			h = gc.getFontMetrics().getHeight() + (2*(textMargin));
		} else {
			h = Math.max(item.getImage().getBounds().height,gc.getFontMetrics().getHeight() + (2*textMargin));
		}
		
		gc.setFont(selectedFont);
		
		h = Math.max(h,gc.getFontMetrics().getHeight() + (2*textMargin));
		
		h += 2*margin;
		
		if (h % 2 != 0)
			h ++;
		
		return new Point(wHint,h);
	}

    
	/** 
     * {@inheritDoc}
     */
    public void paint(GC gc, Object value)
    {
        PShelfItem item = (PShelfItem)value;
        
		//Color back = parent.getBackground();
		Color fore = parent.getForeground();
        
            if (isSelected()){
                gc.setForeground(selectedGradient1);
                gc.setBackground(selectedGradient2);
            } else {
                if (isHover()){
                    gc.setForeground(hoverGradient1);
                    gc.setBackground(hoverGradient2);
                } else {
                    gc.setForeground(gradient1);
                    gc.setBackground(gradient2);
                }
            }

        
		gc.fillGradientRectangle(getBounds().x,getBounds().y,getBounds().width,getBounds().height,true);		

        if ((parent.getStyle() & SWT.SIMPLE) != 0)
        {
    		if (!isSelected()){
    			gc.setForeground(lineColor);
    			gc.drawLine(0,getBounds().y,getBounds().width -1,getBounds().y);
    		}	
        }
        else
        {
            if (parent.getItems()[0] != item)
            {
                gc.setForeground(lineColor);
                gc.drawLine(0,getBounds().y,getBounds().width -1,getBounds().y);
            }
            
            if (isSelected()){
                gc.setForeground(lineColor);
                gc.drawLine(0,getBounds().y + getBounds().height -1,getBounds().width -1,getBounds().y + getBounds().height -1);               
            }
        }
		
        boolean imageLeft = true;
        
        if ((parent.getStyle() & SWT.SIMPLE) != 0)
        {
            imageLeft = !isSelected();
        }
        
		int x = 6;
		if (item.getImage() != null && imageLeft){
			int y2 = (getBounds().height - item.getImage().getBounds().height)/2;
			if ((getBounds().height - item.getImage().getBounds().height) % 2 != 0)
				y2 ++;
			
			gc.drawImage(item.getImage(),x,getBounds().y + y2);
			
			x += item.getImage().getBounds().width + spacing;
		}
		
    		if (isSelected()){
    			gc.setFont(selectedFont);
			gc.setForeground(selectedForeground != null ? selectedForeground : fore);
    		} else {
    			gc.setFont(font);
			gc.setForeground(foreground != null ? foreground : fore);
        }
		
		int y2 = (getBounds().height - gc.getFontMetrics().getHeight())/2;
		if ((getBounds().height - gc.getFontMetrics().getHeight()) % 2 != 0)
			y2 ++;
		
		int textWidth = getBounds().width - 12;
		if (item.getImage() != null){
			textWidth -= item.getImage().getBounds().width;
			textWidth -= 6;
		}
		
		gc.drawString(getShortString(gc,item.getText(),textWidth),x,getBounds().y +y2,true);
		
		if (item.getImage() != null && !imageLeft){
			int y3 = (getBounds().height - item.getImage().getBounds().height)/2;
			if ((getBounds().height - item.getImage().getBounds().height) % 2 != 0)
				y3 ++;
			
			gc.drawImage(item.getImage(),getBounds().width - 6 - item.getImage().getBounds().width,getBounds().y + y3);
		}
		
		if (isFocus()){
			gc.drawFocus(1,1,getBounds().width-2,getBounds().height-1);
		}
	}

	public void initialize(Control control) {
		this.parent = (PShelf)control;
		
        FontData fd = parent.getFont().getFontData()[0];
        initialFont = new Font(parent.getDisplay(), fd.getName(), fd.getHeight(),SWT.BOLD);
		//parent.setFont(initialFont);
		
		Color baseColor = parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
		Color color1 = createNewBlendedColor(baseColor,parent.getDisplay().getSystemColor(SWT.COLOR_WHITE),30);
		
		baseColor = createNewBlendedColor(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT),parent.getDisplay().getSystemColor(SWT.COLOR_WHITE),80);
		
		Color color2 = createNewSaturatedColor(baseColor,.01f);

		if ((parent.getStyle() & SWT.SIMPLE) != 0) {
			gradient1 = color1;
			gradient2 = color2;
		} else {
			selectedGradient1 = color1;
			selectedGradient2 = color2;
		}
		
		baseColor.dispose();
		
		lineColor = createNewSaturatedColor(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION),.02f);

		
		baseColor = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
		
		color1 = createNewBlendedColor(baseColor,parent.getDisplay().getSystemColor(SWT.COLOR_WHITE),70);
		
		baseColor = createNewBlendedColor(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION),parent.getDisplay().getSystemColor(SWT.COLOR_BLACK),80);
		
		color2 = createNewSaturatedColor(baseColor,.02f);

		if ((parent.getStyle() & SWT.SIMPLE) != 0) {
			selectedGradient1 = color1;
			selectedGradient2 = color2;
		} else {
			gradient1 = color1;
			gradient2 = color2;
		}
		
		baseColor.dispose();

		//initialOpenFont = FontUtils.createFont(parent.getFont(),4,SWT.BOLD);
		if ((parent.getStyle() & SWT.SIMPLE) != 0)
		initialOpenFont = new Font(parent.getDisplay(),"Arial",12,SWT.BOLD);
		else
			initialOpenFont = new Font(parent.getDisplay(), initialFont.getFontData());
		
		font = initialFont;
		selectedFont = initialOpenFont;
		
		Color inverseColor = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
		if ((parent.getStyle() & SWT.SIMPLE) != 0)
			selectedForeground = inverseColor;
		else
			foreground = inverseColor;
		// the other color left null, foreground color of the parent will be used for it
		
		baseColor = createNewReverseColor(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		
		hoverGradient1 = createNewBlendedColor(baseColor,parent.getDisplay().getSystemColor(SWT.COLOR_WHITE),30);
		
				
		Color baseColor2 = createNewBlendedColor(baseColor,parent.getDisplay().getSystemColor(SWT.COLOR_WHITE),99);
		
		hoverGradient2 = createNewSaturatedColor(baseColor2,.00f);
		
		baseColor2.dispose();
		baseColor.dispose();

		initialColors = new Color[] { gradient1, gradient2, selectedGradient1, selectedGradient2,
				hoverGradient1, hoverGradient2, lineColor };
	}

	public void dispose() {
		initialFont.dispose();
		initialOpenFont.dispose();
		
		for (int i = 0; i < initialColors.length; i++) {
			initialColors[i].dispose();
		}
        
        super.dispose();
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Color getGradient1() {
		return gradient1;
	}

	public void setGradient1(Color gradient1) {
		this.gradient1 = gradient1;
	}

	public Color getGradient2() {
		return gradient2;
	}

	public void setGradient2(Color gradient2) {
		this.gradient2 = gradient2;
	}

	public Color getHoverGradient1() {
		return hoverGradient1;
	}

	public void setHoverGradient1(Color hoverGradient1) {
		this.hoverGradient1 = hoverGradient1;
	}

	public Color getHoverGradient2() {
		return hoverGradient2;
	}

	public void setHoverGradient2(Color hoverGradient2) {
		this.hoverGradient2 = hoverGradient2;
	}

	public Font getSelectedFont() {
		return selectedFont;
	}

	public void setSelectedFont(Font selectedFont) {
		this.selectedFont = selectedFont;
	}

	public Color getSelectedForeground() {
		return selectedForeground;
	}

	/** Sets text color for the selected item.
	 * @param selectedForeground Can be <code>null</code>, foreground color of the parent is used in that case.
	 */
	public void setSelectedForeground(Color selectedForeground) {
		this.selectedForeground = selectedForeground;
	}

	public Color getForeground() {
		return foreground;
	}


	/** Sets text color for non-selected items.
	 * @param foreground Can be <code>null</code>, foreground color of the parent is used in that case.
	 */
	public void setForeground(Color foreground) {
		this.foreground = foreground;
	}


	public Color getSelectedGradient1() {
		return selectedGradient1;
	}

	public void setSelectedGradient1(Color selectedGradient1) {
		this.selectedGradient1 = selectedGradient1;
	}

	public Color getSelectedGradient2() {
		return selectedGradient2;
	}

	public void setSelectedGradient2(Color selectedGradient2) {
		this.selectedGradient2 = selectedGradient2;
	}
    
    private static String getShortString(GC gc, String t, int width)
    {

        if (t == null)
        {
            return null;
        }

        if (t.equals(""))
        {
            return "";
        }

        if (width >= gc.stringExtent(t).x)
        {
            return t;
        }

        int w = gc.stringExtent("...").x;
        String text = t;
        int l = text.length();
        int pivot = l / 2;
        int s = pivot;
        int e = pivot + 1;
        while (s >= 0 && e < l)
        {
            String s1 = text.substring(0, s);
            String s2 = text.substring(e, l);
            int l1 = gc.stringExtent(s1).x;
            int l2 = gc.stringExtent(s2).x;
            if (l1 + w + l2 < width)
            {
                text = s1 + "..." + s2;
                break;
            }
            s--;
            e++;
        }

        if (s == 0 || e == l)
        {
            text = text.substring(0, 1) + "..." + text.substring(l - 1, l);
        }

        return text;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    private static int blend(int v1, int v2, int ratio)
    {
        return (ratio * v1 + (100 - ratio) * v2) / 100;
    }

    private static RGB blend(RGB c1, RGB c2, int ratio)
    {
        int r = blend(c1.red, c2.red, ratio);
        int g = blend(c1.green, c2.green, ratio);
        int b = blend(c1.blue, c2.blue, ratio);
        return new RGB(r, g, b);
    }

    private static Color createNewBlendedColor(Color c1, Color c2, int ratio)
    {
        Color newColor = new Color(Display.getCurrent(), blend(c1.getRGB(), c2.getRGB(), ratio));

        return newColor;
    }

    private static Color createNewReverseColor(Color c)
    {
        Color newColor = new Color(Display.getCurrent(), 255 - c.getRed(), 255 - c.getGreen(),
                                   255 - c.getBlue());
        return newColor;
    }

    private static RGB saturate(RGB rgb, float saturation)
    {
        float[] hsb = rgb.getHSB();
        
        hsb[1] += saturation;
        if(hsb[1] > 1.0f) {
            hsb[1] = 1.0f;
        }
        if(hsb[1] < 0f) {
            hsb[1] = 0f;
        }

        // hue is 0.0..360.0, saturation and brightness 0.0..1.0
        hsb[0] += 360.0 * saturation;
        if(hsb[0] > 360.0f) {
            hsb[0] = 360.0f;
        }

        if(hsb[0] < 0f) {
            hsb[0] = 0f;
        }
       
        return new RGB(hsb[0], hsb[1], hsb[2]);       
    }

    private static Color createNewSaturatedColor(Color c, float saturation)
    {
        RGB newRGB = saturate(c.getRGB(), saturation);
        return new Color(Display.getCurrent(), newRGB);
    }
    
}