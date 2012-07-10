package org.gumtree.ui.scripting.support;

import org.gumtree.ui.scripting.IEngineDataTreeNode;
import org.gumtree.ui.scripting.IEngineDataViewer;
import org.gumtree.ui.util.workbench.AbstractPartControlProvider;

public abstract class AbstractEgnineDataViewer extends
		AbstractPartControlProvider implements IEngineDataViewer {

	private IEngineDataTreeNode node;

	public IEngineDataTreeNode getEngineDataTreeNode() {
		return node;
	}

	public void setEngineDataTreeNode(IEngineDataTreeNode node) {
		this.node = node;
	}

}
