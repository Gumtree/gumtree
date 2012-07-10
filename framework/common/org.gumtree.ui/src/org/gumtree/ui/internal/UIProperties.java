package org.gumtree.ui.internal;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class UIProperties {

	public static final ISystemProperty PAGE_ODER = new SystemProperty(
			"gumtree.cruise.pageOrder", "");
	
	private UIProperties() {
		super();
	}

}
