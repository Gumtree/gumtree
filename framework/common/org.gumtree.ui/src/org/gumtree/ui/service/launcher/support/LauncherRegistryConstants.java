package org.gumtree.ui.service.launcher.support;

public class LauncherRegistryConstants {

	public static String EXTENSION_LAUNCHERS = "launchers";

	public static String ELEMENT_LAUNCHER = "launcher";

	public static String ELEMENT_CATEGORY = "category";

	public static String ATTRIBUTE_ICON_16 = "icon16";

	public static String ATTRIBUTE_ICON_32 = "icon32";
	
	public static String ATTRIBUTE_ICON_64 = "icon64";

	public static String ATTRIBUTE_CATEGORY = "category";
	
	public static String ATTRIBUTE_QUICK_LAUNCHER = "quickLauncher";

	public static String EXTENTION_POINT_LAUNCHERS = "org.gumtree.ui"
			+ "." + EXTENSION_LAUNCHERS;
	
	/**
	 *
	 * Name for launcher with missing category.
	 *
	 */
	public static String NAME_OTHER = "Other";
	
	private LauncherRegistryConstants() {
		super();
	}
	
}
