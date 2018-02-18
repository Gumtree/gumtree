package org.gumtree.control.ui.viewer.model;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.gumtree.control.core.IControllerData;
import org.gumtree.control.core.IDriveableController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.model.ModelUtils;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.model.PropertyConstants.Privilege;
import org.gumtree.control.model.PropertyConstants.PropertyType;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.control.ui.viewer.ControlViewerConstants.Column;

public class DynamicControllerNode extends DefaultControllerNode {

	private static Image imageComponent;
	
	private static Image imageDrivable;
	
	private static Image imageInternal;
	
	private static Image imageLocked;

	private ISicsControllerListener listener;

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
		controller.addControllerListener(listener);
	}

	public String getColumnText(int columnIndex) {
		if (columnIndex == 0) {
			return super.getColumnText(0);
		} else if (columnIndex == Column.DEVICE.getIndex()) {
			String deviceName = ModelUtils.getPropertyFirstValue(getDynamicController().getModel(), PropertyType.SICS_DEV);
			if(deviceName != null) {
				return deviceName;
			}
		} else if (columnIndex == Column.CURRENT.getIndex()) {
//			try {
//				getDynamicController().getValue(
////						new DynamicControllerCallbackAdapter() {
////					public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
////						listener.valueChanged(controller, value);
////					}
////				}
//			);
//			} catch (SicsException e) {
//				e.printStackTrace();
//			}
			try {
				currentValue = getDynamicController().getValue().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return currentValue;
		} else if (columnIndex == Column.TARGET.getIndex()) {
//			try {
//				getDynamicController().getTargetValue(
////						new DynamicControllerCallbackAdapter() {
////					public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
////						// Update UI if updated value is not null
////						if (value != null) {
////							listener.targetChanged(controller, value);
////						}
////					}
////				}
//						);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return targetValue;
			try {
				targetValue = getDynamicController().getTargetValue().getStringData();
			} catch (Exception e) {
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
//			getDynamicController().getValue(
////					new DynamicControllerCallbackAdapter() {
////				public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
////					listener.valueChanged(controller, value);
////				}
////			}, true
//					);
			getDynamicController().refreshValue();
			// Refresh UI
			getViewer().refresh(this);
		} catch (SicsException e) {
			e.printStackTrace();
		}
	}

	public Image getColumnImage(int columnIndex) {
		if(columnIndex == 0) {
			Privilege privilege = ModelUtils.getPrivilege(getController().getModel());
			if (getController() instanceof IDriveableController) {
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

	private class ControllerListener implements ISicsControllerListener {

		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
			updateViewer();			
		}
		@Override
		public void updateValue(Object oldValue, Object newValue) {
			if (newValue != null && !newValue.toString().equals(currentValue)) {
				currentValue = newValue.toString();
				updateViewer();			
			}
		}
		@Override
		public void updateEnabled(boolean isEnabled) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void updateTarget(Object oldValue, Object newValue) {
			if (newValue != null && !newValue.toString().equals(targetValue)) {
				targetValue = newValue.toString();
				updateViewer();			
			}
		}
	}

	public String toString() {
		return "[DynamicControllerNode] : " + getController().getPath();
	}

}
