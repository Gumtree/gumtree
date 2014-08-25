package org.gumtree.gumnix.sics.widgets.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.SicsCore;

public class EnvironmentControlWidget extends DeviceStatusWidget {

	private boolean isInitialised = false;
	
	public EnvironmentControlWidget(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void handleSicsConnect() {
		if (!isInitialised) {
			checkSicsConnection();
			IComponentController controlFolder = SicsCore.getSicsController().findComponentController("/control");
			if (controlFolder != null) {
				IComponentController[] items = controlFolder.getChildControllers();
				for (IComponentController item : items) {
					String id = item.getId();
					String label = id;
					String units = "";
					if (id.startsWith("T")) {
						units = "K";
					} else if (id.startsWith("P")) {
						units = "psi";
					} else if (id.startsWith("B")) {
						units = "B";
					} else if (id.startsWith("V")) {
						units = "V";
					} else if (id.startsWith("I")) {
						units = "A";
					}
					if (label.contains("SP")) {
						label = id.replace("SP", " SetPoint");
					} else if (label.contains("S")) {
						label = id.replace("S", " Sensor");
					}
					try {
						addDevice(item.getPath(), label, null, units);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						render();
					}
				});
				isInitialised = true;
			}
		}
		super.handleSicsConnect();
	}

}
