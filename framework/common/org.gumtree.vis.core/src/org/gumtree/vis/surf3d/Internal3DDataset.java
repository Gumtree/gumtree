/**
 * 
 */
package org.gumtree.vis.surf3d;

import javax.vecmath.Color3b;

import org.freehep.j3d.plot.Binned2DData;
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IXYZDataset;

/**
 * @author nxi
 *
 */
public class Internal3DDataset implements Binned2DData {
	
	private static final double LOG_INPUT_START = 1.0 / (ColorScale.DIVISION_COUNT + 1);
	private static final double LOG_INPUT_WIDTH = 1 - LOG_INPUT_START;
	private static final double LOG_OUTPUT_START = Math.log(LOG_INPUT_START);
	private static final double LOG_OUTPUT_WIDTH = - LOG_OUTPUT_START;
	private ColorScale colorScale = ColorScale.Rainbow;
	private IXYZDataset xyzDataset;
	private boolean isLogScale = false;

	public Internal3DDataset(IXYZDataset xyzDataset) {
		this.xyzDataset = xyzDataset;
	}
	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#xBins()
	 */
	@Override
	public int xBins() {
		return xyzDataset.getXSize(0);
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#yBins()
	 */
	@Override
	public int yBins() {
		return xyzDataset.getYSize(0);
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#xMin()
	 */
	@Override
	public float xMin() {
		return (float) xyzDataset.getXMin();
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#xMax()
	 */
	@Override
	public float xMax() {
		return (float) xyzDataset.getXMax();
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#yMin()
	 */
	@Override
	public float yMin() {
		return (float) xyzDataset.getYMin();
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#yMax()
	 */
	@Override
	public float yMax() {
		return (float) xyzDataset.getYMax();
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#zAt(int, int)
	 */
	@Override
	public float zAt(int xIndex, int yIndex) {
		return (float) xyzDataset.getZofXY(xIndex, yIndex);
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#colorAt(int, int)
	 */
	@Override
	public Color3b colorAt(int xIndex, int yIndex) {
		double value = zAt(xIndex, yIndex) / zMax();
		if (isLogScale) {
			value = (Math.log((value) * LOG_INPUT_WIDTH + LOG_INPUT_START) 
					- LOG_OUTPUT_START) / LOG_OUTPUT_WIDTH;
		} 
		double[] rgb = getColorScale().getColorRGB(value);
		return new Color3b((byte) (rgb[0] * 255), (byte) (rgb[1] * 255), (byte) (rgb[2] * 255));
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#zMin()
	 */
	@Override
	public float zMin() {
		return (float) xyzDataset.getZMin();
	}

	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.Binned2DData#zMax()
	 */
	@Override
	public float zMax() {
		return (float) xyzDataset.getZMax();
	}

	/**
	 * @return the dataset
	 */
	public IXYZDataset getXYZDataset() {
		return xyzDataset;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setXYZDataset(IXYZDataset dataset) {
		this.xyzDataset = dataset;
	}
	/**
	 * @return the colorScale
	 */
	public ColorScale getColorScale() {
		return colorScale;
	}
	/**
	 * @param colorScale the colorScale to set
	 */
	public void setColorScale(ColorScale colorScale) {
		this.colorScale = colorScale;
	}
	/**
	 * @return the isLogScale
	 */
	public boolean isLogScale() {
		return isLogScale;
	}
	/**
	 * @param isLogScale the isLogScale to set
	 */
	public void setLogScale(boolean isLogScale) {
		this.isLogScale = isLogScale;
	}

}
