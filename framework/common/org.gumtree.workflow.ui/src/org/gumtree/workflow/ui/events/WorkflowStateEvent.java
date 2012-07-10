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

package org.gumtree.workflow.ui.events;

import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.WorkflowState;

public class WorkflowStateEvent extends WorkflowEvent {

	private WorkflowState state;
	
	private String message;
	
	public WorkflowStateEvent(IWorkflow workflow, WorkflowState state, String message) {
		super(workflow);
		this.state = state;
		this.message = message;
	}
	
	public WorkflowState getState() {
		return state;
	}
	
	public String getMessgae() {
		return message;
	}
	
}
