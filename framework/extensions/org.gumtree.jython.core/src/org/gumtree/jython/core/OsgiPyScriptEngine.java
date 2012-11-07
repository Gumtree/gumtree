package org.gumtree.jython.core;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

import javax.script.ScriptEngineFactory;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.jython.core.internal.Activator;
import org.gumtree.scripting.ObservableScriptContext;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.util.eclipse.EclipseUtils;
import org.gumtree.util.eclipse.OsgiUtils;
import org.gumtree.util.string.StringUtils;
import org.python.jsr223.ExtendedPyScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiPyScriptEngine extends ExtendedPyScriptEngine {

	private static final String PLUGIN_PYTHON = "org.python.jython";

	private static final String HEADER_GUMTREE_SCRIPTS_JYTHON = "Gumtree-Scripts-Jython";

	private static final Logger logger = LoggerFactory
			.getLogger(OsgiPyScriptEngine.class);

	private IDataAccessManager dataAccessManager;

	public OsgiPyScriptEngine(ScriptEngineFactory factory) {
		super(factory, new ObservableScriptContext());
	}

	public void activate() {
		// [Tony][2010-10-08] Initialise
		interp.runsource("import sys", "<script>");
		interp.set("__context__", getContext());

		// Set prefix
		try {
			File prefixLocation = EclipseUtils.find(PLUGIN_PYTHON, "/")
					.toLocalFile(EFS.NONE, new NullProgressMonitor());
			interp.runsource(
					"sys.prefix = '"
							+ prefixLocation.getAbsolutePath().replace("\\",
									"/") + "'", "<script>");
		} catch (CoreException e) {
			logger.error("Failed to load Jython location into sys.prefix.", e);
		}

		// Load lib
		try {
			File libLocation = EclipseUtils.find(PLUGIN_PYTHON, "Lib")
					.toLocalFile(EFS.NONE, new NullProgressMonitor());
			interp.runsource("sys.path.append('"
					+ libLocation.getAbsolutePath().replace("\\", "/") + "')",
					"<script>");
		} catch (CoreException e) {
			logger.error("Failed to load Jython library from plug-in.", e);
		}

		// Load site packaes
		try {
			File libLocation = EclipseUtils.find(PLUGIN_PYTHON,
					"Lib/site-packages").toLocalFile(EFS.NONE,
					new NullProgressMonitor());
			for (File fileLocation : libLocation.listFiles()) {
				if (fileLocation.isDirectory()) {
					interp.runsource("sys.path.append('"
							+ fileLocation.getAbsolutePath().replace("\\", "/")
							+ "')", "<script>");
				}
			}
		} catch (CoreException e) {
			logger.error("Failed to load Jython site package from plug-in.", e);
		}

		// Wait for loading OSGi packages info
		if (!Activator.getDefault().isPackageLoaderInitialised()) {
			logger.info("Waiting for package loader to be ready");
		}
		while (!Activator.getDefault().isPackageLoaderInitialised()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("Error occured while waiting for package loader.",
						e);
			}
		}

		// Load system scripts
		try {
			logger.info("Loading info.py for introspection");
			InputStream in = EclipseUtils.find(Activator.PLUGIN_ID,
					"scripts/info.py").openInputStream(EFS.NONE,
					new NullProgressMonitor());
			interp.execfile(in);
		} catch (CoreException e) {
			logger.error("Failed to load buildin script", e);
		}

		// [GUMTREE-518] Auto discovery
		logger.info("Discovering scripts from bundles");
		for (URI uri : OsgiUtils
				.findBundleResources(HEADER_GUMTREE_SCRIPTS_JYTHON)) {
			logger.info("Loading script from path " + uri.toString());
			File scriptFolder = new File(uri);
			if (scriptFolder.exists()) {
				// Windows specific fix
				String absolutePath = scriptFolder.getAbsolutePath().replace(
						'\\', '/');
				interp.runsource("sys.path.append('" + absolutePath + "')",
						"<script>");
			}
		}

		// Append workspace to path as well
		String workspace = Platform.getInstanceLocation().getURL().getFile();
		// Windows string conversion
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			workspace = workspace.substring(1, workspace.length() - 1);
		}
		logger.info("Appending workspace " + workspace + " to path");
		interp.runsource("sys.path.append('" + workspace + "')");

		// Initialisation scripts
		String scripts = JythonCoreProperties.JYTHON_INITIALISATION_SCRIPTS
				.getValue();
		if (!StringUtils.isEmpty(scripts) && getDataAccessManager() != null) {
			for (String script : StringUtils.split(scripts, ",")) {
				try {
					InputStream in = getDataAccessManager().get(
							URI.create(script), InputStream.class);
					interp.execfile(in);
				} catch (Exception e) {
					logger.error("Failed to load initialisation script: "
							+ script, e);
				}
				logger.info("Loaded initialise script: " + script);
			}
		}

		logger.info("Jython scripting engine is ready.");
	}

	public IDataAccessManager getDataAccessManager() {
		if (dataAccessManager == null) {
			dataAccessManager = ServiceUtils.getService(IDataAccessManager.class);
		}
		return dataAccessManager;
	}

}
