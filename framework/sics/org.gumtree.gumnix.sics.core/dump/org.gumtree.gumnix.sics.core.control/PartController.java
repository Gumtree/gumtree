package org.gumtree.gumnix.sics.control;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.gumnix.sics.core.ISicsComponentAdapterManager;
import org.gumtree.gumnix.sics.core.ISicsManager;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Part;

public class PartController extends ComponentController implements IPartController {

	private List<IPartController> partControllers;

	private List<IDeviceController> deviceControllers;

	public PartController() {
		super();
	}

	@SuppressWarnings("unchecked")
	public void setComponent(Component component) {
		if(component instanceof Part) {
			Part part = (Part)component;
			super.setComponent(part);
			ISicsComponentAdapterManager adapterManager = ISicsManager.INSTANCE.service().getAdapterManager();
			for(Part subPart : (List<Part>)part.getPart()) {
				IPartController partController = (IPartController)adapterManager.getComponentAdapter(subPart, IPartController.class);
				if(partController == null) {
					partController = new PartController();
				}
				if(partController instanceof PartController) {
					((PartController)partController).setComponent(subPart);
					((PartController)partController).setParentController(this);
				}
				getPartControllers().add(partController);
			}
			for(Device device : (List<Device>)part.getDevice()) {
				IDeviceController deviceController = (IDeviceController)adapterManager.getComponentAdapter(device, IDeviceController.class);
				if(deviceController == null) {
					deviceController = new DeviceController();
				}
				if(deviceController instanceof DeviceController) {
					((DeviceController)deviceController).setComponent(device);
					((DeviceController)deviceController).setParentController(this);
				}
				getDeviceControllers().add(deviceController);
			}
		} else {
			throw new Error("Component is not an instance of Part.");
		}
	}

	public Part getPart() {
		return (Part)getComponent();
	}

	public IPartController[] getChildPartControllers() {
		return getPartControllers().toArray(new IPartController[getPartControllers().size()]);
	}

	public IDeviceController[] getChildDeviceControllers() {
		return getDeviceControllers().toArray(new IDeviceController[getDeviceControllers().size()]);
	}

	private List<IPartController> getPartControllers() {
		if(partControllers == null) {
			partControllers = new ArrayList<IPartController>();
		}
		return partControllers;
	}

	private List<IDeviceController> getDeviceControllers() {
		if(deviceControllers == null) {
			deviceControllers = new ArrayList<IDeviceController>();
		}
		return deviceControllers;
	}

	@Override
	protected void fireChildrenStateChanged() {
		ComponentStatus potentialStatus = ComponentStatus.OK;
		for(IPartController partController : getPartControllers()) {
			if(partController.getStatus().isMoreImportantThan(potentialStatus)) {
				potentialStatus = partController.getStatus();
			}
		}
		for(IDeviceController deviceControllers : getDeviceControllers()) {
			if(deviceControllers.getStatus().isMoreImportantThan(potentialStatus)) {
				potentialStatus = deviceControllers.getStatus();
			}
		}
		setStatus(potentialStatus);
	}

}
