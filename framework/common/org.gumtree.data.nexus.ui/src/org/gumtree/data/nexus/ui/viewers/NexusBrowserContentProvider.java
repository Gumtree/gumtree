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

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXDataItem;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXGroup;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.INXentry;
import org.gumtree.data.nexus.INXinstrument;
import org.gumtree.data.nexus.INXmonitor;
import org.gumtree.data.nexus.INXnote;
import org.gumtree.data.nexus.INXsample;
import org.gumtree.data.nexus.INXuser;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.IVariance;
import org.gumtree.data.ui.viewers.DatasetBrowserContentProvider;

/**
 * @author nxi
 *
 */
public class NexusBrowserContentProvider extends DatasetBrowserContentProvider {

	/**
	 * 
	 */
	public NexusBrowserContentProvider() {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof INXDataset) {
			INXentry entry = ((INXDataset) parentElement).getNXroot().getDefaultEntry();
			INXdata data = entry.getData();
			INXinstrument instrument = entry.getInstrumentGroup();
			INXmonitor monitor = entry.getMonitorGroup();
			INXsample sample = entry.getSampleGroup();
			INXuser user = entry.getUserGroup();
			INXnote note = entry.getNoteGroup();
			List<IContainer> children = new ArrayList<IContainer>();
			if (data != null) {
				children.add(data);
			}
			if (instrument != null) {
				children.add(instrument);
			}
			if (monitor != null) {
				children.add(monitor);
			}
			if (sample != null) {
				children.add(sample);
			}
			if (user != null) {
				children.add(user);
			}
			if (note != null) {
				children.add(note);
			}
			IDictionary dictionary = entry.findDictionary();
			if (dictionary != null) {
				for (IKey key : dictionary.getAllKeys()) {
					IContainer item = entry.findContainer(key.getName());
					if (item != null) {
						children.add(item);
					}
				}
			}
			return children.toArray(new Object[children.size()]);
		} else if (parentElement instanceof INXdata) {
			ISignal signal = ((INXdata) parentElement).getSignal();
			IVariance variance = ((INXdata) parentElement).getVariance();
			List<IAxis> axes =  ((INXdata) parentElement).getAxisList();
			List<IContainer> children = new ArrayList<IContainer>();
			if (signal != null) {
				children.add(signal);
			}
			if (variance != null) {
				children.add(variance);
			}
			if (axes != null) {
				children.addAll(axes);
			}
			return children.toArray(new Object[children.size()]);
		} else if (parentElement instanceof INXGroup) {
			List<INXDataItem> dataItems = ((INXGroup) parentElement).getNXDataItemList();
			List<INXGroup> groups = ((INXGroup) parentElement).getNXGroupList();
			List<IContainer> children = new ArrayList<IContainer>();
			children.addAll(dataItems);
			children.addAll(groups);
			return children.toArray(new Object[children.size()]);
		}
		return super.getChildren(parentElement);
	}
}
