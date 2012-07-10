package org.gumtree.data.engine.nexus.array;

import org.gumtree.data.engine.nexus.utils.NexusArrayMath;
import org.gumtree.data.engine.nexus.utils.NexusArrayUtils;
import org.gumtree.data.exception.BackupException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.math.IArrayMath;
import org.gumtree.data.utils.IArrayUtils;

import fr.soleil.nexus.DataItem;


public final class NexusArray implements IArray {
    private IIndex    mIndex;        // IIndex corresponding to this IArray (dimension sizes defining the viewable part of the array)
    private Object    mData;         // It's an array of values
    private boolean    mIsRawArray;   // True if the stored array has a rank of 1 (independently of its shape)
    private boolean   mIsDirty;      // Is the array synchronized with the handled file
    private DataItem  mN4TDataItem;  // IArray of dataitem that are used to store the storage backing
    private int[]     mShape;        // Shape of the array (dimension sizes of the storage backing)
    private String    mFactory;      // Name of the instantiating factory 
    private static final int TO_STRING_LENGTH = 1000;

    // Constructors
    public NexusArray(String factoryName, Object oArray, int[] iShape) {
	mIndex = new NexusIndex(mFactory, iShape.clone());
	mData  = oArray;
	mShape = iShape.clone();
	if( iShape.length == 1 ) {
	    mIsRawArray  = true;
	}
	else {
	    mIsRawArray = (
		    oArray.getClass().isArray() && 
		    !(java.lang.reflect.Array.get(oArray, 0).getClass().isArray()) 
		    );
	}
	mN4TDataItem = null;
	mFactory = factoryName;
    }

    public NexusArray(NexusArray array) {
	mFactory = array.mFactory;
	try {
	    mIndex = array.mIndex.clone();
	} catch (CloneNotSupportedException e) {
	    mIndex = null;
	}
	mData  = array.mData;
	mShape = new int[array.mShape.length];
	int i  = 0;
	for( int size : array.mShape ) {
	    mShape[i++]  = size;
	}
	mIsRawArray = array.mIsRawArray;
	mIsDirty    = array.mIsDirty;
	try {
	    mN4TDataItem = array.mN4TDataItem.clone();
	} catch (CloneNotSupportedException e) {
	    mN4TDataItem = null;
	}
    }

    public NexusArray(String factoryName, DataItem ds) {
	mFactory     = factoryName;
	mIndex       = new NexusIndex(mFactory, ds);
	mData        = null;
	mShape       = ds.getSize();
	mIsRawArray  = ds.isSingleRawArray();
	mIsDirty     = false;
	mN4TDataItem = ds;
    }

    // ---------------------------------------------------------
    /// public methods
    // ---------------------------------------------------------
    @Override
    public IArrayMath getArrayMath() {
	return new NexusArrayMath(this);
    }

    @Override
    public IArrayUtils getArrayUtils() {
	return new NexusArrayUtils(this);
    }

    /// Specific method to match NetCDF plug-in behavior
    @Override
    public String toString()
    {
	Object oData = getData();
	if( oData instanceof String ) {
	    return (String) oData;
	}
	StringBuilder sbuff = new StringBuilder();
	IArrayIterator ii = getIterator();
	Object data = null;
	while (ii.hasNext())
	{
	    data = ii.next();
	    sbuff.append(data);
	    sbuff.append(" ");
	}
	return sbuff.toString().substring(0, sbuff.length() < TO_STRING_LENGTH ? sbuff.length() : TO_STRING_LENGTH);
    }

    /// IArray underlying data access
    @Override
    public Object getStorage() {
	return getData();
    }

    @Override
    public int[] getShape() {
	return mIndex.getShape();
    }

    @Override
    public Class<?> getElementType() {
	Class<?> result = null;
	if( mN4TDataItem != null ) {
	    result = mN4TDataItem.getDataClass();
	}
	else {
	    Object oData = getData();
	    if( oData != null )
	    {
		if( oData.getClass().isArray() ) {
		    result = oData.getClass().getComponentType();
		    while( result.isArray() ) {
			result = result.getComponentType();
		    }
		}
		else {
		    result = oData.getClass();
		}
	    }
	}
	return result;
    }

    @Override
    public void lock() {
	// TODO Auto-generated method stub
    }

    @Override
    public boolean isDirty() {
	return mIsDirty;
    }

    @Override
    public void releaseStorage() throws BackupException {
	// TODO Auto-generated method stub
    }

    @Override
    public String shapeToString() {
	int[] shape = getShape();
	StringBuilder sb = new StringBuilder();
	if (shape.length != 0) {
	    sb.append('(');
	    for (int i = 0; i < shape.length; i++) {
		int s = shape[i];
		if (i > 0) {
		    sb.append(",");
		}
		sb.append(s);
	    }
	    sb.append(')');
	}
	return sb.toString();
    }


    @Override
    public void unlock() {
	// TODO Auto-generated method stub

    }

    /// IArray data manipulation
    @Override
    public IIndex getIndex() {
	return mIndex;
    }

    @Override
    public IArrayIterator getIterator() {
	return (IArrayIterator) new NexusArrayIterator(this);
    }

    @Override
    public int getRank() {
	return mIndex.getRank();
    }

    @Override
    public IArrayIterator getRegionIterator(int[] reference, int[] range)
	    throws InvalidRangeException {
	NexusIndex index = new NexusIndex( mFactory, mShape, reference, range );
	return new NexusArrayIterator(this, index);
    }

    @Override
    public long getRegisterId() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public long getSize() {
	return mIndex.getSize();
    }

    @Override
    public ISliceIterator getSliceIterator(int rank)
	    throws ShapeNotMatchException, InvalidRangeException {
	return new NexusSliceIterator(this, rank);
    }

    // IArray data getters and setters
    @Override
    public boolean getBoolean(IIndex ima) {
	boolean result;
	IIndex idx;
	try {
	    idx = ima.clone();
	    Object oData = getData();

	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = (Boolean) oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = ((boolean[]) oData)[lPos];
	    }
	} catch (CloneNotSupportedException e) {
	    result = false;
	}
	return result;
    }

    @Override
    public byte getByte(IIndex ima) {
	byte result;
	IIndex idx;
	try {
	    idx = ima.clone();
	    Object oData = getData();

	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = (Byte) oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = ((byte[]) oData)[lPos];
	    }
	} catch (CloneNotSupportedException e) {
	    result = 0;
	}
	return result;
    }

    @Override
    public char getChar(IIndex ima) {
	char result;
	IIndex idx;
	try {
	    idx = ima.clone();
	    Object oData = getData();

	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = (Character) oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = ((char[]) oData)[lPos];
	    }
	} catch (CloneNotSupportedException e) {
	    result = 0;
	}
	return result;
    }

    @Override
    public double getDouble(IIndex ima) {
	double result;
	IIndex idx;
	try {
	    idx = ima.clone();
	    Object oData = getData();
	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = (Double) oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = ((double[]) oData)[lPos];
	    }
	} catch (CloneNotSupportedException e) {
	    result = 0;
	}
	return result;
    }

    @Override
    public float getFloat(IIndex ima) {
	float result = 0;
	IIndex idx;
	try {
	    idx = ima.clone();
	    Object oData = getData();

	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = (Float) oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = ((float[]) oData)[lPos];
	    }
	} catch (CloneNotSupportedException e) {
	    result = 0;
	}
	return result;
    }

    @Override
    public int getInt(IIndex ima) {
	int result = 0;
	IIndex idx;
	try {
	    idx = ima.clone();
	    Object oData = getData();

	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = (Integer) oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = ((int[]) oData)[lPos];
	    }
	} catch (CloneNotSupportedException e) {
	    result = 0;
	}
	return result;
    }

    @Override
    public long getLong(IIndex ima) {
	long result = 0;
	IIndex idx;
	try {
	    idx = ima.clone();
	    Object oData = getData();

	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = (Long) oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = ((long[]) oData)[lPos];
	    }
	} catch (CloneNotSupportedException e) {
	    result = 0;
	}
	return result;
    }

    @Override
    public Object getObject(IIndex index) {
	Object result = new Object();
	IIndex idx;
	try {
	    idx = index.clone();
	    Object oData = getData();

	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = java.lang.reflect.Array.get(oData, lPos);
	    }
	} catch (CloneNotSupportedException e) {
	    result = 0;
	}
	return result;
    }

    @Override
    public short getShort(IIndex ima) {
	short result = 0;
	IIndex idx;
	try {
	    idx = ima.clone();
	    Object oData = getData();

	    // If it's a scalar value then we return it
	    if( ! oData.getClass().isArray() ) {
		result = (Short) oData;
	    }
	    // else it's a single raw array, then we compute indexes to have the corresponding cell number
	    else {
		int lPos = (int) idx.currentElement();
		result = ((short[]) oData)[lPos];
	    }
	} catch (CloneNotSupportedException e) {
	    result = 0;
	}
	return result;
    }

    @Override
    public void setBoolean(IIndex ima, boolean value) {
	set(ima, value);
    }

    @Override
    public void setByte(IIndex ima, byte value) {
	set(ima, value);
    }

    @Override
    public void setChar(IIndex ima, char value) {
	set(ima, value);
    }

    @Override
    public void setDouble(IIndex ima, double value) {
	set(ima, value);
    }

    @Override
    public void setFloat(IIndex ima, float value) {
	set(ima, value);
    }

    @Override
    public void setInt(IIndex ima, int value) {
	set(ima, value);
    }

    @Override
    public void setLong(IIndex ima, long value) {
	set(ima, value);
    }

    @Override
    public void setObject(IIndex ima, Object value) {
	set(ima, value);
    }

    @Override
    public void setShort(IIndex ima, short value) {
	set(ima, value);
    }

    @Override
    public IArray setDouble(double value) {
	Object oData = getData();
	if( mIsRawArray ) {
	    java.util.Arrays.fill((double[])oData, value);
	}
	else {
	    setDouble(oData, value);
	}
	return this;
    }

    @Override
    public IArray copy() {
	return copy(true);
    }

    @Override
    public IArray copy(boolean data) {
	NexusArray result = new NexusArray(this);

	if( data ) {
	    result.mData = copyJavaArray(mData);
	}

	return result;
    }

    @Override
    public void setIndex(IIndex index) {
	mIndex = index;
    }

    @Override
    public String getFactoryName() {
	return mFactory;
    }

    // --------------------------------------------------
    // Tool methods
    // --------------------------------------------------
    static public Object copyJavaArray(Object array) {
	Object result = array;
	if( result == null ) {
	    return null;
	}
	else {
	    // Determine rank of array (by parsing data array class name)
	    String sClassName = array.getClass().getName();
	    int iRank  = 0;
	    int iIndex = 0;
	    char cChar;
	    while (iIndex < sClassName.length()) {
		cChar = sClassName.charAt(iIndex);
		iIndex++;
		if (cChar == '[') {
		    iRank++;
		}
	    }

	    // Set dimension rank
	    int[] shape    = new int[iRank];

	    // Fill dimension size array
	    for ( int i = 0; i < iRank; i++) {
		shape[i] = java.lang.reflect.Array.getLength(result);
		result = java.lang.reflect.Array.get(result,0);
	    }

	    // Define a convenient array (shape and type)
	    result = java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), shape);
	    result = copyJavaArray(array, result);
	}
	return result;
    }

    static public Object copyJavaArray(Object source, Object target) {
	Object item = java.lang.reflect.Array.get(source, 0);
	int length = java.lang.reflect.Array.getLength(source);

	if( item.getClass().isArray() ) {
	    Object tmpSrc;
	    Object tmpTar;
	    for( int i = 0; i < length; i++ ) {
		tmpSrc = java.lang.reflect.Array.get(source, i);
		tmpTar = java.lang.reflect.Array.get(target, i);
		copyJavaArray( tmpSrc, tmpTar);
	    }
	}
	else {
	    System.arraycopy(source, 0, target, 0, length);
	}

	return target;
    }

    // ---------------------------------------------------------
    /// Protected methods
    // ---------------------------------------------------------
    protected boolean isSingleRawArray() {
	return mIsRawArray;
    }

    protected IArray sectionNoReduce(int[] origin, int[] shape, long[] stride) throws ShapeNotMatchException {
	Object oData = getData();
	NexusArray array = new NexusArray(mFactory, oData, mShape);
	array.mIndex.setShape(shape);
	array.mIndex.setStride(stride);
	((NexusIndex) array.mIndex).setOrigin(origin);
	return array;
    }

    protected void setShape(int[] shape) {
	mShape = shape.clone();
    }

    // ---------------------------------------------------------
    /// Private methods
    // ---------------------------------------------------------
    /**
     * Translate the given IIndex into the corresponding cell index.
     * This method is used to access a multidimensional array's cell when
     * the memory storage is a single raw array.
     * 
     * @param index sibling a cell in a multidimensional array
     * @return the cell number in a single raw array (that carry the same logical shape)
     */
    private int translateIndex(IIndex index) {
	int[] indexes = index.getCurrentCounter();

	int lPos = 0, lStartRaw;
	for( int k = 1; k < mShape.length; k++ ) {

	    lStartRaw = 1;
	    for( int j = 0; j < k; j++ )
	    {
		lStartRaw *= mShape[j];
	    }
	    lStartRaw *= indexes[k - 1];
	    lPos += lStartRaw;
	}
	lPos += indexes[indexes.length - 1];
	return lPos;
    }

    /**
     * Get the object targeted by given index and return it (eventually using outboxing).
     * It's the central data access method that all other methods rely on.
     * 
     * @param index targeting a cell 
     * @return the content of cell designed by the index
     * @throws InvalidRangeException if one of the index is bigger than the corresponding dimension shape
     */
    private Object get(IIndex index) {
	Object oCurObj = null;
	NexusIndex idx = null;
	if( index instanceof NexusIndex ) {
	    idx = (NexusIndex) index;
	}
	else {
	    idx = new NexusIndex(mFactory, index.getShape(), new int[index.getRank()], index.getShape());
	    idx.set(index.getCurrentCounter());
	}
	Object oData = getData();
	// If it's a string then no array 
	if( oData.getClass().equals( String.class ) )
	{
	    return (String) oData;
	}
	// If it's a scalar value then we return it
	else if( ! oData.getClass().isArray() )
	{
	    return oData;
	}
	// If it's a single raw array, then we compute indexes to have the corresponding cell number 
	else if( mIsRawArray )
	{
	    int lPos = (int) idx.currentElement();
	    return java.lang.reflect.Array.get(oData, lPos);
	}
	// If it's a multidimensional array, then we get sub-part until to have the single cell we're interested in
	else
	{
	    int[] indexes = idx.getCurrentCounter();
	    oCurObj = oData;
	    for( int i = 0; i < indexes.length; i++ )
	    {
		oCurObj = java.lang.reflect.Array.get(oCurObj, indexes[i]);
	    }
	}
	return oCurObj;
    }

    private Object getData() {
	Object result = mData;
	if( result == null && mN4TDataItem != null ) {
	    result = mN4TDataItem.getData(((NexusIndex) mIndex).getProjectionOrigin(), ((NexusIndex) mIndex).getProjectionShape());
	}
	return result;
    }

    /**
     * Set the given object into the targeted cell by given index (eventually using autoboxing).
     * It's the central data access method that all other methods rely on.
     * 
     * @param index targeting a cell 
     * @param value new value to set in the array
     * @throws InvalidRangeException if one of the index is bigger than the corresponding dimension shape
     */
    private void set(IIndex index, Object value) {
	// If array has string class: then it's a scalar string 
	Object oData = getData();
	if( oData.getClass().equals( String.class ) )
	{
	    mData = (String) value;
	}
	// If array isn't an array we set the scalar value
	else if( ! oData.getClass().isArray() )
	{
	    mData = value;
	}
	// If it's a single raw array, then we compute indexes to have the corresponding cell number
	else if( mIsRawArray )
	{
	    int lPos = translateIndex(index);
	    java.lang.reflect.Array.set(oData, lPos, value);
	}
	// Else it's a multidimensional array, so we will take slices from each dimension until we can reach requested cell
	else {
	    int[] indexes = null;
	    if( index instanceof NexusIndex ) {
		indexes = ((NexusIndex) index).getCurrentPos();
	    }
	    else {
		indexes = index.getCurrentCounter();
	    }
	    Object oCurObj = oData;
	    for( int i = 0; i < indexes.length - 1; i++ )
	    {
		oCurObj = java.lang.reflect.Array.get(oCurObj, indexes[i]);
	    }
	    java.lang.reflect.Array.set(oCurObj, indexes[indexes.length - 1], value);
	}

    }

    /**
     * Recursive method that sets all values of the given array (whatever it's form is) 
     * to the same given double value
     * 
     * @param array object array to fill
     * @param value double value to be set in the array
     * @return the array filled properly
     * @note ensure the given array is a double[](...[]) or a Double[](...[])
     */
    private Object setDouble(Object array, double value) {
	if( array.getClass().isArray() ) {
	    int iLength = java.lang.reflect.Array.getLength(array);
	    for (int j = 0; j < iLength; j++) {
		Object o = java.lang.reflect.Array.get(array, j);
		if (o.getClass().isArray()) {
		    setDouble(o, value);
		} else {
		    java.util.Arrays.fill( (double[]) array, value);
		    return array;
		}
	    }
	} else {
	    java.lang.reflect.Array.set(array, 0, value);
	}

	return array;
    }

    @Override
    public void setDirty(boolean dirty) {
	// TODO Auto-generated method stub

    }
}
/*
@SuppressWarnings("unused")
  private <T> T get(IIndex index, T output) {
     Object oCurObj = null;
     NexusIndex idx = null;
     if( index instanceof NexusIndex ) {
       idx = (NexusIndex) index;
     }
     else {
       idx = new NexusIndex(mFactory, index.getShape(), new int[index.getRank()], index.getShape());
       idx.set(index.getCurrentCounter());
     }

     Object oData = getData();

     // If it's a string then no array 
     if( output instanceof String )
     {
       output = (T) oData;
     }
     // If it's a scalar value then we return it
     else if( ! oData.getClass().isArray() )
     {
       output = (T) oData;
     }
     // If it's a single raw array, then we compute indexes to have the corresponding cell number
     else if( output instanceof Object ) {
       int lPos = (int) idx.currentElement();
       Object data = java.lang.reflect.Array.get(oData, lPos);
         output = (T) data;
     }
     else
       //if( mIsRawArray )
     {
       int lPos = (int) idx.currentElement();
         output = ((T[]) oData)[lPos];
     }

     return output;
 }
 */