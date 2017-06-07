package org.gumtree.msw.model.structure;

abstract class Particle {
	// fields
	private final int minOccurs;
	private final int maxOccurs;
	
	// construction
	Particle(int minOccurs, int maxOccurs) {
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
	}
	
	// properties
	public abstract boolean isFinalized();
	public int getMinOccurs() {
		return minOccurs;
	}
	public int getMaxOccurs() {
		return maxOccurs;
	}
	public boolean isRequired() {
		return minOccurs > 0;
	}
	
	// methods
	public abstract void accept(IParticleVisitor visitor);
}
