package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.DeviceListenerAdapter;
import org.gumtree.gumnix.sics.control.IDeviceController;
import org.gumtree.gumnix.sics.control.IDeviceListener;
import org.gumtree.gumnix.sics.control.IDevicePropertyCallback;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewerConstants.Column;
import org.gumtree.ui.util.ITreeNode;
import org.gumtree.ui.util.TreeNode;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public class PropertyTreeNode extends TreeNode {

	private static Image imageProperty;

	private IDeviceController controller;

	private PropertyTreeNode instance;

	private IDeviceListener controllerListener;

	static {
		if(Activator.getDefault() != null) {
			imageProperty = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/annotation_obj.gif").createImage();
		}
	}

	public PropertyTreeNode(Property property, StructuredViewer viewer, IDeviceController controller) {
		super(property, viewer);
		this.controller = controller;
		if(viewer instanceof ControlTreeViewer) {
			((ControlTreeViewer)viewer).addPropertyTreeNode(property, this);
		}
		instance = this;
		getDeviceController().addDeviceListener(getControllerListener());
	}

	public String getColumnText(int columnIndex) {
		if(columnIndex == Column.NODE.getIndex()) {
			return getProperty().getId();
		} else if(columnIndex == Column.CURRENT.getIndex()) {
			final ITreeNode treeNode = this;
			if(getDeviceController().getPropertyStatus(getProperty()).equals(IDeviceController.PropertyStatus.OUT_OF_SYNC)) {
				IDevicePropertyCallback callback = new IDevicePropertyCallback() {
					public void handleReply(Property property, String value) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								getViewer().refresh(treeNode);
							}
						});
					}
				};
				getDeviceController().getPropertyCurrentValue(getProperty(), callback);
				return "--";
			} else {
				return getDeviceController().getPropertyCurrentValue(getProperty());
			}
		} else if(columnIndex == Column.TARGET.getIndex()) {
			return getDeviceController().getPropertyTargetValue(getProperty());
		}
		return EMPTY_STRING;
	}

	public Image getColumnImage(int columnIndex) {
		if(columnIndex == Column.NODE.getIndex()) {
			return imageProperty;
		}
		return null;
	}

	public Property getProperty() {
		return (Property)getOriginalObject();
	}

	public IDeviceController getDeviceController() {
		return controller;
	}

	private IDeviceListener getControllerListener() {
		if(controllerListener == null) {
			controllerListener = new DeviceListenerAdapter() {
				public void propertyChanged(Device device, Property property, String newValue) {
					if(property.equals(getProperty())) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								if(getViewer().getControl().isDisposed()) {
									getDeviceController().removeDeviceListener(controllerListener);
								} else {
									getViewer().refresh(instance);
								}
							}
						});
					}
				}
			};
		}
		return controllerListener;
	}
}
