package org.gumtree.control.imp;

import org.gumtree.control.core.IGroupController;
import org.gumtree.control.core.ISicsProxy;

import ch.psi.sics.hipadaba.Component;

public class GroupController extends SicsController implements IGroupController {

	public GroupController(Component model, ISicsProxy sicsProxy) {
		super(model, sicsProxy);
	}

}
