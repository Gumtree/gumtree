package org.gumtree.ui.scripting;

import java.util.Map;

import org.gumtree.ui.util.jface.ITreeNode;

public interface IEngineDataTreeNode extends ITreeNode {

	public String getName();
	
	public Object get(String key);
	
	public void put(String key, Object value);
	
	public Map<String, Object> getParameters();
	
	public Object getData();
	
	public void setData(Object data);
	
}
