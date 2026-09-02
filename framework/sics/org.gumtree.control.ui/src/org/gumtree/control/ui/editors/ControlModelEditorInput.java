package org.gumtree.control.ui.editors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.control.ui.viewer.model.INodeSet;

public class ControlModelEditorInput implements IEditorInput {

	private ISicsController controller;

	private INodeSet nodeSet;

	public ControlModelEditorInput(ISicsController controller) {
		this(controller, null);
	}

	public ControlModelEditorInput(ISicsController controller, INodeSet nodeSet) {
		this.controller = controller;
		this.nodeSet = nodeSet;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/server.gif");
	}

	public String getName() {
		return "Sics Controller";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "SICS";
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == ISicsController.class) {
			return controller;
		} else if (adapter == INodeSet.class) {
			return nodeSet;
		}
		if (controller != null) {
			return Platform.getAdapterManager().getAdapter(controller, adapter);
		}
		return null;
	}

}
