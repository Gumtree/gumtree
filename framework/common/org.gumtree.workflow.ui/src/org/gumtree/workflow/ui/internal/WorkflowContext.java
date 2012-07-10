package org.gumtree.workflow.ui.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gumtree.workflow.ui.IWorkflowContext;

public class WorkflowContext extends ConcurrentHashMap<String, Object> implements IWorkflowContext {

	private static final long serialVersionUID = -335169406662466080L;

	private List<String> persistableKeys;
	
	public WorkflowContext() {
		persistableKeys = new CopyOnWriteArrayList<String>();
	}

	public boolean isPersistable(String key) {
		return persistableKeys.contains(key);
	}

	public void put(Object value) {
		put(value.getClass().getName(), value);
	}

	public void put(Object value, boolean persistable) {
		String key = value.getClass().getName();
		put(key, value, persistable);
	}

	public void put(String key, Object value, boolean persistable) {
		if (persistable) {
			persistableKeys.add(key);
		}
		put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> type) {
		Object value = get(key);
		if (value != null && type.isAssignableFrom(value.getClass())) {
			return (T) value;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T getSingleValue(Class<T> type) {
		for (Object value : values()) {
			if (value != null && type.isAssignableFrom(value.getClass())) {
				return (T) value;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T[] getValues(Class<T> type) {
		List<T> buffer = new ArrayList<T>();
		for (Object value : values()) {
			if (value != null && type.isAssignableFrom(value.getClass())) {
				buffer.add((T) value);
			}
		}
		return (T[]) buffer.toArray(new Object[buffer.size()]);
	}

	@Override
	public void clear() {
		persistableKeys.clear();
		super.clear();
	}
	
	@Override
	public Object remove(Object key) {
		persistableKeys.remove(key);
		return super.remove(key);
	}
	
}
