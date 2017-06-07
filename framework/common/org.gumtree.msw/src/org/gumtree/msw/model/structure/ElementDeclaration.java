package org.gumtree.msw.model.structure;

abstract class ElementDeclaration extends Particle {
	// fields
	private boolean isRoot;
	private String name;
	private String namespace;
	
	// construction
	ElementDeclaration(String name, String namespace, int minOccurs, int maxOccurs, boolean isRoot) {
		super(minOccurs, maxOccurs);
		this.name = name;
		this.namespace = namespace;
		this.isRoot = isRoot;
	}
	
	// properties
	public String getName() {
		return name;
	}
	public String getNamespace() {
		return namespace;
	}
	public boolean isRoot() {
		return isRoot;
	}
	public abstract TypeDefinition getType();
}
