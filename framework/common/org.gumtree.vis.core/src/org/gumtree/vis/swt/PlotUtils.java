/**
 * 
 */
package org.gumtree.vis.swt;

import org.gumtree.vis.hist2d.Hist2DControlWidgetProvider;
import org.gumtree.vis.interfaces.IHist2D;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.IPlot1D;
import org.gumtree.vis.interfaces.ISurf3D;
import org.gumtree.vis.plot1d.Plot1DControlWidgetProvider;
import org.gumtree.vis.surf3d.Surf3DControlWidgetProvider;

/**
 * @author nxi
 *
 */
public class PlotUtils {

	public static IPlotControlWidgetProvider getPlotControlWidgetProvider(IPlot plot) {
		
		if (plot instanceof IPlot1D) {
			return new Plot1DControlWidgetProvider();
		} else if (plot instanceof IHist2D) {
			return new Hist2DControlWidgetProvider();
		} else if (plot instanceof ISurf3D) {
			return new Surf3DControlWidgetProvider();
		}
		return null;
	}
}
