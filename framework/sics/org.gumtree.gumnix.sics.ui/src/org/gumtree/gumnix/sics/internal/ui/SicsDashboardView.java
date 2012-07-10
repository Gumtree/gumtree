package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.internal.ui.dashboard.SicsDashBoardContent;

public class SicsDashboardView extends ViewPart {

	private SicsDashBoardContent content;
	
	public SicsDashboardView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		content = new SicsDashBoardContent();
		content.createContentControl(parent);
	}

	public void dispose() {
		if (content != null) {
			content.dispose();
		}
		super.dispose();
	}
	
	@Override
	public void setFocus() {
	}

}
