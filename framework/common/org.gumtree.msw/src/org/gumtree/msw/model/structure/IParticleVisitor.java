package org.gumtree.msw.model.structure;

interface IParticleVisitor {
	// methods
	public void visit(SimpleElementDeclaration simpleElement);
	public void visit(ComplexElementDeclaration complexElement);
	public void visit(ModelGroup modelGroup);
}
