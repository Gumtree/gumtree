package org.gumtree.control.imp;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.ISicsController;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.model.ModelUtils;
import org.gumtree.control.model.PropertyConstants.ControllerState;

import ch.psi.sics.hipadaba.Component;

public class SicsController implements ISicsController {

	private Component model;
	private List<ISicsController> childControllers;
	private boolean enabled;
	private String errorMessage;
	private ControllerState state;
	private List<ISicsControllerListener> listeners;
	
	public SicsController(Component model) {
		this.model = model;
		listeners = new ArrayList<ISicsControllerListener>();
		createChildrenControllers();
		state = ControllerState.IDLE;
		enabled = true;
	}
	
	private void createChildrenControllers() {
		childControllers = new ArrayList<ISicsController>();
		for(Component childComponent : (List<Component>) getModel().getComponent()) {
			ISicsController controller = ModelUtils.createComponentController(childComponent);
			if(controller != null) {
				childControllers.add(controller);
			}
		}
	}
	
	@Override
	public List<ISicsController> getChildren() {
		return childControllers;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		enabled = isEnabled;
	}

	@Override
	public void addControllerListener(ISicsControllerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeControllerListener(ISicsControllerListener listener) {
		listeners.remove(listener);
	}

	protected List<ISicsControllerListener> getListeners() {
		return listeners;
	}
	
	protected void fireStateChangeEvent(ControllerState oldState, ControllerState newState) {
		for (ISicsControllerListener listener : listeners) {
			listener.updateState(oldState, newState);
		}
	}
	
	protected void fireEnabedEvent() {
		for (ISicsControllerListener listener : listeners) {
			listener.updateEnabled(isEnabled());
		}
	}
	
	public Component getModel() {
		return model;
	}

	@Override
	public String getId() {
		return model.getId();
	}

	@Override
	public String getPath() {
		return ModelUtils.getPath(model);
	}

	@Override
	public String getDeviceId() {
		return ModelUtils.getPropertyFirstValue(model, "sicsdev");
	}
	
	@Override
	public void setErrorMessage(String message) {
		errorMessage = message;
	}
	
	@Override
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public void clearError() {
		errorMessage = null;
	}
	
	@Override
	public void setState(ControllerState state) {
		ControllerState oldState = this.state;
		this.state = state;
		fireStateChangeEvent(oldState, state);
	}
	
	@Override
	public ControllerState getState() {
		return state;
	}

	@Override
	public ISicsController getChild(String childName) {
		for(ISicsController childController : childControllers) {
			if(childController.getModel().getId().equals(childName)) {
				return childController;
			}
		}
		return null;
	}
}
