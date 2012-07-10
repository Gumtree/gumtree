package org.gumtree.gumnix.sics.internal.ui.controlview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.DeviceListenerAdapter;
import org.gumtree.gumnix.sics.control.IDeviceController;
import org.gumtree.gumnix.sics.control.IDeviceListener;
import org.gumtree.gumnix.sics.control.IDeviceProperty;
import org.gumtree.gumnix.sics.control.IDevicePropertyCallback;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewerConstants.Column;
import org.gumtree.ui.util.ITreeNode;
import org.gumtree.ui.util.TreeNode;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public class DeviceControllerNode extends TreeNode {

	private static Image imageDevice;

	private static Image motorDevice;

	static {
		if(Activator.getDefault() != null) {
			imageDevice = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/plugin_obj.gif").createImage();
			motorDevice = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/thread_view.gif").createImage();
		}
	}

	private Property primaryProperty;

	private DeviceControllerNode instance;

	private IDeviceListener controllerListener;

	public DeviceControllerNode(IDeviceController controller, StructuredViewer viewer) {
		super(controller, viewer);
		instance = this;
		getController().addDeviceListener(getControllerListener());
	}

	public ITreeNode[] getChildren() {
		Widget item = getViewer().testFindItem(this);
		item.setData("componentController", getController());
		List<ITreeNode> children = new ArrayList<ITreeNode>();
		for(Property property : (List<Property>)getDevice().getProperty()) {
			children.add(new PropertyTreeNode(property, getViewer(), this.getController()));
		}
		return children.toArray(new ITreeNode[children.size()]);
	}

	public String getColumnText(int columnIndex) {
		if(columnIndex == Column.NODE.getIndex()) {
			String text = getDevice().getLabel();
			if(text == null || text.equals(EMPTY_STRING)) {
				text = getDevice().getId();
			}
			if(text != null) {
				return text;
			}
		} else if(columnIndex == Column.CURRENT.getIndex()) {
			final ITreeNode treeNode = this;
			if(getController().getPropertyStatus(getController().getDefaultProperty()).equals(IDeviceController.PropertyStatus.OUT_OF_SYNC)) {
				IDevicePropertyCallback callback = new IDevicePropertyCallback() {
					public void handleReply(Property property, String value) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								getViewer().refresh(treeNode);
							}
						});
					}
				};
				getController().getPropertyCurrentValue(getController().getDefaultProperty(), callback);
				return "--";
			} else {
				return getController().getPropertyCurrentValue(getController().getDefaultProperty());
			}
		} else if(columnIndex == Column.TARGET.getIndex()) {
			return getController().getPropertyTargetValue(getController().getDefaultProperty());
		}
		return EMPTY_STRING;
	}

	public Image getColumnImage(int columnIndex) {
		if(columnIndex == Column.NODE.getIndex()) {
			if(getDevice().getDeviceType() != null && getDevice().getDeviceType().equals("Motor")) {
				return motorDevice;
			}
			return imageDevice;
		}
		return null;
	}

	public IDeviceController getController() {
		return (IDeviceController)getOriginalObject();
	}

//	public Property getPrimaryProperty() {
//		if(primaryProperty == null) {
//			primaryProperty = SicsUtils.getPrimaryProperty(getDevice());
//		}
//		return primaryProperty;
//	}

	private Device getDevice() {
		return getController().getDevice();
	}

	private IDeviceListener getControllerListener() {
		if(controllerListener == null) {
			controllerListener = new DeviceListenerAdapter() {
				public void propertyChanged(Device device, Property property, String newValue) {
					if(property instanceof IDeviceProperty) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								if(getViewer().getControl().isDisposed()) {
									getController().removeDeviceListener(controllerListener);
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
