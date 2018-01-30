package org.gumtree.control.imp;

import org.gumtree.control.core.IControllerData;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.ControllerData;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Value;

public class DynamicController extends SicsController implements IDynamicController {

	private IControllerData targetValue;
	
	public DynamicController(Component model) {
		super(model);
		Value value = model.getValue();
		if (value != null && value.getValue() != null) {
			targetValue = new ControllerData(value.getValue(), getModel().getDataType());
		}
	}

	@Override
	public IControllerData getValue() throws SicsModelException {
		return new ControllerData(getModel().getValue().getValue(), getModel().getDataType());
	}

	@Override
	public void updateValue(String value) {
		getModel().getValue().setValue(value);
	}

	@Override
	public void setTargetValue(IControllerData value) {
		targetValue = value;
	}

	@Override
	public IControllerData getTargetValue() {
		// TODO Auto-generated method stub
		return targetValue;
	}

}
