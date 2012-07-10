/**
 * 
 */
package org.gumtree.vis.io;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IExporterProvider;

/**
 * @author nxi
 *
 */
public class ExporterManager {

	private static final String CLASS_OSGI_EXPORTER_RESOLVER = "org.gumtree.vis.io.OSGIExporterResolver";
	
	private static final String CLASS_OSGI_BUNDLE_CONTEXT = "org.osgi.framework.BundleContext";
	private static final String EXPORTER_1D_PROPERTY_NAME = "org.gumtree.data.nexus.exporter.1d";
	private static final String EXPORTER_2D_PROPERTY_NAME = "org.gumtree.data.nexus.exporter.2d";
	private static List<IExporter> exporters;
	private static List<IExporter> exporter1ds;
	private static List<IExporter> exporter2ds;

	static {
		findExporters();
	}
	
	private static void findExporters() {
		if (exporters == null) {
			exporters = new ArrayList<IExporter>();
		}
		findJavaExporters();
		findOSGIExporters();
//		findExporterFromSystemProperties();
	}

	private static void findOSGIExporters() {
		try {
			// [ANSTO][Tony][2011-05-25] Check to see if OSGi classes are
			// available before loading the factory
			Class<?> osgiClass = Class.forName(CLASS_OSGI_BUNDLE_CONTEXT);
			if (osgiClass != null) {
				// Use reflection in case OSGi is not available at runtime
				OSGIExporterResolver osgiResolver = (OSGIExporterResolver) Class
						.forName(CLASS_OSGI_EXPORTER_RESOLVER).newInstance();
				exporters.addAll(osgiResolver.getExporter());
			}
		} catch (Exception e) {
			// Don't worry if we can't find the osgi resolver
			e.printStackTrace();
		}
	}

	private static void findJavaExporters() {
		ServiceLoader<IExporterProvider> providers = ServiceLoader.load(IExporterProvider.class);
		for (IExporterProvider provider : providers) {
			exporters.addAll(provider.getExporters());
		}
	}

	private static void findExporter2DFromSystemProperties() {
		exporter2ds = new ArrayList<IExporter>();
		String exporterNames = System.getProperty(EXPORTER_2D_PROPERTY_NAME);
		if (exporterNames != null) {
			String[] exporterNameArray = exporterNames.split(",");
			try {
				for (String name : exporterNameArray) {
					for (IExporter exporter : exporters) {
						if (name.trim().equals(exporter.getClass().getName())) {
							exporter2ds.add(exporter);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void findExporter1DFromSystemProperties() {
		exporter1ds = new ArrayList<IExporter>();
		String exporterNames = System.getProperty(EXPORTER_1D_PROPERTY_NAME);
		if (exporterNames != null) {
			String[] exporterNameArray = exporterNames.split(",");
			try {
				for (String name : exporterNameArray) {
					for (IExporter exporter : exporters) {
						if (name.trim().equals(exporter.getClass().getName())) {
							exporter1ds.add(exporter);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static List<IExporter> getExporters() {
		return exporters;
	}
	
	public static List<IExporter> get1DExporters() {
		if (exporter1ds == null) {
			findExporter1DFromSystemProperties();
		}
		return exporter1ds;
	}
	
	public static List<IExporter> get2DExporters() {
		if (exporter2ds == null) {
			findExporter2DFromSystemProperties();
		}
		return exporter2ds;
	}
	
	public static List<IExporter> get3DExporters() {
		if (exporter2ds == null) {
			findExporter2DFromSystemProperties();
		}
		return exporter2ds;
	}

}
