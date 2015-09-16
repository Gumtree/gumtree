package org.gumtree.msw.ui.ktable.internal;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementPropertyListener;

public class ListElementAdapter<TListElement extends Element> implements IElementAdapter {
	// fields
	private final TListElement element;
	
	// construction
	public ListElementAdapter(TListElement element) {
		this.element = element;
	}
	
	// properties
	public Object get(IDependencyProperty property) {
		return element.get(property);
	}
	
	// listeners
	public void addPropertyListener(IElementPropertyListener listener) {
		element.addPropertyListener(listener);
	}
	public boolean removePropertyListener(IElementPropertyListener listener) {
		return element.removePropertyListener(listener);
	}
}
