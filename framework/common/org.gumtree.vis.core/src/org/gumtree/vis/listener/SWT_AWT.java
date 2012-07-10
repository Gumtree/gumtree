package org.gumtree.vis.listener;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

public class SWT_AWT {
	
	/**
	 * Creates an AWT <code>MouseEvent</code> from a swt event.
	 * This method helps passing SWT mouse event to awt components.
	 * @param event The swt event.
	 * @return A AWT mouse event based on the given SWT event.
	 */
	public static MouseEvent toAwtMouseEvent(org.eclipse.swt.events.MouseEvent event
			, JPanel panel) {
	    int button = MouseEvent.NOBUTTON;
	    switch (event.button) {
	    case 1: button = MouseEvent.BUTTON1; break;
	    case 2: button = MouseEvent.BUTTON2; break;
	    case 3: button = MouseEvent.BUTTON3; break;
	    }
	    int modifiers = 0;
	    if ((event.stateMask & SWT.CTRL) != 0) {
	        modifiers |= InputEvent.CTRL_DOWN_MASK;
	    }
	    if ((event.stateMask & SWT.SHIFT) != 0) {
	        modifiers |= InputEvent.SHIFT_DOWN_MASK;
	    }
	    if ((event.stateMask & SWT.ALT) != 0) {
	        modifiers |= InputEvent.ALT_DOWN_MASK;
	    }
	    MouseEvent awtMouseEvent = new MouseEvent(panel, event.hashCode(),
	            event.time, modifiers, event.x, event.y, 1, false, button);
	    return awtMouseEvent;
	}
	
	public static MouseWheelEvent toMouseWheelEvent(org.eclipse.swt.events.MouseEvent event, 
			JPanel panel) {
	    int modifiers = 0;
	    if ((event.stateMask & SWT.CTRL) != 0) {
	        modifiers |= InputEvent.CTRL_DOWN_MASK;
	    }
	    if ((event.stateMask & SWT.SHIFT) != 0) {
	        modifiers |= InputEvent.SHIFT_DOWN_MASK;
	    }
	    if ((event.stateMask & SWT.ALT) != 0) {
	        modifiers |= InputEvent.ALT_DOWN_MASK;
	    }
		MouseWheelEvent awtMouseWheelEvent = new MouseWheelEvent(
				panel, 
				event.hashCode(), 
				event.time, 
				modifiers, 
				event.x, 
				event.y, 
				event.count, 
				false, 
				MouseWheelEvent.WHEEL_BLOCK_SCROLL, 
				1, 
				event.count);
		return awtMouseWheelEvent;
	}
	
	
    /**
     * Converts an AWT image to SWT.
     *
     * @param image  the image (<code>null</code> not permitted).
     *
     * @return Image data.
     */
    public static ImageData convertAWTImageToSWT(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("Null 'image' argument.");
        }
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        if (w == -1 || h == -1) {
            return null;
        }
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return convertToSWT(bi);
    }

    /**
     * Converts a buffered image to SWT <code>ImageData</code>.
     *
     * @param bufferedImage  the buffered image (<code>null</code> not
     *         permitted).
     *
     * @return The image data.
     */
    public static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel
                    = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(),
                    colorModel.getGreenMask(), colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0],
                            pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel)
                    bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
                        blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        return null;
    }
    
    // [GUMTREE-611] Disabled due to compilation error on mac cocoa
//    public static Cursor toAwtCursor(org.eclipse.swt.graphics.Cursor swtCursor) {
//    	int handle = 0;
//    	switch (swtCursor.handle) {
//		case SWT.CURSOR_HAND:
//			handle = Cursor.HAND_CURSOR;
//			break;
//		case SWT.CURSOR_CROSS:
//			handle = Cursor.CROSSHAIR_CURSOR;
//			break;
//		case SWT.CURSOR_ARROW:
//			handle = Cursor.DEFAULT_CURSOR;
//			break;
//		case SWT.CURSOR_WAIT:
//			handle = Cursor.WAIT_CURSOR;
//			break;
//		case SWT.CURSOR_SIZEALL:
//			handle = Cursor.MOVE_CURSOR;
//			break;
//		case SWT.CURSOR_SIZEE:
//			handle = Cursor.E_RESIZE_CURSOR;
//			break;
//		case SWT.CURSOR_SIZEN:
//			handle = Cursor.N_RESIZE_CURSOR;
//			break;
//		case SWT.CURSOR_SIZENE:
//			handle = Cursor.NE_RESIZE_CURSOR;
//			break;
//		case SWT.CURSOR_SIZENW:
//			handle = Cursor.NW_RESIZE_CURSOR;
//			break;
//		case SWT.CURSOR_SIZES:
//			handle = Cursor.S_RESIZE_CURSOR;
//			break;
//		case SWT.CURSOR_SIZESE:
//			handle = Cursor.SE_RESIZE_CURSOR;
//			break;
//		case SWT.CURSOR_SIZESW:
//			handle = Cursor.SW_RESIZE_CURSOR;
//			break;
//		case SWT.CURSOR_IBEAM:
//			handle = Cursor.TEXT_CURSOR;
//			break;
//		case SWT.CURSOR_SIZEW:
//			handle = Cursor.W_RESIZE_CURSOR;
//			break;
//		default:
//			handle = Cursor.DEFAULT_CURSOR;
//			break;
//		}
//    	return Cursor.getPredefinedCursor(handle);
//    }
    
    public static Color toAwtColor(org.eclipse.swt.graphics.Color swtColor){
    	return new Color(swtColor.getRed(), swtColor.getGreen(), swtColor.getBlue());
    }
}

