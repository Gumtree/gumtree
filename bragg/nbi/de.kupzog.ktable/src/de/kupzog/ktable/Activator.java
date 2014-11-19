package de.kupzog.ktable;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "de.kupzog.ktable";

//    private static Images images;
	private static Activator plugin;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
//        images = new BImages(plugin, PLUGIN_ID, "icons/");
//
//        images.putDescription(IMG_CellActionSmall,   "cell_action_small.gif");
//        images.putDescription(IMG_CellAction,   "cell_action.gif");
}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

//    public static Images images() {
//        return images;
//    }

    public static final String IMG_CellActionSmall = "cell.action.small";
    public static final String IMG_CellAction = "cell.action";
}