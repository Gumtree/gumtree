package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.ui.util.jface.ITreeNode;

public class LabelDecorator implements ILightweightLabelDecorator {

	private ImageDescriptor errorImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/ovr16/error_co.gif");

	private ImageDescriptor runningImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/ovr16/run_co.gif");

//	private Map<Object, ControllerStatus> statusCache;
	
	public LabelDecorator() {
//		statusCache = new HashMap<Object, ControllerStatus>();
	}
	
	public void decorate(Object element, IDecoration decoration) {
		IComponentController controller = null;
		ISicsController sicsController = null;
		if (element instanceof IComponentController) {
			controller = (IComponentController)element;	
		} else if (element instanceof ISicsController) {
			sicsController = (ISicsController)element;
		} else if (element instanceof ITreeNode) {
			ITreeNode treeNode =  (ITreeNode)element;
			if (treeNode.getOriginalObject() instanceof IComponentController) {
				controller = (IComponentController) treeNode.getOriginalObject();
			} else if(treeNode.getOriginalObject() instanceof ISicsController) {
				sicsController = (ISicsController) treeNode.getOriginalObject();
			}
		}
		if (controller != null) {
//			// Optimise: change decorator if status has changed
//			ControllerStatus oldStatus = statusCache.get(controller);
//			if (oldStatus != null && oldStatus.equals(controller.getStatus())) {
//				return;
//			}
//			// Cache status
//			statusCache.put(controller, controller.getStatus());
			if (controller.getStatus().equals(ControllerStatus.ERROR)) {
				decoration.addOverlay(errorImage);
			} else if (controller.getStatus().equals(ControllerStatus.RUNNING)) {
				decoration.addOverlay(runningImage);
			} else {
				decoration.addOverlay(null);
			}
		} else if (sicsController != null) {
//			// Optimise: change decorator if status has changed
//			ControllerStatus oldStatus = statusCache.get(sicsController);
//			if (oldStatus != null && oldStatus.equals(sicsController.getStatus())) {
//				return;
//			}
//			// Cache status
//			statusCache.put(sicsController, sicsController.getStatus());
			if (sicsController.getStatus().equals(ControllerStatus.ERROR)) {
				decoration.addOverlay(errorImage);
			} else if (sicsController.getStatus().equals(ControllerStatus.RUNNING)) {
				decoration.addOverlay(runningImage);
			} else {
				decoration.addOverlay(null);
			}
		}
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
