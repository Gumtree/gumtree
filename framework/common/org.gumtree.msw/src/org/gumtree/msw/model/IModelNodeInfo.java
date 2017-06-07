package org.gumtree.msw.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IModelNodeInfo {
	// properties
	public String getName();
	public IModelNodePropertyInfo getProperty(String name);
	public IModelNodePropertyInfo[] getProperties();
	
	// methods
	public void reset();
	public IModelNodeInfo clone();
	public Element serialize(Document document, IRefIdMapper idMapper);
	public boolean deserialize(Element element, IRefIdMapper idMapper);
	// sub nodes
	public IModelNodeInfo loadSubNodeInfo(String name);
}
