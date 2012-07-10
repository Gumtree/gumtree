package org.gumtree.ui.service.launcher;

import org.eclipse.jface.resource.ImageDescriptor;

public interface ILauncherDescriptor {

	public String getId();
	
	public String getLabel();
	
	public ImageDescriptor getIcon16();
	
	public ImageDescriptor getIcon32();
	
	public ImageDescriptor getIcon64();
	
	public String getDescription();
	
	public String getCategory();
	
	public boolean isQuickLaunch();
	
	public ILauncher getLauncher() throws LauncherException;
	
}
