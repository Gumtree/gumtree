package org.gumtree.ui.scripting;

import org.gumtree.ui.util.workbench.IPartControlProvider;

public interface IEngineDataViewer extends IPartControlProvider {

	public IEngineDataTreeNode getEngineDataTreeNode();
	
	public void setEngineDataTreeNode(IEngineDataTreeNode node);
	
}
