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
import org.gumtree.gumnix.sics.batch.ui.commands.ScriptCommand;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.gumnix.sics.batch.ui.model.SicsCommandType;
import org.gumtree.gumnix.sics.batch.ui.util.SicsCommandFactory;
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
	private String SICS_SCRIPT_HEADER = "# to select an angle, use 'angler $a', e.g., angler 1\r\n" + 
			"# to select a position, use 'position $p', e.g., positioner 1\r\n" + 
			"# to drive a device, use 'drive some_motor some_place', e.g., drive att 10\n";
	
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
		SicsScriptTask sicsTask = new SicsScriptTask();
		if (sicsTask.getDataModel() != null) {
			ISicsCommandElement[] commands = sicsTask.getDataModel().getCommands();
			if (commands != null && commands.length > 0) {
				ISicsCommandElement ce = commands[0];
				if (ce instanceof ScriptCommand) {
					((ScriptCommand) ce).setText(SICS_SCRIPT_HEADER);
				}
			} else {
				ISicsCommandElement ce = SicsCommandFactory.createCommand(SicsCommandType.SCRIPT);
				((ScriptCommand) ce).setText(SICS_SCRIPT_HEADER);
				sicsTask.getDataModel().addCommand(ce);
			}
		}
		workflow.addTask(sicsTask);
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
