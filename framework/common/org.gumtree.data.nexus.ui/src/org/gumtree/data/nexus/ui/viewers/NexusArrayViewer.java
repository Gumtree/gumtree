/**
 * 
 */
package org.gumtree.data.nexus.ui.viewers;

import java.io.IOException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.ui.viewers.ArrayViewer;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.gdm.utils.Factory;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.IPlot1D;
import org.gumtree.vis.interfaces.IXYZDataset;

/**
 * @author nxi
 *
 */
public class NexusArrayViewer extends ArrayViewer {

	private List<IAxis> axes;
	private IArray variance;
	
	/**
	 * @param parent
	 * @param style
	 */
	public NexusArrayViewer(Composite parent, int style) {
		super(parent, style);
	}

	public void setAxes(List<IAxis> axes) {
		this.axes = axes;
	}

	@Override
	protected IDataset createPlotDataset() throws ShapeNotMatchException {
		if (axes == null || axes.size() == 0) {
			return super.createPlotDataset();
		} else {
			try {
				int rank = getArray().getRank();
				if (rank == 1) {
					IAxis axis = axes.get(axes.size() - 1);
					XYErrorDataset dataset = new XYErrorDataset();
					IArray error = null;
					if (variance != null) {
						error = variance.getArrayMath().toSqrt().getArray();
					}
					dataset.addSeries(Factory.createArraySeries(axis.getShortName(), 
							axis.getData(), getArray(), error));
					dataset.setXTitle(axis.getShortName());
					dataset.setXUnits(axis.getUnitsString());
					return dataset;
				}
				IArray xArray = null;
				IArray yArray = null;
				String xTitle = null;
				String yTitle = null;
				String xUnits = null;
				String yUnits = null;
				if (axes.size() >= 1) {
					IAxis xAxis = axes.get(axes.size() - 1);
					xArray = xAxis.getData();
					xTitle = xAxis.getShortName();
					xUnits = xAxis.getUnitsString();
				}
				if (axes.size() >= 2) {
					IAxis yAxis = axes.get(axes.size() - 2);
					yArray = yAxis.getData();
					yTitle = yAxis.getShortName();
					yUnits = yAxis.getUnitsString();
				}
				IXYZDataset dataset;
				if (rank == 2){
					dataset = Factory.createHist2DDataset(xArray, yArray, getArray());
					return dataset;
				} else {
					int[] shape = getArray().getShape();
					int index = 0;
					if (getArray().getRank() < 2) {
						throw new ShapeNotMatchException("short of dimension, rank = " + getArray().getRank());
					}
					if (getArray().getRank() == 2){
						if (getFrameId() == 0 && getLayerId() == 0) {
							dataset =  Factory.createHist2DDataset(xArray, yArray, getArray());
						} else {
							throw new ShapeNotMatchException("index out of bound, " + 
									getFrameId() * getLayerId() + " / " + 1);
						}
					} else {
						index = shape[shape.length - 3] * getFrameId() + getLayerId();
						dataset = Factory.create2DDataset(xArray, yArray, getArray(), index);
					}
				}
				dataset.setXTitle(xTitle);
				dataset.setXUnits(xUnits);
				dataset.setYTitle(yTitle);
				dataset.setYUnits(yUnits);
				return dataset;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public IArray getVariance() {
		return variance;
	}

	public void setVariance(IArray variance) {
		this.variance = variance;
	}

	@Override
	protected void setErrorBarEnabled(IPlot plot) {
		if (plot instanceof IPlot1D) {
			((IPlot1D) plot).setErrorBarEnabled(variance != null);
		}
	}
}
