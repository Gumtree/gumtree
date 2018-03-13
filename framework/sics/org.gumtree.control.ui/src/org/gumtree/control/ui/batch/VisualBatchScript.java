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

package org.gumtree.control.ui.batch;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.gumtree.control.batch.BatchScript;
import org.gumtree.control.batch.tasks.ISicsCommandBlock;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.WorkflowFactory;


public class VisualBatchScript extends BatchScript {
	
	public static final String PROP_BUFFER_NAME = "bufferName";
	
	public VisualBatchScript(String name, IWorkflow workflow) {
		super(name);
		StringWriter writer = new StringWriter();
		WorkflowFactory.saveWorkflow(workflow, writer);
		setSource(writer.toString());
		setName(name);
	}

	public IWorkflow getSource() {
		StringReader reader = new StringReader((String) super.getSource());
		return (IWorkflow) WorkflowFactory.createWorkflow(reader);
	}
	
	public void setName(String name) {
		super.setName(name);
		getSource().getContext().put(PROP_BUFFER_NAME, name, true);
	}
	
	public String getContent() {
//		ISicsBatchScript batchScript = getSource().getContext().getSingleValue(
//				ISicsBatchScript.class);
		String script = "";
		List<ITask> taskList = getSource().getTasks();
		for (ITask task : taskList) {
			Object dataModel = task.getDataModel();
			if (dataModel instanceof ISicsCommandBlock) {
				ISicsCommandBlock commandBlock = (ISicsCommandBlock) dataModel;
				script += commandBlock.toScript() + "\n";
			}
		}
		return script;
//		if (batchScript != null) {
//			return batchScript.toScript();
//		}
//		return EMPTY_CONTENT;
	}
	
}
