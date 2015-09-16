package org.gumtree.msw.ui.ktable;

import org.eclipse.swt.graphics.Image;

public class ButtonInfo<TElement> {
	// fields
	private final Image imageDefault;
	private final Image imageMouseOver;
	private final IButtonListener<TElement> listener;
	
	// construction
	public ButtonInfo(Image imageDefault, Image imageMouseOver, IButtonListener<TElement> listener) {
		this.imageDefault = imageDefault;
		this.imageMouseOver = imageMouseOver;
		this.listener = listener;
	}
	
	// properties
	public Image getImageDefault() {
		return imageDefault;
	}
	public Image getImageMouseOver() {
		return imageMouseOver;
	}
	public IButtonListener<TElement> getListener() {
		return listener;
	}
}
