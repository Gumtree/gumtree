package org.gumtree.data.engine.nexus.navigation;

import org.gumtree.data.engine.nexus.array.NexusArray;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;

public final class NexusAttribute implements IAttribute {

    /// Members
    private String  mName;    // Attribute's name
    private IArray  mValue;    // Attribute's value
    private String  mFactory;   // factory name that attribute depends on

    /// Constructors
    public NexusAttribute(String factoryName, String sName, Object aValue) {
	int i = 1;
	if( aValue.getClass().isArray() ) {
	    i = java.lang.reflect.Array.getLength(aValue);
	}
	mFactory = factoryName;
	mName    = sName;
	mValue   = new NexusArray(mFactory, aValue, new int[] {i} );
    }


    @Override
    public int getLength() {
	Long length = mValue.getSize();
	return length.intValue();
    }

    @Override
    public String getName() {
	return mName;
    }

    @Override
    public Number getNumericValue() {
	if( isString() )
	{
	    return null;
	}

	if( isArray() )
	{
	    return getNumericValue(0);
	}
	else
	{
	    return (Number) mValue.getStorage();

	}
    }

    @Override
    public Number getNumericValue(int index) {
	Object value;
	if( isArray() )
	{
	    value = java.lang.reflect.Array.get(mValue.getStorage(), index);
	}
	else
	{
	    value = mValue.getStorage();
	}

	if( isString() ) {
	    return (Double) value;
	}

	return (Number) value;
    }

    @Override
    public String getStringValue() {
	if( isString() )
	{
	    return (String) mValue.getStorage();
	}
	else
	{
	    return null;
	}
    }

    @Override
    public String getStringValue(int index) {
	if( isString() )
	{
	    return ((String) java.lang.reflect.Array.get(mValue.getStorage(), index));
	}
	else
	{
	    return null;
	}
    }

    @Override
    public Class<?> getType() {
	return mValue.getElementType();
    }

    @Override
    public IArray getValue() {
	return mValue;
    }

    @Override
    public boolean isArray() {
	return mValue.getSize() > 1;
    }

    @Override
    public boolean isString() {
	Class<?> tmpClass = "".getClass();
	Class<?> container = mValue.getElementType();
	return ( container.equals(tmpClass) );
    }

    @Override
    public void setStringValue(String val) {
	mValue = new NexusArray(mFactory, val, new int[] {1});
    }

    @Override
    public void setValue(IArray value) {
	mValue = value;
    }

    public String toString() {
	return mName + "=" + mValue;
    }

    @Override
    public String getFactoryName() {
	return mFactory;
    }
}
