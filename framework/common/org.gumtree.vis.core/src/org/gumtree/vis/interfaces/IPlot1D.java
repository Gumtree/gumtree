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
package org.gumtree.vis.interfaces;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;

import org.gumtree.vis.plot1d.MarkerShape;

/**
 * @author nxi
 *
 */
public interface IPlot1D extends IPlot{

	public Color getCurveColor(IXYErrorSeries pattern);
	
	public Shape getCurveMarkerShape(IXYErrorSeries pattern);
	
	public Stroke getCurveStroke(IXYErrorSeries pattern);
	
	public Stroke getErrorBarStroke();
	
	public int getSelectedCurveIndex();
	
	public boolean isCurveMarkerFilled(IXYErrorSeries pattern);
	
	public boolean isCurveVisible(IXYErrorSeries pattern);
	
	public boolean isErrorBarEnabled();
	
	public boolean isLogarithmXEnabled();
	
	public boolean isLogarithmYEnabled();
	
	public boolean isMarkerEnabled();
	
	public void setCurveColor(IXYErrorSeries pattern, Color color);
	
	public void setCurveMarkerFilled(IXYErrorSeries pattern, boolean filled);
	
	public void setCurveMarkerShape(IXYErrorSeries pattern, MarkerShape shape);
	
	public void setCurveMarkerVisible(IXYErrorSeries pattern, boolean isMarkerVisible);
	
	public void setCurveStroke(IXYErrorSeries pattern, float stroke);
	
	public void setCurveVisible(IXYErrorSeries pattern, boolean visible);
	
	public void setErrorBarEnabled(boolean enabled);
	
	public void setErrorBarStroke(float stroke);
	
	public void setLogarithmXEnabled(boolean enabled);
	
	public void setLogarithmYEnabled(boolean enabled);
	
	public void setMarkerEnabled(boolean enabled);
	
	public void setSelectedSeries(int seriesIndex);
}
