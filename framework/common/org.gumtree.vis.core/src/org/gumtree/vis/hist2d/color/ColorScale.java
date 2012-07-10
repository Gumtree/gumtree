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
package org.gumtree.vis.hist2d.color;

import java.awt.Color;
import java.awt.image.IndexColorModel;

/**
 * @author nxi
 *
 */
public enum ColorScale {

	Rainbow, Nature, Temperature, Spectrum, Grey, VGA, ReversedRainbow, 
	ReversedSpectrum, ReversedGrey, RainbowTail;

	public static final String DEFAULT_COLOR_SCALE_PROPERTY = 
		"vis.default.color.scale";
	public static int DIVISION_COUNT = 256;
	private static ColorScale CURRENT_COLOR_SCALE;
	
	static {
		String defaultColorScaleProperty = System.getProperty(
				DEFAULT_COLOR_SCALE_PROPERTY);
		if (defaultColorScaleProperty != null) {
			try{
				CURRENT_COLOR_SCALE = ColorScale.valueOf(
						defaultColorScaleProperty);
			} catch (Exception e) {
			}
		}
		if (CURRENT_COLOR_SCALE == null) {
			CURRENT_COLOR_SCALE = ColorScale.Nature;
		}
	}
	
	public double[] getColorRGB(double value) {
		int index;
		if (value < 0) {
			index = 0;
		} else if (value > 1) {
			index = DIVISION_COUNT;
		} else {
			index = (int) (value * DIVISION_COUNT);
		}
		
		double[] rgb;
		switch (this) {
		case Rainbow:
			rgb = RainbowColor.rgbs[index];
			break;
		case Nature:
			rgb = getNatureColor(value);
			break;
		case Temperature:
			rgb = TemperatureColor.rgbs[index];
			break;
		case Spectrum:
			rgb = SpectrumColor.rgbs[index];
			break;
		case Grey:
			rgb = GreyColor.rgbs[index];
			break;
		case ReversedGrey:
			rgb = RGreyColor.rgbs[index];
			break;
		case ReversedRainbow:
			rgb = RRainbowColor.rgbs[index];
			break;
		case ReversedSpectrum:
			rgb = RSpectrumColor.rgbs[index];
			break;
		case RainbowTail:
			rgb = SmallColor.rgbs[index];
			break;
		case VGA:
			rgb = VGAColor.rgbs[index];
			break;
		default:
			rgb = RainbowColor.rgbs[index];
			break;
		}
		return rgb;
	}
	
	public Color getColor(double value) {
		double rgb[] = getColorRGB(value);
		return new Color((int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255));
	}
	
	public static IndexColorModel getColorModel(ColorScale colorScale){
        byte[] r = new byte[DIVISION_COUNT];
        byte[] g = new byte[DIVISION_COUNT];
        byte[] b = new byte[DIVISION_COUNT];

        for (int i = 0; i < DIVISION_COUNT; i++) {
        	double value = (double) i / DIVISION_COUNT;
        	Color color = colorScale.getColor(value);
        	r[i] = (byte) color.getRed();
        	g[i] = (byte) color.getGreen();
        	b[i] = (byte) color.getBlue();
        }
        return new IndexColorModel(8, DIVISION_COUNT, r, g, b);
	}
	
	public static double[] getNatureColor(double value){
		double red, green, blue;
		if (value <= 0){
			red = green = blue = 0;
		}else{
			if (value <= 1.0 / 6){
				red = 0;
				green = 0;
				blue = value * 5 + 1.0 / 6;
			}else if (value <= 2.0 / 6){
				red = (value - 1.0 / 6) * 4;
				green = 0;
				blue = 1;
			}else if (value <= 3.0 / 6){
				red = value + 1.0 / 3;
				green = 0;
				blue = 2 - value * 3;
			}else if (value <= 4.0 / 6){
				red = value + 1.0 / 3;
				green = 0;
				blue = 2 - value * 3;
			}else if (value <= 5.0 / 6){
				red = 1;
				green = value * 6 - 4;
				blue = 0;
			}else if (value <= 1){
				red = 1;
//				red = value;
				green = 1;
				blue = value * 6 - 5;
			}else {
				red = green = blue = 1;
			}
		}
		return new double[]{red, green, blue};
//		return new Color((int) (red * 255), (int) (green * 255), (int) (blue * 255)); 
	}
	
	public static void setCurrentColorScale(ColorScale colorScale) {
		CURRENT_COLOR_SCALE = colorScale;
	}
	
	public static ColorScale getCurrentColorScale() {
		return CURRENT_COLOR_SCALE;
	}
}
