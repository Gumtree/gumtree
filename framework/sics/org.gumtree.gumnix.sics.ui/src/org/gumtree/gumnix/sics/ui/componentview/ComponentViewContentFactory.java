package org.gumtree.gumnix.sics.ui.componentview;

import org.eclipse.core.runtime.IAdapterFactory;
import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;

public class ComponentViewContentFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adapterType.equals(IComponentViewContent.class)) {
			if(adaptableObject instanceof IDynamicController) {
				return new DynamicControllerViewContent();
//			} else if(adaptableObject instanceof IOneDDataController) {
//				return new OneDGraphicDataViewContent();
			} else if(adaptableObject instanceof ICommandController) {
				return new ScanControllerViewContent();
			}
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IComponentViewContent.class };
	}

}
