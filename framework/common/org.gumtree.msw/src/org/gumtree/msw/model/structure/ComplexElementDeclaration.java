package org.gumtree.msw.model.structure;

class ComplexElementDeclaration extends ElementDeclaration {
	// fields
	private boolean finalized;
	private ComplexTypeDefinition type;
	
	// construction
	ComplexElementDeclaration(String name, String namespace, int minOccurs, int maxOccurs, boolean isRoot) {
		super(name, namespace, minOccurs, maxOccurs, isRoot);
	}
	boolean finalize(ComplexTypeDefinition type) {
		if (finalized)
			return false;
		
		this.type = type;
		return finalized = true;
	}
	
	// properties
	@Override
	public boolean isFinalized() {
		return finalized;
	}
	@Override
	public ComplexTypeDefinition getType() {
		return type;
	}

	// methods
	@Override
	public void accept(IParticleVisitor visitor) {
		visitor.visit(this);
	}
}
