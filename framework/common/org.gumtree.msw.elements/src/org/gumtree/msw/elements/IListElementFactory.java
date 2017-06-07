package org.gumtree.msw.elements;

public interface IListElementFactory<TListElement extends Element> {
	// methods
	public TListElement create(String elementName);
}
