package org.gumtree.msw.model.structure;

import org.apache.xerces.xs.XSModelGroup;

enum Compositor {
	// options
	SEQUENCE,
	CHOICE,
	ALL;
		
	// construction
	private Compositor() {
	}
	
	// methods
	public static Compositor from(short value) {
		switch (value) {
		case XSModelGroup.COMPOSITOR_SEQUENCE:
			return SEQUENCE;
		case XSModelGroup.COMPOSITOR_CHOICE:
			return CHOICE;
		case XSModelGroup.COMPOSITOR_ALL:
			return ALL;
		default:
			return SEQUENCE;
		}
	}
}
