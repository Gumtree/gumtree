package org.gumtree.cs.sics.ui.browser;

public interface ISicsTreeNode {

	public String getName();
	
	public ISicsTreeNode[] getChildren();
	
	public boolean hasChildren();
	
	public boolean getShowChildren();
	
	public void setShowChildren(boolean showChildren);
	
}
