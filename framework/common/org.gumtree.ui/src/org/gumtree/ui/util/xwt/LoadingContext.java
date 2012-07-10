package org.gumtree.ui.util.xwt;

import org.eclipse.e4.xwt.DefaultLoadingContext;
import org.gumtree.core.object.ObjectFactory;

public class LoadingContext extends DefaultLoadingContext {

	@Override
	public Class<?> loadClass(String name) {
		Class<?> clazz = super.loadClass(name);
		if (clazz == null) {
			try {
				clazz = ObjectFactory.instantiateClass(name);
			} catch (ClassNotFoundException e) {
			}
		}
		return clazz;
	}
	
}
