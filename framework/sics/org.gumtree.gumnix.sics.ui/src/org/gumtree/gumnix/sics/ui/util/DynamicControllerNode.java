package org.gumtree.gumnix.sics.ui.util;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDrivableController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerCallbackAdapter;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.PropertyConstants.Privilege;
import org.gumtree.gumnix.sics.core.PropertyConstants.PropertyType;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.ui.util.ControlViewerConstants.Column;

public class DynamicControllerNode extends DefaultControllerNode {

	private static Image imageComponent;
	
	private static Image imageDrivable;
	
	private static Image imageInternal;
	
	private static Image imageLocked;

	private IDynamicControllerListener listener;

	static {
		if(Activator.getDefault() != null) {
			imageComponent = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/envvar_obj.gif").createImage();
			imageDrivable = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/thread_view.gif").createImage();
			imageInternal = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/innerinterface_public_obj.gif").createImage();
			imageLocked = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/lock.png").createImage();
		}
	}

	private String currentValue = "--";

	private String targetValue = "--";

	public DynamicControllerNode(IDynamicController controller) {
		super(controller);
		listener = new ControllerListener();
		controller.addComponentListener(listener);
	}

	public String getColumnText(int columnIndex) {
		if (columnIndex == 0) {
			return super.getColumnText(0);
		} else if (columnIndex == Column.DEVICE.getIndex()) {
			String deviceName = SicsUtils.getPropertyFirstValue(getDynamicController().getComponent(), PropertyType.SICS_DEV);
			if(deviceName != null) {
				return deviceName;
			}
		} else if (columnIndex == Column.CURRENT.getIndex()) {
			try {
				getDynamicController().getValue(new DynamicControllerCallbackAdapter() {
					public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
						listener.valueChanged(controller, value);
					}
				});
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
			return currentValue;
		} else if (columnIndex == Column.TARGET.getIndex()) {
			try {
				getDynamicController().getTargetValue(new DynamicControllerCallbackAdapter() {
					public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
						// Update UI if updated value is not null
						if (value != null) {
							listener.targetChanged(controller, value);
						}
					}
				});
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
			return targetValue;
		} else if (columnIndex == Column.STATUS.getIndex()) {
			if (!getDynamicController().isEnabled()) {
				return "disabled";
			}
		} else if (columnIndex == Column.MESSAGE.getIndex()) {
			return getDynamicController().getErrorMessage();
		}
		return super.getColumnText(columnIndex);
	}

	// It does two things: clear error and fetch new value
	public void refreshNode() {
		try {
			// Clear error
			getDynamicController().clearError();
			// Get new value
			getDynamicController().getValue(new DynamicControllerCallbackAdapter() {
				public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
					listener.valueChanged(controller, value);
				}
			}, true);
			// Refresh UI
			getViewer().refresh(this);
		} catch (SicsIOException e) {
			e.printStackTrace();
		}
	}

	public Image getColumnImage(int columnIndex) {
		if(columnIndex == 0) {
			Privilege privilege = SicsUtils.getPrivilege(getController().getComponent());
			if (getController() instanceof IDrivableController) {
				return imageDrivable;
			} else if (Privilege.INTERNAL.equals(privilege)) {
				return imageInternal;
			} else if (Privilege.READ_ONLY.equals(privilege)) {
				return imageLocked;
			} else {
				return imageComponent;
			}
		}
		return null;
	}

	public IDynamicController getDynamicController() {
		return (IDynamicController)getOriginalObject();
	}

	private void updateViewer() {
		final DynamicControllerNode node = this;
		Display display = PlatformUI.getWorkbench().getDisplay();
		if(!getViewer().getControl().isDisposed() && !display.isDisposed()) {
			display.asyncExec(new Runnable() {
				public void run() {
					if(!getViewer().getControl().isDisposed()) {
						getViewer().update(node, null);
					}
				}
			});
		}
	}

	private class ControllerListener extends DynamicControllerListenerAdapter {
		private ControllerListener() {
			super();
		}
		public void valueChanged(IDynamicController controller, IComponentData newValue) {
			if (currentValue.equals(newValue.getStringData())) {
				return;
			}
			currentValue = newValue.getStringData();
			updateViewer();
		}
		public void targetChanged(IDynamicController controller, IComponentData newTarget) {
//			// TODO: This may throw NPE?
//			if (newTarget == null) {
//				return;
//			}
			if (targetValue != null && targetValue.equals(newTarget.getStringData())) {
				return;
			}
			targetValue = newTarget.getStringData();
			updateViewer();
		}
	}

	public String toString() {
		return "[DynamicControllerNode] : " + getController().getPath();
	}

}
