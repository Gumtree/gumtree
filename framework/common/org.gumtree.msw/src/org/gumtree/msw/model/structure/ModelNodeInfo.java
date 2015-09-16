package org.gumtree.msw.model.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gumtree.msw.model.IModelNodeInfo;
import org.gumtree.msw.model.IModelNodePropertyInfo;
import org.gumtree.msw.model.IRefIdMapper;
import org.gumtree.msw.model.ModelLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class ModelNodeInfo implements IModelNodeInfo {
	// fields
	private final ComplexElementDeclaration elementDeclaration;
	private final List<ComplexElementDeclaration> complexElements;
	private final List<ModelNodePropertyInfo> properties;

	// construction
	ModelNodeInfo(ComplexElementDeclaration elementDeclaration) {
		this.elementDeclaration = elementDeclaration;

		complexElements = new ArrayList<ComplexElementDeclaration>();
		properties = new ArrayList<ModelNodePropertyInfo>();
		
		// analyze type
		ComplexTypeDefinition type = elementDeclaration.getType();
		Particle subParticle = type.getParticle();
		
		// attributes
		for (Attribute attribute : type.getAttributes())
			properties.add(new ModelNodePropertyInfo(
					attribute.getName(),
					attribute.isRequired(),
					attribute.getConstraint(),
					attribute.getDefaultValue(),
					attribute.getType(),
					true));

		if (subParticle != null) {
			// complex elements
			subParticle.accept(new ElementCollector(complexElements));
			
			// simple elements
			subParticle.accept(new PropertyCollector(properties));
		}
	}
	private ModelNodeInfo(ModelNodeInfo reference) {
		elementDeclaration = reference.elementDeclaration;
		complexElements = reference.complexElements;
				
		properties = new ArrayList<ModelNodePropertyInfo>(reference.properties.size());
		for (ModelNodePropertyInfo property : reference.properties)
			properties.add(property.clone());
	}

	// properties
	@Override
	public String getName() {
		return elementDeclaration.getName();
	}
	@Override
	public IModelNodePropertyInfo getProperty(String name) {
		for (ModelNodePropertyInfo propertyInfo : properties)
			if (propertyInfo.getName().equals(name))
				return propertyInfo;
		
		return null;
	}
	@Override
	public IModelNodePropertyInfo[] getProperties() {
		IModelNodePropertyInfo[] result = new IModelNodePropertyInfo[properties.size()];
		return properties.toArray(result);
	}
	
	// methods
	@Override
	public void reset() {
		for (ModelNodePropertyInfo property : properties)
			property.reset();
	}
	@Override
	public ModelNodeInfo clone() {
		return new ModelNodeInfo(this);
	}
	@Override
	public Element serialize(Document document, IRefIdMapper idMapper) {
		Element result = document.createElementNS(
				ModelLoader.MSW_SCHEMA_NS_URI,
				elementDeclaration.getName());
		
		for (ModelNodePropertyInfo property : properties)
			property.serializeTo(result, idMapper);
		
		return result;
	}
	@Override
	public boolean deserialize(Element element, IRefIdMapper idMapper) {
		if (!elementDeclaration.getName().equals(element.getTagName()) ||
			!ModelLoader.MSW_SCHEMA_NS_URI.equals(element.getNamespaceURI())) {
			reset();
			return false;
		}

		for (ModelNodePropertyInfo property : properties)			
			if (!property.deserializeFrom(element, idMapper))
				property.reset();
		
		return true;
	}
	// sub nodes
	@Override
	public IModelNodeInfo loadSubNodeInfo(String name) {
		for (ComplexElementDeclaration declaration : complexElements)
			if (declaration.getName().equals(name))
				return new ModelNodeInfo(declaration);
		
		return null;
	}

	// property collector
	private static class PropertyCollector implements IParticleVisitor {
		// fields
		private List<ModelNodePropertyInfo> properties;
		private Set<Particle> visited;
		
		// construction
		public PropertyCollector(List<ModelNodePropertyInfo> properties) {
			this.properties = properties;
			this.visited = new HashSet<Particle>();
		}

		// methods
		@Override
		public void visit(SimpleElementDeclaration simpleElement) {
			if (visited.add(simpleElement))
				properties.add(new ModelNodePropertyInfo(
						simpleElement.getName(),
						simpleElement.isRequired(),
						simpleElement.getConstraint(),
						simpleElement.getDefaultValue(),
						simpleElement.getType(),
						false));
		}
		@Override
		public void visit(ComplexElementDeclaration complexElement) {
			// ignore
		}
		@Override
		public void visit(ModelGroup modelGroup) {
			if (visited.add(modelGroup))
				for (Particle particle : modelGroup.getParticles())
					particle.accept(this);
		}
	}
	
	// element collector
	private static class ElementCollector implements IParticleVisitor {
		// fields
		private List<ComplexElementDeclaration> complexElements;
		private Set<Particle> visited;
	
		// construction
		public ElementCollector(List<ComplexElementDeclaration> complexElements) {
			this.complexElements = complexElements;
			this.visited = new HashSet<Particle>();
		}
	
		// methods
		@Override
		public void visit(SimpleElementDeclaration simpleElement) {
			// ignore
		}
		@Override
		public void visit(ComplexElementDeclaration complexElement) {
			if (visited.add(complexElement))
				complexElements.add(complexElement);
		}
		@Override
		public void visit(ModelGroup modelGroup) {
			if (visited.add(modelGroup))
				for (Particle particle : modelGroup.getParticles())
					particle.accept(this);
		}
	}
}
