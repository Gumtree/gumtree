package au.gov.ansto.bragg.quokka.msw.util;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.SicsCore;

import au.gov.ansto.bragg.nbi.core.NBISystemProperties;


public class SampleStage {
	// finals
	private static final String ID_DEVICE_SAMX = "samx";
	private static final String ID_DEVICE_SAMY = "samy";
	private static final String ID_DEVICE_SAMZ = "samz";
	
	// construction
	private SampleStage() {
		
	}
	
	public static boolean isAllLocked() {
		return isLocked(ID_DEVICE_SAMX) && isLocked(ID_DEVICE_SAMY) 
				&& isLocked(ID_DEVICE_SAMZ);
	}
	
	public static boolean isXYLocked() {
		return isLocked(ID_DEVICE_SAMX) && isLocked(ID_DEVICE_SAMY);
	}
	
	public static void lockXY() {
		lockMotor(ID_DEVICE_SAMX);
		lockMotor(ID_DEVICE_SAMY);		
	}
	
	// methods
	public static boolean isLocked(String motorName) {
		if (NBISystemProperties.USE_NEW_PROXY) {
			try {
				ISicsController motor = SicsManager.getSicsModel().findControllerById(
						motorName);

				if (motor instanceof IDynamicController) {
					ISicsController motorFixed = motor.getChild("fixed");
					Object value = ((IDynamicController) motorFixed).getValue();
					if ((float) value > 0) {
						return true;
					} else {
						return false;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				IComponentController motor = SicsCore
						.getSicsController()
						.findDeviceController(motorName);

				if (motor instanceof org.gumtree.gumnix.sics.control.controllers.IDynamicController) {
					IComponentController motorFixed = motor.getChildController("fixed");
					Object value = ((org.gumtree.gumnix.sics.control.controllers.IDynamicController) motorFixed).getValue();
					if ((float) value > 0) {
						return true;
					} else {
						return false;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static void lockAllMotors() {
		lockMotor(ID_DEVICE_SAMX);
		lockMotor(ID_DEVICE_SAMY);
		lockMotor(ID_DEVICE_SAMZ);
	}

	public static void unlockAllMotors() {
		unlockMotor(ID_DEVICE_SAMX);
		unlockMotor(ID_DEVICE_SAMY);
		unlockMotor(ID_DEVICE_SAMZ);
	}

	private static void lockMotor(String motorName) {
		setMotorProperty(motorName, "fixed", 1f);
	}
	
	private static void unlockMotor(String motorName) {
		setMotorProperty(motorName, "fixed", -1f);
	}
	
	private static void setMotorProperty(String motorName, String property, float value) {
		try {
			ISicsController motor = SicsManager.getSicsModel().findControllerById(
					motorName);
			
			if (motor instanceof IDynamicController) {
				IDynamicController motorFixed = (IDynamicController) motor.getChild(property);
				motorFixed.setTargetValue(value);
				motorFixed.commitTargetValue();
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
