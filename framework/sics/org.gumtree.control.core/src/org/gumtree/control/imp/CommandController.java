package org.gumtree.control.imp;

import org.gumtree.control.core.ICommandController;
import org.gumtree.control.core.ISicsCallback;
import org.gumtree.control.exception.SicsException;

import ch.psi.sics.hipadaba.Component;

public class CommandController extends GroupController implements ICommandController {

	public CommandController(Component model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(ISicsCallback callback) throws SicsException {
		// TODO Auto-generated method stub

	}

}
