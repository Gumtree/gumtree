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

import org.gumtree.vis.hist2d.color.ColorScale;
import org.jfree.chart.title.PaintScaleLegend;

/**
 * @author nxi
 *
 */
public interface IHist2D extends IPlot{
	
	public abstract void setColorScale(ColorScale colorScale);
	
	public abstract void setLogarithmScaleEnabled(boolean enabled);
	
	public abstract ColorScale getColorScale();
	
	public abstract PaintScaleLegend getPaintScaleLegend();
	
	public abstract void updatePaintScaleLegend();
	
	public abstract boolean isLogarithmScaleEnabled();
}
