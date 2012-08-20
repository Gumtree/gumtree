package org.gumtree.ui.tasklet;

import java.util.Map;

public interface ITasklet {

	public String getLabel();

	public void setLabel(String label);

	public String getTags();

	public void setTags(String tags);

	public String getContributionURI();

	public void setContributionURI(String contributionURI);
	
	public boolean isSimpleLayout();
	
	public void setSimpleLayout(boolean simpleLayout);
	
	public boolean isNewWindow();
	
	public void setNewWindow(boolean newWindow);
	
	public String getProperty(String key);
	
	public Map<String, String> getProperties();
	
	public void setProperty(String key, String value);

}
