package org.gumtree.control.ui.viewer.model;

import java.util.List;

import org.gumtree.control.core.ISicsController;

public interface INodeSet {

	public enum SetType {
		TREE, FLAT, SUBTREE
	}
	
	public String getTitle();
	
	public SetType getSetType();
	
	public String[] getColumns();
	
	public int getLabelColumnWidth();
	
	public boolean isVisible(ISicsController controller);
	
	public boolean hasAlias(ISicsController controller);
	
	// Return null if alias not found
	public String getAlias(ISicsController controller);
	
	public List<IFilterEntry> getEntries();
	
}
