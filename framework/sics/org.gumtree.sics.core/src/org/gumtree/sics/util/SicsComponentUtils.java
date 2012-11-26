package org.gumtree.sics.util;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.sics.control.ISicsController;

import ch.psi.sics.hipadaba.Property;

public final class SicsComponentUtils {

	private static final String KEY_SICS_DEV = "sicsdev";

	private SicsComponentUtils() {
		super();
	}

	public static List<ISicsController> getDeviceControllers(
			ISicsController parent) {
		List<ISicsController> results = new ArrayList<ISicsController>();
		getDeviceControllers(parent, results);
		return results;
	}

	private static void getDeviceControllers(ISicsController parent,
			List<ISicsController> results) {
		for (ISicsController controller : parent.getChildren()) {
			boolean deviceFound = false;
			for (Property property : controller.getComponentModel()
					.getProperty()) {
				if (KEY_SICS_DEV.equals(property.getId())) {
					results.add(controller);
					deviceFound = true;
					break;
				}
			}
			// Search for children
			if (!deviceFound) {
				getDeviceControllers(controller, results);
			}
		}
	}

}
