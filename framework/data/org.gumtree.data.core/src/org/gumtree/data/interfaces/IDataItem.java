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

import java.io.IOException;
import java.util.List;

import org.gumtree.data.exception.DimensionNotSupportedException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;

/**
 * @brief The IDataItem interface defines a IContainer that carries data. 
 * 
 * A IDataItem is a logical container for data. It has a DataType, a set of 
 * Dimensions that define its array shape, and optionally a set of Attributes.
 * <br>
 * The data item is a descriptor of the underlying IArray. The former comes 
 * with all its metadata and location in data source. It associates the data
 * and all its contextual informations. Handling a data item doesn't mandatory
 * mean that the whole matrix it describes has been loaded.<br>
 * For performance concerns there is a dissociation between the descriptor and
 * the described data.
 * 
 * @author nxi
 */
public interface IDataItem extends IContainer, Cloneable {

	/**
     * Find an attribute by name, ignoring the case.
	 * 
     * @param name of the requested attribute
	 * @return the attribute, or null if not found
	 */
	IAttribute findAttributeIgnoreCase(String name);

	/**
	 * Find the index of the named Dimension in this DataItem.
	 * 
     * @param name of the dimension
	 * @return the index of the named Dimension, or -1 if not found.
	 */
	int findDimensionIndex(String name);

	/**
     * Create a new IDataItem that is a logical slice of this IDataItem, by fixing
	 * the specified dimension at the specified index value. This reduces rank
	 * by 1. No data is read until a read method is called on it.
	 * 
     * @param dimension which dimension to fix
     * @param value at what index value
     * @return a new IDataItem which is a logical slice of this DataItem.
	 * @throws InvalidRangeException
     * @deprecated use {@link IDataItem#getSlice(int, int)}
	 */
	IDataItem getASlice(int dimension, int value) throws InvalidRangeException;

	/**
	 * Get its parent Group, or null if its the root group.
	 * 
	 * @return GDM group object
	 */
	// [ANSTO][Tony][2012-05-02] Required by ANSTO code
	@Override
	IGroup getParentGroup();

	/**
	 * Get the root group of the tree that holds the current Group.
	 * 
	 * @return GDM Group Created on 18/06/2008
	 */
	// [ANSTO][Tony][2012-05-02] Required by ANSTO code
	@Override
	IGroup getRootGroup();

	/**
     * Read all the data for this IDataItem and return a memory resident IArray.
     * The IArray has the same element type and shape as the IDataItem.
     * 
     * @return the requested data in a memory-resident IArray.
     * @throws IOException I/O exception
     */
	IArray getData() throws IOException;

	/**
     * Read a section of the data for this IDataItem and return a memory resident
     * IArray. The IArray has the same element type as the DataItem. The size of
     * the IArray will be either smaller or equal to the DataItem.
	 * 
     * @param origin of the section in each dimension
     * @param shape of the section in each dimension
     * @return the requested data in a memory-resident IArray.
     * @throws IOException I/O exception
     * @throws InvalidRangeException invalid range
	 */
    IArray getData(int[] origin, int[] shape) throws IOException, InvalidRangeException;

	/**
     * Get the description of the DataItem. Default is to use description
     * attribute value.
	 * 
     * @return description string, or null if not found.
	 */
	String getDescription();

	/**
     * Get all dimensions (if several are available return a populated corresponding list)
     * of the data item, that are applied on the axis 'index'.
	 * 
     * @param index of the dimensions
     * @return list of requested IDimension
	 */
    List<IDimension> getDimensions(int index);
	
	/**
     * Get a list of all dimensions used by this IDataItem. The most slowly varying
     * (leftmost for Java and C programmers) dimension is first, the faster varying
     * is the last one. For scalar item, the list is empty.
	 * 
     * @return list of IDimension
	 */
	List<IDimension> getDimensionList();

	/**
	 * Get the list of Dimension names, space delineated.
	 * 
	 * @return String object
	 */
	String getDimensionsString();

	/**
     * Get the number of bytes for one element of this IDataItem. For DataItems
     * of primitive type, this is equal to getDataType().getSize(). Data items of
	 * String type does not know their size, so what they return is undefined.
	 * 
     * @return total number of bytes for <b>one element</b> of the IDataItem
	 */
	int getElementSize();

	/**
     * Display name of the IDataItem, plus the dimensions.
	 * 
	 * @return String object
	 */
	String getNameAndDimensions();

	/**
     * Fill the given buffer with name plus the dimensions.
	 * 
     * @param buf i/o StringBuffer 
     * @param longName display the long name
     * @param length display the length of each dimension
	 */
    void getNameAndDimensions(StringBuffer buf, boolean longName, boolean length);

	/**
     * Get shape as a list of IRange objects.
	 * 
     * @return list of IRanges, one for each Dimension.
	 */
	List<IRange> getRangeList();

	/**
     * Get the number of dimensions of the IDataItem.
	 * 
	 * @return integer value
	 */
	int getRank();

	/**
     * Create a new IDataItem that is a logical subsection of this IDataItem. No
	 * data is read until a read method is called on it.
	 * 
     * @param section list of IRange, with size equal to getRank(). Each Range
     *            corresponds to a dimension, and specifies the section of data
     *            to read in that dimension. A Range object may be null, which
	 *            means use the entire dimension.
     * @return a new IDataItem which is a logical section of this DataItem.
     * @throws InvalidRangeException invalid range
	 */
	IDataItem getSection(List<IRange> section) throws InvalidRangeException;

	/**
	 * Get index subsection as an array of Range objects, relative to the
	 * original variable. If this is a section, will reflect the index range
	 * relative to the original variable. If its a slice, it will have a rank
	 * different from this variable. Otherwise it will correspond to this
     * IDataItem's shape, ie match getRanges().
	 * 
	 * @return array of Ranges, one for each Dimension.
	 */
	List<IRange> getSectionRanges();

	/**
     * Get the shape: length of the IDataItem in each dimension.
	 * 
     * @return int array whose length is the rank of this and values are
     *         the dimensions length.
	 */
	int[] getShape();

	/**
     * Get the total number of elements in the IDataItem. If this is an unlimited
     * IDataItem, will return the current number of elements.
	 * 
     * @return total number of elements in the IDataItem.
	 */
	long getSize();

	/**
	 * If total data is less than SizeToCache in bytes, then cache.
	 * 
	 * @return integer value
	 */
	int getSizeToCache();

	/**
     * Create a new IDataItem that is a logical slice of this IDataItem, by fixing
	 * the specified dimension at the specified index value. This reduces rank
	 * by 1. No data is read until a read method is called on it.
	 * 
     * @param dim which dimension to fix
     * @param value at what index value
     * @return a new IDataItem which is a logical slice of this DataItem.
	 * @throws InvalidRangeException
	 *             invalid range
	 */
	IDataItem getSlice(int dim, int value) throws InvalidRangeException;

	/**
     * Get the class of the IDataItem's elements.
	 * 
	 * @return Class object
	 */
	Class<?> getType();

	/**
     * Get the unit as a string for the DataItem. Default is to use "units" attribute
	 * value
	 * 
	 * @return unit string, or null if not found.
	 */
	String getUnitsString();

	/**
     * Does this item have its data read and cached?
	 * 
	 * @return true or false
	 */
	boolean hasCachedData();

	/**
	 * Override Object.hashCode() to implement equals.
	 * 
	 * @return integer value
	 */
	int hashCode();

	/**
	 * Invalidate the data cache.
	 */
	void invalidateCache();

	/**
     * Will this IDataItem be cached when read. Set externally, or calculated
	 * based on total size < sizeToCache.
	 * 
	 * @return true is caching
	 */
	boolean isCaching();

	/**
	 * Is this variable is a member of a Structure?
	 * 
	 * @return boolean value
	 */
	boolean isMemberOfStructure();

	/**
     * Is this variable metadata?.
	 * 
	 * @return true or false
	 */
	boolean isMetadata();

	/**
     * Whether this is a scalar IDataItem (rank == 0).
	 * 
	 * @return true or false
	 */
	boolean isScalar();

	/**
     * Can this variable's size grow by the time?. This is equivalent to saying at least one
	 * of its dimensions is unlimited.
	 * 
     * @return boolean true if this IDataItem can grow
	 */
	boolean isUnlimited();

	/**
     * Is this IDataItem unsigned?. Only meaningful for byte, short, int, long
	 * types.
	 * 
	 * @return true or false
	 */
	boolean isUnsigned();

	/**
     * Get the value as a byte for a scalar IDataItem. May also be
	 * one-dimensional of length 1.
	 * 
	 * @return byte object
     * @throws IOException if there is an IO Error
	 */
	byte readScalarByte() throws IOException;

	/**
     * Get the value as a double for a scalar IDataItem. May also be
	 * one-dimensional of length 1.
	 * 
	 * @return double value
     * @throws IOException if there is an IO Error
	 */
	double readScalarDouble() throws IOException;

	/**
     * Get the value as a float for a scalar IDataItem. May also be
	 * one-dimensional of length 1.
	 * 
	 * @return float value
     * @throws IOException if there is an IO Error
	 */
	float readScalarFloat() throws IOException;

	/**
     * Get the value as a int for a scalar IDataItem. May also be one-dimensional
	 * of length 1.
	 * 
	 * @return integer value
     * @throws IOException if there is an IO Error
	 */
	int readScalarInt() throws IOException;

	/**
     * Get the value as a long for a scalar IDataItem. May also be
	 * one-dimensional of length 1.
	 * 
	 * @return long value
     * @throws IOException if there is an IO Error
	 */
	long readScalarLong() throws IOException;

	/**
     * Get the value as a short for a scalar IDataItem. May also be
	 * one-dimensional of length 1.
	 * 
	 * @return short value
     * @throws IOException if there is an IO Error
	 */
	short readScalarShort() throws IOException;

	/**
     * Get the value as a String for a scalar IDataItem. May also be
	 * one-dimensional of length 1. May also be one-dimensional of type CHAR,
	 * which will be turned into a scalar String.
	 * 
	 * @return String object
     * @throws IOException if there is an IO Error
	 */
	String readScalarString() throws IOException;

	/**
     * Remove the given IAttribute: uses the attribute hashCode to find it.
	 * 
     * @param a IAttribute object
	 * @return true if was found and removed
	 */
	boolean removeAttribute(IAttribute a);

	/**
	 * Set the data cache.
	 * 
     * @param cacheData IArray object to cache
     * @param isMetadata : synthesized data, set true if must be saved (i.e. data not actually in the file).
     * @throws InvalidArrayTypeException invalid type
	 */
	void setCachedData(IArray cacheData, boolean isMetadata)
			throws InvalidArrayTypeException;

	/**
	 * Set whether to cache or not. Implies that the entire array will be
	 * stored, once read. Normally this is set automatically based on size of
	 * data.
	 * 
     * @param caching set if caching.
	 */
	void setCaching(boolean caching);

		/**
	 * Set the data type.
	 * 
     * @param dataType Class object
	 */
	void setDataType(Class<?> dataType);

	/**
	 * Set the dimensions using the dimensions names. The dimension is searched
	 * for recursively in the parent groups.
	 * 
     * @param dimString : whitespace separated list of dimension names, or '*' for
	 *            Dimension.UNKNOWN.
	 */
	void setDimensions(String dimString);

    /**
     * Set the dimension on the specified index.
     * 
     * @param dim IDimension to add to this data item
     * @param ind Index the dimension matches
     */
    void setDimension(IDimension dim, int ind) throws DimensionNotSupportedException;
    
	/**
	 * Set the element size. Usually elementSize is determined by the dataType,
	 * use this only for exceptional cases.
	 * 
     * @param elementSize integer value
	 */
	void setElementSize(int elementSize);

	/**
	 * Set sizeToCache.
	 * 
     * @param sizeToCache integer value
	 */
	void setSizeToCache(int sizeToCache);

	/**
     * Set the units of the IDataItem.
	 * 
     * @param units as a String object 
	 */
	void setUnitsString(String units);

	/**
     * String representation of IDataItem and its attributes.
	 * 
	 * @return String object
	 */
	String toStringDebug();

	/**
     * String representation of a IDataItem and its attributes.
	 * 
     * @param indent start each line with this much space
     * @param useFullName use full name, else use short name
     * @param strict strictly comply with ncgen syntax
     * @return CDL representation of the IDataItem.
     * @deprecated [SOLEIL][clement][2012-04-18] seems to be a plug-in dependent method 
	 */
    //[SOLEIL][clement][2012-04-18] seems to be a plug-in dependent method maybe I'm wrong. I think we should remove it from the Core or rename it like write(...)
	//[ANSTO][Tony][2012-05-02] This is used by NetCDF internally, may be we can remove this because none of ANSTO code is relying on this.
	String writeCDL(String indent, boolean useFullName, boolean strict);
	
	/**
     * Clone this data item. Return a new IDataItem instance but share the same
     * IArray data storage.
	 * 
     * @return new IDataItem instance
	 */
	@Override
    IDataItem clone() throws CloneNotSupportedException;
    
}
