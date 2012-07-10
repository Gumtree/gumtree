package org.gumtree.gumnix.sics.ui.util;

import org.eclipse.core.runtime.IAdapterFactory;
import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.controllers.IGraphDataController;

public class ControllerNodeFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IComponentController
				&& adapterType.equals(ISicsTreeNode.class)) {
			if (adaptableObject instanceof IDynamicController) {
				return new DynamicControllerNode(
						(IDynamicController) adaptableObject);
			} else if (adaptableObject instanceof IGraphDataController) {
				return new GraphicDataControllerNode(
						(IGraphDataController) adaptableObject);
			} else if (adaptableObject instanceof ICommandController) {
				return new CommandControllerNode(
						(ICommandController) adaptableObject);
			}
			return new DefaultControllerNode(
					(IComponentController) adaptableObject);
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { ISicsTreeNode.class };
	}

}
