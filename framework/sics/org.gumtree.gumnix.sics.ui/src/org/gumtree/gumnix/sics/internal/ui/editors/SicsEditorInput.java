package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;

public class SicsEditorInput implements IEditorInput {

	private ISicsController controller;
	
	private INodeSet nodeSet;

	public SicsEditorInput(ISicsController controller) {
		this(controller, null);
	}

	public SicsEditorInput(ISicsController controller, INodeSet nodeSet) {
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

	public Object getAdapter(Class adapter) {
		if (adapter == ISicsController.class) {
			return controller;
		} else if (adapter == INodeSet.class) {
			return nodeSet;
		}
		return Platform.getAdapterManager().getAdapter(controller, adapter);
	}

}
