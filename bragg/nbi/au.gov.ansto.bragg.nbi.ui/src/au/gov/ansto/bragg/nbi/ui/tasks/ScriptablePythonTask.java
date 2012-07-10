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

package au.gov.ansto.bragg.nbi.ui.tasks;

import javax.script.ScriptException;

import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptBlock;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.models.SingleStringDataModel;

public class ScriptablePythonTask extends AbstractTask {

	private IScriptExecutor executor;
	
//	private boolean resultReady;
	
	private Object result;
	
	@Override
	protected Object createModelInstance() {
		return new SingleStringDataModel("");
	}

	@Override
	protected ITaskView createViewInstance() {
		return null;
	}

	public IScriptExecutor getScriptExecutor() {
		return executor;
	}
	
	public void setScriptExecutor(IScriptExecutor executor) {
		this.executor = executor;
	}
	
	public String getFunctionName() {
		return getDataModel().getString();
	}
	
	public void setFunctionName(String functionName) {
		getDataModel().setString(functionName);
	}
	
	// Note: This method is intended to run within the executor thread.
	@Override
	protected Object run(final Object input) throws WorkflowException {
		/*********************************************************************
		 * Creates script executor
		 *********************************************************************/
		final IScriptExecutor executor = getScriptExecutor();
		
		/*********************************************************************
		 * Set input to script engine
		 *********************************************************************/
		executor.getEngine().put("__workflow_input__", input);
		
		/*********************************************************************
		 * Creates behind scene script
		 *********************************************************************/
		IScriptBlock block = new ScriptBlock();
		block.append("__workflow_output__ = " + getFunctionName() + "(__workflow_input__)");
		
		/*********************************************************************
		 * Run
		 *********************************************************************/
		result = null;
		try {
			executor.getEngine().eval(block.getScript());
			result = executor.getEngine().get("__workflow_output__");
		} catch (ScriptException e) {
			throw new WorkflowException("Failed to execute script.", e);
		}
		
		return result;
	}

	public SingleStringDataModel getDataModel() {
		return (SingleStringDataModel) super.getDataModel();
	}
	
	public Class<?>[] getInputTypes() {
		return new Class[] { Object.class };
	}
	
	public Class<?>[] getOutputTypes() {
		return new Class[] { Object.class };
	}
	
	protected void handleDispose() {
		executor = null;
		result = null;
	}
	
}
