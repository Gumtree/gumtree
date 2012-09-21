package org.gumtree.ui.missioncontrol.support;

import org.gumtree.ui.missioncontrol.IAppHubData;

public class AppDisplayData implements IAppHubData {

	private String appId;

	private int width;

	private int height;

	public AppDisplayData() {
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public static AppDisplayData createData(String appId, int width, int height) {
		AppDisplayData data = new AppDisplayData();
		data.setAppId(appId);
		data.setWidth(width);
		data.setHeight(height);
		return data;
	}

}