package org.gumtree.workflow.ui.internal.util;

import org.gumtree.workflow.ui.internal.Activator;

public final class WorkflowRegistryConstants {

	public static String EXTENSION_WORKFLOWS = "workflows";

	public static String ELEMENT_WORKFLOW = "workflow";

	public static String ELEMENT_CATEGORY = "category";

	public static String ATTRIBUTE_CONFIG = "config";
	
	public static String ATTRIBUTE_CATEGORY = "category";

	public static String EXTENTION_POINT_WORKFLOWS = Activator.PLUGIN_ID + "."
			+ EXTENSION_WORKFLOWS;

	private WorkflowRegistryConstants() {
		super();
	}

}
