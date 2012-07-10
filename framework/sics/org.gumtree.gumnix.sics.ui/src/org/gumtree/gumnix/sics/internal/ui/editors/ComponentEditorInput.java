package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.internal.ui.Activator;

public class ComponentEditorInput implements IEditorInput {

	private IComponentController controller;

	public ComponentEditorInput(IComponentController controller) {
		this.controller = controller;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/plugin_obj.gif");
	}

	public String getName() {
		return "Component Controller";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Component Controller";
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IComponentController.class) {
			return controller;
		}
		return Platform.getAdapterManager().getAdapter(controller, adapter);
	}

	public IComponentController getController() {
		return controller;
	}

}
