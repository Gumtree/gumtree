package org.gumtree.gumnix.sics.internal.ui.controlview;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

import ch.psi.sics.hipadaba.Property;

public class ControlTreeViewer extends TreeViewer {

	Map<Property, PropertyTreeNode> propertyTreeNodeMap;
	
	public ControlTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	public void addPropertyTreeNode(Property property, PropertyTreeNode node) {
		getMap().put(property, node);
	}
	
	public void removePropertyTreeNode(Property property) {
		getMap().remove(property);
	}
	
	public PropertyTreeNode findNode(Property property) {
		return getMap().get(property);
	}
	
	private Map<Property, PropertyTreeNode> getMap() {
		if(propertyTreeNodeMap == null) {
			propertyTreeNodeMap = new HashMap<Property, PropertyTreeNode>();
		}
		return propertyTreeNodeMap;
	}
	
}
