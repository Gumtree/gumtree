package org.gumtree.control.imp;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.model.ModelUtils;
import org.gumtree.control.model.PropertyConstants.ControllerState;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Property;

public class SicsController implements ISicsController {

	private Component model;
	private List<ISicsController> childControllers;
	private boolean enabled;
	private String errorMessage;
	private ControllerState state;
	private ISicsProxy sicsProxy;
	private List<ISicsControllerListener> listeners;
	
	public SicsController(Component model, ISicsProxy sicsProxy) {
		this.model = model;
		this.sicsProxy = sicsProxy;
		listeners = new ArrayList<ISicsControllerListener>();
		createChildrenControllers();
		state = ControllerState.IDLE;
		enabled = true;
	}
	
	protected ISicsProxy getSicsProxy() {
		return sicsProxy;
	}
	
	private void createChildrenControllers() {
		childControllers = new ArrayList<ISicsController>();
		for(Component childComponent : (List<Component>) getModel().getComponent()) {
			ISicsController controller = ModelUtils.createComponentController(sicsProxy, childComponent);
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
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeControllerListener(ISicsControllerListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected List<ISicsControllerListener> getListeners() {
		return listeners;
	}
	
	protected void fireStateChangeEvent(ControllerState oldState, ControllerState newState) {
		synchronized (listeners) {
			for (ISicsControllerListener listener : listeners) {
				listener.updateState(oldState, newState);
			}
		}
	}
	
	protected void fireEnabedEvent() {
		synchronized (listeners) {
			for (ISicsControllerListener listener : listeners) {
				listener.updateEnabled(isEnabled());
			}
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
	public List<String> getPropertyValue(String propId) {
		List<Property> propList = model.getProperty();
		if (propList != null) {
			for (Property prop : propList) {
				if (propId.equals(prop.getId())) {
					return prop.getValue();
				}
			}
		}
		return null;
	}
	
	@Override
	public ISicsController getChild(String childName) {
		synchronized (childControllers) {
			for(ISicsController childController : childControllers) {
				if(childController.getModel().getId().equals(childName)) {
					return childController;
				}
			}
		}
		return null;
	}
	
	@Override
	public void dispose() {
		if (listeners != null) {
			synchronized (listeners) {
				listeners.clear();
			}
		}
		synchronized (childControllers) {
			for(ISicsController childController : childControllers) {
				childController.dispose();
			}
			childControllers.clear();
		}
	}
}
