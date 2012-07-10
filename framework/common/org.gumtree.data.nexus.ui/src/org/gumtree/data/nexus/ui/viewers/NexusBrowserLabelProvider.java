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

import java.io.File;

import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.ui.viewers.DatasetBrowserLabelProvider;

/**
 * @author nxi
 *
 */
public class NexusBrowserLabelProvider extends DatasetBrowserLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof INXDataset) {
			String title = ((INXDataset) element).getTitle();
			if (title != null && title.trim().length() > 0) {
				return title;
			}
			String location = ((INXDataset) element).getLocation();
			if (location != null) {
				File file = new File(location);
				return file.getName();
			}
		}
		return super.getText(element);
	}
}
