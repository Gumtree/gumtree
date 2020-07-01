package org.gumtree.control.imp;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.gumtree.control.core.IControllerData;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.ControllerData;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.Property;
import ch.psi.sics.hipadaba.Value;

public class DynamicController extends SicsController implements IDynamicController {

	private static Logger logger = LoggerFactory.getLogger(DynamicController.class);
	private static final String TARGET_NODE = "target";
	private static final String SOFTZERO_NODE = "softzero";
	private IControllerData targetValue;
	private boolean isBusy;
	private TargetListener targetListener;
	private static ThreadPoolExecutor executor;
	
	static {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
	}
	
	class TargetListener implements ISicsControllerListener {
		
		@Override
		public void updateValue(Object oldValue, Object newValue) {
			if (newValue != null) {
				double value = Double.valueOf(newValue.toString());
				value = calculateTargetValue(value);
				setTargetValue(value);
			}
		}
		
		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
		}
		
		@Override
		public void updateEnabled(boolean isEnabled) {
		}
	}

	public DynamicController(Component model, ISicsProxy sicsProxy) {
		super(model, sicsProxy);
		Value value = model.getValue();
		if (value != null && value.getValue() != null) {
			targetValue = new ControllerData(value.getValue(), getModel().getDataType());
		}
		ISicsController targetChild = getChild(TARGET_NODE);
		if (targetChild != null && targetChild instanceof IDynamicController) {
			targetListener = new TargetListener();
			targetChild.addControllerListener(targetListener);
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
		if (getChildren().size() == 0) {
			setTargetValue(value);
		}
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
			getSicsProxy().syncRun("hget " + getPath(), null);
		} finally {
			isBusy = false;
		}
	}
	
	private double calculateTargetValue(double newValue) {
		ISicsController softzeroController = getChild(SOFTZERO_NODE);
		if (softzeroController != null && softzeroController instanceof IDynamicController) {
			try {
				double softzero = Double.valueOf(String.valueOf(
						((IDynamicController) softzeroController).getValue()));
				if (!Double.isNaN(softzero)) {
					return newValue - softzero;
				} 
			} catch (NumberFormatException e) {
			} catch (SicsModelException e) {
			}
		}
		return newValue;
	}
	
	@Override
	public boolean commitTargetValue() throws SicsException {
		isBusy = true;
		try {
			getSicsProxy().syncRun("hset " + getPath() + " " 
					+ getTargetValue().getSicsString(), null);
		} finally {
			isBusy = false;
		}
		return false;
	}
	
	protected void fireValueChangeEvent(Object oldValue, Object newValue) {
		for (ISicsControllerListener listener : getListeners()) {
			executor.submit(new Runnable() {
				
				@Override
				public void run() {
					try {
						listener.updateValue(oldValue, newValue);
					} catch (Exception e) {
						logger.error("failed to fire value change " + getPath(), e);
					}
				}
			});
		}
	}

	protected void fireTargetChangeEvent(Object oldValue, Object newValue) {
		for (ISicsControllerListener listener : getListeners()) {
			executor.submit(new Runnable() {
				
				@Override
				public void run() {
					try {
						listener.updateTarget(oldValue, newValue);
					} catch (Exception e) {
						logger.error("failed to fire target change", e);
					}
				}
			});
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
