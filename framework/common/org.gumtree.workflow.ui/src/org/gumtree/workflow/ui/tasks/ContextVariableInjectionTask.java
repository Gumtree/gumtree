package org.gumtree.workflow.ui.tasks;

import org.gumtree.core.object.ObjectConfigException;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;

public class ContextVariableInjectionTask extends AbstractTask {

	private static final String KEY_KEY = "key";
	
	private static final String KEY_VARIABLE = "variable";
	
	private static final String KEY_PERSISTABLE = "persistable";
	
	@Override
	protected Object createModelInstance() {
		return null;
	}

	@Override
	protected ITaskView createViewInstance() {
		return new EmptyTaskView();
	}
	
	@Override
	public void initialise() {
		String objectClass = getParameters().getString(KEY_VARIABLE);
		String key = getParameters().containsKey(KEY_KEY) ? getParameters().getString(KEY_KEY) : objectClass;
		boolean persistable = getParameters().get(KEY_PERSISTABLE, boolean.class);
		// Inject only if it doesn't exist in the context
		// Reason: variable may exist when it was loaded
		if (objectClass != null && !getContext().containsKey(key)) {
			try {
				Object value = ObjectFactory.instantiateObject(objectClass);
				getContext().put(key, value, persistable);
			} catch (ObjectCreateException e) {
				throw new ObjectConfigException("Cannot initantiate class " + objectClass, e);
			}
		}
	}
	
	@Override
	protected Object run(Object input) throws WorkflowException {
		return null;
	}

	public Class<?>[] getInputTypes() {
		return null;
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}
	
}
