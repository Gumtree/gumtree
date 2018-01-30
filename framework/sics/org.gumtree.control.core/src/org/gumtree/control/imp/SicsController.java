package org.gumtree.control.imp;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.ISicsController;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.model.ModelUtils;

import ch.psi.sics.hipadaba.Component;

public class SicsController implements ISicsController {

	private Component model;
	private List<ISicsController> childControllers;
	private boolean enabled;
	
	public SicsController(Component model) {
		this.model = model;
		createChildrenControllers();
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
		// TODO Auto-generated method stub

	}

	@Override
	public void removeControllerListener(ISicsControllerListener listener) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return ModelUtils.getPath(model);
	}

	@Override
	public String getDeviceId() {
		// TODO Auto-generated method stub
		return ModelUtils.getPropertyFirstValue(model, "sicsdev");
	}
}
