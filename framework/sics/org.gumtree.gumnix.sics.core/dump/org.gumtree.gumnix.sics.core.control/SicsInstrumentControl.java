package org.gumtree.gumnix.sics.internal.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.sdo.EDataObject;
import org.gumtree.gumnix.sics.control.DeviceController;
import org.gumtree.gumnix.sics.control.IComponentController;
import org.gumtree.gumnix.sics.control.IDeviceController;
import org.gumtree.gumnix.sics.control.IInstrumentController;
import org.gumtree.gumnix.sics.control.ISicsInstrumentControl;
import org.gumtree.gumnix.sics.control.InstrumentController;
import org.gumtree.gumnix.sics.core.ISicsComponentAdapterManager;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Instrument;
import ch.psi.sics.hipadaba.Property;

public class SicsInstrumentControl implements ISicsInstrumentControl {

//	private static Logger logger = Logger.getLogger(SicsInstrumentControl.class);

	private Map<String, IComponentController> componentControllerMap;

	private ISicsManager manager;

	private SicsPropertyMonitor monitor;

	private Instrument model;

	private IInstrumentController instrumentController;

	public SicsInstrumentControl(ISicsManager manager) {
		this.manager = manager;
	}

	protected ISicsManager getManager() {
		return manager;
	}

	public Instrument getModel() throws SicsIOException {
		if(model == null) {
			SicsInstrumentModelLoader loader = new SicsInstrumentModelLoader(getManager());
			loader.loadModel();
			model = loader.getModel();
			if(model != null) {
				initialiseInstrumentController(model);
			}
		}
		return model;
	}

	public IInstrumentController getInstrumentController() {
		if(model == null) {
			try {
				getModel();
			} catch (SicsIOException e) {
//				e.printStackTrace();
			}
		}
		return instrumentController;
	}

	private void initialiseInstrumentController(Instrument model) {
		ISicsComponentAdapterManager adapterManager = ISicsManager.INSTANCE.service().getAdapterManager();
		IInstrumentController controller = (IInstrumentController)adapterManager.getComponentAdapter(model, IInstrumentController.class);
		if(controller == null) {
			controller = new InstrumentController();
		}
		if(controller instanceof InstrumentController) {
			((InstrumentController)controller).setComponent(model);
		}
		instrumentController = controller;
		getComponentControllerMap().put(instrumentController.getPath(), instrumentController);
		// attach monitor
		List<IDeviceController> deviceControllers = new ArrayList<IDeviceController>();
		deviceControllers = SicsUtils.findAllDeviceControllers(controller, deviceControllers);
		for(IDeviceController deviceController : deviceControllers) {
			if (deviceController instanceof ISicsPropertyListener) {
				getSicsPropertyMonitor().addListener(deviceController.getDevice(), (ISicsPropertyListener)deviceController);
			}
			getComponentControllerMap().put(deviceController.getPath(), deviceController);
		}
	}

	private SicsPropertyMonitor getSicsPropertyMonitor() {
		if(monitor == null) {
			monitor = new SicsPropertyMonitor(this);
		}
		return monitor;
	}

	private Map<String, IComponentController> getComponentControllerMap() {
		if(componentControllerMap == null) {
			componentControllerMap = new HashMap<String, IComponentController>();
		}
		return componentControllerMap;
	}

	public IComponentController findComponentController(String path) {
		// ensures controller is initialised
		getInstrumentController();
		return getComponentControllerMap().get(path);
	}

	public IComponentController findComponentController(Component component) {
		return findComponentController(SicsUtils.getPath(component));
	}

}
