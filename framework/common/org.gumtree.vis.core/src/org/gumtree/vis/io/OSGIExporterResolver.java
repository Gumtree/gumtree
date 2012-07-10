package org.gumtree.vis.io;

import java.util.List;

import org.gumtree.vis.core.internal.Activator;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IExporterProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OSGIExporterResolver {

	public List<IExporter> getExporter(){
		BundleContext context = Activator.getContext();
		ServiceReference[] refs = null;
		try {
			refs = context.getServiceReferences(IExporterProvider.class.getName(), null);
		} catch (InvalidSyntaxException e) {
		}
		if (refs != null) {
			for (ServiceReference ref : refs) {
				IExporterProvider provider = (IExporterProvider) context.getService(ref);
				return provider.getExporters();
			}
		}
		return null;
	}
}
