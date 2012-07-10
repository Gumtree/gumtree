package org.gumtree.data.engine.nexus.utils;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.math.ArrayMath;
import org.gumtree.data.math.IArrayMath;

public final class NexusArrayMath extends ArrayMath {

    public NexusArrayMath(IArray array) {
	super(array, Factory.getFactory(array.getFactoryName()));
    }

    @Override
    public IArrayMath toAdd(IArray array) throws ShapeNotMatchException {
	throw new UnsupportedOperationException();
    }

    @Override
    public IArrayMath add(IArray array) throws ShapeNotMatchException {
	throw new UnsupportedOperationException();
    }

    @Override
    public IArrayMath matMultiply(IArray array) throws ShapeNotMatchException {
	throw new UnsupportedOperationException();
    }

    @Override
    public IArrayMath matInverse() throws ShapeNotMatchException {
	throw new UnsupportedOperationException();
    }

    @Override
    public IArrayMath sumForDimension(int dimension, boolean isVariance)
	    throws ShapeNotMatchException {
	throw new UnsupportedOperationException();
    }

    @Override
    public IArrayMath enclosedSumForDimension(int dimension, boolean isVariance)
	    throws ShapeNotMatchException {
	throw new UnsupportedOperationException();
    }

    @Override
    public double getNorm() {
	throw new UnsupportedOperationException();
    }

    @Override
    public IArrayMath normalise() {
	throw new UnsupportedOperationException();
    }

    @Override
    public double getDeterminant() throws ShapeNotMatchException {
	throw new UnsupportedOperationException();
    }

}
