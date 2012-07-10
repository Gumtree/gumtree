package org.gumtree.gumnix.sics.internal.ui.batch;

import org.gumtree.gumnix.sics.ui.SicsUIConstants;
import org.gumtree.ui.util.workbench.ViewLaunchAction;

public class ControlViewLaunchAction extends ViewLaunchAction {

	@Override
	public String getViewId() {
		return SicsUIConstants.ID_VIEW_BATCH_CONTROL;
	}

}
