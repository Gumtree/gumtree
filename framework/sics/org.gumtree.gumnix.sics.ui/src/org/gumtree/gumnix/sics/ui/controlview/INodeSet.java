package org.gumtree.gumnix.sics.ui.controlview;

import java.util.List;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;

public interface INodeSet {

	public enum SetType {
		TREE, FLAT, SUBTREE
	}
	
	public String getTitle();
	
	public SetType getSetType();
	
	public String[] getColumns();
	
	public int getLabelColumnWidth();
	
	public boolean isVisible(IComponentController controller);
	
	public boolean hasAlias(IComponentController controller);
	
	// Return null if alias not found
	public String getAlias(IComponentController controller);
	
	public List<IFilterEntry> getEntries();
	
}
