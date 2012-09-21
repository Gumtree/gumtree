package org.gumtree.ui.missioncontrol.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gumtree.ui.missioncontrol.IAppHubData;
import org.gumtree.ui.missioncontrol.IHub;

public class Hub implements IHub {

	private static final int DEFAULT_SIZE = 4;

	private String label;

	private List<IAppHubData> appHubData;

	public Hub() {
		this("");
	}

	public Hub(String label) {
		this.label = label;
		appHubData = new ArrayList<IAppHubData>(2);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public void addApp(String appId) {
		addApp(appId, DEFAULT_SIZE, DEFAULT_SIZE);
	}

	@Override
	public void addApp(String appId, int width, int height) {
		appHubData.add(AppDisplayData.createData(appId, width, height));
	}

	@Override
	public void setAppSize(String appId, int width, int height) {
		IAppHubData data = getAppHubData(appId);
		if (data != null) {
			data.setWidth(width);
			data.setHeight(height);
		}
	}

	@Override
	public void removeApp(String appId) {
		IAppHubData data = getAppHubData(appId);
		if (data != null) {
			appHubData.remove(data);
		}
	}

	@Override
	public List<IAppHubData> getAppHubData() {
		return Collections.unmodifiableList(appHubData);
	}

	private IAppHubData getAppHubData(String appId) {
		for (IAppHubData data : appHubData) {
			if (data.getAppId().equals(appId)) {
				return data;
			}
		}
		return null;
	}

}
