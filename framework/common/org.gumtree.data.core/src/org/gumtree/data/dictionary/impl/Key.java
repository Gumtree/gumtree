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

package org.gumtree.data.dictionary.impl;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IPathParameter;
import org.gumtree.data.interfaces.IKey;

public final class Key implements IKey, Cloneable {
    private String mFactory;
    private String mKey = "";   // key name
    private List<IPathParameter> mFilters;

    public Key(IFactory factory, String name) {
        mKey     = name;
        mFactory = factory.getName();
        mFilters = new ArrayList<IPathParameter>();
    }

    public Key(IKey key) {
        mKey     = key.getName();
        mFactory = key.getFactoryName();
        mFilters = new ArrayList<IPathParameter>();
        for( IPathParameter param : key.getParameterList() ) {
            mFilters.add(param.clone());
        }
    }

    @Override
    public List<IPathParameter> getParameterList() {
        return mFilters;
    }

    @Override
    public String getName() {
        return mKey;
    }

    @Override
    public void setName(String name) {
        mKey = name;
    }

    @Override
    public boolean equals(Object key) {
        if( ! (key instanceof IKey) ) {
            return false;
        }
        else {
            return mKey.equals( ((IKey) key).getName());
        }
    }

    @Override
    public int hashCode() {
        return mKey.hashCode();
    }

    @Override
    public String toString() {
        return mKey;
    }

    @Override
    public void pushParameter(IPathParameter filter) {
        mFilters.add(filter);
    }

    @Override
    public IPathParameter popParameter() {
        if( mFilters.size() > 0) {
            return mFilters.remove(0);
        }
        else {
            return null;
        }
    }

    @Override
    public IKey clone() {
        IKey key = Factory.getFactory(mFactory).createKey(mKey);
        for( IPathParameter filter : mFilters ) {
            key.pushParameter( filter.clone() );
        }
        return key;
    }

    @Override
    public String getFactoryName() {
        return mFactory;
    }

    @Override
    public int compareTo(Object arg0) {
        return this.getName().compareTo(arg0.toString());
    }
}
