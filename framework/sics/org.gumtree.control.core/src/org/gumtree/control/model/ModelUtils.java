package org.gumtree.control.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.sdo.EDataGraph;
import org.eclipse.emf.ecore.sdo.SDOFactory;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;
import org.gumtree.control.batch.tasks.PropertySelectionCriterion;
import org.gumtree.control.core.IDriveableController;
import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.core.SicsRole;
import org.gumtree.control.imp.CommandController;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.imp.GroupController;
import org.gumtree.control.imp.SicsConnectionContext;
import org.gumtree.control.imp.SicsController;
import org.gumtree.control.model.PropertyConstants.ComponentType;
import org.gumtree.control.model.PropertyConstants.Privilege;
import org.gumtree.control.model.PropertyConstants.PropertyType;
import org.gumtree.security.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.DocumentRoot;
import ch.psi.sics.hipadaba.Property;
import ch.psi.sics.hipadaba.SICS;
import ch.psi.sics.hipadaba.impl.HipadabaPackageImpl;
import ch.psi.sics.hipadaba.util.HipadabaResourceFactoryImpl;
import commonj.sdo.DataObject;

public class ModelUtils {

	private static final Logger logger = LoggerFactory
			.getLogger(ModelUtils.class);
	
	private static final String PREFIX_SAMPLE = "sample";

	private static final String PROP_NXALIAS = "nxalias";

	private static final String PROP_NICK_VAR = "nick_var";
	
	private static List<IDriveableController> drivableCache;

	private static List<String> drivableIdCache;

	
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
	
	public static ISicsController createComponentController(ISicsProxy sicsProxy, Component component) {
		ISicsController controller = (ISicsController) getDefaultAdapter(sicsProxy, component, ISicsController.class);
//		System.out.println(controller.getId() + ":" + controller.getClass().getName());
		return controller;
	}
	
	protected static Object getDefaultAdapter(ISicsProxy sicsProxy, Object adaptableObject, Class adapterType) {
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
					return new CommandController(component, sicsProxy);
				} else if (type.equals(ComponentType.DRIVABLE)) {
					return new DriveableController(component, sicsProxy);
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
				return new DynamicController(component, sicsProxy);
				// Testing
//				return new DynamicController2(component);
			} else if (dataType != null && component.getComponent().size() > 0) {
				return new GroupController(component, sicsProxy);
			}

			return new SicsController(component, sicsProxy);

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
	
	public static Privilege getPrivilege(Component component) {
		return Privilege.getPrivilege(getPropertyFirstValue(component,
				PropertyType.PRIVILEGE));
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

	public static List<String> getSicsDrivableIdList(ISicsProxy sicsProxy) {
		if (drivableIdCache == null) {
			// SICS proxy is not yet ready
			if (sicsProxy.getSicsModel() == null) {
				return new ArrayList<String>(2);
			}
			List<String> buffer = new ArrayList<String>();
			for (IDriveableController controller : getSicsDrivables(sicsProxy)) {
				if (controller.getDeviceId() != null) {
					buffer.add(controller.getDeviceId());	
				}
			}
			Collections.sort(buffer);
			drivableIdCache = Collections.unmodifiableList(buffer);
		}
		return drivableIdCache;
	}

	public static String[] getSicsDrivableIds(ISicsProxy sicsProxy) {
		return getSicsDrivableIdList(sicsProxy).toArray(
				new String[drivableIdCache.size()]);
	}

	// Note: this is not thread safe
	private static IDriveableController[] getSicsDrivables(ISicsProxy sicsProxy) {
		if (drivableCache == null) {
			// SICS proxy is not yet ready
			if (sicsProxy.getSicsModel() == null) {
				return new IDriveableController[0];
			}
			drivableCache = new ArrayList<IDriveableController>();
			for (ISicsController childController : sicsProxy
					.getSicsModel().getSicsControllers()) {
				findDrivableControllers(childController, drivableCache);
			}
		}
		return drivableCache.toArray(new IDriveableController[drivableCache
		                                                      .size()]);
	}

	private static void findDrivableControllers(
			ISicsController controller, List<IDriveableController> buffer) {
		if (controller instanceof IDriveableController) {
			buffer.add((IDriveableController) controller);
		}
		for (ISicsController childController : controller
				.getChildren()) {
			findDrivableControllers(childController, buffer);
		}
	}
	
	public static ISicsConnectionContext createConnectionContext() {
		ISicsConnectionContext connectionContext = new SicsConnectionContext();
		// Set host
		connectionContext.setServerAddress(SicsCoreProperties.SERVER_HOST.getValue() 
				+ ":" + SicsCoreProperties.SERVER_PORT.getValue());
		// Set port
		connectionContext.setPublisherAddress(SicsCoreProperties.SERVER_HOST.getValue() 
				+ ":" + SicsCoreProperties.PUBLISHER_PORT.getValue());
		// Set role
		connectionContext.setRole(SicsRole.getRole(SicsCoreProperties.ROLE
				.getValue()));
		// Set password
		if (SicsCoreProperties.PASSWORD_ENCRYPTED.getBoolean()) {
			try {
				connectionContext.setPassword(EncryptionUtils
						.decryptBase64(SicsCoreProperties.PASSWORD.getValue()));
			} catch (Exception e) {
				logger.error("Cannot decrypt SICS password from system properties.");
			}
		} else {
			connectionContext.setPassword(SicsCoreProperties.PASSWORD
					.getValue());
		}
		return connectionContext;
	}
	
	public static Component[] findComponentsFromProperties(SICS sicsModel,
			List<PropertySelectionCriterion> selectionCriteria) {
		List<Component> result = new ArrayList<Component>();
		for (Component childComponent : sicsModel.getComponent()) {
			// Find components that matches with the provided property map
			Component[] partialResult = findComponentsFromProperties(
					childComponent, selectionCriteria);
			// Append result
			result.addAll(Arrays.asList(partialResult));
		}
		// Returns result in array form
		return result.toArray(new Component[result.size()]);
	}

	public static Component[] findComponentsFromProperties(Component component,
			List<PropertySelectionCriterion> selectionCriteria) {
		List<Component> buffer = new ArrayList<Component>();
		findComponentsFromProperties(component, selectionCriteria, buffer);
		return buffer.toArray(new Component[buffer.size()]);
	}

	private static void findComponentsFromProperties(Component component,
			List<PropertySelectionCriterion> selectionCriteria,
			List<Component> buffer) {
		// Assume properties are available
		boolean found = true;
		// Loop through provided selection conditions
		for (PropertySelectionCriterion criterion : selectionCriteria) {
			// We only interest in the first value for matching
			String property = getPropertyFirstValue(component,
					criterion.getPropertyId());
			if (criterion.getSelectionType() == PropertySelectionType.EQUALS) {
				if (property == null
						|| !property.equals(criterion.getPropertyValue())) {
					// We hit the counterexample
					found = false;
					break;
				}
			} else if (criterion.getSelectionType() == PropertySelectionType.NOT_EQUAL) {
				if (property == null
						|| property.equals(criterion.getPropertyValue())) {
					found = false;
					break;
				}
			} else if (criterion.getSelectionType() == PropertySelectionType.CONTAINS) {
				if (property == null
						|| !property.contains(criterion.getPropertyValue())) {
					found = false;
					break;
				}
			} else if (criterion.getSelectionType() == PropertySelectionType.NOT_CONTAIN) {
				if (property == null
						|| property.contains(criterion.getPropertyValue())) {
					found = false;
					break;
				}
			} else if (criterion.getSelectionType() == PropertySelectionType.STARTS_WITH) {
				if (property == null
						|| !property.startsWith(criterion.getPropertyValue())) {
					found = false;
					break;
				}
			} else if (criterion.getSelectionType() == PropertySelectionType.ENDS_WITH) {
				if (property == null
						|| !property.endsWith(criterion.getPropertyValue())) {
					found = false;
					break;
				}
			} else if (criterion.getSelectionType() == PropertySelectionType.EXISTS) {
				if (property == null) {
					found = false;
					break;
				}
			} else if (criterion.getSelectionType() == PropertySelectionType.NOT_EXIST) {
				if (property != null) {
					found = false;
					break;
				}
			}
		}
		// Add component to buffer if selection test is passed
		if (found) {
			buffer.add(component);
		}
		// Transverse to its child
		for (Component child : (List<Component>) component.getComponent()) {
			findComponentsFromProperties(child, selectionCriteria, buffer);
		}
	}
	
	public static ISicsController getNicknameController(ISicsController seItem) {
		List<String> prop = seItem.getPropertyValue(PROP_NXALIAS);
		List<String> varProp = seItem.getPropertyValue(PROP_NICK_VAR);
		if (prop.size() > 0) {
			String alias = prop.get(0).trim();
			String[] path = alias.split("_");
			String nickPath = null;
			if (!PREFIX_SAMPLE.equals(path[0])) {
				nickPath = "/" + PREFIX_SAMPLE;
			}
			for (int i = 0; i < path.length - 1; i++) {
				nickPath += "/" + path[i];
			}
			if (varProp.size() > 0) {
				nickPath += "/" + varProp.get(0).trim();
			} else {
				nickPath += "/nick";
			}
			return SicsManager.getSicsModel().findControllerByPath(nickPath);
		}
		return null;
	}

}
