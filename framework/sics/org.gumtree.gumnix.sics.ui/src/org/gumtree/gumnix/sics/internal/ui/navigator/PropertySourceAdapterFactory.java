package org.gumtree.gumnix.sics.internal.ui.navigator;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;
import org.gumtree.gumnix.sics.ui.util.DefaultControllerNode;

public class PropertySourceAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adapterType.equals(IPropertySource.class) && adaptableObject instanceof DefaultControllerNode) {
			return new ComponentPropertySource(((DefaultControllerNode)adaptableObject).getController());
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IPropertySource.class };
	}

}
