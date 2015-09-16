package org.gumtree.msw.model;

public interface IModelNodeListener {
	// methods
	public void onNewListNode(IModelNode node);
	// properties
	public void onChangedProperty(IModelNode node, String property, Object oldValue, Object newValue);
	// list elements
	public void onAddedListNode(IModelNode nodeList, IModelNode subNode);	// adding one sample can lead to adding many schedule-nodes (e.g. for each configuration)
	public void onDeletedListNode(IModelNode nodeList, IModelNode subNode);
	public void onRecoveredListNode(IModelNode nodeList, IModelNode subNode);
}
