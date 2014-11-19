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
package de.kupzog.ktable.renderers;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.SWTX;
import de.kupzog.ktable.models.KTableModel;

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
	private ITextFormatter formatter;

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
        formatter = DefaultTextFormatter.getInst();
    }
    public TextCellRenderer(int style, ITextFormatter formatter) {
        this(style);
        this.formatter = formatter;
    }

    /**
     * @see de.kupzog.ktable.KTableCellRenderer#getOptimalWidth(org.eclipse.swt.graphics.GC, int, int, java.lang.Object, boolean)
     */
    public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
        return content != null ? SWTX.getCachedStringExtent(gc, content.toString()).x + 8 : 8;
    }

    /** 
     * A default implementation that paints cells in a way that is more or less
     * Excel-like. Only the cell with focus looks very different.
     * @see de.kupzog.ktable.KTableCellRenderer#drawCell(GC, Rectangle, int, int, Object, boolean, boolean, boolean, KTableModel)
     */
    public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean fixed, boolean clicked, KTableModel model) {
        applyFont(gc);

        content = formatter.format(content);
        if (content == null) content = "";

        // draw focus sign:
        if (focus && (m_Style & INDICATION_FOCUS) != 0) {
            rect = drawDefaultSolidCellLine(gc, rect, borderColorVertical, borderColorHorizontal);
            drawCellContent(gc, rect, content.toString(), null, getForeground(), COLOR_BGFOCUS);
            drawFocus(gc, rect);
        } else if (focus && (m_Style & INDICATION_FOCUS_ROW) != 0) {
            rect = drawDefaultSolidCellLine(gc, rect, borderColorVertical, borderColorHorizontal);
            drawCellContent(gc, rect, content.toString(), null, COLOR_FGROWFOCUS, COLOR_BGROWFOCUS);
        } else {
            rect = drawDefaultSolidCellLine(gc, rect, borderColorVertical, borderColorHorizontal);
            drawCellContent(gc, rect, content.toString(), null, getForeground(), getBackground());
        }

        if ((m_Style & INDICATION_COMMENT) != 0) drawCommentSign(gc, rect);

        resetFont(gc);
    }

    /**
     * @param value If true, the comment sign is painted. Else it is omitted.
     */
	public void setCommentIndication(boolean value) {
	    if (value) m_Style = m_Style | INDICATION_COMMENT;
	    else m_Style = m_Style & ~INDICATION_COMMENT;
	}

	public void setFormatter(ITextFormatter formatter) {
		this.formatter = formatter;
	}

	/**
	 * @param style Стил?дл?назначен? рендерерам
	 * @param light Цвет светлы?рамо?
	 * @param dark Цвет тёмных рамо?
	 * @return Двумерны?массив рендереров 2?, содержащий четыре рендерер? необходимы?дл?обрисовк?
	 * груп??ее?рамкой.
	 */
	public static <E extends TextCellRenderer> E[][] createRenderers(Class<E> clazz, int style, int align, Color light, Color dark, Color bg, Color fg, ITextFormatter formatter) {
		try {
			Constructor<E> constructor = clazz.getDeclaredConstructor(int.class);
	        constructor.setAccessible(true);
	        
		    E r00 = constructor.newInstance(style);
		    r00.setFormatter(formatter);
		    r00.setBorderColorVertical(light);
		    r00.setBorderColorHorizontal(light);
	        r00.setBackground(bg);
	        r00.setForeground(fg);
		    r00.setAlignment(align);
	
		    E r01 = constructor.newInstance(style);
		    r01.setFormatter(formatter);
	        r01.setBorderColorVertical(light);
	        r01.setBorderColorHorizontal(dark);
	        r01.setBackground(bg);
	        r01.setForeground(fg);
	        r01.setAlignment(align);
	
	        E r10 = constructor.newInstance(style);
		    r10.setFormatter(formatter);
	        r10.setBorderColorVertical(dark);
	        r10.setBorderColorHorizontal(light);
	        r10.setBackground(bg);
	        r10.setAlignment(align);
	        r10.setForeground(fg);
	        r10.setStyle(r10.getStyle() | SWT.VERTICAL);
	
	        E r11 = constructor.newInstance(style);
		    r11.setFormatter(formatter);
	        r11.setBorderColorVertical(dark);
	        r11.setBorderColorHorizontal(dark);
	        r11.setBackground(bg);
	        r11.setForeground(fg);
	        r11.setAlignment(align);
	
	        @SuppressWarnings("unchecked")
	        E[][] result = (E[][]) Array.newInstance(clazz, 2, 2);
	        result[0][0] = r00;
	        result[0][1] = r01;
	        result[1][0] = r10;
	        result[1][1] = r11;
	        
		    return result;
		} catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
	}
	
	public static <E extends TextCellRenderer> E[][] createRenderers(Class<E> clazz, int style, int align, Color light, Color dark, Color bg, Color fg) {
		return createRenderers(clazz, style, align, light, dark, bg, fg, DefaultTextFormatter.getInst());
	}
	
	public static TextCellRenderer[][] createRenderers(int style, int align, Color light, Color dark, Color bg, Color fg, ITextFormatter formatter) {
		return createRenderers(TextCellRenderer.class, style, align, light, dark, bg, fg, formatter);
	}
	
	public static TextCellRenderer[][] createRenderers(int style, int align, Color light, Color dark, Color bg, Color fg) {
		return createRenderers(TextCellRenderer.class, style, align, light, dark, bg, fg, DefaultTextFormatter.getInst());
	}
	
	

	public static <E extends TextCellRenderer> E[][] createRenderers(Class<E> clazz, Color light, Color dark) {
        try {
            Constructor<E> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);

            E r00 = constructor.newInstance();
            r00.setBorderColorVertical(light);
            r00.setBorderColorHorizontal(light);

            E r01 = constructor.newInstance();
            r01.setBorderColorVertical(light);
            r01.setBorderColorHorizontal(dark);

            E r10 = constructor.newInstance();
            r10.setBorderColorVertical(dark);
            r10.setBorderColorHorizontal(light);
            r10.setStyle(r10.getStyle() | SWT.VERTICAL);

            E r11 = constructor.newInstance();
            r11.setBorderColorVertical(dark);
            r11.setBorderColorHorizontal(dark);

            @SuppressWarnings("unchecked")
            E[][] result = (E[][]) Array.newInstance(clazz, 2, 2);
            result[0][0] = r00;
            result[0][1] = r01;
            result[1][0] = r10;
            result[1][1] = r11;

            return result;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
	}
	
	
}