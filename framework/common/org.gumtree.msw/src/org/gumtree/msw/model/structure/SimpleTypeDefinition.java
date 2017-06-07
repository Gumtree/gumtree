package org.gumtree.msw.model.structure;

class SimpleTypeDefinition extends TypeDefinition {
	// fields
	private final Class<?> valueClass;
	private final FacetList facets;
	
	// construction
	SimpleTypeDefinition(String name, String namespace, Class<?> valueClass, FacetList facets) {
		super(name, namespace);
		this.valueClass = valueClass;
		this.facets = facets;
	}
	
	// properties
	@Override
	public boolean isFinalized() {
		return true;
	}
	public Class<?> getValueClass() {
		return valueClass;
	}
	public FacetList getFacets() {
		return facets;
	}
}
