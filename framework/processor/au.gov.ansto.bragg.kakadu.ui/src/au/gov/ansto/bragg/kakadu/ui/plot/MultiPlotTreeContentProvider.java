/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.plot;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 * @author Danil Klimontov (dak)
 */
public class MultiPlotTreeContentProvider implements ITreeContentProvider {

	private Plot plot;
	
	
	public MultiPlotTreeContentProvider(Plot plot) {
		this.plot = plot;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement == "" || parentElement == null) {
			return plot.getMultiPlotDataManager().getChildren(null).toArray();
		}
		return ((PlotDataItem)parentElement).getChildren().toArray();
	}

	public Object getParent(Object element) {
		return ((PlotDataItem)element).getParent();
	}

	public boolean hasChildren(Object element) {
		return element != null ? ((PlotDataItem)element).getChildrenCount() > 0 : getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
