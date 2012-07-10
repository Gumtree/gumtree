/**
 * 
 */
package org.gumtree.vis.swt;

import java.util.List;

import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.gumtree.vis.interfaces.IPlot;

/**
 * @author nxi
 *
 */
public interface IPlotControlWidgetProvider {

	
	public List<ToolItem> createControlToolItems(ToolBar toolBar);
	
	public void setPlot(IPlot plot);
	
	public IPlot getPlot();
}
