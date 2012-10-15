package org.gumtree.data.core.tests;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;

public class DataTestObject {

	private IFactory factory;
	
	public IFactory getFactory() {
		if (factory == null) {
			factory = Factory.getFactory();
		}
		return factory;
	}
	
	public void setFactory(IFactory factory) {
		this.factory = factory;
	}
	
}
