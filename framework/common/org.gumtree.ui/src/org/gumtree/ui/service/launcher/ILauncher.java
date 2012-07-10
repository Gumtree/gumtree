package org.gumtree.ui.service.launcher;

public interface ILauncher {

	public void launch() throws LauncherException;
	
	public ILauncherDescriptor getDescriptor();
	
	public void setDescriptor(ILauncherDescriptor descriptor);
	
}
