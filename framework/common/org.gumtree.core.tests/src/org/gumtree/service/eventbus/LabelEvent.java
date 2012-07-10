package org.gumtree.service.eventbus;

public class LabelEvent extends Event {

	private String label;

	public LabelEvent(Object publisher, String label) {
		super(publisher);
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
