package org.gumtree.jython.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.core.service.ServiceManager;
import org.gumtree.jython.core.internal.Activator;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.eclipse.OsgiUtils;
import org.gumtree.util.string.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.python.core.Py;
import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiPackageLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(OsgiPackageLoader.class);

	private boolean isInitialised;

	private Lock lock;

	public OsgiPackageLoader() {
		lock = new ReentrantLock();
	}

	public void load() {
		lock.lock();
		logger.info("Loading OSGi bundle packages into Jython.");
		IServiceManager serviceManager = new ServiceManager();
		EventAdmin eventAdmin = serviceManager.getService(EventAdmin.class);
		if (Py.getSystemState() == null) {
			// Initialise system state if necessary
			new PySystemState();
		}
		if (!isInitialised) {
			String filterString = JythonCoreProperties.JYTHON_EXCLUDE_BUNDLES
					.getValue();
			List<String> filters = StringUtils.split(filterString, ",");
			for (Bundle bundle : Activator.getContext().getBundles()) {
				// Check if bundle is excluded
				boolean isExcluded = false;
				for (String filter : filters) {
					if (bundle.getSymbolicName().matches(filter)) {
						isExcluded = true;
						break;
					}
				}
				if (isExcluded) {
					logger.info("Exclude loading bundle: "
							+ bundle.getSymbolicName());
					continue;
				}

				// Find all java packages from jar or folder
				File[] javaFiles = OsgiUtils.getBundleClasspaths(bundle);
				for (File javaFile : javaFiles) {
					// Notify which classpath we are loading to the OSGi system
					if (eventAdmin != null) {
						Map<String, String> props = new HashMap<String, String>(
								2);
						props.put(
								PlatformUtils.EVENT_PROP_RUNTIME_STARTUP_MESSAGE,
								"Loading Jython module " + javaFile.getName());
						eventAdmin
								.postEvent(new Event(
										PlatformUtils.EVENT_TOPIC_RUNTIME_STARTUP_MESSAGE,
										props));
					}
					if (javaFile.getName().endsWith(".jar")) {
						logger.info("Loading {} from bundle {}",
								javaFile.getName(), bundle.getSymbolicName());
						// PySystemState is not initialised!!!
						PySystemState.packageManager.addJar(
								javaFile.getAbsolutePath(), true);
					} else {
						logger.info("Loading directory {} from bundle {}",
								javaFile, bundle.getSymbolicName());
						PySystemState.packageManager.addDirectory(javaFile);
						// IDE mode
						PySystemState.packageManager.addDirectory(new File(
								javaFile, "bin"));
					}
				}
			}
			isInitialised = true;
		}
		logger.info("OSGi bundle packages have been loaded into Jython.");
		lock.unlock();
	}

	public boolean isInitialised() {
		return isInitialised;
	}

}
