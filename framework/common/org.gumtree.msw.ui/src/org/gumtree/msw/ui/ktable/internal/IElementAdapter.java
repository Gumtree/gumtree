package org.gumtree.msw.ui.ktable.internal;

import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementPropertyListener;

public interface IElementAdapter {
	// properties
	public Object get(IDependencyProperty property);
	
	// listeners
	public void addPropertyListener(IElementPropertyListener listener);
	public boolean removePropertyListener(IElementPropertyListener listener);
}
