package org.gumtree.control.imp;

import org.gumtree.control.core.IDriveableController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.exception.SicsException;

import ch.psi.sics.hipadaba.Component;

public class DriveableController extends DynamicController implements IDriveableController {

	public DriveableController(Component model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean drive() throws SicsException {
		return commitTargetWithDrive(null);
	}
	
	@Override
	public boolean drive(double target) throws SicsException {
		return drive(target, null);
	}

	@Override
	public boolean drive(double target, ISicsCallback callback) throws SicsException {
		setTarget(target);
		return commitTargetWithDrive(callback);
	}

	@Override
	public boolean commitTargetWithDrive(ISicsCallback callback) throws SicsException {
		setBusy(true);
		try {
			SicsManager.getSicsProxy().syncRun("drive " + getDeviceId() + " " 
					+ getTargetValue().getSicsString(), callback);
		} finally {
			setBusy(false);
		}
		return true;
	}

	@Override
	public boolean commitTargetValue() throws SicsException {
		return commitTargetWithDrive(null);
	}
	
	@Override
	public void run() throws SicsException {
		setBusy(true);
		try {
			SicsManager.getSicsProxy().syncRun("run " + getDeviceId() + " " 
					+ getTargetValue().getSicsString(), null);
		} finally {
			setBusy(false);
		}
	}
	
	@Override
	public void run(double target) throws SicsException {
		setTarget(target);
		run();
	}

	@Override
	public void setTarget(double target) {
		setTargetValue(String.valueOf(target));
	}

	private double getChildDoubleValue(String name) {
		ISicsController child = getChild(name);
		if (child instanceof IDynamicController) {
			try {
				return ((IDynamicController) child).getControllerDataValue().getFloatData();
			} catch (Exception e) {
			}
		}
		return Double.NaN;
	}
	
	@Override
	public double getSoftzero() {
		return getChildDoubleValue("softzero");
	}

	@Override
	public double getUpperlim() {
		return getChildDoubleValue("softupperlim");
	}

	@Override
	public double getLowerlim() {
		return getChildDoubleValue("softlowerlim");
	}

	@Override
	public boolean isFixed() {
		double value = getChildDoubleValue("fixed");
		if (!Double.isNaN(value)) {
			if (value > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public double getPrecision() {
		return getChildDoubleValue("precision");
	}

	
}
