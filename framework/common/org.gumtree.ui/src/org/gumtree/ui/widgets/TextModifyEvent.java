package org.gumtree.ui.widgets;

import org.gumtree.service.eventbus.Event;

public class TextModifyEvent extends Event {

	private String text;
	
	public TextModifyEvent(Object publisher, String text) {
		super(publisher);
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
