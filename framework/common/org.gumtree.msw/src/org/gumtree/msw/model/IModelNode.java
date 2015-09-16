package org.gumtree.msw.model;

import java.util.Map;

public interface IModelNode {
	// finals
	public static final String ID = "Id";
	
	// construction
	public void dispose();
	
	// properties
	public IModelNode getOwner();
	public IModelNode getSub(String elementName);
	public String getName();
	public Iterable<String> getPath();
	public IModelNodeInfo getNodeInfo();
	//
	public Iterable<? extends IModelNode> getNodes();
	public Iterable<? extends IModelNode> getDeleted();

	// methods
	public IModelNode findNode(Iterable<String> path);
	// properties
	public Object getProperty(String property);
	public Map<String, Object> getProperties();
	public boolean changeProperty(String property, Object newValue);
	public boolean parseProperty(String property, String newValue);
	// list elements
	public Iterable<String> getListElements();
	public boolean addListElement(String elementName, int targetIndex);
	public boolean duplicateListElement(String originalElementName, String newElementName);
	public boolean deleteListElement(String elementName);
	public boolean recoverListElement(String elementName);

	// listeners
	public void addListener(IModelNodeListener listener);
	public boolean removeListener(IModelNodeListener listener);
}
