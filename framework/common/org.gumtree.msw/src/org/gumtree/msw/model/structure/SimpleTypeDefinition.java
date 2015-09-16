package org.gumtree.msw.model.structure;

class SimpleTypeDefinition extends TypeDefinition {
	// fields
	private Class<?> valueClass;
	
	// construction
	SimpleTypeDefinition(String name, String namespace, Class<?> valueClass) {
		super(name, namespace);
		this.valueClass = valueClass;
	}
	
	// properties
	@Override
	public boolean isFinalized() {
		return true;
	}
	public Class<?> getValueClass() {
		return valueClass;
	}
}
