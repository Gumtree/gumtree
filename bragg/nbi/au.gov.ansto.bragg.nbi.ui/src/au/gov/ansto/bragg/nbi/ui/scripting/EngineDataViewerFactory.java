package au.gov.ansto.bragg.nbi.ui.scripting;

import org.eclipse.core.runtime.IAdapterFactory;
import org.gumtree.ui.scripting.IEngineDataTreeNode;
import org.gumtree.ui.scripting.IEngineDataViewer;

public class EngineDataViewerFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IEngineDataTreeNode) {
			return new DataSetEngineDataViewer();
		}
		return null;
	}

	public Class<?>[] getAdapterList() {
		return new Class[] { IEngineDataViewer.class };
	}

}
