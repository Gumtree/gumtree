package org.gumtree.msw.model;

public interface IModelNodePropertyInfo {
	// properties
	public String getName();
	public Class<?> getValueClass();
	public boolean isDefaultValue();
	
	// methods
	public void reset();
	public Object get();
	public boolean set(Object newValue);
	public boolean parse(String newValue);
	public IModelNodePropertyInfo clone();
}
