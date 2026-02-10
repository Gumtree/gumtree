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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptBlock;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.models.SingleStringDataModel;

public class PythonScriptingTask extends AbstractTask {

	private Composite consoleHolder;
	
	private boolean resultReady;
	
	private Object result;
	
	private FormToolkit toolkit;
	
	@Override
	protected Object createModelInstance() {
		StringBuilder builder = new StringBuilder();
		builder.append("def run(input):");
		builder.append("	\n\n");
		builder.append("	# Your code goes here\n");
		builder.append("	\n");
		builder.append("	\n");
		builder.append("	return input");
		return new SingleStringDataModel(builder.toString());
	}

	@Override
	protected ITaskView createViewInstance() {
		return new PythonScriptTaskView();
	}

	@Override
	protected Object run(final Object input) throws WorkflowException {
		/*********************************************************************
		 * Creates script executor
		 *********************************************************************/
		final IScriptExecutor executor = new ScriptExecutor("jep");
		
		/*********************************************************************
		 * Set input to script engine
		 *********************************************************************/
		executor.runTask(new Runnable() {
			// Jepp needs this to run in its own threead
			public void run() {
				executor.getEngine().put("__workflow_input__", input);
			}
		});
		
		/*********************************************************************
		 * Creates behind scene script
		 *********************************************************************/
		IScriptBlock block = new ScriptBlock();
		block.append(getDataModel().getString());
		block.append("");
		block.append("__workflow_output__ = run(__workflow_input__)");
		
		/*********************************************************************
		 * Update UI and run
		 *********************************************************************/
		result = null;
		resultReady = false;
		createNewScriptConsoleAndRun(executor, block);
		
		/*********************************************************************
		 * Monitoring
		 *********************************************************************/
		// Ensure it runs (timeout in 5sec)
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return executor.isBusy();
			}
		}, 5000);
		if (status == LoopRunnerStatus.TIMEOUT) {
			throw new WorkflowException("Script has failed to execute.");
		}
		// Wait for completion
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return !executor.isBusy();
			}
		}, LoopRunner.NO_TIME_OUT);
		// Wait for result (timeout in 5sec)
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return resultReady;
			}
		}, 5000);
		if (status == LoopRunnerStatus.TIMEOUT) {
			throw new WorkflowException("Script has failed to obtain result.");
		}
		
		return result;
	}

	protected void handleDispose() {
		consoleHolder = null;
		result = null;
		toolkit = null;
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
	
	// UI update
	private void createNewScriptConsoleAndRun(final IScriptExecutor executor, final IScriptBlock block) {
		if (consoleHolder == null || consoleHolder.isDisposed()) {
			return;
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				/*************************************************************
				 * Refresh UI
				 *************************************************************/
				for (Control child : consoleHolder.getChildren()) {
					child.dispose();
				}
				if (consoleHolder.getMenu() == null || consoleHolder.getMenu().isDisposed()) {
					consoleHolder.setMenu(new Menu(consoleHolder));
				}
				
				/*************************************************************
				 * Creates new viewer
				 *************************************************************/
				ICommandLineViewer console = new CommandLineViewer();
				Composite composite	= toolkit.createComposite(consoleHolder, SWT.NONE);
				composite.setLayout(new FillLayout());
				console.createPartControl(composite, ICommandLineViewer.NO_INPUT_TEXT | ICommandLineViewer.NO_UTIL_AREA);
				console.setScriptExecutor(executor);
				consoleHolder.layout(true, true);
				
				/*************************************************************
				 * Run script
				 *************************************************************/
				executor.runScript(block);
				executor.runTask(new Runnable() {
					public void run() {
						result = executor.getEngine().get("__workflow_output__");
						resultReady = true;
					}
				});
			}
		});
	}
	
	private class PythonScriptTaskView extends AbstractTaskView {
		
		@Override
		public void createPartControl(Composite parent) {
			toolkit = getToolkit();
			GridLayoutFactory.fillDefaults().applyTo(parent);
			final Text text = getToolkit().createText(parent, "", SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 100).applyTo(text);
			// Data binding
			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(
							WidgetProperties.text(SWT.Modify).observe(text),
							BeanProperties.value("string").observe(getDataModel()),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
			consoleHolder = getToolkit().createComposite(parent);
			consoleHolder.setLayout(new FillLayout());
			GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 100).applyTo(consoleHolder);
		}
		
	}
	
}
