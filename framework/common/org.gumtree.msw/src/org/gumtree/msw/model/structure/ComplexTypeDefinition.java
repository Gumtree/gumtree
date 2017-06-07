package org.gumtree.msw.model.structure;

import java.util.List;

class ComplexTypeDefinition extends TypeDefinition {
	// fields
	private boolean finalized;
	private List<Attribute> attributes;
	private Particle particle;
	
	// construction
	ComplexTypeDefinition(String name, String namespace) {
		super(name, namespace);
		finalized = false;
	}
	boolean finalize(List<Attribute> attributes, Particle particle) {
		if (finalized)
			return false;

		this.attributes = attributes;
		this.particle = particle;
		return finalized = true;
	}
	
	// properties
	@Override
	public boolean isFinalized() {
		return finalized;
	}
	public List<Attribute> getAttributes() {
		return attributes;
	}
	public Particle getParticle() {
		return particle;
	}
}
