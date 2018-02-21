package org.gumtree.control.ui.widgets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;

public class EnvironmentStatusWidget extends ControllerStatusWidget {

	private boolean isInitialised = false;
	
	public EnvironmentStatusWidget(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void handleSicsConnect() {
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
				
				for (ISicsController item : itemList) {
					String id = item.getId();
					String label = id;
					if (label.contains("SP")) {
						label = id.replace("SP", " SetPoint");
					} else if (label.contains("S")) {
						label = id.replace("S", " Sensor");
					}
					try {
						addDevice(item.getPath(), label, null, null);
					} catch (Exception e) {
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
