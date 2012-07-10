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

import java.util.List;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.exception.IndexOutOfBoundException;


/**
 * @author nxi
 * Created on 07/07/2008
 */
public class PlotIndex {

	private IIndex dataIndex;
	private List<IIndex> axisIndexList;
	
	public PlotIndex(IIndex dataIndex, List<IIndex> axisIndexList){
		this.dataIndex = dataIndex;
		this.axisIndexList = axisIndexList;
	}
	
	/** Get the number of dimensions in the array. */
	public int getRank(){
		return dataIndex.getRank();
	}

	/** Get the shape: length of array in each dimension. */
	public int[] getShape(){
		return dataIndex.getShape();
	}

	/** Get the total number of elements in the array. */
	public long getSize(){
		return dataIndex.getSize();
	}

	/** Get the current element's index into the 1D backing array. */
	public int currentElement(){
		return (int) dataIndex.currentElement();
	}

	/** Set the current element's index. General-rank case.
	 * @return this, so you can use A.get(i.set(i))
	 * @throws IndexOutOfBoundException 
	 * @exception ArrayIndexOutOfBoundsException if index.length != rank.
	 */
	public PlotIndex set(int[] index) throws IndexOutOfBoundException{
		
		try {
			dataIndex.set(index);
			int axisIndexCounter = 0;
			for (IIndex axisIndex : axisIndexList){
				axisIndex.set(index[axisIndexCounter ++]);
			}
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		}
		return this;
	}

	/** set current element at dimension dim to v 
	 * @throws IndexOutOfBoundException */
	public void setDim(int dim, int value) throws IndexOutOfBoundException{
		try {
			dataIndex.setDim(dim, value);
			axisIndexList.get(dim).set(value);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		}
	}

	/** set current element at dimension 0 to v
	    @return this, so you can use A.get(i.set(i)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set0(int v) throws IndexOutOfBoundException{
		try {
			dataIndex.set0(v);
			axisIndexList.get(0).set(v);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;
	}

	/** set current element at dimension 1 to v
	    @return this, so you can use A.get(i.set(i)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set1(int v) throws IndexOutOfBoundException{
		try {
			dataIndex.set1(v);
			axisIndexList.get(1).set(v);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;
	}

	/** set current element at dimension 2 to v
	    @return this, so you can use A.get(i.set(i)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set2(int v) throws IndexOutOfBoundException{
		try {
			dataIndex.set2(v);
			axisIndexList.get(2).set(v);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;
	}

	/** set current element at dimension 3 to v
	    @return this, so you can use A.get(i.set(i)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set3(int v) throws IndexOutOfBoundException{
		try {
			dataIndex.set3(v);
			axisIndexList.get(3).set(v);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;

	}

	/** set current element at dimension 4 to v
	    @return this, so you can use A.get(i.set(i)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set4(int v) throws IndexOutOfBoundException{
		try {
			dataIndex.set4(v);
			axisIndexList.get(4).set(v);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;

	}

	/** set current element at dimension 5 to v
	    @return this, so you can use A.get(i.set(i)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set5(int v) throws IndexOutOfBoundException{
		try {
			dataIndex.set5(v);
			axisIndexList.get(5).set(v);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;

	}

	/** set current element at dimension 6 to v
	    @return this, so you can use A.get(i.set(i)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set6(int v) throws IndexOutOfBoundException{
		try {
			dataIndex.set6(v);
			axisIndexList.get(6).set(v);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;

	}

	/** set current element at dimension 0 to v0
	    @return this, so you can use A.get(i.set(i)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set(int v0) throws IndexOutOfBoundException{
		try{
			dataIndex.set(v0);
			axisIndexList.get(0).set(v0);
		}catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		}
		return this;
	}

	/** set current element at dimension 0,1 to v0,v1
	    @return this, so you can use A.get(i.set(i,j)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set(int v0, int v1) throws IndexOutOfBoundException{
		try {
			dataIndex.set(v0, v1);
			axisIndexList.get(0).set(v0);
			axisIndexList.get(1).set(v1);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;
	}

	/** set current element at dimension 0,1,2 to v0,v1,v2
	    @return this, so you can use A.get(i.set(i,j,k)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set(int v0, int v1, int v2) throws IndexOutOfBoundException{
		try {
			dataIndex.set(v0, v1);
			axisIndexList.get(0).set(v0);
			axisIndexList.get(1).set(v1);
			axisIndexList.get(2).set(v2);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;		
	}

	/** set current element at dimension 0,1,2,3 to v0,v1,v2,v3
	    @return this, so you can use A.get(i.set(i,j,k,l)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set(int v0, int v1, int v2, int v3) throws IndexOutOfBoundException{
		try {
			dataIndex.set(v0, v1);
			axisIndexList.get(0).set(v0);
			axisIndexList.get(1).set(v1);
			axisIndexList.get(2).set(v2);
			axisIndexList.get(3).set(v3);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;
	}

	/** set current element at dimension 0,1,2,3,4 to v0,v1,v2,v3,v4
	    @return this, so you can use A.get(i.set(i,j,k,l,m)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set(int v0, int v1, int v2, int v3, int v4) throws IndexOutOfBoundException{
		try {
			dataIndex.set(v0, v1);
			axisIndexList.get(0).set(v0);
			axisIndexList.get(1).set(v1);
			axisIndexList.get(2).set(v2);
			axisIndexList.get(3).set(v3);
			axisIndexList.get(4).set(v4);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;
	}

	/** set current element at dimension 0,1,2,3,4,5 to v0,v1,v2,v3,v4,v5
	    @return this, so you can use A.get(i.set(i,j,k,l,m,n)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set(int v0, int v1, int v2, int v3, int v4, int v5) throws IndexOutOfBoundException{
		try {
			dataIndex.set(v0, v1);
			axisIndexList.get(0).set(v0);
			axisIndexList.get(1).set(v1);
			axisIndexList.get(2).set(v2);
			axisIndexList.get(3).set(v3);
			axisIndexList.get(4).set(v4);
			axisIndexList.get(5).set(v5);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;
	}

	/** set current element at dimension 0,1,2,3,4,5,6 to v0,v1,v2,v3,v4,v5,v6
	    @return this, so you can use A.get(i.set(i,j,k,l,m,n,p)) 
	 * @throws IndexOutOfBoundException */
	public PlotIndex set(int v0, int v1, int v2, int v3, int v4, int v5, int v6) throws IndexOutOfBoundException{
		try {
			dataIndex.set(v0, v1);
			axisIndexList.get(0).set(v0);
			axisIndexList.get(1).set(v1);
			axisIndexList.get(2).set(v2);
			axisIndexList.get(3).set(v3);
			axisIndexList.get(4).set(v4);
			axisIndexList.get(5).set(v5);
			axisIndexList.get(6).set(v6);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		};
		return this;
	}

	/**
	 * Override toString() method
	 * @return String type
	 * Created on 18/06/2008
	 */
	public String toString(){
		String result = dataIndex.toString() + "\n";
		for (IIndex index : axisIndexList){
			result += index.toString() + "\n";
		}
		return result;
	}

	/**
	 * Return the current location of the index. 
	 * @return java array of integer
	 * Created on 18/06/2008
	 */
	public int[] getCurrentCounter(){
		return dataIndex.getCurrentCounter();
	}

	//////////////////////////////////////////////////////////////
	/**
	 * Set the name of one of the indices.
	 * @param dim which index?
	 * @param indexName name of index
	 */
	public void setIndexName(int dim, String indexName){
		dataIndex.setIndexName(dim, indexName);
		axisIndexList.get(dim).setIndexName(0, indexName);
	}

	/**
	 * Get the name of one of the indices.
	 * @param dim which index?
	 * @return name of index, or null if none.
	 */
	public String getIndexName(int dim){
		return dataIndex.getIndexName(dim);
	}

	/**
	 * Make a clone the plot index. It will be a new index that independent from the old one. 
	 * Both the indexes must serve for the same plot. 
	 * @param from in Plot type
	 * @return new PlotIndex object
	 * @throws SignalNotAvailableException
	 * @throws IndexOutOfBoundException
	 * Created on 15/07/2008
	 */
	public PlotIndex cloneFrom(Plot from) throws SignalNotAvailableException, 
	IndexOutOfBoundException{
		PlotIndex index = from.getIndex();
		index.set(getCurrentCounter());
		return index;
	}
	
	/**
	 * Return the DataIndex of the plot. 
	 * @return GDM Index object
	 * Created on 15/07/2008
	 */
	public IIndex getDataIndex(){
		return dataIndex;
	}
	
	/**
	 * Return the axis index of the plot in a certain dimension. 
	 * @param dimension a integer value
	 * @return GDM Index object
	 * @throws IndexOutOfBoundException
	 * Created on 15/07/2008
	 */
	public IIndex getAxisIndex(int dimension) throws IndexOutOfBoundException{
		IIndex index = null;
		try {
			index = axisIndexList.get(dimension);
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		}
		return index;
	}
}
