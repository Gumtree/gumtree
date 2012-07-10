package org.gumtree.ui.service.launcher;

import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.core.service.IService;

public interface ILauncherRegistry extends IService {

	/**
	 *
	 * ID for launcher with missing category.
	 *
	 */
	public static String ID_CATEGORY_OTHER = "other";
	
	public ILauncherDescriptor[] getQuickLaunchers();
	
	public String[] getCatagoryIds();
	
	public String getCategoryLabel(String categoryId);
	
	public ImageDescriptor getCategoryIcon(String categoryId);
	
	public ILauncherDescriptor[] getLaunchers(String categoryId);
	
}
