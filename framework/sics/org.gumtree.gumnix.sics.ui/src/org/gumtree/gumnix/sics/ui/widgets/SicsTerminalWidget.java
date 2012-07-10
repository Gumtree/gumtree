/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;

public class SicsTerminalWidget extends FormControlWidget {

	private int height = 250;
	
	private SicsTerminalView terminal;
	
	public SicsTerminalWidget(Composite parent, int style) {
		super(parent, style);
	}

	protected void widgetDispose() {
		if (terminal != null) {
			terminal.dispose();
			terminal = null;
		}
	}

	public void afterParametersSet() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(SicsTerminalWidget.this);
				
				Composite composite = getToolkit().createComposite(SicsTerminalWidget.this);
				composite.setLayout(new FillLayout());
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, true).hint(SWT.DEFAULT, getHeight()).applyTo(composite);
				
				SicsTerminalView terminal = new SicsTerminalView();
				terminal.createPartControl(composite);
				
				getParent().layout(true, true);
			}
		});
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
