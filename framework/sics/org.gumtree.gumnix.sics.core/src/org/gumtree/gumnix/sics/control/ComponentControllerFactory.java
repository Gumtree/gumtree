package org.gumtree.gumnix.sics.control;

import org.gumtree.gumnix.sics.control.controllers.CommandController;
import org.gumtree.gumnix.sics.control.controllers.DefaultController;
import org.gumtree.gumnix.sics.control.controllers.DrivableController;
import org.gumtree.gumnix.sics.control.controllers.DynamicController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.ISicsObjectController;
import org.gumtree.gumnix.sics.control.controllers.OneDDataController;
import org.gumtree.gumnix.sics.control.controllers.PLCStatusController;
import org.gumtree.gumnix.sics.control.controllers.ScanController;
import org.gumtree.gumnix.sics.control.controllers.ScriptContextController;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.core.PropertyConstants.ComponentType;
import org.gumtree.gumnix.sics.core.PropertyConstants.PropertyType;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;

// TODO: remove ExtensibleFactory
public class ComponentControllerFactory implements IComponentControllerFactory {


	public IComponentController createComponentController(Component component) {
		return (IComponentController) getDefaultAdapter(component, IComponentController.class);
	}
	
	protected Object getDefaultAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof Component && adapterType.equals(IComponentController.class)) {
			Component component = (Component)adaptableObject;
//			IComponentController controller = null;

			ComponentType type = SicsUtils.getComponentType(component);
			if (type != null) {
				if (type.equals(ComponentType.COMMAND)) {
					// TODO: use scan type instead of checking id
					if (component.getId().contains("scan") && ScanController.hasValidChildComponents(component)) {
						return new ScanController(component);
					} else {
						return new CommandController(component);
					}
				} else if (type.equals(ComponentType.GRAPH_DATA)) {
					String rank = SicsUtils.getPropertyFirstValue(component, PropertyType.RANK);
					if(rank != null && rank.equals("1")) {
						return new OneDDataController(component);
					}
				} else if (type.equals(ComponentType.DRIVABLE)) {
					return new DrivableController(component);
				} else if (type.equals(ComponentType.SCRIPT_CONTEXT_OBJECT)) {
					return new ScriptContextController(component);
				}
			}

			DataType dataType = component.getDataType();
			if(dataType != null && !dataType.equals(DataType.NONE_LITERAL)) {
				Component parent = SicsUtils.getComponentParent(component);
				// TODO: use type instead of parent id
				if(parent != null && parent.getId().equals("plc")) {
					return new PLCStatusController(component);
				}
				return new DynamicController(component);
				// Testing
//				return new DynamicController2(component);
			}

			return new DefaultController(component);

//			if(component.getDataType().equals(DataType.NONE_LITERAL)) {
//				// Assume this is an organisational controller
//				controller = new DefaultController(component);
//				if(type != null && type.equals(ComponentType.GRAPH_DATA)) {
//					String rank = SicsUtils.getPropertyFirstValue(component, PropertyType.RANK);
//					if(rank != null && rank.equals("1")) {
//						controller = new OneDDataController(component);
//					}
//				}
//			} else if (type != null && type.equals(ComponentType.COMMAND)) {
//				// Creates command controller
//				// temp: hardcode bmonscan for testing scan command
//				if(component.getId().equals("bmonscan")) {
//					controller = new ScanController(component);
//				} else {
//					controller = new CommandController(component);
//				}
//			} else if (component.getDataType() != null) {
//				// Creates dynamic (get/set) controller
//				if(ComponentType.DRIVABLE.equals(type)) {
//					controller = new DrivableController(component);
//				} else {
//					controller = new DynamicController(component);
//				}
//			} else {
//				controller = new DefaultController(component);
//			}
//			if (controller instanceof IHipadabaListener) {
//				SicsMonitor.getDefault().addListener(controller.getPath(),
//						(IHipadabaListener) controller);
//			}
//			return controller;

		}
		return null;
	}

	public ISicsObjectController[] createSicsObjectControllers() {
		return new ISicsObjectController[0];
	}
	
	public Class[] getAdapterList() {
		return new Class[] { IComponentController.class };
	}


}
