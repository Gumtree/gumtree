package org.gumtree.control.imp;

import org.gumtree.control.core.ICommandController;
import org.gumtree.control.core.ISicsCallback;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsException;

import ch.psi.sics.hipadaba.Component;

public class CommandController extends GroupController implements ICommandController {

	private boolean isBusy;
	
	public CommandController(Component model) {
		super(model);
	}

	@Override
	public boolean run(ISicsCallback callback) throws SicsException {
		isBusy = true;
		setErrorMessage(null);
		try {
			SicsManager.getSicsProxy().send("hset " + getDeviceId() + " start", null);
		} catch (SicsException e) {
			setErrorMessage(e.getMessage());
		}finally {
			isBusy = false;
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return isBusy;
	}

}
