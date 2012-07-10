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

package org.gumtree.workflow.ui.tasks;

import java.io.PrintWriter;
import java.io.StringReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.ScriptingUI;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptEngineTask extends AbstractTask {
	
	public static final String CONTEXT_KEY_SCRIPT_EXECUTOR = "scriptExcutor";

	public static final String CONTEXT_KEY_SCRIPT_VIEWER = "scriptViewer";
	
	private static Logger logger = LoggerFactory.getLogger(ScriptEngineTask.class);
	
	private ScriptEngineFactory targetFactory;
	
	private IScriptExecutor scriptExecutor;
	
	public void initialise() {
		// Set engine factory directly from the model
		String defaultEngine = getDataModel().getEngineName();
		if (defaultEngine != null) {
			targetFactory = ServiceUtils.getService(IScriptingManager.class).getFactoryByName(defaultEngine);
		}
		if (targetFactory == null) {
			targetFactory = ServiceUtils.getService(IScriptingManager.class).getDefaultFactory();
		}
		/*********************************************************************
		 * Set an instance of engine into the workflow context
		 *********************************************************************/
		if (targetFactory != null) {
			scriptExecutor = new ScriptExecutor(targetFactory.getNames().get(0));
		} else {
			ScriptEngine engine = ServiceUtils.getService(IScriptingManager.class).createEngine();
			scriptExecutor = new ScriptExecutor(engine);
		}
		getContext().put(CONTEXT_KEY_SCRIPT_EXECUTOR, scriptExecutor);
		/*********************************************************************
		 * Pre configure terminal
		 *********************************************************************/
		if (getDataModel().isLaunchTerminal()) {
			if (getContext().get(CONTEXT_KEY_SCRIPT_VIEWER) instanceof ICommandLineViewer) {
				// Use the viewer supplied by the workflow context
				final ICommandLineViewer viewer = getContext().get(CONTEXT_KEY_SCRIPT_VIEWER, ICommandLineViewer.class);
				if (!viewer.isDisposed()) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						@Override
						public void run() throws Exception {
							// This can only be run in the UI thread
							viewer.setScriptExecutor(scriptExecutor);
						}
					});
				}
			}
		}
	}

	@Override
	protected ITaskView createViewInstance() {
		return new ScriptEngineTaskView();
	}
	
	@Override
	protected Object createModelInstance() {
		return new ScriptEngineSetterTaskModel();
	}
	
	public ScriptEngineSetterTaskModel getDataModel() {
		return (ScriptEngineSetterTaskModel) super.getDataModel();
	}
	
	public ScriptEngineFactory getTargetFactory() {
		return targetFactory;
	}
	
	private void setTargetFactory(ScriptEngineFactory factory) {
		targetFactory = factory;
		// Update model as well
		getDataModel().setEngineName(targetFactory.getNames().get(0));
	}
	
	@Override
	public Object run(Object input) {
		/*********************************************************************
		 * Launch terminal
		 *********************************************************************/
		if (getDataModel().isLaunchTerminal()) {
			if (getContext().get(CONTEXT_KEY_SCRIPT_VIEWER) instanceof ICommandLineViewer) {
				// Use the viewer supplied by the workflow context
				final ICommandLineViewer viewer = getContext().get(CONTEXT_KEY_SCRIPT_VIEWER, ICommandLineViewer.class);
				if (viewer.isDisposed()) {
					ScriptingUI.launchNewCommandLineView(scriptExecutor, ICommandLineViewer.NO_INPUT_TEXT | ICommandLineViewer.NO_UTIL_AREA);
				}
			} else {
				// Else create a new view with new viewer
				ScriptingUI.launchNewCommandLineView(scriptExecutor, ICommandLineViewer.NO_INPUT_TEXT | ICommandLineViewer.NO_UTIL_AREA);	
			}
			// Wait until the output writer is ready
			LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
				@Override
				public boolean getExitCondition() {
					boolean result = false;
					try {
						result = scriptExecutor.getEngine() != null && scriptExecutor.getEngine().getContext() != null && scriptExecutor.getEngine().getContext().getWriter() instanceof PrintWriter;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return result;
				}
			}, 30000);
			if (status.equals(LoopRunnerStatus.TIMEOUT)) {
				logger.warn("Time out on waiting for engine writer to be ready.");
			}
			// Ready, but need to wait / delay a bit more
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*********************************************************************
		 * Run initial script
		 *********************************************************************/
		StringReader reader = new StringReader(getDataModel().getInitialScript());
		scriptExecutor.runScript(reader);
		return null;
	}
	
	private class ScriptEngineTaskView extends AbstractTaskView {

		@Override
		public void createPartControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));
			getToolkit().createLabel(parent, "Engine: ");
			
			/*****************************************************************
			 * Combo - engine selection
			 *****************************************************************/
			ComboViewer viewer = new ComboViewer(parent, SWT.READ_ONLY);
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					if (element instanceof ScriptEngineFactory) {
						return ((ScriptEngineFactory) element).getEngineName();
					}
					return super.getText(element);
				}
			});
			viewer.setInput(ServiceUtils.getService(IScriptingManager.class).getAllEngineFactories());
			
			// Set default selection
			ScriptEngineFactory defaultFactory = getTask().getTargetFactory();
			viewer.setSelection(new StructuredSelection(defaultFactory));
			
			// Set selection behaviour
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
					ScriptEngineFactory factory = (ScriptEngineFactory) selection;
					getTask().setTargetFactory(factory);
				}
			});
			
			/*****************************************************************
			 * Check button - launch terminal option
			 *****************************************************************/
			final Button launchButton = getToolkit().createButton(parent, "Launch terminal when run", SWT.CHECK);
			launchButton.setSelection(getDataModel().isLaunchTerminal());
			GridDataFactory.fillDefaults().span(2, 1).applyTo(launchButton);
			launchButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					getDataModel().setLaunchTerminal(launchButton.getSelection());
				}
			});
			/*****************************************************************
			 * Text box - initial script
			 *****************************************************************/
			final Text text = getToolkit().createText(parent, "", SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			text.setText(getDataModel().getInitialScript());
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).span(2, 1).hint(SWT.DEFAULT, 100).applyTo(text);
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getDataModel().setInitialScript(text.getText());
				}
			});
		}

		public ScriptEngineTask getTask() {
			return (ScriptEngineTask) super.getTask();
		}
		
	}

	public Class<?>[] getInputTypes() {
		return null;
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}
	
}

class ScriptEngineSetterTaskModel {

	private String engineName;
	
	private boolean launchTerminal = true;
	
	private String initialScript;

	public String getEngineName() {
		if (engineName == null) {
			engineName =  ServiceUtils.getService(IScriptingManager.class).getDefaultEngineName();
		}
		return engineName;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}
		
	public boolean isLaunchTerminal() {
		return launchTerminal;
	}

	public void setLaunchTerminal(boolean launchTerminal) {
		this.launchTerminal = launchTerminal;
	}

	public String getInitialScript() {
		if (initialScript == null) {
			initialScript = "";
		}
		return initialScript;
	}

	public void setInitialScript(String initialScript) {
		this.initialScript = initialScript;
	}
	
}
