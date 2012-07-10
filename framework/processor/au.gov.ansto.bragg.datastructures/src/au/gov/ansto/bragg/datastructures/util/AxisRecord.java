/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.datastructures.util;

import java.io.IOException;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.plot.Axis;

public final class AxisRecord {

	private IArray centres;
	private IArray bounds;
	// private Array widths;
	final private Axis  scale;
	final private int   axisIndex;
	private long        length;
	
	private boolean usesScaleCentres;
	
	/** 
	 * 
	 * @param scale
	 * @param index
	 * @param dataShape
	 * @throws IOException
	 * 
	 * @deprecated
	 */
	public AxisRecord (Axis scale, int index, int[] dataShape) throws IOException {
		this.scale = scale;
		this.axisIndex = index;
		setAxisBins(index, dataShape);
	}
	
	private AxisRecord (Axis scale, int index) {
		this.scale = scale;
		this.axisIndex = index;
	}

	static public AxisRecord createRecord(Axis scale, int index, int[] dataShape) throws IOException {
		AxisRecord me = new AxisRecord(scale,index);
		me.setAxisBins(index, dataShape);
		me.length = me.centres().getSize();
		return me;	
	}
	
	// Assumes bins contiguous
	private void setAxisBins(int index, int[] dataShape) throws IOException {
		int dataWidth = dataShape[index];
		int scaleLength = scale.getShape()[0];
		this.usesScaleCentres = (scaleLength == dataWidth);
		
		double centre;
		double centreNext;
		double binWidth;
		
		IArray scaleData = this.scale.getData();
		IArrayIterator scaleIter = scaleData.getIterator();
		
		//this.widths = Factory.createArray(Double.TYPE, new int[]{dataWidth});
		//ArrayIterator widthIter = widths.getIterator();
		double scaleValue = scaleIter.getDoubleNext();

		if(usesScaleCentres) { // scale is nominal bin centres
			this.centres = scaleData;
			this.bounds = Factory.createArray(Double.TYPE, new int[]{dataWidth+1});
			IArrayIterator boundsIter = bounds.getIterator();
			
			centre = scaleValue;
			centreNext = scaleIter.getDoubleNext();
			binWidth = centreNext - centre;
//			scaleIter.setDoubleCurrent(binWidth);
			if (boundsIter.hasNext())
				boundsIter.next().setDoubleCurrent(centre - binWidth/2.0);
			
			while (scaleIter.hasNext()) {
				centre = centreNext;				
				centreNext = scaleIter.getDoubleNext();
				binWidth = centreNext - centre;
				boundsIter.next().setDoubleCurrent(centre + binWidth/2.0);
			}
			
			centre = centreNext;				
			boundsIter.next().setDoubleCurrent(centre + binWidth/2.0);
						
		} else { // scale is bin boundaries (contiguous)
			this.bounds = scaleData;
			this.centres = Factory.createArray(Double.TYPE, new int[]{dataWidth});
			IArrayIterator centreIter = centres.getIterator();			
			double lo = scaleValue;
			double hi = scaleIter.getDoubleNext();
			binWidth = hi - lo; //NOT abs() for binWidth, so as to preserve direction of gradient
			centreIter.next().setDoubleCurrent(middle(hi,lo));  // lo + binWidth/2.0
			while (scaleIter.hasNext()) {
				lo = hi;
				hi = scaleIter.getDoubleNext();
				centreIter.next().setDoubleCurrent(middle(hi,lo));
			}
		}
	}

	private double middle(double s1,double s2) {
		return (s1 + s2)/2.0;
	}

	public double centre(int index) {
		IIndex ima = centres.getIndex();
		double result = Double.NaN;
		if (index < ima.getShape()[0]) {
			result = centres.getDouble(ima.set(index));
		}
		return result;
	}
	
	public double width(int index) {
		IIndex ima = bounds.getIndex();
		double result = Double.NaN;
		if (index < (ima.getShape()[0]-1)) {
			result = Math.abs(bounds.getDouble(ima.set(index)) 
							- bounds.getDouble(ima.set(index+1)));
		}
		return result;
	}
	
	public double upperBound(int index) {
		IIndex ima = bounds.getIndex();
		return Math.max(bounds.getDouble(ima.set(index)),
						bounds.getDouble(ima.set(index+1)));
	}
	
	public double lowerBound(int index) {
		IIndex ima = bounds.getIndex();
		return Math.min(bounds.getDouble(ima.set(index)),
						bounds.getDouble(ima.set(index+1)));
	}

	public IArray centres() {
		return this.centres;
	}
	
	public IArray bounds() {
		return this.bounds;
	}
	
	public int binIndex(double value) {
		// TODO: Implement return of bin index for placement of value
		return 0;
	}
	
	public int getAxisIndex() {
		return this.axisIndex;
	}
	
	public long length() {
		return this.length;
	}
	
	public String getUnits(){
		return scale.getUnitsString();
	}
}

