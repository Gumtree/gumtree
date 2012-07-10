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
package au.gov.ansto.bragg.datastructures.core.plot;

import java.io.IOException;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IArrayIterator;

import au.gov.ansto.bragg.datastructures.core.exception.IndexOutOfBoundException;

/**
 * Iterator that iterates through a Plot object.
 * It will iterates the data storage with an ArrayIterator wrapped inside. 
 * The iterator can be used to retrieve values with axis information.
 * @author nxi
 * Created on 08/07/2008
 */
public class PlotIterator implements IArrayIterator {

	IArrayIterator dataIterator;
	Plot plot;
	
	/**
	 * Constructor with a plot argument. The plot iterator must be constructed in this way to 
	 * function well. 
	 * @param plot
	 * @throws IOException
	 */
	public PlotIterator(Plot plot) throws IOException{
		this.plot = plot;
		dataIterator = plot.findSignalArray().getIterator();
	}
	
	/**
	 * Get the current iterator value as a boolean type.
	 * @return boolean value
	 */
//	public boolean getBooleanCurrent() {
//		return dataIterator.getBooleanCurrent();
//	}

	/**
	 * Move the iterator to next and get the value as boolean type.
	 * @return boolean value
	 */
	public boolean getBooleanNext() {
		return dataIterator.getBooleanNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getByteCurrent()
	 */
//	public byte getByteCurrent() {
//		return dataIterator.getByteCurrent();
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getByteNext()
	 */
	public byte getByteNext() {
		return dataIterator.getByteNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getCharCurrent()
	 */
//	public char getCharCurrent() {
//		return dataIterator.getCharCurrent();
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getCharNext()
	 */
	public char getCharNext() {
		return dataIterator.getCharNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getCurrentCounter()
	 */
	public int[] getCounter() {
		return dataIterator.getCounter();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getDoubleCurrent()
	 */
//	public double getDoubleCurrent() {
//		return dataIterator.getDoubleCurrent();
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getDoubleNext()
	 */
	public double getDoubleNext() {
		return dataIterator.getDoubleNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getFloatCurrent()
	 */
//	public float getFloatCurrent() {
//		return dataIterator.getFloatCurrent();
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getFloatNext()
	 */
	public float getFloatNext() {
		return dataIterator.getFloatNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getIntCurrent()
	 */
//	public int getIntCurrent() {
//		return dataIterator.getIntCurrent();
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getIntNext()
	 */
	public int getIntNext() {
		return dataIterator.getIntNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getLongCurrent()
	 */
//	public long getLongCurrent() {
//		return dataIterator.getLongCurrent();
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getLongNext()
	 */
	public long getLongNext() {
		return dataIterator.getLongNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getObjectCurrent()
	 */
//	public Object getObjectCurrent() {
//		return dataIterator.getObjectCurrent();
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getObjectNext()
	 */
	public Object getObjectNext() {
		return dataIterator.getObjectNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getShortCurrent()
	 */
//	public short getShortCurrent() {
//		return dataIterator.getShortCurrent();
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#getShortNext()
	 */
	public short getShortNext() {
		return dataIterator.getShortNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#hasNext()
	 */
	public boolean hasNext() {
		return dataIterator.hasNext();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#next()
	 */
	public PlotIterator next() {
		dataIterator.next();
		return this;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setBooleanCurrent(boolean)
	 */
	public void setBooleanCurrent(boolean val) {
		dataIterator.setBooleanCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setBooleanNext(boolean)
	 */
//	public void setBooleanNext(boolean val) {
//		dataIterator.setBooleanNext(val);
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setByteCurrent(byte)
	 */
	public void setByteCurrent(byte val) {
		dataIterator.setByteCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setByteNext(byte)
	 */
//	public void setByteNext(byte val) {
//		dataIterator.setByteNext(val);
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setCharCurrent(char)
	 */
	public void setCharCurrent(char val) {
		dataIterator.setCharCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setCharNext(char)
	 */
//	public void setCharNext(char val) {
//		dataIterator.setCharNext(val);
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setDoubleCurrent(double)
	 */
	public void setDoubleCurrent(double val) {
		dataIterator.setDoubleCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setDoubleNext(double)
	 */
//	public void setDoubleNext(double val) {
//		dataIterator.setDoubleNext(val);
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setFloatCurrent(float)
	 */
	public void setFloatCurrent(float val) {
		dataIterator.setFloatCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setFloatNext(float)
	 */
//	public void setFloatNext(float val) {
//		dataIterator.setFloatNext(val);
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setIntCurrent(int)
	 */
	public void setIntCurrent(int val) {
		dataIterator.setIntCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setIntNext(int)
	 */
//	public void setIntNext(int val) {
//		dataIterator.setIntNext(val);
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setLongCurrent(long)
	 */
	public void setLongCurrent(long val) {
		dataIterator.setLongCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setLongNext(long)
	 */
//	public void setLongNext(long val) {
//		dataIterator.setLongNext(val);
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setObjectCurrent(java.lang.Object)
	 */
	public void setObjectCurrent(Object val) {
		dataIterator.setObjectCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setObjectNext(java.lang.Object)
	 */
//	public void setObjectNext(Object val) {
//		dataIterator.setObjectNext(val);
//	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setShortCurrent(short)
	 */
	public void setShortCurrent(short val) {
		dataIterator.setShortCurrent(val);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.data.gdm.core.ArrayIterator#setShortNext(short)
	 */
//	public void setShortNext(short val) {
//		dataIterator.setShortNext(val);
//	}

	public Point getPointCurrent() throws SignalNotAvailableException {
		PlotIndex index = plot.getIndex();
		try {
			index.set(getCounter());
		} catch (IndexOutOfBoundException e) {
			throw new SignalNotAvailableException(e);
		}
		return plot.getPoint(index);
	}
	
	public Point getPointNext() throws SignalNotAvailableException {
		PlotIndex index = plot.getIndex();
		try {
			next();
			index.set(getCounter());
		} catch (IndexOutOfBoundException e) {
			throw new SignalNotAvailableException(e);
		}
		return plot.getPoint(index);		
	}
	
	public double[] getCoordinateCurrent() throws IndexOutOfBoundException{
		try {
			return getPointCurrent().getCoordinate();
		} catch (SignalNotAvailableException e) {
			throw new IndexOutOfBoundException(e);
		}
	}
	
	public double[] getCoordinateNext() throws IndexOutOfBoundException{
		try {
			return getPointNext().getCoordinate();
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		}
	}

	public String getFactoryName() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasCurrent() {
		// TODO Auto-generated method stub
		return false;
	}
}
