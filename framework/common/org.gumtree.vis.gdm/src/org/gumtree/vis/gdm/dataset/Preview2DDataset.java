/**
 * 
 */
package org.gumtree.vis.gdm.dataset;

import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.vis.interfaces.IPreview2DDataset;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 * @author nxi
 *
 */
public class Preview2DDataset implements IPreview2DDataset {

	private IArray storage;
	private double max;
	private double min;
	private int xSize;
	private int ySize;
	private int numberOfFrames;
	/**
	 * 
	 */
	public Preview2DDataset() {
		// TODO Auto-generated constructor stub
	}

	public void setStorage(IArray array) {
		int rank = array.getRank();
		if (rank < 2) {
			throw new IllegalArgumentException("array dimension must be 2 and above");
		}
		int[] shape = array.getShape();
		xSize = shape[rank - 1];
		ySize = shape[rank - 2];
		numberOfFrames = 1;
		for (int i = rank - 3; i >= 0; i--) {
			numberOfFrames *= shape[i];
		}
		
		int[] newShape = new int[]{numberOfFrames, ySize, xSize};
		try {
			storage = array.getArrayUtils().reshape(newShape).getArray();
		} catch (ShapeNotMatchException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException("array dimension is not acceptable");
		}
		max = array.getArrayMath().getMaximum();
		min = array.getArrayMath().getMinimum();
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#getXTitle()
	 */
	@Override
	public String getXTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#setXTitle(java.lang.String)
	 */
	@Override
	public void setXTitle(String xTitle) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#getYTitle()
	 */
	@Override
	public String getYTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#setYTitle(java.lang.String)
	 */
	@Override
	public void setYTitle(String yTitle) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#getXUnits()
	 */
	@Override
	public String getXUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#setXUnits(java.lang.String)
	 */
	@Override
	public void setXUnits(String xUnits) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#getYUnits()
	 */
	@Override
	public String getYUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#setYUnits(java.lang.String)
	 */
	@Override
	public void setYUnits(String yUnits) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#getTitle()
	 */
	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IDataset#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getDomainOrder()
	 */
	@Override
	public DomainOrder getDomainOrder() {
		// TODO Auto-generated method stub
		return DomainOrder.ASCENDING;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	@Override
	public int getItemCount(int series) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	@Override
	public Number getX(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getXValue(int, int)
	 */
	@Override
	public double getXValue(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	@Override
	public Number getY(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.xy.XYDataset#getYValue(int, int)
	 */
	@Override
	public double getYValue(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#getSeriesCount()
	 */
	@Override
	public int getSeriesCount() {
		return storage == null ? 0 : 1;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#getSeriesKey(int)
	 */
	@Override
	public Comparable getSeriesKey(int series) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.SeriesDataset#indexOf(java.lang.Comparable)
	 */
	@Override
	public int indexOf(Comparable seriesKey) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#addChangeListener(org.jfree.data.general.DatasetChangeListener)
	 */
	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#removeChangeListener(org.jfree.data.general.DatasetChangeListener)
	 */
	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#getGroup()
	 */
	@Override
	public DatasetGroup getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Dataset#setGroup(org.jfree.data.general.DatasetGroup)
	 */
	@Override
	public void setGroup(DatasetGroup group) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPreview2DDataset#getNumberOfFrames()
	 */
	@Override
	public int getNumberOfFrames(int series) {
		// TODO Auto-generated method stub
		return numberOfFrames;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPreview2DDataset#getXSize(int)
	 */
	@Override
	public int getXSize(int series) {
		// TODO Auto-generated method stub
		return xSize;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPreview2DDataset#getYSize(int)
	 */
	@Override
	public int getYSize(int series) {
		return ySize;
	}

	@Override
	public double getZMax(int series) {
		return max;
	}

	@Override
	public double getZMin(int series) {
		return min;
	}

	@Override
	public double getZValue(int series, int frameIndex, int verticalIndex,
			int horizontalIndex) {
		IIndex index = storage.getIndex();
		try {
		index.set(frameIndex, verticalIndex, horizontalIndex);
		} catch (Exception e) {
			System.out.println(frameIndex + "," + verticalIndex + "," + horizontalIndex);
		}
		return storage.getDouble(index);
	}
}
