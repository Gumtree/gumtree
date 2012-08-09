package org.gumtree.ui.tasklet.support;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.gumtree.ui.util.workbench.WorkbenchUtils;

@SuppressWarnings("restriction")
public final class TaskletUtilities {

	public static MPerspectiveStack getMPerspectiveStack(MWindow mWindow) {
		return WorkbenchUtils.getFirstChild(mWindow, MPerspectiveStack.class);
	}

	private TaskletUtilities() {
		super();
	}

}
