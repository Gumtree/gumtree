package org.gumtree.control.ui.viewer.model;

import org.eclipse.core.runtime.IAdapterFactory;
import org.gumtree.control.core.ICommandController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;

public class ControllerNodeFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ISicsController
				&& adapterType.equals(ISicsTreeNode.class)) {
			if (adaptableObject instanceof IDynamicController) {
				return new DynamicControllerNode(
						(IDynamicController) adaptableObject);
			} else if (adaptableObject instanceof ICommandController) {
				return new CommandControllerNode(
						(ICommandController) adaptableObject);
			}
			return new DefaultControllerNode(
					(ISicsController) adaptableObject);
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { ISicsTreeNode.class };
	}

	public static Object getControllerNode(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ISicsController
				&& adapterType.equals(ISicsTreeNode.class)) {
			if (adaptableObject instanceof IDynamicController) {
				return new DynamicControllerNode(
						(IDynamicController) adaptableObject);
			} else if (adaptableObject instanceof ICommandController) {
				return new CommandControllerNode(
						(ICommandController) adaptableObject);
			}
			return new DefaultControllerNode(
					(ISicsController) adaptableObject);
		}
		return null;
	}

}
