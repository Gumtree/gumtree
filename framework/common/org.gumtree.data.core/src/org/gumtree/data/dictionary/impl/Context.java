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

import org.gumtree.data.dictionary.IContext;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IKey;

public final class Context implements IContext {
    private IDataset    mDataset;
    private IContainer  mCaller;
    private IKey        mKey;
    private IPath       mPath;
    private Object[]    mParams;
	
	public Context(IDataset dataset) {
        mDataset = dataset;
        mCaller  = null;
        mKey     = null;
        mPath    = null;
        mParams  = null;
	}
	
	public Context( IDataset dataset, IContainer caller, IKey key, IPath path ) {
        mDataset = dataset;
        mCaller  = caller;
        mKey     = key;
        mPath    = path;
        mParams  = null;
	}
	
	@Override
	public String getFactoryName() {
        return mDataset.getFactoryName();
	}

	@Override
	public IDataset getDataset() {
        return mDataset;
	}

	@Override
	public void setDataset(IDataset dataset) {
        mDataset = dataset;
	}

	@Override
	public IContainer getCaller() {
        return mCaller;
	}

	@Override
	public void setCaller(IContainer caller) {
        mCaller = caller;
	}

	@Override
	public IKey getKey() {
        return mKey;
	}

	@Override
	public void setKey(IKey key) {
        mKey = key;
	}

	@Override
	public IPath getPath() {
        return mPath;
	}

	@Override
	public void setPath(IPath path) {
        mPath = path;
	}

	@Override
	public Object[] getParams() {
        return mParams.clone();
	}

	@Override
	public void setParams(Object[] params) {
        mParams = params.clone();
	}
}
