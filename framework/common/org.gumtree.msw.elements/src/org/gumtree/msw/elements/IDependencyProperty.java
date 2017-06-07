package org.gumtree.msw.elements;

public interface IDependencyProperty {
	// properties
	public String getName();
	public Class<?> getPropertyType();
	
	// methods
	public boolean matches(String name, Class<?> propertyType);
}
