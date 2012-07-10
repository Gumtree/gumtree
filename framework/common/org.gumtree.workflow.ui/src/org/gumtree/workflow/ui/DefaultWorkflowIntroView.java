/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.workflow.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.workflow.ui.internal.InternalImage;

public class DefaultWorkflowIntroView extends AbstractWorkflowIntroView {

	private Font titleFont;
	
	private Font labelFont;
	
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		
		Label textLabel = getToolkit().createLabel(parent, "", SWT.CENTER);
		textLabel.setText("Welcome to GumTree Workflow");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, true).applyTo(textLabel);
//		textLabel.setLayoutData("aligny bottom, push, growx, wrap");
		FontData fontData = new FontData(textLabel.getFont().getFontData()[0].toString());
		fontData.setStyle(SWT.BOLD);
		fontData.setHeight(fontData.getHeight() + 12);
		titleFont = new Font(Display.getDefault(), fontData);
		textLabel.setFont(titleFont);
		
		Label imageLabel = getToolkit().createLabel(parent, "", SWT.CENTER);
		imageLabel.setImage(InternalImage.WORKFLOW_INTRO.getImage());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(imageLabel);
//		imageLabel.setLayoutData("aligny center, push, grow, wrap");
		
		textLabel = getToolkit().createLabel(parent, "", SWT.CENTER);
		textLabel.setText("Click \"Begin >\" button to start this workflow");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(textLabel);
		fontData = new FontData(textLabel.getFont().getFontData()[0].toString());
		fontData.setStyle(SWT.BOLD);
		fontData.setHeight(fontData.getHeight() + 2);
		labelFont = new Font(Display.getDefault(), fontData);
		textLabel.setFont(labelFont);
	}

	@Override
	public void dispose() {
		if (titleFont != null) {
			titleFont.dispose();
			titleFont = null;
		}
		if (labelFont != null) {
			labelFont.dispose();
			labelFont = null;
		}
		super.dispose();
	}
	
	public static void main(String[] args) throws Exception {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Test");
		shell.setSize(500, 500);
		shell.setLayout(new FillLayout());

		DefaultWorkflowIntroView view = new DefaultWorkflowIntroView();
		view.createPartControl(shell);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	

}
