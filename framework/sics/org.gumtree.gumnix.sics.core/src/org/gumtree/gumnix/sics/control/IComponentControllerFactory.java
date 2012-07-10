package org.gumtree.gumnix.sics.control;

import org.gumtree.core.service.IContributionService;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.ISicsObjectController;

import ch.psi.sics.hipadaba.Component;

public interface IComponentControllerFactory extends IContributionService {

	public IComponentController createComponentController(Component component);
	
	// Create custom controllers for non-hdb sics objects 
	public ISicsObjectController[] createSicsObjectControllers();
	
}
