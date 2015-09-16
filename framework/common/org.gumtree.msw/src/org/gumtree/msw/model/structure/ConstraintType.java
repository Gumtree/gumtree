package org.gumtree.msw.model.structure;

import org.apache.xerces.xs.XSConstants;

enum ConstraintType {
	// options
	NONE,
	DEFAULT,
	FIXED;
		
	// construction
	private ConstraintType() {
	}
	
	// methods
	public static ConstraintType from(short value) {
		switch (value) {
		case XSConstants.VC_NONE:
			return NONE;
		case XSConstants.VC_DEFAULT:
			return DEFAULT;
		case XSConstants.VC_FIXED:
			return FIXED;
		default:
			return NONE;
		}
	}
}
