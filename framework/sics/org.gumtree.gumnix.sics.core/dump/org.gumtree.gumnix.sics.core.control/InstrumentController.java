package org.gumtree.gumnix.sics.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gumtree.gumnix.sics.core.ISicsComponentAdapterManager;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.io.ISicsData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.json.JSONObject;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Instrument;
import ch.psi.sics.hipadaba.Part;

public class InstrumentController extends ComponentController implements IInstrumentController {

	private List<IPartController> partControllers;

	private List<IDeviceController> deviceControllers;

	public InstrumentController() {
		super();
	}

	@SuppressWarnings("unchecked")
	public void setComponent(Component component) {
		if(component instanceof Instrument) {
			Instrument instrument = (Instrument)component;
			super.setComponent(instrument);
			ISicsComponentAdapterManager adapterManager = ISicsManager.INSTANCE.service().getAdapterManager();
			for(Part part : (List<Part>)instrument.getPart()) {
				IPartController partController = (IPartController)adapterManager.getComponentAdapter(part, IPartController.class);
				if(partController == null) {
					partController = new PartController();
				}
				if(partController instanceof PartController) {
					((PartController)partController).setComponent(part);
					((PartController)partController).setParentController(this);
				}
				getPartControllers().add(partController);
			}
			for(Device device : (List<Device>)instrument.getDevice()) {
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
			// Set up status reset notification
			try {
				ISicsManager.INSTANCE.proxy().send("status interest", new SicsCallbackAdapter() {
					public void receiveWarning(ISicsData data) {
						String message = data.getString();
						if(message != null && message.equals("status = Eager to execute commands")) {
							List<IDeviceController> buffer = new ArrayList<IDeviceController>();
							buffer.addAll(Arrays.asList(getChildDeviceControllers()));
							for(IPartController partController : getChildPartControllers()) {
								getAllDeviceControllers(partController, buffer);
							}
							for(IDeviceController controller : buffer) {
								if(controller instanceof DeviceController) {
									((DeviceController)controller).setStatus(ComponentStatus.OK);
								}
							}
						}
					}
				});
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
		} else {
			throw new Error("Component is not an instance of Instrument.");
		}
	}

	public Instrument getInstrument() {
		return (Instrument)getComponent();
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

	private List<IDeviceController> getAllDeviceControllers(IPartController parent, List<IDeviceController> buffer) {
		buffer.addAll(Arrays.asList(parent.getChildDeviceControllers()));
		for(IPartController partController : parent.getChildPartControllers()) {
			getAllDeviceControllers(partController, buffer);
		}
		return buffer;
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
