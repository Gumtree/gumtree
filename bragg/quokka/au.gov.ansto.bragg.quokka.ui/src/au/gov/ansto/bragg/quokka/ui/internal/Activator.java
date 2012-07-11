package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.util.eclipse.WorkspaceUtils;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.quokka.ui";

	// The shared instance
	private static Activator plugin;

	private boolean imageRegistryInitialised = false;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// Prepare data area
		IFolder folder = WorkspaceUtils
				.createWorkspaceFolder(SystemProperties.CONFIG_FOLDER
						.getValue());
		WorkspaceUtils.refreshFolder(folder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
