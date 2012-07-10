package org.gumtree.workflow.ui;

import java.util.Map;

/**
 * @since 1.0
 */
public interface IWorkflowContext extends Map<String, Object> {
	
	public void put(Object value);
	
	public void put(Object value, boolean persistable);
	
	public void put(String key, Object value, boolean persistable);
	
	public boolean isPersistable(String key);
	
	public <T> T get(String key, Class<T> type);
	
	/**
	 * Returns whatever the first match of an object which is assignable from a given class interface.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T> T getSingleValue(Class<T> type);
	
	public <T> T[] getValues(Class<T> type);
	
}
