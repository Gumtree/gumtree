/**
 * 
 */
package org.gumtree.vis.nexus.utils;

import java.io.IOException;

import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.nexus.dataset.Hist2DNXDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;

/**
 * @author nxi
 *
 */
public class NXFactory {

	public static Hist2DNXDataset createHist2DDataset(INXDataset nxDataset) 
	throws IOException, ShapeNotMatchException {
		Hist2DNXDataset dataset = new Hist2DNXDataset();
		if (nxDataset != null) {
			dataset.setData(nxDataset);
		}
		return dataset;
	}
	
	public static NXDatasetSeries createNexusSeries(String name, INXDataset nxDataset) 
	throws ShapeNotMatchException, IOException {
		NXDatasetSeries series = new NXDatasetSeries(name);
		if (nxDataset != null) {
			series.setData(nxDataset);
		}
		return series;
	}
	
	public static XYErrorDataset createSingleXYDataset(String name, INXDataset nxDataset) 
	throws ShapeNotMatchException, IOException {
		XYErrorDataset dataset = new XYErrorDataset();
		if (nxDataset != null) {
			dataset.addSeries(createNexusSeries(name, nxDataset));
		}
		dataset.setTitle(name);
		return dataset;
	}
}
