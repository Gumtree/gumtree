package org.gumtree.app.workbench.tasks;

import java.io.StringReader;

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
import org.eclipse.swt.widgets.Text;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.SingleStringDataModel;

public class ScriptRunAppender extends AbstractTask {

	@Override
	protected Object createModelInstance() {
		return new SingleStringDataModel();
	}

	@Override
	protected ITaskView createViewInstance() {
		return new ScriptTaskView();
	}

	@Override
	public Object run(Object input) {
		IScriptExecutor executor = getContext().getSingleValue(IScriptExecutor.class);
		StringReader reader = new StringReader(getDataModel().getString());
		executor.runScript(reader);
		return null;
	}
	
	public SingleStringDataModel getDataModel() {
		return (SingleStringDataModel) super.getDataModel();
	}
	
	private class ScriptTaskView extends AbstractTaskView {

		@Override
		public void createPartControl(Composite parent) {
			/*****************************************************************
			 * Create text box
			 *****************************************************************/
			parent.setLayout(new GridLayout());
			final Text text = getToolkit().createText(parent, "",
					SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(
					true, false).hint(SWT.DEFAULT, 100).applyTo(text);
			
			/*****************************************************************
			 * Bind text box with data model
			 *****************************************************************/
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()),
					new Runnable() {
						public void run() {
							DataBindingContext bindingContext = new DataBindingContext();
							bindingContext.bindValue(SWTObservables
									.observeText(text, SWT.Modify),
									BeansObservables.observeValue(
											getDataModel(), "string"),
									new UpdateValueStrategy(),
									new UpdateValueStrategy());
						}
					});
		}
		
		public ScriptRunAppender getTask() {
			return (ScriptRunAppender) super.getTask();
		}

	}

	public Class<?>[] getInputTypes() {
		return null;
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}
	
}
