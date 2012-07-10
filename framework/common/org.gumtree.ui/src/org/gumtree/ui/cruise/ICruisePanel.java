package org.gumtree.ui.cruise;

public interface ICruisePanel {
	
	public boolean isFullMode();

	public void setPage(String pageName);
	
	public String getCurrentPageName();
	
}
