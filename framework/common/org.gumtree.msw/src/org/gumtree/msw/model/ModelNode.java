package org.gumtree.msw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

// contains properties and sub elements
class ModelNode implements IModelNode {	
	// fields
	private final IModelNodeInfo nodeInfo;
	// hierarchy
	private final ModelNode owner;
	private final String name;
	private Iterable<String> pathCache;
	// for recovery
	private int oldIndex;
	// content
	private Map<String, IModelNodePropertyInfo> pathToProperty;
	private Map<String, ModelNode> subNodes;
	private Map<String, ModelNode> deleted;
	private List<ModelNode> listOrder;
	// listeners
	private List<IModelNodeListener> listeners;
	
	// construction
	public ModelNode(IModelNodeInfo nodeInfo, String name) {
		this(nodeInfo, null, name);
	}
	private ModelNode(IModelNodeInfo nodeInfo, ModelNode owner, String name) {
		this.nodeInfo = nodeInfo;
		this.owner = owner;
		this.name = name;

		// content (LinkedHashMap is used to preserve order)
		pathToProperty = new LinkedHashMap<String, IModelNodePropertyInfo>();
		subNodes = new HashMap<String, ModelNode>();
		deleted = new HashMap<String, ModelNode>();
		listOrder = new ArrayList<ModelNode>();
		
		for (IModelNodePropertyInfo property : nodeInfo.getProperties())
			pathToProperty.put(property.getName(), property);
		
		// listeners
		listeners = new ArrayList<IModelNodeListener>();
	}
	@Override
	public void dispose() {
		for (ModelNode subNode : subNodes.values())
			subNode.dispose();
		for (ModelNode subNode : deleted.values())
			subNode.dispose();

		pathToProperty.clear();
		subNodes.clear();
		deleted.clear();
		listOrder.clear();
		listeners.clear();

		pathToProperty = null;
		subNodes = null;
		deleted = null;
		listOrder = null;
		listeners = null;
	}
	
	// properties
	@Override
	public ModelNode getOwner() {
		return owner;
	}
	@Override
	public ModelNode getSub(String elementName) {
		return subNodes.get(elementName);
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public Iterable<String> getPath() {
		if (pathCache == null) {
			List<String> list = new ArrayList<String>();
			
			// ignore root node
			ModelNode node = this;
			while (node.owner != null) {
				list.add(node.name);
				node = node.owner;
			}
			Collections.reverse(list);
			
			pathCache = Collections.unmodifiableList(list);
		}
		return pathCache;
	}
	// used for serialization and deserialization
	@Override
	public IModelNodeInfo getNodeInfo() {
		return nodeInfo;
	}
	//
	@Override
	public Iterable<? extends IModelNode> getNodes() {
		return listOrder;
	}
	@Override
	public Iterable<? extends IModelNode> getDeleted() {
		return deleted.values();
	}
	
	// methods
	@Override
	public ModelNode findNode(Iterable<String> path) {
		ModelNode result = this;
		if (path != null)
			for (String elementName : path) {
				ModelNode subNode = result.subNodes.get(elementName);
				if (subNode == null)
					return null;
				
				result = subNode;
			}
		return result;
	}
	// properties
	@Override
	public Object getProperty(String property) {
		if ((owner != null) && IModel.INDEX.equals(property))
			return owner.listOrder.indexOf(this);
		
		IModelNodePropertyInfo propertyInfo = pathToProperty.get(property);
		if (propertyInfo == null)
			return null;
			
		return propertyInfo.get();
	}
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> result = new HashMap<String, Object>(pathToProperty.size() + 1);
		
		if (owner != null)
			result.put(IModel.INDEX, owner.listOrder.indexOf(this));
		
		for (IModelNodePropertyInfo property : pathToProperty.values())
			result.put(property.getName(), property.get());

		return result;
	}
	@Override
	public boolean changeProperty(String property, Object newValue) {
		if (ID.equals(property))
			return false;
		
		if ((owner != null) && IModel.INDEX.equals(property))
			if (newValue instanceof Integer)
				return owner.move(this, (int)newValue);
			else
				return false;

		// return true if property is valid
		IModelNodePropertyInfo propertyInfo = pathToProperty.get(property);
		if (propertyInfo == null)
			return false;

		Object oldValue = propertyInfo.get();
		if (!Objects.equals(oldValue, newValue)) {
			if (!propertyInfo.set(newValue))
				return false;

			for (IModelNodeListener listener : listeners)
				listener.onChangedProperty(this, property, oldValue, newValue);
		}
		return true;
	}
	@Override
	public boolean parseProperty(String property, String newValue) {
		if (ID.equals(property))
			return false;
		
		if ((owner != null) && IModel.INDEX.equals(property))
			return owner.move(this, Integer.parseInt(newValue));

		// return true if property is valid
		IModelNodePropertyInfo propertyInfo = pathToProperty.get(property);
		if (propertyInfo == null)
			return false;

		Object oldValue = propertyInfo.get();
		if (!propertyInfo.parse(newValue))
			return false;
		
		if (!Objects.equals(oldValue, propertyInfo.get())) {
			for (IModelNodeListener listener : listeners)
				listener.onChangedProperty(this, property, oldValue, newValue);
		}
		return true;
	}
	// list elements
	@Override
	public Iterable<String> getListElements() {
		List<String> result = new ArrayList<String>(listOrder.size());
		for (ModelNode subNode : listOrder)
			result.add(subNode.getName());
		return result;
	}
	@Override
	public boolean addListElement(String elementName, int targetIndex) {
		// parse name
		String nodeInfoName = extractNodeInfoName(elementName);
		RefId id = extractId(elementName);

		// create new node and append
		IModelNodeInfo subNodeInfo = nodeInfo.loadSubNodeInfo(nodeInfoName);
		if (!appendChild(subNodeInfo, elementName, id, targetIndex))
			return false;
		
		return true;
	}
	@Override
	public boolean duplicateListElement(String originalElementName, String newElementName) {
		if (subNodes.containsKey(newElementName) || deleted.containsKey(newElementName))
			return false;
		
		ModelNode originalNode = subNodes.get(originalElementName);
		if (originalNode == null)
			return false;
		
		String nodeInfoName = extractNodeInfoName(newElementName);
		if (!Objects.equals(nodeInfoName, originalNode.nodeInfo.getName()))
			return false;

		ModelNode clone = originalNode.clone(this, newElementName);
		if (clone == null)
			return false;

		int index = listOrder.indexOf(originalNode);
		subNodes.put(newElementName, clone);
		listOrder.add(index, clone);

		for (IModelNodeListener listener : listeners)
			listener.onNewListNode(clone);
		for (IModelNodeListener listener : listeners)
			listener.onAddedListNode(this, clone);

		index++; // onChangedProperty event is not needed for new element
		int oldIndexOffset = -1;
		
		while (index != listOrder.size()) {
			ModelNode changedNode = listOrder.get(index);
			for (IModelNodeListener listener : changedNode.listeners)
				listener.onChangedProperty(changedNode, IModel.INDEX, index + oldIndexOffset, index);
			index++;
		}
		
		return true;
	}
	@Override
	public boolean deleteListElement(String elementName) {
		ModelNode subNode = subNodes.remove(elementName);
		if (subNode == null)
			return false;

		int index = listOrder.indexOf(subNode);
		subNode.oldIndex = index;
		listOrder.remove(index);
		deleted.put(elementName, subNode);

		for (IModelNodeListener listener : listeners)
			listener.onDeletedListNode(this, subNode);

		int oldIndexOffset = 1;
		
		while (index != listOrder.size()) {
			ModelNode changedNode = listOrder.get(index);
			for (IModelNodeListener listener : changedNode.listeners)
				listener.onChangedProperty(changedNode, IModel.INDEX, index + oldIndexOffset, index);
			index++;
		}
		
		return true;
	}
	@Override
	public boolean recoverListElement(String elementName) {
		ModelNode subNode = deleted.remove(elementName);
		if (subNode == null)
			return false;

		// update index
		int index = subNode.oldIndex;
		boolean oldIndexValid = index <= listOrder.size();
		
		if (!oldIndexValid)
			index = listOrder.size();

		subNodes.put(elementName, subNode);
		listOrder.add(index, subNode);

		for (IModelNodeListener listener : listeners)
			listener.onRecoveredListNode(this, subNode);

		if (!oldIndexValid)
			for (IModelNodeListener listener : subNode.listeners)
				listener.onChangedProperty(subNode, IModel.INDEX, subNode.oldIndex, index);

		index++;
		int oldIndexOffset = -1;
		
		while (index != listOrder.size()) {
			ModelNode changedNode = listOrder.get(index);
			for (IModelNodeListener listener : changedNode.listeners)
				listener.onChangedProperty(changedNode, IModel.INDEX, index + oldIndexOffset, index);
			index++;
		}
		
		return true;
	}
	// internal
	boolean appendChild(IModelNodeInfo subNodeInfo, String elementName) {
		// parse name
		RefId id = extractId(elementName);
		
		// create new node and append
		return appendChild(subNodeInfo, elementName, id, Integer.MAX_VALUE);
	}
	private boolean appendChild(IModelNodeInfo subNodeInfo, String elementName, RefId id, int targetIndex) {
		if (subNodeInfo == null)
			return false;
		if (subNodes.containsKey(elementName) || deleted.containsKey(elementName))
			return false;
		
		ModelNode subNode = new ModelNode(subNodeInfo, this, elementName);
		IModelNodePropertyInfo idProperty = subNode.pathToProperty.get(ID);
		if (idProperty != null) {
			// id required
			if (id == null)
				return false;

			idProperty.set(id.toString());
		}
		else {
			// id cannot be applied
			if (id != null)
				return false;
		}

		// update index
		int index = Math.max(0, targetIndex);
		if (index > listOrder.size())
			index = listOrder.size();
		
		subNodes.put(elementName, subNode);
		listOrder.add(index, subNode);

		for (IModelNodeListener listener : listeners)
			listener.onNewListNode(subNode);
		for (IModelNodeListener listener : listeners)
			listener.onAddedListNode(this, subNode);

		index++; // onChangedProperty event is not needed for new element
		int oldIndexOffset = -1;
		
		while (index != listOrder.size()) {
			ModelNode changedNode = listOrder.get(index);
			for (IModelNodeListener listener : changedNode.listeners)
				listener.onChangedProperty(changedNode, IModel.INDEX, index + oldIndexOffset, index);
			index++;
		}
		
		return true;
	}
	private ModelNode clone(ModelNode owner, String elementName) {
		ModelNode clone = new ModelNode(
				nodeInfo.clone(),
				owner,
				elementName);
		
		IModelNodePropertyInfo idProperty = clone.pathToProperty.get(ID);
		if (idProperty != null) {
			// id required
			RefId id = extractId(elementName);
			if (id == null)
				return null;

			idProperty.set(id.toString());
		}

		for (ModelNode child : listOrder) {
			String childName = child.getName();
			ModelNode childClone = child.clone(clone, childName);

			clone.subNodes.put(childName, childClone);
			clone.listOrder.add(childClone);
		}
		
		return clone;
	}
	
	// listeners
	@Override
 	public void addListener(IModelNodeListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);

		for (ModelNode subNode : subNodes.values())
			listener.onNewListNode(subNode);
		for (ModelNode subNode : deleted.values())
			listener.onNewListNode(subNode);
	}
	@Override
	public boolean removeListener(IModelNodeListener listener) {
		return listeners.remove(listener);
	}
	
	// helper
	private static String extractNodeInfoName(String name) {
		int i = name.indexOf(RefId.HASH);
		if (i != -1)
			return name.substring(0, i);
		else
			return name;
	}
	private static RefId extractId(String name) {
		int i = name.indexOf(RefId.HASH);
		if (i != -1)
			return RefId.parse(name.substring(i));
		else
			return null;
	}
	private boolean move(ModelNode subNode, int newIndex) {
		if (newIndex < 0)
			return false;
		else if (newIndex >= listOrder.size())
			return false;
		
		int oldIndex = listOrder.indexOf(subNode);
		if (oldIndex == -1)
			return false;
		
		if (oldIndex != newIndex) {
			listOrder.add(newIndex, listOrder.remove(oldIndex));
			for (IModelNodeListener listener : subNode.listeners)
				listener.onChangedProperty(subNode, IModel.INDEX, oldIndex, newIndex);

			int index, indexLast;
			int oldIndexOffset;
			
			if (newIndex < oldIndex) {
				index = newIndex + 1;
				indexLast = oldIndex;
				oldIndexOffset = -1;
			}
			else {
				index = oldIndex;
				indexLast = newIndex - 1;
				oldIndexOffset = +1;
			}

			// update indices
			while (index <= indexLast) {
				ModelNode changedNode = listOrder.get(index);
				for (IModelNodeListener listener : changedNode.listeners)
					listener.onChangedProperty(changedNode, IModel.INDEX, index + oldIndexOffset, index);
				index++;
			}
		}
		return true;
	}
}
