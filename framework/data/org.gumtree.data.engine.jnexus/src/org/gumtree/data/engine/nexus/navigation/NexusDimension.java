package org.gumtree.data.engine.nexus.navigation;

import java.io.IOException;

import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDimension;

public final class NexusDimension implements IDimension {

    private IArray    mArray;
    private String    mLongName;
    private boolean   mIsVariableLength;
    private boolean   mIsUnlimited;
    private boolean   mIsShared;
    private String    mFactory;

    public NexusDimension(String factoryName, IDataItem item) {
	mFactory   = factoryName;
	mLongName  = item.getName();
	mIsUnlimited = item.isUnlimited();
	try {
	    mArray = item.getData();
	} catch( IOException e ) {
	    mArray = null;
	}
    }

    public NexusDimension(NexusDimension dim) {
	mFactory          = dim.mFactory;
	mLongName         = dim.mLongName;
	mArray            = dim.mArray;
	mIsVariableLength = dim.mIsVariableLength;
	mIsUnlimited      = dim.mIsUnlimited;
    }

    @Override
    public int compareTo(Object o) {
	if( this.equals(o) ) {
	    return 0;
	}
	else {
	    IDimension dim = (IDimension) o;
	    return mLongName.compareTo( dim.getName() );
	}
    }

    @Override
    public boolean equals(Object dim) {
	boolean result;
	if( dim instanceof IDimension ) {
	    result = mLongName.equals(((IDimension) dim).getName());
	}
	else {
	    result = false;
	}
	return result;
    }

    @Override
    public int hashCode() {
	return mLongName.hashCode();
    }

    @Override
    public IArray getCoordinateVariable() {
	return mArray;
    }

    @Override
    public int getLength() {
	return Long.valueOf(mArray.getSize()).intValue();
    }

    @Override
    public String getName() {
	return mLongName;
    }

    @Override
    public boolean isShared() {
	return mIsShared;
    }

    @Override
    public boolean isUnlimited() {
	return mIsUnlimited;
    }

    @Override
    public boolean isVariableLength() {
	return mIsVariableLength;
    }

    @Override
    public void setLength(int n) {
	try {
	    mArray.getArrayUtils().reshape( new int[] {n} );
	} catch ( ShapeNotMatchException e) {
	}
    }

    @Override
    public void setName(String name) {
	mLongName = name;
    }

    @Override
    public void setShared(boolean b) {
	mIsShared = b;
    }

    @Override
    public void setUnlimited(boolean b) {
	mIsUnlimited = b;
    }

    @Override
    public void setVariableLength(boolean b) {
	mIsVariableLength = b;
    }

    @Override
    public void setCoordinateVariable(IArray array) throws ShapeNotMatchException {
	if( java.util.Arrays.equals(mArray.getShape(), array.getShape()) ) {
	    throw new ShapeNotMatchException("Arrays must have same shape!");
	}
	mArray = array;
    }

    @Override
    public String writeCDL(boolean strict) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getFactoryName() {
	return mFactory;
    }
}
