package org.gumtree.msw.model.structure;

abstract class TypeDefinition {
	// fields
	private final String name;
	private final String namespace;
	
	// construction
	TypeDefinition(String name, String namespace) {
		this.name = name;
		this.namespace = namespace;
	}
	
	// properties
	public abstract boolean isFinalized();
	public String getName() {
		return name;
	}
	public String getNamespace() {
		return namespace;
	}
}
