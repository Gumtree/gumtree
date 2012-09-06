package org.gumtree.pydev.configurator;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.endsWith;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IStartup;
import org.gumtree.pydev.configurator.internal.Activator;
import org.gumtree.util.eclipse.OsgiUtils;
import org.gumtree.util.string.StringUtils;
import org.osgi.framework.Bundle;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterpreterRegister implements IStartup {

	private static final Logger logger = LoggerFactory
			.getLogger(InterpreterRegister.class);

	@Override
	public void earlyStartup() {
		try {
			// Prepare interpreter info
			IInterpreterManager interpreterManager = PydevPlugin
					.getJythonInterpreterManager();
			IInterpreterInfo info = null;
			try {
				// Get existing interpreter info
				info = interpreterManager.getDefaultInterpreterInfo(false);
			} catch (Exception e) {
				// Create one if info is missing
				String interpreterPath = OsgiUtils.findFilePath(
						"org.python.jython", "jython.jar");
				info = interpreterManager.createInterpreterInfo(
						interpreterPath, new NullProgressMonitor(), false);
			}
			List<String> libraries = ((InterpreterInfo) info).libs;

			// Register gumpy if necessary
			try {
				String scriptPath = OsgiUtils.findFilePath(
						"org.gumtree.gumpy.scripts", "scripts");
				addPath(libraries, "org.gumtree.gumpy.scripts", scriptPath);
				logger.info("Registered script bundle org.gumtree.gumpy.scripts to pydev.");
			} catch (Exception e) {
				logger.info("Script bundle org.gumtree.gumpy.scripts not found.");
			}

			// Register nbi
			try {
				String scriptPath = OsgiUtils.findFilePath(
						"au.gov.ansto.bragg.nbi.scripts", "scripts");
				addPath(libraries, "au.gov.ansto.bragg.nbi.scripts", scriptPath);
				logger.info("Registered script bundle au.gov.ansto.bragg.nbi.scripts to pydev.");
			} catch (Exception e) {
				logger.info("Script bundle au.gov.ansto.bragg.nbi.scriptss not found.");
			}

			// Add libraries
			String filterString = PydevConfiguratorProperties.PYDEV_INCLUDE_BUNDLES
					.getValue();
			List<String> filters = StringUtils.split(filterString, ",");
			for (Bundle bundle : Activator.getContext().getBundles()) {
				// Check if bundle is excluded
				boolean isInclude = false;
				for (String filter : filters) {
					if (bundle.getSymbolicName().matches(filter)) {
						isInclude = true;
						break;
					}
				}
				if (!isInclude) {
					continue;
				}
				// Find all java packages from jar or folder
				File[] javaFiles = OsgiUtils.getBundleClasspaths(bundle);
				for (File javaFile : javaFiles) {
					if (javaFile.getName().endsWith(".jar")) {
						addPath(libraries, bundle.getSymbolicName(),
								javaFile.getAbsolutePath());
					} else {
						File binDir = new File(javaFile, "target");
						if (binDir.exists()) {
							// PyDev does not read from directory
							// So the next best thing to do is to read from
							// Maven produced jar
							File file = (File) select(
									binDir.listFiles(),
									having(on(File.class).getName(),
											endsWith("SNAPSHOT.jar"))).get(0);
							addPath(libraries, bundle.getSymbolicName(),
									file.getAbsolutePath());
							// } else {
							// addPath(libraries, javaFile.getAbsolutePath());
						}
					}
				}

			}
			// Set to PyDev
			interpreterManager.setInfos(new IInterpreterInfo[] { info }, null,
					new NullProgressMonitor());
		} catch (Exception e) {
			logger.error("Failed to register interpreter", e);
		}
	}

	private void addPath(List<String> libraries, String key, String path) {
		// Remove old entry (based on key)
		int index = -1;
		for (int i = 0; i < libraries.size(); i++) {
			if (libraries.get(i).contains(key)) {
				index = i;
			}
		}
		if (index >= 0) {
			libraries.remove(index);
		}
		// Add to library
		if (!libraries.contains(path)) {
			libraries.add(path);
		}
	}

}
