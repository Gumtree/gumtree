package org.gumtree.msw.model.structure;

class SimpleElementDeclaration extends ElementDeclaration {
	// fields
	private boolean finalized;
	private SimpleTypeDefinition type;
	private final ConstraintType constraint;
	private final Value defaultValue;
	
	// construction
	SimpleElementDeclaration(String name, String namespace, int minOccurs, int maxOccurs, ConstraintType constraint, Value defaultValue) {
		super(name, namespace, minOccurs, maxOccurs, false);
		this.constraint = constraint;
		this.defaultValue = defaultValue;
	}
	boolean finalize(SimpleTypeDefinition type) {
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
	public SimpleTypeDefinition getType() {
		return type;
	}
	public ConstraintType getConstraint() {
		return constraint;
	}
	public Value getDefaultValue() {
		return defaultValue;
	}

	// methods
	@Override
	public void accept(IParticleVisitor visitor) {
		visitor.visit(this);
	}
}
