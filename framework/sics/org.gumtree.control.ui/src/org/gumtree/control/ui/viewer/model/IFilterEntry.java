package org.gumtree.control.ui.viewer.model;

import org.gumtree.control.ui.viewer.EntryType;

public interface IFilterEntry {

	public EntryType getType();
	
	public String getEntry();
	
	public String[] getEntryAsParts();
	
}
