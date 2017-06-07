package org.gumtree.msw.ui.ktable.internal;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;

public class ListElementAdapter<TListElement extends Element> implements IElementAdapter {
	// fields
	private final TListElement element;
	
	// construction
	public ListElementAdapter(TListElement element) {
		this.element = element;
	}
	
	// methods
	@Override
	public Object get(IDependencyProperty property) {
		return element.get(property);
	}
	@Override
	public boolean validate(IDependencyProperty property, Object newValue) {
		return element.validate(property, newValue);
	}
	
	// listeners
	@Override
	public void addPropertyListener(IElementListener listener) {
		element.addElementListener(listener);
	}
	@Override
	public boolean removePropertyListener(IElementListener listener) {
		return element.removeElementListener(listener);
	}
}
