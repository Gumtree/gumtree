package org.gumtree.msw.elements;

public interface IElementVisitor {
	// for elements
	public <TElement extends Element>
	void visit(TElement element);
	
	// for list elements
	public <TElementList extends ElementList<TListElement>, TListElement extends Element>
	void visit(TElementList elementList);
}
