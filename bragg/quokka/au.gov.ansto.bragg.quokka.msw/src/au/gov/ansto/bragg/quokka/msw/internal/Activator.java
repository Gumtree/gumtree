package au.gov.ansto.bragg.quokka.msw.internal;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.msw.model.DataSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.quokka.msw"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
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

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	// helper
	public static DataSource getEntry(String path) {
        Bundle bundle = Platform.getBundle(PLUGIN_ID);
        if (isReady(bundle)) {
            URL fullPathString = FileLocator.find(bundle, new Path(path), null);
            if (fullPathString != null)
            	return new DataSource(fullPathString);
        }

        return new DataSource(new File("../au.gov.ansto.bragg.quokka.msw/" + path));

		//return "file:../org.gumtree.msw/" + path;
		
		// TODO use BundleContext
		//return getContext().getBundle().getEntry("resources/msw.xsd");
		//try {
		//	return new URL("file:../org.gumtree.msw/" + path);
		//}
		//catch (MalformedURLException e) {
		//	return null;
		//}
	}
	
    private static boolean isReady(Bundle bundle) {
    	if (bundle == null)
    		return false;
    	
    	return (bundle.getState() & (Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0;
    }
}
