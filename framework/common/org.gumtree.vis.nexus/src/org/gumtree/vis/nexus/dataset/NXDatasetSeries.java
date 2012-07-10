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
import org.gumtree.data.nexus.IVariance;
import org.gumtree.vis.gdm.dataset.ArraySeries;

/**
 * @author nxi
 *
 */
public class NXDatasetSeries extends ArraySeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4238650232821379106L;
	private INXDataset nxDataset;
	/**
	 * @param key
	 */
	public NXDatasetSeries(Comparable key) {
		super(key);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param key
	 * @param description
	 */
	public NXDatasetSeries(Comparable key, String description) {
		super(key, description);
		// TODO Auto-generated constructor stub
	}

	public void setData(INXDataset nxDataset)
			throws ShapeNotMatchException, IOException {
		this.nxDataset = nxDataset;
		INXdata nxData = nxDataset.getNXroot().getDefaultData();
		if (nxData != null) {
			IArray x = null;
			IArray y = nxData.getSignal().getData();
			IArray e = null;
			List<IAxis> axes = nxData.getAxisList();
			if (axes != null && axes.size() > 0) {
				IAxis xAxis = axes.get(axes.size() - 1);
				x = xAxis.getData();
			}
			IVariance variance = nxData.getVariance();
			if (variance != null) {
				e = variance.getData().getArrayMath().toSqrt().getArray();
			}
			setData(x, y, e, true);
		}
	}

	/**
	 * @return the nxDataset
	 */
	public INXDataset getNxDataset() {
		return nxDataset;
	}
}
