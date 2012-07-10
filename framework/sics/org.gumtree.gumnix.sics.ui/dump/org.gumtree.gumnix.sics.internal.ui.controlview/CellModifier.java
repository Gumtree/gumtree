package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.gumtree.gumnix.sics.control.IDeviceController;

import ch.psi.sics.hipadaba.Property;

public class CellModifier implements ICellModifier {
	private StructuredViewer viewer;

	public CellModifier(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public boolean canModify(Object element, String property) {
		return true;
	}

	public Object getValue(Object element, String property) {
		if(element instanceof PropertyTreeNode) {
			PropertyTreeNode treeNode = (PropertyTreeNode)element;
			Property deviceProperty = treeNode.getProperty();
			IDeviceController controller = treeNode.getDeviceController();
			String value = controller.getPropertyTargetValue(deviceProperty);
			if(value == null) {
				return "";
			} else {
				return value;
			}
		} else if(element instanceof DeviceControllerNode) {
			DeviceControllerNode treeNode = (DeviceControllerNode)element;
			Property deviceProperty = treeNode.getController().getDefaultProperty();
			IDeviceController controller = treeNode.getController();
			String value = controller.getPropertyTargetValue(deviceProperty);
			if(value == null) {
				return "";
			} else {
				return value;
			}
		}
		return null;
	}

	public void modify(Object element, String property, Object value) {
		Object node = ((TreeItem)element).getData();
		if(node instanceof PropertyTreeNode && value instanceof String) {
			String newValue = (String)value;
			if(newValue == null || newValue.length() == 0) {
				return;
			}
			PropertyTreeNode treeNode = (PropertyTreeNode)node;
			Property deviceProperty = treeNode.getProperty();
			IDeviceController controller = treeNode.getDeviceController();
			controller.setPropertyValue(deviceProperty, newValue);
			viewer.refresh(element);
		} else if(node instanceof DeviceControllerNode && value instanceof String) {
			String newValue = (String)value;
			if(newValue == null || newValue.length() == 0) {
				return;
			}
			DeviceControllerNode treeNode = (DeviceControllerNode)node;
			Property deviceProperty = treeNode.getController().getDefaultProperty();
			IDeviceController controller = treeNode.getController();
			controller.setPropertyValue(deviceProperty, newValue);
			viewer.refresh(element);
		}
	}

}
