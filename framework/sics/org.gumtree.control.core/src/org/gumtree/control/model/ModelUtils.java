package org.gumtree.control.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.sdo.EDataGraph;
import org.eclipse.emf.ecore.sdo.SDOFactory;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.imp.CommandController;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.imp.GroupController;
import org.gumtree.control.imp.SicsController;
import org.gumtree.control.model.PropertyConstants.ComponentType;
import org.gumtree.control.model.PropertyConstants.PropertyType;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.DocumentRoot;
import ch.psi.sics.hipadaba.Property;
import ch.psi.sics.hipadaba.SICS;
import ch.psi.sics.hipadaba.impl.HipadabaFactoryImpl;
import ch.psi.sics.hipadaba.impl.HipadabaPackageImpl;
import ch.psi.sics.hipadaba.util.HipadabaResourceFactoryImpl;
import commonj.sdo.DataObject;

public class ModelUtils {

	
	public static SICS deserialiseSICSModel(byte[] data) throws IOException {
		return loadSICSModel(new ByteArrayInputStream(data));
	}

	public static SICS loadSICSModel(java.net.URI fileURI) throws IOException {
		return loadSICSModel(new FileInputStream(new File(fileURI)));
	}

	public static SICS loadSICSModel(String filename) throws IOException {
		return loadSICSModel(new FileInputStream(filename));
	}

	public static SICS loadSICSModel(InputStream inputStream)
			throws IOException {
		// Similar to SDOUtil.loadDataGraph(InputStream, Map), but it needs
		// to register ProtocolPackage for XML deserialisation
		// see: http://www.devx.com/Java/Article/29093/1954?pf=true
		ResourceSet resourceSet = SDOUtil.createResourceSet();
		resourceSet.getPackageRegistry().put(HipadabaPackageImpl.eNS_URI,
				HipadabaPackageImpl.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap().put("hipadaba", 
				new HipadabaResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI
				.createURI("all.hipadaba"), "hipadaba");
		resource.load(inputStream, null);
		Object content = resource.getContents().get(0);
		SICS sicsModel = null;
		if (content instanceof DocumentRoot) {
			DocumentRoot documentRoot = (DocumentRoot) content;
			sicsModel = documentRoot.getSICS();
		} else if (content instanceof SICS) {
			// It case of no OSGi execution, content can be SICSImpl, not
			// DocumentRoot.....very funny.
			sicsModel = (SICS) content;
		} else {
			throw new IOException("Badly formatted XML");
		}
		EDataGraph dataGraph = SDOFactory.eINSTANCE.createEDataGraph();
		dataGraph.setERootObject((EObject) sicsModel);
		EObject root = dataGraph.getERootObject();
		return (SICS) root;
	}
	
	public static ISicsController createComponentController(Component component) {
		ISicsController controller = (ISicsController) getDefaultAdapter(component, ISicsController.class);
//		System.out.println(controller.getId() + ":" + controller.getClass().getName());
		return controller;
	}
	
	protected static Object getDefaultAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof Component && adapterType.equals(ISicsController.class)) {
			Component component = (Component)adaptableObject;
//			IComponentController controller = null;
			ComponentType type = getComponentType(component);
			if (type != null) {
				if (type.equals(ComponentType.COMMAND)) {
					// TODO: use scan type instead of checking id
//					if (component.getId().contains("scan") && ScanController.hasValidChildComponents(component)) {
//						return new ScanController(component);
//					} else {
//						return new CommandController(component);
//					}
					return new CommandController(component);
				} else if (type.equals(ComponentType.DRIVABLE)) {
					return new DriveableController(component);
				} 
			}

			DataType dataType = component.getDataType();
//			System.out.println(component.getId() + ":" + type + ":" + dataType);
			if(dataType != null && !dataType.equals(DataType.NONE_LITERAL)) {
//				Component parent = getComponentParent(component);
				// TODO: use type instead of parent id
//				if(parent != null && parent.getId().equals("plc")) {
//					return new PLCStatusController(component);
//				}
				return new DynamicController(component);
				// Testing
//				return new DynamicController2(component);
			} else if (dataType != null && component.getComponent().size() > 0) {
				return new GroupController(component);
			}

			return new SicsController(component);

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
	
	public static Component getComponentParent(Component component) {
		Assert.isNotNull(component);
		DataObject dataObject = ((DataObject) component).getContainer();
		if (dataObject instanceof Component) {
			return (Component) dataObject;
		}
		return null;
	}
	
	public static ComponentType getComponentType(Component component) {
		return ComponentType.getType(getPropertyFirstValue(component,
				PropertyType.TYPE));
	}
	
	public static String getPropertyFirstValue(Component component, String key) {
		Property property = getProperty(component, key);
		if (property != null) {
			if (property.getValue().size() > 0) {
				return (String) property.getValue().get(0);
			} else {
				return null;
			}
		}
		return null;
	}

	public static String getPropertyFirstValue(Component component,
			PropertyType propertyType) {
		return getPropertyFirstValue(component, propertyType.getId());
	}

	public static Property getProperty(Component component, String key) {
		Assert.isNotNull(component);
		Assert.isNotNull(key);
		for (Property property : (List<Property>) component.getProperty()) {
			if (property.getId().equals(key)) {
				return property;
			}
		}
		return null;
	}
	
	public static Component findComponentFromSingleProperty(SICS sics,
			String propertyId, String propertyValue) {
		Assert.isNotNull(sics);
		for (Component component : (List<Component>) sics.getComponent()) {
			Component matchedComponent = findComponentFromSingleProperty(
					component, propertyId, propertyValue);
			if (matchedComponent != null) {
				return matchedComponent;
			}
		}
		return null;
	}
	
	public static Component findComponentFromSingleProperty(
			Component parentComponent, String propertyId, String propertyValue) {
		String property = getPropertyFirstValue(parentComponent, propertyId);
		if (property != null && property.equals(propertyValue)) {
			return parentComponent;
		}
		for (Component child : (List<Component>) parentComponent.getComponent()) {
			Component matchedComponent = findComponentFromSingleProperty(child,
					propertyId, propertyValue);
			if (matchedComponent != null) {
				return matchedComponent;
			}
		}
		return null;
	}
	
	public static String getPath(Object object) {
		Assert.isNotNull(object);
		if (object instanceof DataObject && !(object instanceof SICS)) {
			String id = ((DataObject) object).getString("id");
			return getPath(((DataObject) object).getContainer()) + "/" + id;
		}
		return "";
	}


}
