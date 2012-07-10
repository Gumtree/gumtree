package org.gumtree.gumnix.sics.internal.ui.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;

import ch.psi.sics.hipadaba.Property;

public class ComponentPropertySource implements IPropertySource {

	private static final String PROP_NAME_PATH = "path";
	
	private IComponentController controller;

	protected ComponentPropertySource(IComponentController controller) {
		this.controller = controller;
	}

	private IComponentController getController() {
		return controller;
	}

	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
		descriptors.add(new PropertyDescriptor("id", "ID"));
		if(getController().getComponent().getDataType() != null) {
			descriptors.add(new PropertyDescriptor("datatype", "Data Type"));
		}
		for(Property property : (List<Property>)getController().getComponent().getProperty()) {
			descriptors.add(new PropertyDescriptor(property, property.getId()));
		}
		// Extra properties (derived from hipadaba)
		descriptors.add(new PropertyDescriptor(PROP_NAME_PATH, "path"));
		return descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (id instanceof Property) {
			Property property = (Property)id;
			StringBuilder stringBuilder = new StringBuilder();
			List values = property.getValue();
			for(int i = 0; i < values.size(); i++) {
				stringBuilder.append(property.getValue().get(i));
				if(i != (values.size() - 1)) {
					stringBuilder.append(", ");
				}
			}
			return stringBuilder.toString();
		} else if (id.equals("id")) {
			return getController().getComponent().getId();
		} else if (id.equals("datatype")) {
			return getController().getComponent().getDataType().getLiteral();
		} else if (id.equals(PROP_NAME_PATH)) {
			return getController().getPath();
		}
		return "";
	}

	public boolean isPropertySet(Object id) {
		return false;
	}

	public void resetPropertyValue(Object id) {
	}

	public void setPropertyValue(Object id, Object value) {
	}

}
