package org.gumtree.gumnix.sics.internal.ui.statusview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.gumtree.gumnix.sics.control.IDeviceController;
import org.gumtree.gumnix.sics.control.IDevicePropertyChangeListener;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlTreeViewer;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewerColumn;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewerContentProvider;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewerLabelProvider;
import org.gumtree.gumnix.sics.internal.ui.controlview.DeviceControllerNode;
import org.gumtree.gumnix.sics.internal.ui.controlview.PropertyTreeNode;
import org.gumtree.gumnix.sics.internal.ui.util.ControlViewerConstants.Column;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public class DevicePropertyViewer implements IDevicePropertyChangeListener {

	private ControlTreeViewer treeViewer;

	private IDeviceController deviceController;

	private List<ControlViewerColumn> columns;

	public DevicePropertyViewer(IDeviceController deviceController) {
		this.deviceController = deviceController;
	}

	public void createPartControl(Composite parent) {
		createTreeViewer(parent);
		// Adds listener to sics
//		ISicsManager.INSTANCE.control().instrument().addListener(this);
	}

	public void dispose() {
//		ISicsManager.INSTANCE.control().instrument().removeListener(this);
	}

	private void createTreeViewer(Composite parent) {
		treeViewer = new ControlTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeViewer.setContentProvider(new ControlViewerContentProvider());
		treeViewer.setLabelProvider(new ControlViewerLabelProvider());
		createColumns(treeViewer.getTree());
		treeViewer.setInput(new DeviceControllerNode(deviceController, treeViewer));
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object selectedObject = ((IStructuredSelection)event.getSelection()).getFirstElement();
				boolean previousExpandedState = treeViewer.getExpandedState(selectedObject);
				treeViewer.setExpandedState(selectedObject, !previousExpandedState);
			}
		});
	}

	private void createColumns(Tree tree) {
		columns = new ArrayList<ControlViewerColumn>();
		TreeColumn nodeColumn = new TreeColumn(tree, SWT.LEFT);
		nodeColumn.setText(Column.NODE.getLabel());
		nodeColumn.setWidth(250);
		columns.add(new ControlViewerColumn(Column.NODE, nodeColumn));

		TreeColumn currentColumn = new TreeColumn(tree, SWT.CENTER);
		currentColumn.setText(Column.CURRENT.getLabel());
		currentColumn.setWidth(80);
		columns.add(new ControlViewerColumn(Column.CURRENT, currentColumn));

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
	}

	public void propertyChanged(Device device, final Property property, String newValue) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(treeViewer != null) {
					PropertyTreeNode node = treeViewer.findNode(property);
					if(node != null && !treeViewer.getTree().isDisposed()) {
						treeViewer.refresh(node);
					}
				}
			}
		});
	}

}
