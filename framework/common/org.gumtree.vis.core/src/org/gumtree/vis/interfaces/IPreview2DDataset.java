/**
 * 
 */
package org.gumtree.vis.interfaces;

/**
 * @author nxi
 *
 */
public interface IPreview2DDataset extends IDataset {

	public abstract int getNumberOfFrames(int series);
	
	public abstract int getXSize(int series);
	
	public abstract int getYSize(int series);
	
	public abstract double getZMax(int series);

	public abstract double getZValue(int series, int frameIndex, int verticalIndex,
			int horizontalIndex);

	public abstract double getZMin(int series);
	
	
//	public abstract 
}
