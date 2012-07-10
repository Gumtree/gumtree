package org.gumtree.cs.sics.ui.browser;

import java.util.ArrayList;
import java.util.List;

public class ContainerTreeNode implements ISicsTreeNode {
	
	private String name;
	
	private List<ISicsTreeNode> children;
	
	private boolean showChildren;
	
	public ContainerTreeNode() {
		children = new ArrayList<ISicsTreeNode>(2);
		showChildren = true;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void add(ISicsTreeNode node) {
		children.add(node);
	}
	
	public void add(int index, ISicsTreeNode node) {
		children.add(index, node);
	}
	
	public void remove(int index) {
		children.remove(index);
	}
	
	public void remove(ISicsTreeNode child) {
		children.remove(child);
	}
	
	public void clear() {
		children.clear();
	}

	@Override
	public ISicsTreeNode[] getChildren() {
		if (getShowChildren()) {
			return children.toArray(new ISicsTreeNode[children.size()]);
		}
		return null;
	}

	@Override
	public boolean hasChildren() {
		return getShowChildren() && children.size() > 0;
	}

	@Override
	public boolean getShowChildren() {
		return showChildren;
	}

	@Override
	public void setShowChildren(boolean showChildren) {
		this.showChildren = showChildren;
	}
	
}
