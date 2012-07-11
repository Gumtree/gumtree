/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.echidna.ui.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewer;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.gumtree.gumnix.sics.ui.controlview.NodeSet;

import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.echidna.ui.internal.Activator;

/**
 * @author nxi
 * Created on 29/07/2009
 */
public class EchidnaControlView extends ViewPart {

	private Composite parent;

	private ControlViewer viewer;

	/**
	 * 
	 */
	public EchidnaControlView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new FillLayout());
		viewer = new ControlViewer();
		INodeSet nodeSet = null;
		try{
			File filterFile = ConverterLib.findFile("au.gov.ansto.bragg.echidna", 
			"sicsfilters/UserFilter.xml");
			nodeSet = NodeSet.read(new FileInputStream(filterFile));
		}catch (Exception e) {
			e.printStackTrace();
		}
		viewer.createPartControl(parent, nodeSet);
		for (TreeColumn column : viewer.getTreeViewer().getTree().getColumns())
			column.pack();
		createActions();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void createActions() {
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createRefreshAction());
//		IMenuManager mgr = bars.getMenuManager();
//		mgr.add(createColumnSelectionAction());
	}

//	private Action createColumnSelectionAction() {
//		Action action = new Action("Select columns") {
//			public void run() {
//				ListSelectionDialog dialog = new ListSelectionDialog(getSite()
//						.getShell(), viewer.getAllColumns(),
//						new ArrayContentProvider(), new LabelProvider(),
//						"Select columns for display");
//				dialog.setHelpAvailable(true);
//				dialog.setInitialSelections(viewer.getVisibleColumns());
//				dialog.open();
//				if(dialog.getResult() != null) {
//					final Object[] selecton = dialog.getResult();
//					Display.getDefault().asyncExec(new Runnable() {
//						public void run() {
//							for(ITreeViewerColumn column : viewer.getAllColumns()) {
//								viewer.setColumnVisibility(column, false);
//							}
//							for(Object column : selecton) {
//								viewer.setColumnVisibility((ITreeViewerColumn)column, true);
//							}
//							parent.update();
//						}
//					});
//				}
//
//			}
//		};
//		action.setImageDescriptor(Activator.imageDescriptorFromPlugin(
//				Activator.PLUGIN_ID, "icons/full/obj16/layout_co.gif"));
//		return action;
//	}

	private Action createRefreshAction() {
		Action action = new Action("Refresh View") {
			public void run() {
				if(viewer != null) {
					viewer.refresh();
				}
			}
		};
		action.setImageDescriptor(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/refresh_nav.gif"));
		return action;
	}

	public void dispose() {
		super.dispose();
		viewer.dispose();
		viewer = null;
		parent = null;
	}

}
