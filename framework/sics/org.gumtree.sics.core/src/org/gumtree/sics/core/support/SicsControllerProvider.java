package org.gumtree.sics.core.support;

import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.gumtree.sics.control.IServerController;
import org.gumtree.sics.control.ISicsController;
import org.gumtree.sics.control.support.DynamicController;
import org.gumtree.sics.control.support.ServerController;
import org.gumtree.sics.control.support.SicsController;
import org.gumtree.sics.core.ISicsControllerProvider;
import org.gumtree.sics.core.ISicsModelProvider;
import org.gumtree.sics.core.PropertyConstants.PropertyType;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.util.SicsModelUtils;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.SICS;

public class SicsControllerProvider implements ISicsControllerProvider{

	private ISicsProxy proxy;
	
	private ISicsModelProvider modelProvider;
	
	private IServerController serverController;
	
	@Override
	public IServerController createServerController() {
		if (serverController == null) {
			serverController = new ServerController();
		}
		if (getProxy() != null) {
			serverController.setProxy(getProxy());
		}
		SICS sicsModel = modelProvider.getModel();
		createSicsController(serverController, sicsModel.getComponent());
		return serverController;
	}
	
	protected ISicsController createComponentBasedController(Component component) {
		ISicsController controller = null;
		DataType dataType = component.getDataType();
		if (dataType != null && !DataType.NONE_LITERAL.equals(dataType)) {
			controller = new DynamicController();
		} else {
			controller = new SicsController();
		}
		controller.setId(component.getId());
		controller.setDeviceId(SicsModelUtils.getPropertyFirstValue(component, PropertyType.SICS_DEV));
		controller.setComponentModel(component);
		return controller;
	}
	
	private void createSicsController(ISicsController parent, List<Component> childComponents) {
		for (Component childComponent : childComponents) {
			ISicsController controller = createComponentBasedController(childComponent);
			parent.addChild(controller);
			controller.setParent(parent);
			controller.setProxy(getProxy());
			createSicsController(controller, childComponent.getComponent());
		}
	}
	
	@Override
	public ISicsProxy getProxy() {
		return proxy;
	}

	@Override
	@Inject
	public void setProxy(ISicsProxy proxy) {
		this.proxy = proxy;
	}
	
	@Override
	public ISicsModelProvider getModelProvider() {
		return modelProvider;
	}
	
	@Override
	@Inject
	public void setModelProvider(ISicsModelProvider modelProvider) {
		this.modelProvider = modelProvider;
	}

	@Override
	@PreDestroy
	public void disposeObject() {
		proxy = null;
		modelProvider = null;
	}

}
