package org.gumtree.msw.model.structure;

class Attribute {
	// fields
	private final String name;
	private final SimpleTypeDefinition type;
	private final boolean required;
	private final ConstraintType constraint;
	private final Value defaultValue;

	// construction
	public Attribute(String name, SimpleTypeDefinition type, boolean required, ConstraintType constraint, Value defaultValue) {
		this.name = name;
		this.type = type;
		this.required = required;
		this.constraint = constraint;
		this.defaultValue = defaultValue;
	}

	// properties
	public String getName() {
		return name;
	}
	public SimpleTypeDefinition getType() {
		return type;
	}
	public boolean isRequired() {
		return required;
	}
	public ConstraintType getConstraint() {
		return constraint;
	}
	public Value getDefaultValue() {
		return defaultValue;
	}
}
