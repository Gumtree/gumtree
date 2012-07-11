package au.gov.ansto.bragg.quokka.sics;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.DrivableController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerCallback;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;

public class JulaboLH45Controller extends DrivableController {

	private enum Status {
		IDLE, BUSY
	}
	
	private static final Logger logger = LoggerFactory.getLogger(JulaboLH45Controller.class);
	
	private static final String PATH_VALUE = "/sensor/value";
	
	private static final String PATH_TARGET = "/setpoint";
	
	private static final String PATH_STATUS = "/status";
	
	private static final String PATH_OVER_LIMIT = "/overtemp_warnlimit";
	
	private static final String PATH_SUB_LIMIT = "/subtemp_warnlimit";
	
	private static final String PATH_TOLERANCE = "/tolerance";
	
	private IDynamicController valueController;
	
	private IDynamicController targetController;
	
	private IDynamicController stateController;
	
	private IDynamicController overLimitController;
	
	private IDynamicController subLimitController;
	
	private IDynamicController toleranceController;
	
	public JulaboLH45Controller(Component component) {
		super(component);
	}

	public void activate() {
		super.activate();
		valueController = (IDynamicController) getChildController(PATH_VALUE);
		targetController = (IDynamicController) getChildController(PATH_TARGET);
		stateController = (IDynamicController) getChildController(PATH_STATUS);
		overLimitController = (IDynamicController) getChildController(PATH_OVER_LIMIT);
		subLimitController = (IDynamicController) getChildController(PATH_SUB_LIMIT);
		toleranceController = (IDynamicController) getChildController(PATH_TOLERANCE);
		
		// Manually update target value
		try {
			// TODO: targetController will not be initialise properly (default target will be missing)
			setTargetValue(targetController.getValue());
		} catch (SicsIOException e) {
			logger.warn("Failed to update Julabo target (setpoint).");
		}
		
		// Listen to target
		targetController.addComponentListener(new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				setTargetValue(newValue);
			}
		});
		
		// Listen to the state
		stateController.addComponentListener(new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				String newStatus = newValue.getStringData();
				if (newStatus.equalsIgnoreCase(Status.IDLE.name())) {
					setStatus(ControllerStatus.OK);
				} else if (newStatus.equalsIgnoreCase(Status.BUSY.name())) {
					// Required by drive() method
					setDirty(true);
					setStatus(ControllerStatus.RUNNING);
				} else {
					// Else don't update status
				}
			}
		});
	}
	
	protected IDynamicController getTargetController() {
		return targetController;
	}
	
	@Override
	public IComponentData getValue(boolean update) throws SicsIOException {
		return valueController.getValue(update);
	}
	
	@Override
	public void getValue(final IDynamicControllerCallback callback, boolean update) throws SicsIOException {
		valueController.getValue(callback, update);
	}
	
	@Override
	public boolean commitTargetValue(final IDynamicControllerCallback callback) throws SicsIOException {
		if(getTargetValue() == null) {
			return false;
		}
		// Reset status after failure
		clearError();
		getTargetController().setTargetValue(getTargetValue());
		
		// [TLA][2010-07-19] Removed set limit logic after the SICS code review
//		// Set warning limit
//		try {
//			float tolerance = toleranceController.getValue().getFloatData();
//			float setpoint = getTargetValue().getFloatData(); 
//			
//			overLimitController.setTargetValue(ComponentData.createData(setpoint + tolerance));
//			overLimitController.commitTargetValue(null);
//			subLimitController.setTargetValue(ComponentData.createData(setpoint - tolerance));
//			overLimitController.commitTargetValue(null);
//		} catch (ComponentDataFormatException e) {
//			throw new RuntimeException(e);
//		}
		
		return getTargetController().commitTargetValue(callback);
	}
	
	@Override
	protected int getStateChangeTimeout() {
		// Give 10 sec to observe state change on driving this device
		return 10000;
	}
	
}
