package org.gumtree.msw.elements;

public interface IElementPropertyListener {
	// methods
	public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue);
}
