package org.gumtree.msw.elements;

public interface IElementListener {
	// methods
	public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue);
	public void onDisposed();
}
