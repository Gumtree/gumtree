package org.gumtree.app.workbench.tasks;

import java.io.StringReader;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
// FIX: Corrected import for modern JFace databinding library
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;
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
	public Object run(Object input) throws WorkflowException {
		IScriptExecutor executor = getContext().getSingleValue(IScriptExecutor.class);
		if (executor == null) {
			throw new WorkflowException("Script executor service is not available.");
		}
		String script = getDataModel().getString();
		if (script != null && !script.trim().isEmpty()) {
			StringReader reader = new StringReader(script);
			executor.runScript(reader);
		}
		return null;
	}

	public SingleStringDataModel getDataModel() {
		return (SingleStringDataModel) super.getDataModel();
	}

	private class ScriptTaskView extends AbstractTaskView {

		@Override
		public void createPartControl(Composite parent) {
			parent.setLayout(new GridLayout());
			final Text text = getToolkit().createText(parent, "", SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).hint(SWT.DEFAULT, 100)
					.applyTo(text);

			DataBindingContext bindingContext = new DataBindingContext();
			// The usage of WidgetProperties remains the same, only the import path changes.
			IObservableValue<?> target = WidgetProperties.text(SWT.Modify).observe(text);
			IObservableValue<?> model = BeanProperties.value("string").observe(getDataModel());
			bindingContext.bindValue(target, model);
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
