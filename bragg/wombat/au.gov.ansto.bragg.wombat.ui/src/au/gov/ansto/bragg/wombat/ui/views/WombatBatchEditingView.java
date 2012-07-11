/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.wombat.ui.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.workflow.ui.IWorkflow;

import au.gov.ansto.bragg.wombat.exp.task.HeaderInformationBlockTask;
import au.gov.ansto.bragg.wombat.exp.task.WombatExperimentWorkflow;
import au.gov.ansto.bragg.wombat.ui.workflow.ExperimentTaskViewer;

/**
 * @author nxi
 * Created on 23/03/2009
 */
public class WombatBatchEditingView extends ViewPart {

	ExperimentTaskViewer viewer;
	/**
	 * 
	 */
	public WombatBatchEditingView() {
		viewer = new ExperimentTaskViewer();
		IWorkflow workflow = WombatExperimentWorkflow.createEmptyWorkflow();
		workflow.addTask(new HeaderInformationBlockTask());
		viewer.setWorkflow(workflow);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer.createPartControl(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}

	public ExperimentTaskViewer getViewer(){
		return viewer;
	}
}
