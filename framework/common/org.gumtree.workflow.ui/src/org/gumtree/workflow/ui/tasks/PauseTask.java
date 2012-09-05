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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.models.SingleNumberDataModel;

public class PauseTask extends AbstractTask {
	
	private Thread currentThread = null;

	/*************************************************************************
	 * Controller
	 *************************************************************************/
	
	@Override
	protected Object run(Object input) throws WorkflowException {
		/*********************************************************************
		 * Get sleep value from the data model
		 *********************************************************************/
		int pauseInSec = 0;
		try {
			pauseInSec = getDataModel().getNumber().intValue();
		} catch (Exception e) {
			throw new WorkflowException("Invalid pause value", e);
		}
		/*********************************************************************
		 * Sleep
		 *********************************************************************/		
		try {
			currentThread = Thread.currentThread();
			Thread.sleep(pauseInSec * 1000);
		} catch (InterruptedException e) {
			currentThread.interrupt();
		} finally {
			currentThread = null;
		}
		return null;
	}
	
	protected void handleStop() {
		if (currentThread != null) {
			currentThread.interrupt();
		}
	}

	/*************************************************************************
	 * Model
	 *************************************************************************/
	
	@Override
	protected Object createModelInstance() {
		// Default: 1 sec
		return new SingleNumberDataModel(1);
	}
	
	public int getSleepTime() {
		return getDataModel().getNumber().intValue();
	}
	
	public void setSleepTime(int sec) {
		getDataModel().setNumber(sec);
	}
	
	public SingleNumberDataModel getDataModel() {
		return (SingleNumberDataModel) super.getDataModel();
	}
	
	/*************************************************************************
	 * View
	 *************************************************************************/

	@Override
	protected ITaskView createViewInstance() {
		return new PauseTaskView();
	}
	
	private class PauseTaskView extends AbstractTaskView {

		public void createPartControl(Composite parent) {
			/*****************************************************************
			 * Create label and spinner
			 *****************************************************************/
			parent.setLayout(new GridLayout(2, false));
			Label label = getToolkit().createLabel(parent, "Pause (in sec)");
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
			final Spinner spinner = new Spinner(parent, SWT.BORDER);
			GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).applyTo(spinner);
			spinner.setMinimum(0);
			spinner.setMaximum(1000);
			spinner.setIncrement(1);
			
			/*****************************************************************
			 * Bind data model with spinner
			 *****************************************************************/
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeSelection(spinner),
						BeansObservables.observeValue(getDataModel(), "number"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
		
	}

	public Class<?>[] getInputTypes() {
		return null;
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}
	
}
