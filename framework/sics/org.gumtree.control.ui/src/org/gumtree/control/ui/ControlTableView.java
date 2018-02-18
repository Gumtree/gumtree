package org.gumtree.control.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.control.ui.viewer.ControlViewer;

public class ControlTableView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		ControlViewer viewer = new ControlViewer();
		viewer.createPartControl(parent, null);
	}

	@Override
	public void setFocus() {
		
	}

	
}
