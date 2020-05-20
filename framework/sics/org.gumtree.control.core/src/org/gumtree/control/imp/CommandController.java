package org.gumtree.control.imp;

import java.util.Map;

import org.gumtree.control.core.ICommandController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.model.PropertyConstants.ControllerState;

import ch.psi.sics.hipadaba.Component;

public class CommandController extends GroupController implements ICommandController {

	private boolean isBusy;
	private CommandStatusListener statusListener;
	
	class CommandStatusListener implements ISicsControllerListener {
		
		boolean isRegistered = false;

		@Override
		public void updateValue(Object oldValue, Object newValue) {
			if (newValue != null) {
				setState(ControllerState.valueOf(String.valueOf(newValue).toUpperCase()));
			}
		}
		
		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
		}
		
		@Override
		public void updateEnabled(boolean isEnabled) {
		}
	}
	
	public CommandController(Component model, ISicsProxy sicsProxy) {
		super(model, sicsProxy);
		statusListener = new CommandStatusListener();
	}

	private synchronized void registerStatusListener() {
		if (!statusListener.isRegistered) {
			ISicsController feedback = this.getChild("feedback");
			if (feedback != null) {
				ISicsController status = feedback.getChild("status");
				if (status != null) {
					status.addControllerListener(statusListener);
					statusListener.isRegistered = true;
				}
			}
		}
	}
	
	@Override
	public boolean run(ISicsCallback callback) throws SicsException {
		if (isBusy) {
			throw new SicsException("command controller is busy");
		}
		isBusy = true;
		setErrorMessage(null);
		registerStatusListener();
		try {
			getSicsProxy().syncRun("hset " + getPath() + " start", null);
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
