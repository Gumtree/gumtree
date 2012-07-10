package org.gumtree.cs.sics.ui.browser;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.sics.control.ISicsController;

public class SicsTreeNode implements ISicsTreeNode {

	private ISicsController sicsController;
	
	private List<ISicsTreeNode> childNodes;
	
	private boolean showChildren;
	
	public SicsTreeNode(ISicsController sicsController) {
		this.sicsController = sicsController;
		showChildren = true;
	}

	@Override
	public String getName() {
		return sicsController.getId();
	}

	@Override
	public ISicsTreeNode[] getChildren() {
		if (getShowChildren()) {
			return getChildNodes().toArray(new ISicsTreeNode[getChildNodes().size()]);
		}
		return null;
	}

	@Override
	public boolean hasChildren() {
		return getShowChildren() && getChildNodes().size() > 0;
	}

	@Override
	public boolean getShowChildren() {
		return showChildren;
	}

	@Override
	public void setShowChildren(boolean showChildren) {
		this.showChildren = showChildren;
	}

	protected List<ISicsTreeNode> getChildNodes() {
		if (childNodes == null) {
			childNodes = new ArrayList<ISicsTreeNode>(2);
			for (ISicsController controller : sicsController.getChildren()) {
				childNodes.add(new SicsTreeNode(controller));
			}
		}
		return childNodes;
	}
}
