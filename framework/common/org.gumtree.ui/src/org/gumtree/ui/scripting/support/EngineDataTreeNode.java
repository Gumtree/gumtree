package org.gumtree.ui.scripting.support;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.ui.scripting.IEngineDataTreeNode;
import org.gumtree.ui.util.jface.TreeNode;

public class EngineDataTreeNode extends TreeNode implements IEngineDataTreeNode {
	
	private volatile Map<String, Object> parameters;
	
	private Object data;
	
	public EngineDataTreeNode(String name) {
		super(name);
	}
	
	public String getName() {
		return (String) getOriginalObject();
	}

	public Object get(String key) {
		return getParameters().get(key);
	}
	
	public void put(String key, Object value) {
		getParameters().put(key, value);
	}
	
	public Map<String, Object> getParameters() {
		if (parameters == null) {
			synchronized (this) {
				if (parameters == null) {
					parameters = new HashMap<String, Object>(2);
				}
			}
		}
		return parameters;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
