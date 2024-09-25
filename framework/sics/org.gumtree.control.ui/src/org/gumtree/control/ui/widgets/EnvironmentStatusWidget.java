package org.gumtree.control.ui.widgets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.SicsControllerAdapter;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.ModelUtils;

public class EnvironmentStatusWidget extends ControllerStatusWidget {

	private boolean isInitialised = false;
	private static final String UNKNOWN_VALUE = "UNKNOWN";
	
	public EnvironmentStatusWidget(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected synchronized void handleSicsConnect() {
		if (!isInitialised) {
			checkSicsConnection();
			ISicsController controlFolder = SicsManager.getSicsModel().findControllerByPath("/control");
			if (controlFolder != null) {
				List<ISicsController> itemList = controlFolder.getChildren();
				
				Collections.sort(itemList, new Comparator<ISicsController>() {

					@Override
					public int compare(ISicsController o1,
							ISicsController o2) {
						return o1.getId().compareTo(o2.getId());
					}
				});
				
				for (final ISicsController item : itemList) {
//					String id = item.getId();
//					String label = id;
//					if (label.contains("SP")) {
//						label = id.replace("SP", " SetPoint");
//					} else if (label.contains("S")) {
//						label = id.replace("S", " Sensor");
//					}
//					try {
//						addDevice(item.getPath(), label, null, null);
//					} catch (Exception e) {
//					}
					String id = item.getId();
					String label = id;
					String nickname = null;
					final String path = item.getPath();
					List<String> nickList = item.getPropertyValue("nick");
					if (nickList != null && nickList.size() > 0) {
						nickname = nickList.get(0).trim();
					}
					String aliasName = null;
					List<String> aliasList = item.getPropertyValue("nxalias");
					if (aliasList != null && aliasList.size() > 0) {
						aliasName = aliasList.get(0).trim();
						if (aliasName.length() > 30) {
							aliasName = null;
						}
					}
					String units = null;
					List<String> unitsList = item.getPropertyValue("units");
					if (unitsList != null && unitsList.size() > 0) {
						units = unitsList.get(0).trim();
					}
					String fullname = null;
					if (nickname != null) {
						nickname = nickname.trim();
						if (UNKNOWN_VALUE.equalsIgnoreCase(nickname)) {
							nickname = "";
							fullname = label;
						}
						fullname = label + " (" + nickname + ")";
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
					
					final String labelPrefix = label;
					ISicsController nickController = ModelUtils.getNicknameController(item);
					if (nickController != null) {
						if (nickController instanceof IDynamicController) {
							((IDynamicController) nickController).addControllerListener(new SicsControllerAdapter() {

								private String prefix = labelPrefix;
								@Override
								public void updateValue(Object oldValue, Object newValue) {
									String newTitle = prefix + " (" + newValue.toString() + ")";
									setDeviceTitle(path, newTitle);
									String nick = newValue.toString().trim();
									if (UNKNOWN_VALUE.equalsIgnoreCase(nick)) {
										nick = "";
										setDeviceTitle(item.getPath(), prefix);
									} else {
										setDeviceTitle(item.getPath(), prefix + " (" + nick + ")");
									}
								}
							});
							try {
								String nick = ((IDynamicController) nickController).getValue().toString().trim();
								if (UNKNOWN_VALUE.equalsIgnoreCase(nick)) {
									nick = "";
									fullname = labelPrefix;
								} else {
									fullname = labelPrefix + " (" + nick + ")";
								}
							} catch (SicsModelException e) {
							}
						}
					}
					try {
						addDevice(item.getPath(), fullname, null, units);
					} catch (Exception e) {
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
