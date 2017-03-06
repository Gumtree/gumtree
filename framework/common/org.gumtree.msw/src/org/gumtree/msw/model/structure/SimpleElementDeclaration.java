package org.gumtree.msw.model.structure;

class SimpleElementDeclaration extends ElementDeclaration {
	// fields
	private boolean finalized;
	private SimpleTypeDefinition type;
	private Value defaultValue;
	private final ConstraintType constraint;
	
	// construction
	SimpleElementDeclaration(String name, String namespace, int minOccurs, int maxOccurs, ConstraintType constraint) {
		super(name, namespace, minOccurs, maxOccurs, false);
		this.constraint = constraint;
	}
	boolean finalize(SimpleTypeDefinition type, Value defaultValue) {
		if (finalized)
			return false;

		this.type = type;
		this.defaultValue = defaultValue;
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
