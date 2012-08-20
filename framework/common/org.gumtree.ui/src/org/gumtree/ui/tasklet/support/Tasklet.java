package org.gumtree.ui.tasklet.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gumtree.ui.tasklet.ITasklet;

import com.google.common.base.Objects;

public class Tasklet implements ITasklet {

	private String label;

	private String tags;

	private String contributionURI;

	private boolean simpleLayout;
	
	private boolean newWindow;

	private Map<String, String> properties;

	public Tasklet() {
		properties = new HashMap<String, String>(2);
		simpleLayout = true;
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getTags() {
		return tags;
	}

	@Override
	public void setTags(String tags) {
		this.tags = tags;
	}

	@Override
	public String getContributionURI() {
		return contributionURI;
	}

	@Override
	public void setContributionURI(String contributionURI) {
		this.contributionURI = contributionURI;
	}
	
	@Override
	public boolean isSimpleLayout() {
		return simpleLayout;
	}

	@Override
	public void setSimpleLayout(boolean simpleLayout) {
		this.simpleLayout = simpleLayout;
	}

	@Override
	public boolean isNewWindow() {
		return newWindow;
	}

	@Override
	public void setNewWindow(boolean newWindow) {
		this.newWindow = newWindow;
	}

	@Override
	public String getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	@Override
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("label", getLabel())
				.add("tags", getTags())
				.add("contributionURI", getContributionURI())
				.add("simple", isSimpleLayout())
				.add("newWindow", isNewWindow()).toString();
	}

}
