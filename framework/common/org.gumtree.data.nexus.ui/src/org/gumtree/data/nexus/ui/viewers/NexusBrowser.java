/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.nexus.ui.viewers;

import java.net.URI;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.utils.NexusUtils;
import org.gumtree.data.ui.viewers.DatasetBrowser;

/**
 * @author nxi
 *
 */
public class NexusBrowser extends DatasetBrowser {

	/**
	 * @param parent
	 * @param style
	 */
	public NexusBrowser(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected ITreeContentProvider makeContentProvider() {
		return new NexusBrowserContentProvider();
	}
	
	@Override
	protected IDataset makeDataset(URI uri) throws Exception {
		// TODO Auto-generated method stub
		return NexusUtils.readNexusDataset(uri);
	}
	
	@Override
	protected IBaseLabelProvider makeLabelProvider() {
		// TODO Auto-generated method stub
		return new NexusBrowserLabelProvider();
	}
}
