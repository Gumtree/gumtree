package org.gumtree.msw.ui.ktable;

public interface IButtonListener<TElement> {
	// methods
	public void onClicked(int col, int row, TElement element);
}
