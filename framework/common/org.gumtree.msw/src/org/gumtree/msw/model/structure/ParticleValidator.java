package org.gumtree.msw.model.structure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

final class ParticleValidator implements IParticleVisitor {
	// fields
	private boolean successful;
	private Set<Object> tested;
	
	// construction
	ParticleValidator() {
		successful = true;
		tested = new HashSet<Object>();
	}
	
	// properties successful
	public boolean successful() {
		return !tested.isEmpty() && successful;
	}

	// methods
	@Override
	public void visit(SimpleElementDeclaration simpleElement) {
		if (tested.add(simpleElement))
			if (!simpleElement.isFinalized())
				successful = false;
			else
				validateType(simpleElement.getType());
	}
	@Override
	public void visit(ComplexElementDeclaration complexElement) {
		if (tested.add(complexElement))
			if (!complexElement.isFinalized())
				successful = false;
			else
				validateType(complexElement.getType());
	}
	@Override
	public void visit(ModelGroup modelGroup) {
		if (tested.add(modelGroup))
			if (!modelGroup.isFinalized())
				successful = false;
			else
				for (Particle particle : modelGroup.getParticles()) {
					particle.accept(this);
					if (!successful)
						break;
				}
	}
	// helpers
	private void validateType(SimpleTypeDefinition simpleType) {
		if (tested.add(simpleType))
			if (!simpleType.isFinalized())
				successful = false;
	}
	private void validateType(ComplexTypeDefinition complexType) {
		if (tested.add(complexType))
			if (!complexType.isFinalized())
				successful = false;
			else {
				// each property name must only appear once
				Set<String> propertyNames = new HashSet<String>();

				// check attribute names
				for (Attribute attribute : complexType.getAttributes()) {
					String attributeName = attribute.getName();
					if ((attributeName == null) || (attributeName.length() == 0) || !propertyNames.add(attributeName.toLowerCase())) {
						successful = false;
						return;
					}
				}
				
				// check particle
				Particle particle = complexType.getParticle();
				if (particle != null) {
					// check order
					ParticleOrderValidator particleOrderValidator = new ParticleOrderValidator();
					particle.accept(particleOrderValidator);
					if (!particleOrderValidator.successful()) {
						successful = false;
						return;
					}
					
					// check element names
					ParticleNameValidator particleNameValidator = new ParticleNameValidator(propertyNames);
					particle.accept(particleNameValidator);
					if (!particleNameValidator.successful()) {
						successful = false;
						return;
					}

					// validate particle
					particle.accept(this);
					if (!successful)
						return;
				}
			}
	}

	// helper
	private static class ParticleNameValidator implements IParticleVisitor {
		// fields
		private boolean successful;
		private Set<Particle> visited;
		private Set<String> propertyNames;
		
		// construction
		public ParticleNameValidator(Set<String> propertyNames) {
			this.successful = true;
			this.propertyNames = propertyNames;
			this.visited = new HashSet<Particle>();
		}
		
		// properties successful
		public boolean successful() {
			return !visited.isEmpty() && successful;
		}
		
		// methods
		@Override
		public void visit(SimpleElementDeclaration simpleElement) {
			if (visited.add(simpleElement)) {
				String elementName = simpleElement.getName();
				if ((elementName == null) || (elementName.length() == 0) || !propertyNames.add(elementName.toLowerCase()))
					successful = false;
			}
		}
		@Override
		public void visit(ComplexElementDeclaration complexElement) {
			if (visited.add(complexElement)) {
				String elementName = complexElement.getName();
				if ((elementName == null) || (elementName.length() == 0) || !propertyNames.add(elementName.toLowerCase()))
					successful = false;
			}
		}
		@Override
		public void visit(ModelGroup modelGroup) {
			if (visited.add(modelGroup))
				for (Particle particle : modelGroup.getParticles()) {
					particle.accept(this);
					if (!successful)
						break;
				}
		}
	}

	// simple elements can only exist before complex elements (see how model is serialized to xml)
	private static class ParticleOrderValidator implements IParticleVisitor {
		// fields
		private boolean successful;
		private Deque<Boolean> isSimple;
		
		// construction
		public ParticleOrderValidator() {
			successful = true;
			isSimple = new ArrayDeque<Boolean>();
			isSimple.addLast(true);
		}
		
		// properties successful
		public boolean successful() {
			return successful;
		}
		
		// methods
		@Override
		public void visit(SimpleElementDeclaration simpleElement) {
			if (isSimple.peekLast() == false)
				successful = false;
		}
		@Override
		public void visit(ComplexElementDeclaration complexElement) {
			if (isSimple.peekLast() == true) {
				isSimple.pollLast();
				isSimple.addLast(false);
			}
		}
		@Override
		public void visit(ModelGroup modelGroup) {
			isSimple.addLast(isSimple.peekLast());
			for (Particle particle : modelGroup.getParticles())
				particle.accept(this);
			isSimple.pollLast();
		}
	}
}
