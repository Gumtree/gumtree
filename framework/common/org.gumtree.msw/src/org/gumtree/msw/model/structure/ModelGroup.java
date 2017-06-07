package org.gumtree.msw.model.structure;

import java.util.List;

class ModelGroup extends Particle {
	// fields
	private boolean finalized;
	private Compositor compositor;
	private List<Particle> particles;

	// construction
	ModelGroup(Compositor compositor, int minOccurs, int maxOccurs) {
		super(minOccurs, maxOccurs);
		this.compositor = compositor;
	}
	boolean finalize(List<Particle> particles) {
		if (finalized)
			return false;
		
		this.particles = particles;
		return finalized = true;
	}
	
	// properties
	@Override
	public boolean isFinalized() {
		return finalized;
	}
	public Compositor getCompositor() {
		return compositor;
	}
	public List<Particle> getParticles() {
		return particles;
	}

	// methods
	@Override
	public void accept(IParticleVisitor visitor) {
		visitor.visit(this);
	}
}
