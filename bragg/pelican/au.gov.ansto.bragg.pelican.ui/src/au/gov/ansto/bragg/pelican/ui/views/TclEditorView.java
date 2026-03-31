/******************************************************************************* 
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.pelican.ui.views;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.control.ui.batch.taskeditor.BatchTaskFrameViewer;
import org.gumtree.gumnix.sics.batch.ui.VisualBatchBufferViewer;
import org.gumtree.workflow.ui.viewer2.AbstractWorkflowViewer;

import au.gov.ansto.bragg.pelican.ui.internal.PelicanWorkbenchLauncher;



/**
 * @author nxi
 *
 */
public class TclEditorView extends ViewPart {

	private AbstractWorkflowViewer viewer;
	/**
	 * 
	 */
	public TclEditorView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		boolean newProxy = Boolean.valueOf(System.getProperty(PelicanWorkbenchLauncher.USE_NEW_PROXY, "false"));
		if (newProxy) {
			viewer = new BatchTaskFrameViewer(parent, SWT.NONE);
		} else {
			viewer = new VisualBatchBufferViewer(parent, SWT.NONE);
		}
		GridLayoutFactory.fillDefaults().applyTo(viewer);
		viewer.afterParametersSet();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}

}
