package au.gov.ansto.bragg.wombat.ui.internal.status;

import org.eclipse.core.runtime.IAdapterFactory;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.ui.ISicsStatusContent;

public class SicsStatusAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof ISicsController && adapterType.equals(ISicsStatusContent.class)) {
			return new WombatStatusContent();
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { ISicsStatusContent.class };
	}

}
