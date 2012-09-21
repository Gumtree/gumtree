package org.gumtree.ui.missioncontrol;

import java.util.List;

public interface IHub {

	public String getLabel();

	public void setLabel(String label);
	
	public void addApp(String appId);
	
	public void addApp(String appId, int width, int height);
	
	public void setAppSize(String appId, int width, int height);
	
	public void removeApp(String appId);
	
	public List<IAppHubData> getAppHubData();

}
