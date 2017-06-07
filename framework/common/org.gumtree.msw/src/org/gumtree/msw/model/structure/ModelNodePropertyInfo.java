package org.gumtree.msw.model.structure;

import java.util.Objects;

import org.gumtree.msw.RefId;
import org.gumtree.msw.model.IModelNode;
import org.gumtree.msw.model.IModelNodePropertyInfo;
import org.gumtree.msw.model.IRefIdMapper;
import org.gumtree.msw.model.ModelLoader;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class ModelNodePropertyInfo implements IModelNodePropertyInfo {
	// field
	private final String name;
	private final boolean required;
	private final ConstraintType constraint;
	private final boolean serializeAsAttribute;
	private final SimpleTypeDefinition typeDefinition;
	// content
	private final Object defaultValue;
	private Value value;
	
	// construction
	ModelNodePropertyInfo(String name, boolean required, ConstraintType constraint, Value defaultValue, SimpleTypeDefinition typeDefinition, boolean serializeAsAttribute) {
		this.name = name;
		this.required = required;
		this.constraint = constraint;
		this.serializeAsAttribute = serializeAsAttribute;
		this.typeDefinition = typeDefinition;
		
		Class<?> valueClass = typeDefinition.getValueClass();

		if (defaultValue == null)
			defaultValue = new Value(valueClass, typeDefinition.getFacets());
		else if (defaultValue.getValueClass() != valueClass)
			System.out.println(String.format(
					"WARNING: inconsistent value type (type: %s, defaultValue-type: %s)",
					valueClass,
					defaultValue.getValueClass()));

		this.defaultValue = defaultValue.get();
		this.value = defaultValue.clone();
	}
	private ModelNodePropertyInfo(ModelNodePropertyInfo reference) {
		name = reference.name;
		required = reference.required;
		constraint = reference.constraint;
		serializeAsAttribute = reference.serializeAsAttribute;
		typeDefinition = reference.typeDefinition;

		defaultValue = reference.defaultValue;
		value = reference.value.clone();
	}
	
	// properties
	@Override
	public String getName() {
		return name;
	}
	@Override
	public Class<?> getValueClass() {
		return value.getValueClass();
	}
	@Override
	public boolean isDefaultValue() {
		return Objects.equals(value.get(), defaultValue);
	}
	
	// methods
	@Override
	public void reset() {
		value.set(defaultValue);
	}
	@Override
	public Object get() {
		return value.get();
	}
	@Override
	public boolean validate(Object newValue) {
		if (Objects.equals(value.get(), newValue))
			return true;
		
		if (constraint == ConstraintType.FIXED)
			return false;
		
		return value.validate(newValue);
	}
	@Override
	public boolean set(Object newValue) {
		if (Objects.equals(value.get(), newValue))
			return true;
		
		if (constraint == ConstraintType.FIXED)
			return false;
		
		return value.set(newValue);
	}
	@Override
	public ModelNodePropertyInfo clone() {
		return new ModelNodePropertyInfo(this);
	}
	// xml
	public void serializeTo(Element owner, IRefIdMapper idMapper) {
		if (required || !isDefaultValue()) {
			String xmlValue;
			
			if (ModelLoader.MSW_SCHEMA_NS_URI.equals(typeDefinition.getNamespace()) &&
				IModelNode.ID.equals(typeDefinition.getName())) {
				
				xmlValue = idMapper.map(RefId.parse(value.get().toString())).toString();
			}
			else {
				xmlValue = value.serialize();
			}

			Document doc = owner.getOwnerDocument();
			if (serializeAsAttribute) {
				// org.w3c.dom.Attr doesn't seem to understand the global name-space
				//Attr attr = doc.createAttributeNS(ModelLoader.MSW_SCHEMA_NS_URI, name);
				Attr attr = doc.createAttribute(name);
				attr.setValue(xmlValue);
				owner.setAttributeNode(attr);
			}
			else {
				Element node = doc.createElementNS(ModelLoader.MSW_SCHEMA_NS_URI, name);
				node.setTextContent(xmlValue);
				owner.appendChild(node);
			}
		}
	}
	public boolean deserializeFrom(Element owner, IRefIdMapper idMapper) {
		String xmlValue;
		
		if (serializeAsAttribute) {
			Attr attr = owner.getAttributeNodeNS(ModelLoader.MSW_SCHEMA_NS_URI, name);
			if (attr == null) {
				// org.w3c.dom.Attr doesn't seem to understand the global name-space
				attr = owner.getAttributeNode(name);
				if (attr == null)
					return false;
			}
			
			xmlValue = attr.getValue();
		}
		else {
			NodeList nodes = owner.getElementsByTagNameNS(ModelLoader.MSW_SCHEMA_NS_URI, name);
			if (nodes.getLength() == 0)
				return false;

			xmlValue = nodes.item(0).getTextContent();
		}
		
		if (ModelLoader.MSW_SCHEMA_NS_URI.equals(typeDefinition.getNamespace()) &&
			IModelNode.ID.equals(typeDefinition.getName())) {
			
			return value.deserialize(idMapper.map(RefId.parse(xmlValue)).toString());
		}
		else {
			return value.deserialize(xmlValue);
		}
	}
}
