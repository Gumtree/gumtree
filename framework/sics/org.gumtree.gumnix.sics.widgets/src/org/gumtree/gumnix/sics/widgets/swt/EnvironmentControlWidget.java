package org.gumtree.gumnix.sics.widgets.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IComponentControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;

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
				List<IComponentController> itemList = Arrays.asList(items);
				Collections.sort(itemList, new Comparator<IComponentController>() {

					@Override
					public int compare(IComponentController o1,
							IComponentController o2) {
						return o1.getId().compareTo(o2.getId());
					}
				});
				for (final IComponentController item : itemList) {
					String id = item.getId();
					String label = id;
					String nickname = null;
					List<String> nickList = item.getPropertyValue("nick");
					if (nickList != null && nickList.size() > 0) {
						nickname = nickList.get(0).trim();
					}
					String aliasName = null;
					List<String> aliasList = item.getPropertyValue("nxalias");
					if (aliasList != null && aliasList.size() > 0) {
						aliasName = aliasList.get(0).trim();
					}
					String units = null;
					List<String> unitsList = item.getPropertyValue("units");
					if (unitsList != null && unitsList.size() > 0) {
						units = unitsList.get(0).trim();
					}
//					String units = "";
//					if (id.startsWith("T")) {
//						units = "K";
//					} else if (id.startsWith("P")) {
//						units = "psi";
//					} else if (id.startsWith("B")) {
//						units = "B";
//					} else if (id.startsWith("V")) {
//						units = "V";
//					} else if (id.startsWith("I")) {
//						units = "A";
//					}
					String fullname = null;
					if (nickname != null) {
						nickname = nickname.trim();
						fullname = label + "(" + nickname + ")";
					} else {
						if (aliasName != null) {
							label = aliasName;
						} else {
							if (label.contains("SP")) {
								label = id.replace("SP", " SetPoint");
							} else if (label.contains("S")) {
								label = id.replace("S", " Sensor");
							}
						}
						fullname = label;
					}
					try {
						addDevice(item.getPath(), fullname, null, units);
					} catch (Exception e) {
					}
					
					final String labelPrefix = label;
					IComponentController nickController = SicsUtils.getNicknameController(item);
					if (nickController != null) {
						if (nickController instanceof IDynamicController) {
							((IDynamicController) nickController).addComponentListener(new DynamicControllerListenerAdapter() {
								
								private String prefix = labelPrefix;
								@Override
								public void valueChanged(IDynamicController controller, IComponentData newValue) {
									String newTitle = prefix + "(" + newValue.getStringData() + ")";
									setDeviceTitle(item.getPath(), newTitle);
								}
							});
						}
					}
				}
				sortDevices();
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
