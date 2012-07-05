package org.gumtree.data.engine.nexus.array;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;

public final class NexusArrayIterator implements IArrayIterator {
    /// Members
    private IArray  mArray;
    private IIndex  mIndex;
    private Object  mCurrent;
    private boolean mAccess; // Indicates that this can access the storage memory or not

    public NexusArrayIterator(NexusArray array)
    {
	mArray  = array;
	// [ANSTO][Tony][2011-08-31] Should mAccess set to true for NxsArrayInterface??
	// If m_access is set to false, next() does not work.
	// [SOLEIL][Clement][2011-11-22] Yes it should. It indicates that the iterator shouldn't access memory. In case of hudge matrix the next() will update m_current (i.e. value), but the underlying NeXus engine will automatically load the part corresponding to the view defined by this iterator, which can lead to java heap space memory exception (see NxsArray : private Object getData() ) 
	mAccess = true;
	try {
	    mIndex = array.getIndex().clone();
	    mIndex.set( new int[mIndex.getRank()] );
	    mIndex.setDim( mIndex.getRank() - 1, -1);
	} catch (CloneNotSupportedException e) {
	}
    }

    public NexusArrayIterator(IArray array, IIndex index) {
	this(array, index, true);
    }

    public NexusArrayIterator(IArray array, IIndex index, boolean accessData) {
	int[] count = index.getCurrentCounter();
	mArray     = array;
	mIndex     = index;
	mAccess    = accessData;
	count[mIndex.getRank() - 1]--;
	mIndex.set( count );
    }

    @Override
    public boolean getBooleanNext() {
	incrementIndex(mIndex);
	return mArray.getBoolean(mIndex);
    }

    @Override
    public byte getByteNext() {
	incrementIndex(mIndex);
	return mArray.getByte(mIndex);
    }

    @Override
    public char getCharNext() {
	return ((Character) getObjectNext()).charValue();
    }

    @Override
    public int[] getCounter() {
	return mIndex.getCurrentCounter();
    }

    @Override
    public double getDoubleNext() {
	incrementIndex(mIndex);
	return mArray.getDouble(mIndex);
    }

    @Override
    public float getFloatNext()  {
	incrementIndex(mIndex);
	return mArray.getFloat(mIndex);
    }

    @Override
    public int getIntNext() {
	incrementIndex(mIndex);
	return mArray.getInt(mIndex);
    }

    @Override
    public long getLongNext() {
	incrementIndex(mIndex);
	return mArray.getLong(mIndex);
    }

    @Override
    public Object getObjectNext() {
    	incrementIndex(mIndex);
    	if( mAccess ) {
    	    long currentPos = mIndex.currentElement();
    	    if( currentPos <= mIndex.lastElement() && currentPos != -1 ) {
    		mCurrent = mArray.getObject(mIndex);
    	    }
    	    else {
    		mCurrent = null;
    	    }
    	}
    	return mCurrent;
    }

    @Override
    public short getShortNext() {
	incrementIndex(mIndex);
	return mArray.getShort(mIndex);
    }

    @Override
    public boolean hasNext()
    {
	long index = mIndex.currentElement();
	long last  = mIndex.lastElement();
	return ( index < last && index >= -1);
    }

    @Override
    public IArrayIterator next()
    {
    	getObjectNext();
    	return this;
    	
    }

    @Override
    public void setBooleanCurrent(boolean val) {
	setObjectCurrent(val);
    }

    @Override
    public void setByteCurrent(byte val) {
	setObjectCurrent(val);
    }

    @Override
    public void setCharCurrent(char val) {
	setObjectCurrent(val);
    }

    @Override
    public void setDoubleCurrent(double val) {
	setObjectCurrent(val);
    }

    @Override
    public void setFloatCurrent(float val) {
	setObjectCurrent(val);
    }

    @Override
    public void setIntCurrent(int val) {
	setObjectCurrent(val);
    }

    @Override
    public void setLongCurrent(long val) {
	setObjectCurrent(val);
    }

    @Override
    public void setObjectCurrent(Object val) {
	mCurrent = val;
	mArray.setObject(mIndex, val);
    }

    @Override
    public void setShortCurrent(short val) {
	setObjectCurrent(val);
    }

    public static void incrementIndex(IIndex index)
    {
	int[] current = index.getCurrentCounter();
	int[] shape = index.getShape();
	for( int i = current.length - 1; i >= 0; i-- )
	{
	    if( current[i] + 1 >= shape[i] && i > 0)
	    {
		current[i] = 0;
	    }
	    else
	    {
		current[i]++;
		index.set(current);
		return;
	    }
	}
    }

    @Override
    public String getFactoryName() {
	return mArray.getFactoryName();
    }

    /// protected method
    protected void incrementIndex() {
	NexusArrayIterator.incrementIndex(mIndex);
    }
}