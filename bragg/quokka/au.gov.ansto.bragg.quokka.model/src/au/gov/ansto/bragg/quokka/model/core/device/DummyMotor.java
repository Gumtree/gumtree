/**
 * 
 */
package au.gov.ansto.bragg.quokka.model.core.device;

import au.gov.ansto.bragg.quokka.model.core.exception.DriveDeviceFailedException;

/**
 * @author quokka
 *
 */
public class DummyMotor extends VirtualDevice {

	protected static DummyMotor instance;
	/**
	 * 
	 */
	protected DummyMotor() {
//		TODO Auto-generated constructor stub
		super();
		setId("dummy_motor");
		setName("Dummy Motor");
		setDescription("The virtual device.\n" +
			"Default position is 0mm.");
		deviceType = DeviceType.moveable;
	}

	public static DummyMotor getInstance(){
		if (instance == null){
			instance = new DummyMotor();
		}
		return instance;
	}
//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#getDescription()
//	 */
//	@Override
//	public String getDescription() {
////		TODO Auto-generated method stub
//		return description;
//	}
//
//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(double)
//	 */
//	@Override
//	public String moveTo(List<String> positionDescription) {
////		TODO Auto-generated method stub
//		return moveDrivable(positionDescription);
//	}

	protected void changeModel(Object positionObject) throws DriveDeviceFailedException {
		// TODO Auto-generated method stub
	}	
}