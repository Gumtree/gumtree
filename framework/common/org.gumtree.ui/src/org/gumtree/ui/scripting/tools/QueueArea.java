package org.gumtree.ui.scripting.tools;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class QueueArea extends AbstractCommandLineTool {

	private TableViewer queueTableViewer;
	
	public void createControl(Composite parent) {
		queueTableViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
	}

	public void dispose() {
		queueTableViewer = null;
	}


}
