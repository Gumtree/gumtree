package org.gumtree.cs.sics.ui.browser;

import org.eclipse.jface.viewers.TreeViewer;
import org.gumtree.widgets.IWidget;

public interface ISicsTableTreeViewer extends IWidget {

	public TreeViewer getTreeViewer();
	
	public void setRoot(ISicsTreeNode root);
	
	public ISicsTreeNode getRoot();
	
}
