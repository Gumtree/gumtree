package org.gumtree.pydev.configurator;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IStartup;
import org.gumtree.pydev.configurator.internal.Activator;
import org.gumtree.util.eclipse.OsgiUtils;
import org.gumtree.util.string.StringUtils;
import org.osgi.framework.Bundle;
import org.python.pydev.ast.interpreter_managers.InterpreterInfo;
import org.python.pydev.ast.interpreter_managers.InterpreterManagersAPI;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.plugin.PydevPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterpreterRegister implements IStartup {

	private static final Logger logger = LoggerFactory
			.getLogger(InterpreterRegister.class);

	@Override
	public void earlyStartup() {
		try {
			// Prepare interpreter info
//			IInterpreterManager interpreterManager = PydevPlugin
//					.getJythonInterpreterManager();
//			IInterpreterManager manager = PydevPlugin.getInterpreterManager(
//					PydevInterpreterPreferences.JYTHON_INTERPRETER_TYPE);
			IInterpreterManager interpreterManager = InterpreterManagersAPI.getJythonInterpreterManager();
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
						// PyDev does not read from directory
						// So the next best thing to do is to read from
						// Maven produced jar
						File jar = findSnapshotJar(new File(javaFile, "target"));
						if (jar != null) {
							addPath(libraries, bundle.getSymbolicName(),
									jar.getAbsolutePath());
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

	/**
	 * Returns the Maven built jar in the given folder, or null when there is
	 * none. Deliberately plain Java: the lambdaj equivalent this replaces went
	 * through cglib, which cannot call ClassLoader.defineClass reflectively on
	 * a module based JRE and died with InaccessibleObjectException - an Error
	 * that escaped earlyStartup() and left PyDev unconfigured.
	 */
	private static File findSnapshotJar(File targetDir) {
		File[] files = targetDir.listFiles();
		if (files == null) {
			return null;
		}
		for (File file : files) {
			if (file.getName().endsWith("SNAPSHOT.jar")) {
				return file;
			}
		}
		return null;
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
