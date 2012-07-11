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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.ui.widgets.BatchControlComposite;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.config.*;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.gumtree.workflow.ui.viewer.AutomatorWorkflowViewer;

import au.gov.ansto.bragg.wombat.ui.workflow.SicsCommandTask;



/**
 * @author nxi
 * Created on 11/02/2009
 */
public class WorkflowView extends ViewPart {

	IWorkflow workflow;
	StyledText terminal;
	CTabItem codeItem;
	CTabFolder tabFolder;
	BatchControlComposite batchComposite;
	/**
	 * 
	 */
	public WorkflowView() {
		// TODO Auto-generated constructor stub
		WorkflowConfig config = new WorkflowConfig();

		TaskConfig taskConfig = new TaskConfig();

		taskConfig.setClassname("au.gov.ansto.bragg.wombat.ui.workflow.SicsCommandTask");
		taskConfig.getParameters().put(WorkflowConfigConstants.PARAM_LABEL, "wombatWorkflow");
		config.getTaskConfigs().add(taskConfig);
		
		workflow = WorkflowFactory.createWorkflow(config);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		tabFolder = new CTabFolder(parent, SWT.CLOSE | SWT.BOTTOM);
		tabFolder.setUnselectedCloseVisible(false);
		tabFolder.setSimple(false);

		CTabItem workflowItem = new CTabItem(tabFolder, SWT.NONE);
		workflowItem.setText("Design");
		Composite workflowComposite = new Composite(tabFolder, SWT.NONE);
		
		AutomatorWorkflowViewer viewer = new AutomatorWorkflowViewer();
		viewer.setWorkflow(workflow);
		viewer.createPartControl(workflowComposite);
		workflowItem.setControl(workflowComposite);
		
		codeItem = new CTabItem(tabFolder, SWT.NONE);
		codeItem.setText("TCL Source");
		Composite codeComposite = new Composite(tabFolder, SWT.None);
//		createCodeEditor(codeComposite);
		createBatchEditor(codeComposite);
		codeItem.setControl(codeComposite);
		
		tabFolder.setSelection(workflowItem);
		initListeners();
	}

	private void initListeners(){
		tabFolder.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				if (tabFolder.getSelection() == codeItem){
					ITask task = workflow.getTasks().get(0);
					if (task instanceof SicsCommandTask){
						String script = ((SicsCommandTask) task).getScripts();
						if (script != null)
							terminal.setText(script);
					}
				}
			}});
		
	}
	
	private void createCodeEditor(Composite codeComposite) {
		// TODO Auto-generated method stub
		codeComposite.setLayout(new GridLayout());
		terminal = new StyledText(codeComposite, SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.WRAP);
		terminal.setText("");
		terminal.setFont(new Font(terminal.getDisplay(), new FontData("Courier New", 9, SWT.NORMAL)));
		GridData terminalData = new GridData(SWT.FILL, SWT.FILL, true, true);
		terminal.setLayoutData(terminalData);
	}
	
	private void createBatchEditor(Composite codeComposite) {
		// TODO Auto-generated method stub
		batchComposite = new BatchControlComposite(codeComposite, SWT.NONE);
		batchComposite.setLayout(new GridLayout());
		terminal = batchComposite.getCommandTextBox();
		terminal.setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
