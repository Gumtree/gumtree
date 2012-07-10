package org.gumtree.gumnix.sics.control;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;

public class ControllerMap implements IControllerMap {

	private IComponentController baseController;

	private Map<IControllerKey, IComponentController> controllerMap;

	public ControllerMap(IComponentController baseController) {
		super();
		this.baseController = baseController;
	}

	public IComponentController getBaseController() {
		return baseController;
	}

	public Component getBaseComponent() {
		return getBaseController().getComponent();
	}

	public IComponentController getController(IControllerKey key) throws SicsCoreException {
		if(controllerMap == null) {
			controllerMap = new HashMap<IControllerKey, IComponentController>();
		}
		IComponentController controller = controllerMap.get(key);
		if(controller == null) {
			IComponentController candidate = SicsCore.getSicsController().findComponentController(getBaseController(), key.getRelativePath());
			if(candidate instanceof IComponentController) {
				controller = (IComponentController)candidate;
				controllerMap.put(key, controller);
			} else {
				throw new SicsCoreException("Cannot find controller for node " + baseController.getPath() + key.getRelativePath());
			}
		}
		return controller;
	}

	public Component getComponent(IControllerKey key) throws SicsCoreException {
		return getController(key).getComponent();
	}

	public int getIntData(IControllerKey key) throws SicsIOException, SicsCoreException {
		IComponentController controller = getController(key);
		if(controller instanceof IDynamicController) {
			return ((IDynamicController)controller).getValue().getIntData();
		} else {
			throw new SicsCoreException("Cannot get data from controller" + controller.getPath());
		}
	}

	public void setIntData(IControllerKey key, int data) throws SicsIOException, SicsCoreException {
		IComponentController controller = getController(key);
		if(controller instanceof IDynamicController) {
			((IDynamicController)controller).setTargetValue(ComponentData.createIntData(data));
			((IDynamicController)controller).commitTargetValue(null);
		} else {
			throw new SicsCoreException("Cannot get data from controller" + controller.getPath());
		}
	}

	public float getFloatData(IControllerKey key) throws SicsIOException, SicsCoreException {
		IComponentController controller = getController(key);
		if(controller instanceof IDynamicController) {
			return ((IDynamicController)controller).getValue().getFloatData();
		} else {
			throw new SicsCoreException("Cannot get data from controller" + controller.getPath());
		}
	}

	public void setFloatData(IControllerKey key, float data) throws SicsIOException, SicsCoreException {
		IComponentController controller = getController(key);
		if(controller instanceof IDynamicController) {
			((IDynamicController)controller).setTargetValue(ComponentData.createFloatData(data));
			((IDynamicController)controller).commitTargetValue(null);
		} else {
			throw new SicsCoreException("Cannot get data from controller" + controller.getPath());
		}
	}

	public String getStringData(IControllerKey key) throws SicsIOException, SicsCoreException {
		IComponentController controller = getController(key);
		if(controller instanceof IDynamicController) {
			return ((IDynamicController)controller).getValue().getStringData();
		} else {
			throw new SicsCoreException("Cannot get data from controller" + controller.getPath());
		}
	}

	public void setStringData(IControllerKey key, String data) throws SicsIOException, SicsCoreException {
		IComponentController controller = getController(key);
		if(controller instanceof IDynamicController) {
			((IDynamicController)controller).setTargetValue(ComponentData.createStringData(data));
			((IDynamicController)controller).commitTargetValue(null);
		} else {
			throw new SicsCoreException("Cannot get data from controller" + controller.getPath());
		}
	}

	public int[] getIntArray(IControllerKey key) throws SicsIOException, SicsCoreException {
		IComponentController controller = getController(key);
		if(controller instanceof IDynamicController) {
			return ((IDynamicController)controller).getValue().getIntArrayData();
		} else {
			throw new SicsCoreException("Cannot get data from controller" + controller.getPath());
		}
	}

	public float[] getFloatArray(IControllerKey key) throws SicsIOException, SicsCoreException {
		IComponentController controller = getController(key);
		if(controller instanceof IDynamicController) {
			return ((IDynamicController)controller).getValue().getFloatArrayData();
		} else {
			throw new SicsCoreException("Cannot get data from controller" + controller.getPath());
		}
	}

}
