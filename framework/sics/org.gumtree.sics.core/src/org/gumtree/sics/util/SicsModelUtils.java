package org.gumtree.sics.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.sdo.EDataGraph;
import org.eclipse.emf.ecore.sdo.SDOFactory;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;
import org.gumtree.sics.core.PropertySelectionCriterion;
import org.gumtree.sics.core.PropertySelectionType;
import org.gumtree.sics.control.ISicsController;
import org.gumtree.sics.core.ISicsManager;
import org.gumtree.sics.core.PropertyConstants.ComponentType;
import org.gumtree.sics.core.PropertyConstants.Privilege;
import org.gumtree.sics.core.PropertyConstants.PropertyType;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DocumentRoot;
import ch.psi.sics.hipadaba.Property;
import ch.psi.sics.hipadaba.SICS;
import ch.psi.sics.hipadaba.impl.HipadabaPackageImpl;

import commonj.sdo.DataObject;

public final class SicsModelUtils {

	private static final String PROP_NXALIAS = "nxalias";
	private static final String PROP_NICK_VAR = "nick_var";

	private static final String PREFIX_SAMPLE = "sample";
	
	public static SICS deserialiseSICSModel(byte[] data) throws IOException {
		return loadSICSModel(new ByteArrayInputStream(data));
	}

	public static SICS loadSICSModel(java.net.URI fileURI) throws IOException {
		return loadSICSModel(new FileInputStream(new File(fileURI)));
	}

	public static SICS loadSICSModel(String filename) throws IOException {
		return loadSICSModel(new FileInputStream(filename));
	}

	private static SICS loadSICSModel(InputStream inputStream)
			throws IOException {
		// Similar to SDOUtil.loadDataGraph(InputStream, Map), but it needs
		// to register ProtocolPackage for XML deserialisation
		// see: http://www.devx.com/Java/Article/29093/1954?pf=true
		ResourceSet resourceSet = SDOUtil.createResourceSet();
		resourceSet.getPackageRegistry().put(HipadabaPackageImpl.eNS_URI,
				HipadabaPackageImpl.eINSTANCE);
		Resource resource = resourceSet.createResource(URI
				.createURI("all.hipadaba"));
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
	
	public static String getPath(Object object) {
		if (object instanceof DataObject && !(object instanceof SICS)) {
			String id = ((DataObject) object).getString("id");
			return getPath(((DataObject) object).getContainer()) + "/" + id;
		}
		return "";
	}

	public static Component getComponentParent(Component component) {
		DataObject dataObject = ((DataObject) component).getContainer();
		if (dataObject instanceof Component) {
			return (Component) dataObject;
		}
		return null;
	}
	
	public static Component getComponent(SICS sicsModel, String path) {
		String[] ids = path.split("/");
		StringBuilder builder = new StringBuilder("/SICS");
		if (ids.length < 2) {
			return null;
		}
		for (int i = 1; i < ids.length; i++) {
			builder.append("/component[id=");
			builder.append(ids[i]);
			builder.append("]");
		}
		try {
			return (Component) ((DataObject) sicsModel).get(builder.toString());
		} catch (Exception e) {
			return null;
		}
	}

	public static List<Property> findDeviceProperties(EObject eObject,
			List<Property> cache) {
		for (Object object : eObject.eContents()) {
			if (object instanceof Property) {
				cache.add((Property) object);
			} else if (object instanceof EObject) {
				findDeviceProperties((EObject) object, cache);
			}
		}
		return cache;
	}

	public static Property getProperty(Component component, String key) {
		for (Property property : (List<Property>) component.getProperty()) {
			if (property.getId().equals(key)) {
				return property;
			}
		}
		return null;
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

	public static ComponentType getComponentType(Component component) {
		return ComponentType.getType(getPropertyFirstValue(component,
				PropertyType.TYPE));
	}

	public static Privilege getPrivilege(Component component) {
		return Privilege.getPrivilege(getPropertyFirstValue(component,
				PropertyType.PRIVILEGE));
	}

	public static List<Component> findComponentsFromSingleProperty(SICS sics,
			String propertyId, String propertyValue) {
		List<Component> cache = new ArrayList<Component>();
		for (Component component : (List<Component>) sics.getComponent()) {
			findComponentsFromSingleProperty(component, propertyId,
					propertyValue, cache);
		}
		return cache;
	}

	public static List<Component> findComponentsFromSingleProperty(
			Component parentComponent, String propertyId, String propertyValue,
			List<Component> cache) {
		String property = getPropertyFirstValue(parentComponent, propertyId);
		if (property != null && property.equals(propertyValue)) {
			cache.add(parentComponent);
		}
		for (Component child : (List<Component>) parentComponent.getComponent()) {
			findComponentsFromSingleProperty(child, propertyId, propertyValue,
					cache);
		}
		return cache;
	}

	public static Component findComponentFromSingleProperty(SICS sics,
			String propertyId, String propertyValue) {
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

	public static Component getDescendantComponent(Component baseComponent,
			String relativePath) {
		String[] parts = relativePath.split("/");
		if (parts.length <= 1) {
			return null;
		}
		for (Component childComponent : (List<Component>) baseComponent
				.getComponent()) {
			if (childComponent.getId().equals(parts[1])) {
				relativePath = relativePath.substring(parts[1].length() + 1);
				if (relativePath.length() == 0) {
					return childComponent;
				} else {
					return getDescendantComponent(childComponent, relativePath);
				}
			}
		}
		return null;
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
	
	private SicsModelUtils() {
		super();
	}
	
	public static ISicsController getNicknameController(ISicsManager sicsManager, ISicsController seItem) {
		List<String> prop = seItem.getPropertyValue(PROP_NXALIAS);
		List<String> varProp = seItem.getPropertyValue(PROP_NICK_VAR);
		if (prop != null && prop.size() > 0) {
			String alias = prop.get(0).trim();
			String[] path = alias.split("_");
			String nickPath = null;
			if (!PREFIX_SAMPLE.equals(path[0])) {
				nickPath = "/" + PREFIX_SAMPLE;
			}
			for (int i = 0; i < path.length - 1; i++) {
				nickPath += "/" + path[i];
			}
			if (varProp != null && varProp.size() > 0) {
				nickPath += "/" + varProp.get(0).trim();
			} else {
				nickPath += "/nick";
			}
			return sicsManager.getServerController().findChild(nickPath);
		}
		return null;
	}

}
