/**
 * 
 */
package org.gumtree.vis.interfaces;

import org.freehep.j3d.plot.Plot3D;
import org.freehep.j3d.plot.Plot3D.ColorTheme;
import org.freehep.j3d.plot.RenderStyle;
import org.gumtree.vis.hist2d.color.ColorScale;

/**
 * @author nxi
 *
 */
public interface ISurf3D extends IPlot {

	public boolean isLegoFeel();

	/**
	 * @param isLegoFeel the isLegoFeel to set
	 */
	public void setLegoFeel(boolean isLegoFeel);
	
	public Plot3D get3DRenderer();
	
	public void resetOrientation();
	
	public void resetZoomScale();
	
	public void resetCenter();
	
	public void resetPlot();
	
	public void setColorScale(ColorScale colorScale);
	
	public ColorScale getColorScale(); 
	
	public RenderStyle getRenderStyle();
	
	public void setRenderStyle(RenderStyle style);
	
	public void setColorTheme(ColorTheme theme);
	
	public ColorTheme getColorTheme();
	
	public void toggleOutsideBoxEnabled();
}
