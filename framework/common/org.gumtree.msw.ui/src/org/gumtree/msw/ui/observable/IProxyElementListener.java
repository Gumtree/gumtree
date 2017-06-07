package org.gumtree.msw.ui.observable;

import org.gumtree.msw.elements.Element;

public interface IProxyElementListener<TElement extends Element> {
	// methods
	public void onTargetChange(TElement oldTarget, TElement newTarget);
}
