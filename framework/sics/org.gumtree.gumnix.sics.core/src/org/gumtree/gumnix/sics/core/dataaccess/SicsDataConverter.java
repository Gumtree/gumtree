package org.gumtree.gumnix.sics.core.dataaccess;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.service.dataaccess.IDataConverter;
import org.gumtree.service.dataaccess.InvalidResourceException;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;

import ch.psi.sics.hipadaba.Property;

public class SicsDataConverter implements IDataConverter<Object> {

	public static final String PROP_ATTRIBUTE = "attribute";
	
	private static final List<Class<?>> SUPPORTED_FORMATS = Arrays
			.asList(new Class<?>[] { String.class, Integer.class, Float.class,
					IComponentData.class, IComponentController.class, IDynamicController.class, ControllerStatus.class });
	
	@SuppressWarnings("unchecked")
	public <T> T convert(Object data, Class<T> representation,
			Map<String, Object> properties)
			throws RepresentationNotSupportedException {
		if (data instanceof IComponentController) {
		
			// IComponentController
			if (representation.equals(IComponentController.class)) {
				return (T) data;
			}
			
			// IDynamicController
			if (data instanceof IDynamicController) {
				if (representation.equals(IDynamicController.class)) {
					return (T) data;
				}
				
				IDynamicController dynamicController = (IDynamicController) data;
				if (properties!= null && properties.get(PROP_ATTRIBUTE) instanceof String) {
					// Get target
					if ("target".equals(properties.get(PROP_ATTRIBUTE))) {
						try {
						return handleComponentDataConversion(dynamicController.getTargetValue(), representation);
						} catch (SicsIOException e) {
							throw new InvalidResourceException(e);
						}
					}
				} else {
					// Get data
					try {
						return handleComponentDataConversion(dynamicController.getValue(), representation);
					} catch (SicsIOException e) {
						throw new InvalidResourceException(e);
					}
				}
			}
			
			IComponentController controller = (IComponentController) data;
			// Get properties
			if (properties!= null && properties.get(PROP_ATTRIBUTE) instanceof String) {
				String propertyId = (String) properties.get(PROP_ATTRIBUTE);
				if ("status".equals(propertyId)) {
					return handleStatusDataConversion(controller.getStatus(), representation);
				} else if ("ID".equalsIgnoreCase(propertyId)) {
					return handleStringDataConversion(controller.getId(), representation);
				} else if ("label".equalsIgnoreCase(propertyId)) {
					String propertyString = getPropertyString(controller, "sicdev");
					if (propertyString != null) {
						// Use sicsdev
						return handleStringDataConversion(propertyString, representation);
					} else {
						// Use component ID
						return handleStringDataConversion(controller.getId(), representation);
					}
				} else {
					String propertyString = getPropertyString(controller, propertyId);
					if (propertyString == null) {
						throw new InvalidResourceException(controller.getPath() + " has no property " + propertyId);
					}
					return handleStringDataConversion(propertyString, representation);
				}
			}
			
			// Other subclass
			if (representation.isAssignableFrom(data.getClass())) {
				return (T) data;
			}
		}
		throw new RepresentationNotSupportedException();
	}

	@SuppressWarnings("unchecked")
	private <T> T handleComponentDataConversion(IComponentData data, Class<T> representation) {
		try {
			if (representation.equals(IComponentData.class)) {
				return (T) data;
			} else if (representation.equals(String.class)) {
				return (T) data.getStringData();
			} else if (representation.equals(Integer.class)) {
				return (T) (Integer) data.getIntData();
			} else if (representation.equals(Float.class)) {
				return (T) (Float) data.getFloatData();
			}
		} catch (Exception e) {
			throw new RepresentationNotSupportedException(e);
		}
		throw new RepresentationNotSupportedException();
	}
	
	@SuppressWarnings("unchecked")
	private <T> T handleStringDataConversion(String data, Class<T> representation) {
		try {
			if (representation.equals(String.class)) {
				return (T) data;
			} else if (representation.equals(Integer.class)) {
				return (T) (Integer) Integer.parseInt(data);
			} else if (representation.equals(Float.class)) {
				return (T) (Float) Float.parseFloat(data);
			}
		} catch (Exception e) {
			throw new RepresentationNotSupportedException(e);
		}
		throw new RepresentationNotSupportedException();
	}
	
	@SuppressWarnings("unchecked")
	private <T> T handleStatusDataConversion(ControllerStatus data, Class<T> representation) {
		try {
			if (representation.equals(ControllerStatus.class)) {
				return (T) data;
			} else if (representation.equals(String.class)) {
				return (T) data.toString();
			}
		} catch (Exception e) {
			throw new RepresentationNotSupportedException(e);
		}
		throw new RepresentationNotSupportedException();
	}
	
	public List<Class<?>> getSupportedRepresentations() {
		return SUPPORTED_FORMATS;
	}

	private String getPropertyString(IComponentController controller, String propertyId) {
		Property hdbProperty = SicsUtils.getProperty(controller.getComponent(), propertyId);
		if (hdbProperty == null) {
			return null;
		}
		List<String> values = hdbProperty.getValue();
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < values.size(); i++) {
			stringBuilder.append(values.get(i));
			if(i < values.size() - 1) {
				stringBuilder.append(",");
			}
		}
		return stringBuilder.toString();
	}
	
}
