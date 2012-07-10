package org.gumtree.ui.service.applaunch;

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;

public interface IAppLaunchDescriptor {

	public String getCommandId();
	
	public String getLabel();
	
	public ImageDescriptor getIcon64();
	
	public boolean hasParameters();
	
	public Map<String, String> getParameters();
	
}
