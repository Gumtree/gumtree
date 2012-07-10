package org.gumtree.gumnix.sics.control;

import org.gumtree.core.object.ObjectFactory;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.ISicsObjectController;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;

public class ComponentControllerFactoryWrapper implements IComponentControllerFactory {

	private static Logger logger = LoggerFactory.getLogger(ComponentControllerFactoryWrapper.class);
	
	private String concreteClass;
	
	private volatile IComponentControllerFactory factory;
	
	public IComponentController createComponentController(Component component) {
		return getConcreteFactory().createComponentController(component);
	}

	public ISicsObjectController[] createSicsObjectControllers() {
		return getConcreteFactory().createSicsObjectControllers();
	}
	
	public String getConcreteClass() {
		if (concreteClass == null) {
			concreteClass = SicsCoreProperties.COMPONENT_CONTROLLER_FACTORY.getValue();
		}
		return concreteClass;
	}

	public void setConcreteClass(String concreteClass) {
		this.concreteClass = concreteClass;
	}
	
	private IComponentControllerFactory getConcreteFactory() {
		if (factory == null) {
			synchronized (this) {
				if (factory == null) {
					try {
						factory = ObjectFactory.instantiateObject(getConcreteClass(), IComponentControllerFactory.class);
					} catch (Exception e) {
						logger.error("Fail to instantiate class " + getConcreteClass(), e);
						// Use default implementation
						factory = new ComponentControllerFactory();
					}
				}
			}
		}
		return factory;
	}
	
}
