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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;

/**
 * Supports dragging PlotDataItems from a structured viewer.
 * 
 * @author Danil Klimontov (dak)
 * @see PlotDataItem
 */
public class PlotTableDragListener extends DragSourceAdapter {

	private final StructuredViewer viewer;

	public PlotTableDragListener(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragFinished(DragSourceEvent event) {
		if (!event.doit)
			return;
		//if the gadget was moved, remove it from the source viewer
		if (event.detail == DND.DROP_MOVE) {
//			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
//			for (Iterator it = selection.iterator(); it.hasNext();) {
//				((Gadget) it.next()).setParent(null);
//			}
			viewer.refresh();
		}
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragSetData(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		PlotDataItem data = (PlotDataItem) selection.getFirstElement();
//		PlotDataItem[] data = (PlotDataItem[]) selection.toList().toArray(new PlotDataItem[selection.size()]);
		
		if (PlotDataItemTransfer.getInstance().isSupportedType(event.dataType)) {
			System.out.println("drag> "+data);
			event.data = data;
		}
	}

	/**
	 * Method declared on DragSourceListener
	 */
	public void dragStart(DragSourceEvent event) {
		event.doit = !viewer.getSelection().isEmpty();
	}
}
