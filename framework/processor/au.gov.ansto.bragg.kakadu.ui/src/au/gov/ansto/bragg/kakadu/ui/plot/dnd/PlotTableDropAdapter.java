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
package au.gov.ansto.bragg.kakadu.ui.plot.dnd;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

import au.gov.ansto.bragg.kakadu.ui.plot.Plot;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataReference;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotException;

/**
 * 
 * @author Danil Klimontov (dak)
 */
public class PlotTableDropAdapter extends ViewerDropAdapter {

	private final Plot plot;

	public PlotTableDropAdapter(Viewer tableViewer, Plot plot) {
		super(tableViewer);
		this.plot = plot;
	}

	/**
	 * Method declared on ViewerDropAdapter
	 */
	public boolean performDrop(Object data) {
		System.out.println("drop> "+data);
		if (data instanceof PlotDataReference) {
			PlotDataReference plotDataReference = (PlotDataReference) data;
			try {
				plot.getMultiPlotDataManager().addPlotDataItem(plotDataReference);
				return true;
			} catch (PlotException e) {
				plot.handleException(e);
			}
			
		} else if (data instanceof PlotDataItem) {
			PlotDataItem plotDataItem = (PlotDataItem) data;
			try {
				PlotDataItem newDataItem = plotDataItem.clone();
//				newDataItem.setColor(color)
				// disable link when drop a data in the plot -- nxi
				newDataItem.setLinked(false);
				plot.getMultiPlotDataManager().addPlotDataItem(newDataItem);
				return true;
			} catch (PlotException e) {
				plot.handleException(e);
			}
		}
		//all gadgets in a table are children of the root
//		Gadget parent = (Gadget) getViewer().getInput();
//		Gadget[] toDrop = (Gadget[]) data;
//		for (int i = 0; i < toDrop.length; i++) {
//			//get the flat list of all gadgets in this tree
//			Gadget[] flatList = toDrop[i].flatten();
//			for (int j = 0; j < flatList.length; j++) {
//				flatList[j].setParent(parent);
//			}
//			((TableViewer) getViewer()).add(flatList);
//		}
		return false;
	}

	/**
	 * Method declared on ViewerDropAdapter
	 */
	public boolean validateDrop(Object target, int op, TransferData type) {
		return PlotDataItemTransfer.getInstance().isSupportedType(type) 
		|| PlotDataReferenceTransfer.getInstance().isSupportedType(type);
	}
}
