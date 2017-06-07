package org.gumtree.msw.elements;

public interface IElementListListener<TListElement extends Element> {
	// methods
	public void onAddedListElement(TListElement element);
	public void onDeletedListElement(TListElement element);
}
