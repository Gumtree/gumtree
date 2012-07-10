package org.gumtree.gumnix.sics.ui.util;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.controllers.IGraphDataController;
import org.gumtree.gumnix.sics.control.controllers.IScanController;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;

public class ControllerEditorFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof IComponentController && adapterType.equals(IEditorDescriptor.class)) {
			if(adaptableObject instanceof IDynamicController) {
				return PlatformUI.getWorkbench().getEditorRegistry().findEditor(SicsUIConstants.ID_EDITOR_COMPONENT_CONTROL);
			} else if(adaptableObject instanceof IScanController) {
				return PlatformUI.getWorkbench().getEditorRegistry().findEditor(SicsUIConstants.ID_EDITOR_SCAN);
			} else if(adaptableObject instanceof IGraphDataController) {
				return PlatformUI.getWorkbench().getEditorRegistry().findEditor(SicsUIConstants.ID_EDITOR_COMPONENT_CONTROL);
			}
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IEditorDescriptor.class };
	}
	
}
