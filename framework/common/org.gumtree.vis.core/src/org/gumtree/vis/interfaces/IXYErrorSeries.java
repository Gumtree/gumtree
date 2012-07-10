package org.gumtree.vis.interfaces;


public interface IXYErrorSeries {

	/* (non-Javadoc)
	 * @see org.jfree.data.general.Series#getItemCount()
	 */
	public abstract int getItemCount();

	public abstract Number getX(int item);

	public abstract Number getY(int item);

	public abstract double getYValue(int item);
	
	public abstract double getXValue(int item);
	
	public abstract double getMaxX();

	public abstract double getMinX();

	public abstract double getMaxY();

	public abstract double getMinY();

	public abstract int getItemFromX(double x);

	public abstract double getYError(int item);

	public abstract double getMinPositiveValue();

	public abstract String getKey();
}