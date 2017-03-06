package org.gumtree.msw.ui.ktable.internal;

import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;

public interface IElementAdapter {
	// methods
	public Object get(IDependencyProperty property);
	public boolean validate(IDependencyProperty property, Object newValue);
	
	// listeners
	public void addPropertyListener(IElementListener listener);
	public boolean removePropertyListener(IElementListener listener);
}
