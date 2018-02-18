package org.gumtree.control.ui.viewer;

import org.gumtree.control.ui.viewer.model.IFilterEntry;

public class FilterEntry implements IFilterEntry {
	
	private EntryType type;
	
	private String entry;
	
	private String[] entryAsParts;

	public FilterEntry(EntryType type, String entry) {
		setType(type);
		setEntry(entry);
	}
	
	public EntryType getType() {
		return type;
	}

	public void setType(EntryType type) {
		this.type = type;
	}

	public String getEntry() {
		return entry;
	}

	public String[] getEntryAsParts() {
		return entryAsParts;
	}
	
	public void setEntry(String entry) {
		this.entry = entry;
		entryAsParts = entry.split("/");
	}

	public static FilterEntry createIncludeEntry(String entry) {
		return new FilterEntry(EntryType.INCLUDE, entry);
	}
	
	public static FilterEntry createExcludeEntry(String entry) {
		return new FilterEntry(EntryType.EXCLUDE, entry);
	}
	
}
