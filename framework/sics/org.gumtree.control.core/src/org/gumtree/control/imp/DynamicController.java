package org.gumtree.control.imp;

import java.util.List;

import org.gumtree.control.core.IControllerData;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsExecutionException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.ControllerData;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.Property;
import ch.psi.sics.hipadaba.Value;

public class DynamicController extends SicsController implements IDynamicController {

	private IControllerData targetValue;
	private boolean isBusy;
	
	public DynamicController(Component model) {
		super(model);
		Value value = model.getValue();
		if (value != null && value.getValue() != null) {
			targetValue = new ControllerData(value.getValue(), getModel().getDataType());
		}
	}

	@Override
	public Object getValue() throws SicsModelException {
		DataType type = getModel().getDataType();
		switch (type) {
		case FLOAT_LITERAL:
		case FLOATAR_LITERAL:
		case FLOATVARAR_LITERAL:
			return Float.parseFloat(getModel().getValue().getValue());
		case INT_LITERAL:
		case INTAR_LITERAL:
		case INTVARAR_LITERAL:
			return Integer.parseInt(getModel().getValue().getValue());
		case TEXT_LITERAL :
			return getModel().getValue().getValue();
		default:
			return getModel().getValue().getValue();
		}
	}

	@Override
	public IControllerData getControllerDataValue() throws SicsModelException {
		return new ControllerData(getModel().getValue().getValue(), getModel().getDataType());
	}
	
	@Override
	public void updateModelValue(String value) throws SicsModelException {
		Object oldValue = getValue();
		getModel().getValue().setValue(value);
		fireValueChangeEvent(oldValue, getValue());
		
	}

	@Override
	public void setTargetValue(Object value) {
		IControllerData oldValue = targetValue;
		if (value instanceof IControllerData) {
			targetValue = (IControllerData) value;
		} else {
			targetValue = new ControllerData(String.valueOf(value), getModel().getDataType());
		}
		fireTargetChangeEvent(oldValue != null ? oldValue.getStringData() : null, 
				targetValue != null ? targetValue.getStringData() : null);
	}

	@Override
	public IControllerData getTargetValue() {
		return targetValue;
	}

	@Override
	public void refreshValue() throws SicsException {
		try {
			SicsManager.getSicsProxy().send("hget " + getPath(), null);
		} finally {
			isBusy = false;
		}
	}
	
	@Override
	public boolean commitTargetValue() throws SicsException {
		isBusy = true;
		try {
			SicsManager.getSicsProxy().send("hset " + getPath() + " " 
					+ getTargetValue().getSicsString(), null);
		} finally {
			isBusy = false;
		}
		return false;
	}
	
	protected void fireValueChangeEvent(Object oldValue, Object newValue) {
		for (ISicsControllerListener listener : getListeners()) {
			listener.updateValue(oldValue, newValue);
		}
	}

	protected void fireTargetChangeEvent(Object oldValue, Object newValue) {
		for (ISicsControllerListener listener : getListeners()) {
			listener.updateTarget(oldValue, newValue);
		}
	}

	@Override
	public boolean isBusy() {
		return isBusy;
	}

	protected void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}

	@Override
	public String getUnits() {
		String units = getProperty("units");
		if (units != null) {
			return units;
		} else {
			return "";
		}
	}
	
	private String getProperty(String name) {
		List<Property> properties = getModel().getProperty();
		if (properties != null) {
			for (Property property : properties) {
				if (name.equals(property.getId())) {
					List<String> values = property.getValue();
					switch (values.size()) {
					case 0:
						return "";
					case 1:
						return values.get(0);
					default:
						return values.toArray().toString();
					}
				}
			}
		}
		return null;
	}
}
