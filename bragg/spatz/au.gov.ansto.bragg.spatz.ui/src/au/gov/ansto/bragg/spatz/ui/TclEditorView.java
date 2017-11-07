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
package au.gov.ansto.bragg.spatz.ui;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.batch.ui.VisualBatchBufferViewer;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.WorkflowFactory;

import au.gov.ansto.bragg.nbi.ui.tasks.SicsScriptTask;
import au.gov.ansto.bragg.spatz.ui.tasks.AngleTableTask;
import au.gov.ansto.bragg.spatz.ui.tasks.PositionTableTask;



/**
 * @author nxi
 *
 */
public class TclEditorView extends ViewPart {

	private VisualBatchBufferViewer viewer;
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
		viewer = new VisualBatchBufferViewer(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(viewer);
		IWorkflow workflow = WorkflowFactory.createEmptyWorkflow();
		AngleTableTask angleTableTask = new AngleTableTask();
		angleTableTask.loadPreference();
		workflow.addTask(angleTableTask);
		PositionTableTask positionTableTask = new PositionTableTask();
		positionTableTask.loadPreference();
		workflow.addTask(positionTableTask);
		workflow.addTask(new SicsScriptTask());
		viewer.setWorkflow(workflow);
		viewer.afterParametersSet();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}

}
