/**
 * 
 */
package org.gumtree.vis.nexus.dataset;

import java.io.IOException;
import java.util.List;

import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.vis.gdm.dataset.Hist2DDataset;

/**
 * @author nxi
 *
 */
public class Hist2DNXDataset extends Hist2DDataset {

	private INXDataset nxDataset; 
	
	public void setData(INXDataset nxDataset) 
	throws IOException, ShapeNotMatchException {
		this.nxDataset = nxDataset;
		INXdata nxData = nxDataset.getNXroot().getDefaultData();
		if (nxData != null) {
			IArray x = null;
			IArray y = null;
			IArray z = nxData.getSignal().getData();
			String zUnits = nxData.getSignal().getUnitsString();
			String xTitle = null;
			String yTitle = null;
			String xUnits = null;
			String yUnits = null;
			List<IAxis> axes = nxData.getAxisList();
			if (axes != null && axes.size() > 1) {
				IAxis yAxis = axes.get(axes.size() - 2);
				y = yAxis.getData();
				yTitle = yAxis.getTitle();
				if (yTitle == null) {
					yTitle = yAxis.getShortName();
				}
				yUnits = yAxis.getUnitsString();
			}
			if (axes != null && axes.size() > 0) {
				IAxis xAxis = axes.get(axes.size() - 1);
				x = xAxis.getData();
				xTitle = xAxis.getTitle();
				if (xTitle == null) {
					xTitle = xAxis.getShortName();
				}
				xUnits = xAxis.getUnitsString();
			}
			setData(x, y, z);
			String title = nxDataset.getTitle();
			setTitle(title);
			setXTitle(xTitle);
			setXUnits(xUnits);
			setYTitle(yTitle);
			setYUnits(yUnits);
			setZUnits(zUnits);
		}
		
	}
	
	public INXDataset getNXDataset() {
		return nxDataset;
	}
}
