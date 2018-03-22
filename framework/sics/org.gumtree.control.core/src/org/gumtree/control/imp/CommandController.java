package org.gumtree.control.imp;

import java.util.Map;

import org.gumtree.control.core.ICommandController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.exception.SicsException;

import ch.psi.sics.hipadaba.Component;

public class CommandController extends GroupController implements ICommandController {

	private boolean isBusy;
	
	public CommandController(Component model, ISicsProxy sicsProxy) {
		super(model, sicsProxy);
	}

	@Override
	public boolean run(ISicsCallback callback) throws SicsException {
		isBusy = true;
		setErrorMessage(null);
		try {
			getSicsProxy().syncRun("hset " + getDeviceId() + " start", null);
		} catch (SicsException e) {
			setErrorMessage(e.getMessage());
		}finally {
			isBusy = false;
		}
		return false;
	}

	@Override
	public boolean run(Map<String, Object> parameters, ISicsCallback callback) throws SicsException {
		for (String key : parameters.keySet()) {
			ISicsController child = getChild(key);
			if (child instanceof IDynamicController) {
				((IDynamicController) child).setTargetValue(parameters.get(key));
				((IDynamicController) child).commitTargetValue();
			}
		}
		return run(callback);
	}	
	
	@Override
	public boolean isBusy() {
		return isBusy;
	}

}
