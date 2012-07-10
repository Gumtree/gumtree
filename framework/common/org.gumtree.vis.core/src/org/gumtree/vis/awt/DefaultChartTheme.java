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
package org.gumtree.vis.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.jfree.chart.ChartColor;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;

/**
 * @author nxi
 *
 */
public class DefaultChartTheme extends StandardChartTheme {

	public static Paint[] createDefaultPaintArray() {

		return new Paint[] {
				ChartColor.DARK_BLUE,
				ChartColor.DARK_GREEN,
				ChartColor.DARK_MAGENTA,
				ChartColor.DARK_CYAN,
				ChartColor.DARK_RED,
				ChartColor.DARK_YELLOW,
				Color.darkGray,
				new Color(0xFF, 0x55, 0x55),
				new Color(0x55, 0x55, 0xFF),
				new Color(0x00, 0x77, 0x00),
				new Color(0x77, 0x77, 0x00),
				new Color(0xCA, 0x00, 0xCA),
				new Color(0x00, 0x88, 0x88),
//				Color.pink,
				Color.gray,
//				ChartColor.LIGHT_RED,
//				ChartColor.LIGHT_BLUE,
//				ChartColor.LIGHT_GREEN,
//				ChartColor.LIGHT_YELLOW,
//				ChartColor.LIGHT_MAGENTA,
//				ChartColor.LIGHT_CYAN,
//				Color.lightGray,
				ChartColor.VERY_DARK_RED,
				ChartColor.VERY_DARK_BLUE,
				ChartColor.VERY_DARK_GREEN,
				ChartColor.VERY_DARK_YELLOW,
				ChartColor.VERY_DARK_MAGENTA,
				ChartColor.VERY_DARK_CYAN,
				ChartColor.VERY_LIGHT_RED,
				ChartColor.VERY_LIGHT_BLUE,
				ChartColor.VERY_LIGHT_GREEN,
				ChartColor.VERY_LIGHT_YELLOW,
				ChartColor.VERY_LIGHT_MAGENTA,
				ChartColor.VERY_LIGHT_CYAN
		};
	}
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 8646464211316694479L;

	/**
	 * @param name
	 */
	public DefaultChartTheme(String name) {
		super(name);
		setExtraLargeFont(new Font("SansSerif", Font.BOLD, 16));
		setLargeFont(new Font("SansSerif", Font.BOLD, 14));
		setRegularFont(new Font("SansSerif", Font.PLAIN, 12));
		setSmallFont(new Font("SansSerif", Font.PLAIN, 10));
		setDrawingSupplier(new DefaultDrawingSupplier(
				createDefaultPaintArray(),
				DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
				));
	}

}
