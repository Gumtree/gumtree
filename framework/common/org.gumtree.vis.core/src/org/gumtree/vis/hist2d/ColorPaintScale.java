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
package org.gumtree.vis.hist2d;

import java.awt.Paint;
import java.io.Serializable;

import org.gumtree.vis.hist2d.color.ColorScale;
import org.jfree.chart.renderer.PaintScale;

/**
 * @author nxi
 *
 */
public class ColorPaintScale implements PaintScale, Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7718989178364258151L;
	private static final double LOG_INPUT_START = 1.0 / (ColorScale.DIVISION_COUNT + 1);
	private static final double LOG_INPUT_WIDTH = 1 - LOG_INPUT_START;
	private static final double LOG_OUTPUT_START = Math.log(LOG_INPUT_START);
	private static final double LOG_OUTPUT_WIDTH = - LOG_OUTPUT_START;
	
	private double lower;
	private double upper;
	private double width = 1;
	private ColorScale scale;
	private boolean isLogScale = false;
	private double lowerBoundPercent = 0;
    private double upperBoundPercent = 1;
	
	public ColorPaintScale(double lower, double upper, ColorScale scaleName){
		this.lower = lower;
		this.upper = upper;
		this.scale = scaleName;
		updateWidth();
	}
	
	private void updateWidth(){
		double wholeWidth = upper - lower;
		width = wholeWidth * (upperBoundPercent - lowerBoundPercent);
		if (width == 0) {
			width = 1;
		}
	}
	
	@Override
	public double getLowerBound() {
		return lower;
	}

	@Override
	public Paint getPaint(double value) {
		double colorValue = (value - ((upper - lower) * lowerBoundPercent + lower)) / width;
		if (colorValue < 0) {
			colorValue = 0;
		}
		if (colorValue > 1) {
			colorValue = 1;
		}
		if (isLogScale) {
			return scale.getColor((Math.log((colorValue) * LOG_INPUT_WIDTH + LOG_INPUT_START) 
					- LOG_OUTPUT_START) / LOG_OUTPUT_WIDTH);
		}
		return scale.getColor(colorValue);
	}

	@Override
	public double getUpperBound() {
		return upper;
	}

	public void setUpperBound(double upper) {
		this.upper = upper;
		updateWidth();
	}
	
	public void setLowerBound(double lower) {
		this.lower = lower;
		updateWidth();
	}
	
	public ColorScale getColorScale(){
		return scale;
	}
	
	public void setColorScale(ColorScale scale) {
		this.scale = scale;
	}

	/**
	 * @return the isLogScale
	 */
	public boolean isLogScale() {
		return isLogScale;
	}

	/**
	 * @param isLogScale the isLogScale to set
	 */
	public void setLogScale(boolean isLogScale) {
		this.isLogScale = isLogScale;
	}

	/**
	 * @param lowerBoundPercent the lowerBoundPercent to set
	 */
	public void setLowerBoundPercent(double lowerBoundPercent) {
		this.lowerBoundPercent = lowerBoundPercent;
		updateWidth();
	}

	/**
	 * @return the lowerBoundPercent
	 */
	public double getLowerBoundPercent() {
		return lowerBoundPercent;
	}

	/**
	 * @param upperBoundPercent the upperBoundPercent to set
	 */
	public void setUpperBoundPercent(double upperBoundPercent) {
		this.upperBoundPercent = upperBoundPercent;
		updateWidth();
	}

	/**
	 * @return the upperBoundPercent
	 */
	public double getUpperBoundPercent() {
		return upperBoundPercent;
	}
	
	public void resetBoundPercentage() {
		upperBoundPercent = 1;
		lowerBoundPercent = 0;
		updateWidth();
	}
}
