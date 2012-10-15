/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.interfaces;

/**
 * @brief The IIndex interface defines a view on a IArray.
 * 
 * The IIndex fully describes the IArray's visible part. Whatever the storage is, it defines
 * the shape of the visible part of the storage, its origin, how two consecutive
 * cells should be considered. Manages the stride and the way the parsing will be done.
 * <br>
 * The IIndex can describe the whole array storage or just a portion of it.
 * It can be used to refer to a particular element of an IArray.
 * 
 * @author nxi
 * 
 */
public interface IIndex extends IModelObject, Cloneable {

	/**
	 * Get the number of dimensions in the array.
	 * 
	 * @return integer value
	 */
	int getRank();

	/**
	 * Get the shape: length of array in each dimension.
	 * 
	 * @return array of integer
	 */
	int[] getShape();
	
    /**
     * Get the origin: first index of array in each dimension.
     * 
     * @return array of integer
     */
    int[] getOrigin();

	/**
	 * Get the total number of elements in the array.
	 * 
	 * @return long value
	 */
	long getSize();

    /**
     * Get the stride: for each dimension number elements to jump in the array between two 
     * consecutive element of the same dimension
     *  
     * @return array of integer
     */
    long[] getStride();
    
	/**
	 * Get the current element's index into the 1D backing array.
	 * 
	 * @return integer value
	 */
	long currentElement();

	/**
	 * Get the last element's index into the 1D backing array.
	 * 
	 * @return integer value
	 */
	long lastElement();
	
	/**
     * Set the current element's index.
	 * 
     * @param index array of integer
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set(int[] index);

	/**
     * Set current element at dimension dim to value.
	 * 
     * @param dim integer value
     * @param value integer value
	 */
	void setDim(int dim, int value);
    
    /**
     * Set the origin on each dimension for this index
     * 
     * @param origin array of integers
     */
    void setOrigin(int[] origin);
    
    /**
     * Set the given shape for this index
     * 
     * @param shape array of integers
     */
    void setShape(int[] shape);

    /**
     * Set the stride for this index. The stride is the number of 
     * cells between two consecutive cells in the same dimension.
     * 
     * @param stride array of integers
     */
    void setStride(long[] stride);
    
	/**
     * Set current element at dimension 0 to v.
	 * 
     * @param v integer value
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set0(int v);

	/**
     * Set current element at dimension 1 to v.
	 * 
     * @param v integer value
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set1(int v);

	/**
     * Set current element at dimension 2 to v.
	 * 
     * @param v integer value
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set2(int v);

	/**
     * Set current element at dimension 3 to v.
	 * 
     * @param v integer value
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set3(int v);

	/**
     * Set current element at dimension 4 to v.
	 * 
     * @param v integer value
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set4(int v);

	/**
     * Set current element at dimension 5 to v.
	 * 
     * @param v integer value
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set5(int v);

	/**
     * Set current element at dimension 6 to v.
	 * 
     * @param v integer value
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set6(int v);

	/**
     * Set current element at dimension 0 to v0.
	 * 
     * @param v0 integer value
	 * @return this, so you can use A.get(i.set(i))
	 */
	IIndex set(int v0);

	/**
     * Set current element at dimension 0,1 to v0,v1.
	 * 
     * @param v0 integer value
     * @param v1 integer value
	 * @return this, so you can use A.get(i.set(i,j))
	 */
	IIndex set(int v0, int v1);

	/**
     * Set current element at dimension 0,1,2 to v0,v1,v2.
	 * 
     * @param v0 integer value
     * @param v1 integer value
     * @param v2 integer value
	 * @return this, so you can use A.get(i.set(i,j,k))
	 */
	IIndex set(int v0, int v1, int v2);

	/**
     * Set current element at dimension 0,1,2,3 to v0,v1,v2,v3.
	 * 
     * @param v0 integer value
     * @param v1 integer value
     * @param v2 integer value
     * @param v3 integer value
	 * @return this, so you can use A.get(i.set(i,j,k,l))
	 */
	IIndex set(int v0, int v1, int v2, int v3);

	/**
     * Set current element at dimension 0,1,2,3,4 to v0,v1,v2,v3,v4.
	 * 
     * @param v0 integer value
     * @param v1 integer value
     * @param v2 integer value
     * @param v3 integer value
     * @param v4 integer value
	 * @return this, so you can use A.get(i.set(i,j,k,l,m))
	 */
	IIndex set(int v0, int v1, int v2, int v3, int v4);

	/**
     * Set current element at dimension 0,1,2,3,4,5 to v0,v1,v2,v3,v4,v5.
	 * 
     * @param v0 integer value
     * @param v1 integer value
     * @param v2 integer value
     * @param v3 integer value
     * @param v4 integer value
     * @param v5 integer value
	 * @return this, so you can use A.get(i.set(i,j,k,l,m,n))
	 */
	IIndex set(int v0, int v1, int v2, int v3, int v4, int v5);

	/**
     * Set current element at dimension 0,1,2,3,4,5,6 to v0,v1,v2,v3,v4,v5,v6.
	 * 
     * @param v0 integer value
     * @param v1 integer value
     * @param v2 integer value
     * @param v3 integer value
     * @param v4 integer value
     * @param v5 integer value
     * @param v6 integer value
	 * @return this, so you can use A.get(i.set(i,j,k,l,m,n,p))
	 */
	IIndex set(int v0, int v1, int v2, int v3, int v4, int v5, int v6);

	/**
	 * String representation.
	 * 
	 * @return String object
	 */
	String toStringDebug();

	/**
     * Return the current location of the index on each dimension.
	 * 
     * @return java array of integer 
	 */
	int[] getCurrentCounter();

	/**
	 * Set the name of one of the indices.
	 * 
     * @param dim which index
     * @param indexName name of index
	 */
	void setIndexName(int dim, String indexName);

	/**
	 * Get the name of one of the indices.
	 * 
     * @param dim which index?
	 * @return name of index, or null if none.
	 */
	String getIndexName(int dim);

    /**
     * Remove all index with length one.
     * @return the new IIndex
     */
    IIndex reduce();

    /**
     * Eliminate the specified dimension.
     * 
     * @param dim dimension to eliminate: must be of length one, else IllegalArgumentException
     * @return the new index
     */
    IIndex reduce(int dim) throws IllegalArgumentException;
    
    IIndex clone() throws CloneNotSupportedException;

}
