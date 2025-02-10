package au.gov.ansto.bragg.koala.ui.sics;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.core.IDriveableController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsInterruptException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.service.db.RemoteTextDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.scan.ExperimentModel;
import au.gov.ansto.bragg.koala.ui.scan.KoalaInterruptionException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaModelException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;

public class ControlHelper {

	public enum InstrumentPhase {
		ERASE,
		EXPOSE,
		EXPOSE_ENDING,
		READ,
		SHUTTER_CLOSE,
		IDLE,
		ERROR;
		
		public String getText() {
			switch (this) {
			case ERASE:
				return "Erasing";
			case EXPOSE:
				return "Exposing";
			case EXPOSE_ENDING:
				return "Ending exposure";
			case READ:
				return "Reading";
			case SHUTTER_CLOSE:
				return "Closing shutter";
			case ERROR:
				return "Error:";
			case IDLE:
				return "Idle";
			default:
				return "Idle";
			}
		}
	};
	
	public static final String SAMPLE_PHI = "gumtree.koala.samplephi";
	public static final String SAMPLE_CHI = "gumtree.koala.samplechi";
	public static final String ENV_VALUE = "gumtree.koala.environmentValue";
	public static final String ENV_SETPOINT = "gumtree.koala.environmentSetpoint";
	public static final String SX_PATH = "gumtree.koala.sx";
	public static final String SY_PATH = "gumtree.koala.sy";
	public static final String SZ_PATH = "gumtree.koala.sz";
	public static final String SZ_ZERO = "gumtree.koala.szZero";
	public static final String SZ_UP_VALUE = "gumtree.koala.szUp";
	public static final String LED_PATH = "gumtree.path.koalaLed";
	public static final String STEP_PATH = "gumtree.koala.currpoint";
	public static final String STEP_TEXT_PATH = "gumtree.koala.stepText";
	public static final String DRUM_PATH = "gumtree.path.koalaDrum";
	public static final String DRUM_DOWN_VALUE = "gumtree.koala.drumDownValue";
	public static final String FILENAME_PATH = "gumtree.koala.filename";
	public static final String BM1_COUNT_PATH = "gumtree.koala.bm1_count";
	public static final String BM1_TIME_PATH = "gumtree.koala.bm1_time";
	public static final String GUMTREE_VERSION_PATH = "gumtree.sics.gumtreeVersionPath";
	public static final String GUMTREE_VERSION_PROP = "gumtree.client.version";
	
	public static final String PHASE_PATH = "gumtree.koala.phase";
	public static final String IMAGE_STATE_PATH = "gumtree.path.imageState";
	public static final String IMAGE_ERROR_PATH = "gumtree.path.imageError";
	public static final String EXPOSURE_TIME_PATH = "gumtree.path.exposuretime";
	public static final String ABORT_COLLECTION_PATH = "gumtree.path.abortCollection";
	public static final String TIFF_STATE_PATH = "gumtree.path.tiffStatus";
	public static final String TIFF_ERROR_PATH = "gumtree.path.tiffError";
	public static final String TIFF_SAVE_COMMAND = "gumtree.path.tiffCommand";
	public static final String GUMTREE_STATUS_PATH = "gumtree.path.gumtreestatus";
	public static final String GUMTREE_TIME_PATH = "gumtree.path.gumtreetime";
	public static final String GUMTREE_SAMPLE_NAME = "gumtree.koala.samplename";
	public static final String GUMTREE_COMMENTS = "gumtree.koala.comments";
	public static final String GUMTREE_USER_NAME = "gumtree.koala.username";
	public static final String GALIL_STATE = "gumtree.koala.galilState";
	public static final String GALIL_STATUS = "gumtree.koala.galilStatus";
	public static final String GALIL_STATUS_MESSAGE = "gumtree.koala.galilStatusMsg";
	
	private final static Color BUSY_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final static Color IDLE_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	private final static Logger logger = LoggerFactory.getLogger(ControlHelper.class);
	
	private static DocumentBuilder documentBuilder;
	private static RemoteTextDbService logbookService;
	
	public static String TEMP_DEVICE_NAME;
	public static String CHI_DEVICE_NAME;
	public static String PHI_DEVICE_NAME;
	public static String SX_DEVICE_NAME;
	public static String SY_DEVICE_NAME;
	public static String SZ_DEVICE_NAME;
	public static String EXPO_TIME_NAME;
	public static String ABORT_COLLECTION_NAME;
	
	public static int ERASURE_TIME = 30;
	public static int READ_TIME = 240;
	public static String proposalFolder;
	public static ExperimentModel experimentModel;
	
	static {
		TEMP_DEVICE_NAME = System.getProperty(ENV_SETPOINT);
		CHI_DEVICE_NAME = System.getProperty(SAMPLE_CHI);
		PHI_DEVICE_NAME = System.getProperty(SAMPLE_PHI);
		SX_DEVICE_NAME = System.getProperty(SX_PATH);
		SY_DEVICE_NAME = System.getProperty(SY_PATH);
		SZ_DEVICE_NAME = System.getProperty(SZ_PATH);
		EXPO_TIME_NAME = System.getProperty(EXPOSURE_TIME_PATH);
		ABORT_COLLECTION_NAME = System.getProperty(ABORT_COLLECTION_PATH);
	}
	
	public ControlHelper() {
		CollectionHelper.getInstance();
	}

	public static void driveTemperature(float value) 
			throws KoalaServerException, KoalaInterruptionException {
//		logger.warn(String.format("drive %s %f", TEMP_DEVICE_NAME, value));
//		syncDrive(TEMP_DEVICE_NAME, value);
		String pathValues = System.getProperty(ControlHelper.ENV_SETPOINT);
		String[] paths = pathValues.split(",");
		ISicsController controller = null;
		for (int i = 0; i < paths.length; i++) {
			controller = SicsManager.getSicsModel().findController(paths[i]);
			if (controller != null) {
				break;
			}
		}
		final ISicsController targetController = controller;
		
		if (targetController != null) {
			if (targetController instanceof DriveableController) {
				logger.warn(String.format("drive %s %f", targetController.getDeviceId(), value));
				try {
					((DriveableController) targetController).setTarget(value);
					((DriveableController) targetController).drive();
				} catch (SicsException e) {
					logger.error("failed to drive " + targetController.getDeviceId(), e);
					throw new KoalaServerException("falied to drive " + targetController.getDeviceId(), e);
				}
			} else {
				throw new KoalaServerException("device " + targetController.getDeviceId() + " is not driveable");
			}
		} else {
			throw new KoalaServerException("unable to find temperature controller");
		}
	}

	public static void driveChi(float value) 
			throws KoalaServerException, KoalaInterruptionException {
		logger.warn(String.format("drive %s %f", CHI_DEVICE_NAME, value));
		syncDrive(CHI_DEVICE_NAME, value);
	}

	public static void drivePhi(float value) 
			throws KoalaServerException, KoalaInterruptionException {
		logger.warn(String.format("drive %s %f", PHI_DEVICE_NAME, value));
		syncDrive(PHI_DEVICE_NAME, value);
	}

	public static void setValue(String idOrPath, Object value) throws KoalaModelException {
		ISicsController device = getProxy().getSicsModel().findController(idOrPath);
		if (device == null) {
			throw new KoalaModelException("can't find model node: " + idOrPath);
		}
		if (device instanceof IDynamicController) {
			try {
				((IDynamicController) device).setValue(value);
			} catch (SicsException e) {
				throw new KoalaModelException("failed to set value to " + idOrPath);
			}
		} else {
			throw new KoalaModelException("invalid model node: " + idOrPath);
		}
	}
	
//	public static void scanPhi(float start, float inc, int numSteps, int erasure, int exposure) 
//			throws KoalaServerException, KoalaInterruptionException {
//		float pos;
//		for (int i = 0; i < numSteps; i++) {
//			pos = start + inc * i;
//			syncDrive(PHI_DEVICE_NAME, pos);
//			
//		}
//	}
	
	public static ISicsProxy getProxy() {
		return SicsManager.getSicsProxy();
	}
	
	private static ISicsModel getModel() {
		return SicsManager.getSicsModel();
	}
	
	private static ControlHelper instance;
	
	public static synchronized ControlHelper getInstance() {
		if (instance == null) {
			instance = new ControlHelper();
		}
		return instance;
	}
	
	public void observePath(final String path, final Label currentControl, final Text targetControl) {
		if (isConnected()) {
			final ISicsControllerListener listener = new ControllerListener(currentControl, targetControl);
			final ISicsController controller = SicsManager.getSicsModel().findController(path);
			if (controller != null) {
				controller.addControllerListener(listener);
			}
			targetControl.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(final DisposeEvent e) {
					controller.removeControllerListener(listener);
				}
			});
		}
		getProxy().addProxyListener(new SicsProxyListenerAdapter() {
			
			@Override
			public void modelUpdated() {
//				if (controller instanceof IDynamicController) {
//					try {
//						Object value = ((IDynamicController) controller).getValue();
//						currentControl.setText(String.valueOf(value));
//					} catch (SicsModelException e) {
//						e.printStackTrace();
//					}
//				}
				final ISicsControllerListener listener = new ControllerListener(currentControl, targetControl);
				final ISicsController controller = SicsManager.getSicsModel().findController(path);
				if (controller != null) {
					controller.addControllerListener(listener);
				}
				targetControl.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(final DisposeEvent e) {
						controller.removeControllerListener(listener);
					}
				});
			}
			
			@Override
			public void disconnect() {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						try {
							if (currentControl != null) {
								currentControl.setText("unknown");
							}
							if (targetControl != null) {
								targetControl.setText("");
							}
						} catch (Exception e) {
						}
					}
				});
			}
			
		});
	}
	
	public void addProxyListener(ISicsProxyListener listener) {
		getProxy().addProxyListener(listener);
	}
	
	public void removeProxyListener(ISicsProxyListener listener) {
		getProxy().removeProxyListener(listener);
	}
	
	public boolean isConnected() {
		return getProxy().isConnected();
	}
	
	class ControllerListener implements ISicsControllerListener {

		private Label currentControl;
		private Text targetControl;
		private Object currentValue;
		
		public ControllerListener(Label current, Text target) {
			this.currentControl = current;
			this.targetControl = target;
		}
		
		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
			Display.getCurrent().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if (newState == ControllerState.BUSY) {
						currentControl.setForeground(BUSY_COLOR);
					} else {
						currentControl.setForeground(IDLE_COLOR);
					}
				}
			});
			
		}
		
		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			if (newValue != null && !newValue.toString().equals(currentValue)) {
				currentValue = newValue.toString();
				Display.getCurrent().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						currentControl.setText(String.valueOf(newValue));
					}
				});
			}
		}
		
		@Override
		public void updateEnabled(boolean isEnabled) {
		}
		
		@Override
		public void updateTarget(final Object oldValue, final Object newValue) {
			if (newValue != null) {
				Display.getCurrent().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						targetControl.setText(String.valueOf(newValue));
					}
				});
			}
		}
	}
	
	public static String syncExec(String command) throws KoalaServerException {
		String res;
		try {
			res = getProxy().syncRun(command);
		} catch (Exception e) {
			throw new KoalaServerException(e);
		}
		return res;
	}

	public static void asyncExec(String command) throws KoalaServerException {
		try {
			getProxy().asyncRun(command, null);
		} catch (Exception e) {
			throw new KoalaServerException(e);
		}
	}

	public static void publishGumtreeStatus(String status) {
//		try {
//			getProxy().syncRun(String.format("hset %s %s", 
//					System.getProperty(ControlHelper.GUMTREE_STATUS_PATH), status));
//		} catch (Exception e) {
//			throw new KoalaServerException(e);
//		}
		try {
			asyncExec(String.format("hset %s %s", 
						System.getProperty(ControlHelper.GUMTREE_STATUS_PATH), status));
		} catch (KoalaServerException e) {
			logger.error("failed to publish gumtree status");
		}
	}
	
	public static void publishFinishTime(long finishTime) {
//		try {
//			getProxy().syncRun(String.format("hset %s %s", 
//					System.getProperty(ControlHelper.GUMTREE_TIME_PATH), String.valueOf(finishTime)));
//		} catch (Exception e) {
//			logger.warn("failed to publish finish time estimation");
//		}
		try {
			asyncExec(String.format("hset %s %s", 
					System.getProperty(ControlHelper.GUMTREE_TIME_PATH), String.valueOf(finishTime)));
		} catch (KoalaServerException e) {
			logger.error("failed to publish finish time estimation");
		}
	}
	
	public static void interrupt() throws KoalaServerException {
//		asyncExec("INT1712 3");
		getProxy().interrupt();
		CollectionHelper.getInstance().abort();
		experimentModel.getPhysicsModel().finish();
		experimentModel.getChemistryModel().finish();
	}
	
	public static void syncDrive(String deviceName, float value) 
			throws KoalaServerException, KoalaInterruptionException {
		ISicsController device = getModel().findController(deviceName);
		if (device instanceof IDriveableController) {
			try {
				((IDriveableController) device).drive(value);
			} catch (SicsException e) {
				if (e instanceof SicsInterruptException) {
					throw new KoalaInterruptionException("driving was interrupted", e);
				} else {
					throw new KoalaServerException("failed to drive " + deviceName + ": " + e.getMessage(), e);
				}
			}
		} else {
			throw new KoalaServerException("device not driveable: " + deviceName);
		}
	}

	public static void concurrentDrive(final String deviceName, final float value) {
		Thread runThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ControlHelper.syncDrive(deviceName, value);
				} catch (Exception e1) {
					ControlHelper.experimentModel.publishErrorMessage(e1.getMessage());
				}
			}
		});
		runThread.start();
	}
	
	public static void syncMultiDrive(Map<String, Number> devices) 
			throws KoalaServerException, KoalaInterruptionException {
		try {
			getProxy().multiDrive(devices);
		} catch (SicsException e) {
			if (e instanceof SicsInterruptException) {
				throw new KoalaInterruptionException(e);
			} else {
				throw new KoalaServerException(e);
			}
		}
	}

	public static void syncCollect(int exposure) 
			throws KoalaServerException, KoalaInterruptionException {
		CollectionHelper.getInstance().collect(exposure, CollectionHelper.COLLECTION_RETRY);
	}
	
	public static void endExposure() throws KoalaServerException {
//		IDynamicController expTimeController = (IDynamicController) getModel().findController(EXPO_TIME_NAME);
//		if (expTimeController != null) {
//			try {
//				expTimeController.setValue(0);
//			} catch (SicsException e) {
//				throw new KoalaServerException("failed to end exposure: " + e.getMessage());
//			}
//		} else {
//			throw new KoalaServerException(String.format("failed to end exposure: can't find %s node in model", 
//					EXPO_TIME_NAME));
//		}
		CollectionHelper.getInstance().endExposure();
	}
	
	public static String getProposalFolder() {
		if (proposalFolder == null) {
			proposalFolder = Activator.getPreference(Activator.NAME_PROP_FOLDER);
		}
		return proposalFolder;
	}
	
	public void popupInfo(final String title, final String text, final Shell shell) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openInformation(shell, title, text);
			}
		});
	}

	public static DocumentBuilder getDocumentBuilder() {
		if (documentBuilder == null) {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			try {
				documentBuilder = builderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
			}
		}
		return documentBuilder;
	}
	
	public static RemoteTextDbService getLogbookService() {
		if (logbookService == null) {
			logbookService = RemoteTextDbService.getInstance();
		}
		return logbookService;
	}
	
//	public static String getMCError() {
//		String err = "";
//		try {
//			ISicsController device = getProxy().getSicsModel().findController(System.getProperty(GALIL_STATE));
//			if (device != null) {
//				int state = ((IDynamicController) device).getControllerDataValue().getIntData();
//				if (state < 0) {
//					err = "GALIL_STATE = " + state + "; ";
//					switch (state) {
//					case -1:
//						err += "Motion controller shows a -1 state, possibly cased by drum door status. \n" 
//								+ "Reset any Safety Trip that has occurred (on Safety Touchscreen) and "
//								+ "unlock, then relock the doors to reset the Motion Controller Sequencer.";
//						break;
//					case -2:
//						err += "A Drum Carriage Z Axis Limit Switch has been activated while in a sequence. \n"
//								+ "Contact an Instrument Scientist or Electrical Engineering Team to drive "
//								+ "the Drum Carriage Z Axis away from the limit switch and reset all errors.";
//						break;
//					case -3:
//						err += "The sequence failed to complete a step within an acceptable time (i.e. timeout error).\n" 
//								+ "Contact the Electrical Engineering Team to determine the State that the sequence "
//								+ "failed to complete (ST_ERR[0]) and to unlock, then lock the doors to reset.";
//						break;
//					case -4:
//						err += "The Safety System detected a fault (E-stop activated, vibration was too high, "
//								+ "laser safety relay fault, etc.) was activated during a sequence.\n" 
//								+ "Contact an Instrument Scientist or Electrical Engineering Team to reset all "
//								+ "errors and unlock, then lock the doors to reset.";
//						break;
//					case -5:
//						err += "The IFM vibration monitoring system detected vibration exceeding a warning level "
//								+ "during a sequence.\n" 
//								+ "Contact the Electrical Engineering Team to check the IFM vibration monitoring "
//								+ "system history data (where necessary they will rectify the imbalance) and to "
//								+ "unlock, then lock the doors to reset.";
//						break;
//					case -6:
//						err += "The Motion Controller detected that the Sample or Drum Carriage Z Axis exceeded "
//								+ "their acceptable maximum value (possibly free fell) or the Motion Controller "
//								+ "was reset or power cycled\n" 
//								+ "Contact an Instrument Scientist or Electrical Engineering Team to reset all "
//								+ "errors and unlock, then lock the doors to reset.";
//						break;
//					case -7:
//						err += "Drum Rotation Axis Servo Amplifier is in fault: please check the following: \n"
//								+ "  1. Is software disabled (the motion controller has not enabled the Servo "
//								+ "Drive and/or the DriveWare App has connected to the Servo Drive but the "
//								+ "'Software Enable' button has not been clicked – or was not clicked after "
//								+ "connecting and then closing the App)? \n"
//								+ "  2. Positive and Negative Velocity Limit? \n"
//								+ "  3. Motor Over Temperature? \n"
//								+ "  4. Feedback Sensor (encoder) error? \n"
//								+ "  5. Drive Internal Error? \n" 
//								+ "  6. Short Circuit \n"
//								+ "  7. Over Current? \n"
//								+ "  8. Under Voltage (this occurs when motion is disabled but is automatically "
//								+ "reset when motion is enabled)? \n"
//								+ "  9. Over Voltage? \n"
//								+ "  10. Drive Over Temperature?";
//						break;
//					case -8:
//						err += "Drum Rotation Axis Stepper Amplifier is in fault: please check the following: \n"
//								+ "  1. No power is applied to the Stepper Amplifier (or AC voltage is too low)? \n"
//								+ "  2. Stepper Amplifier temperature is above 55°C? \n"
//								+ "  3. Stepper Amplifier detects a short circuit in the motor or motor cable? \n"
//								+ "  4. Motor is not connected? \n"
//								+ "  5. No continuity in Interlock connector? \n"
//								+ "  6. Stepper is not enabled (motion controller has not enabled the Stepper Drive)?";
//						break;
//					default:
//						err += "Unknown issue code, please contact an instrument scientist or the electrical "
//								+ "engineering team for help.";
//						break;
//					}
//				}
//			}
//		} catch (Exception e) {
//		}
//		return err;
//	}

	public static String getGalilError() {
		String err = "";
		try {
			ISicsController device = getProxy().getSicsModel().findController(System.getProperty(GALIL_STATUS));
			ISicsController msgDevice = getProxy().getSicsModel().findController(System.getProperty(GALIL_STATUS_MESSAGE));
			if (device != null) {
				int status = ((IDynamicController) device).getControllerDataValue().getIntData();
				if (status < 0) {
					err = "GALIL_STATUS = " + status + "; ";
					switch (status) {
					case -1:
						err += "Motion was disabled (E-stop, Safety Trip, Motion Isolation Sw turned off or Doors not locked) during a sequence.\n"
								+ "\r\n"
								+ "Contact an Instrument Scientist or Electrical Engineering Team. "
								+ "Once Safety System errors are reset, unlock, then lock the doors to reset.";
						break;
					case -2:
						err += "The sequence failed to complete a step within an acceptable time (i.e. timeout error).\n" 
								+ "\r\n"
								+ "Contact the Electrical Engineering Team to determine the State that the sequence failed to complete (ST_ERR[0]). "
								+ "Once resolved, unlock, then lock the doors to reset.";
						break;
					case -3:
						err += "The Motion Controller detected that the Sample or Drum Carriage Z Axis exceeded their acceptable maximum value "
								+ "(possibly free fell) or the Motion Controller was reset or power cycled.\n"
								+ "\r\n"
								+ "Contact an Instrument Scientist or Electrical Engineering Team to reset all errors. "
								+ "Once resolved, unlock, then lock the doors to reset.";
						break;
					case -4:
						err += "The IFM vibration monitoring system detected vibration exceeding a warning level during a sequence.\n"
								+ "\r\n"
								+ "Contact the Electrical Engineering Team to check the IFM vibration monitoring system history data "
								+ "(where necessary they will rectify the imbalance). "
								+ "Once resolved, unlock, then lock the doors to reset.";
						break;
					case -11:
						err += "The Drum Rotation Axis Servo Amplifier is in fault (or hasn’t enabled when commanded to do so)\n"
								+ "\r\n"
								+ "Contact the Electrical Engineering Team to determine the status of the Servo Amplifier (via the AMC DriveWare App). "
								+ "Once resolved, unlock, then lock the doors to reset.";
						break;
					case -12:
						err += "The Drum Carriage Z Axis Stepper Amplifier is in fault (or hasn’t enabled when commanded to do so)\n" 
								+ "\r\n"
								+ "Contact the Electrical Engineering Team to determine the status of the Axis B Stepper Amplifier. "
								+ "Once resolved, to unlock, then lock the doors to reset.";
						break;
					case -13:
						err += "The Sample Carriage Z Axis Stepper Amplifier is in fault (or hasn’t enabled when commanded to do so)\n" 
								+ "\r\n" 
								+ "Contact the Electrical Engineering Team to determine the status of the Axis B Stepper Amplifier. "
								+ "Once resolved, to unlock, then lock the doors to reset.";
						break;
					case -19:
						err += "The Drum Rotation Axis has a following error.\n" 
								+ "\r\n" 
								+ "Contact the Electrical Engineering Team to determine why the Drum Rotation Axis incurred a following error. "
								+ "Once resolved, to unlock, then lock the doors to reset.";
						break;
					case -21:
						err += "A Drum Carriage Z Axis Limit Switch has been activated while in a sequence\n" 
								+ "\r\n" 
								+ "Contact an Instrument Scientist or Electrical Engineering Team to drive the Drum Carriage Z Axis "
								+ "away from the limit switch and reset all errors.";
						break;
					default:
						err += "Unknown issue code, please contact an instrument scientist or the electrical "
								+ "engineering team for help.";
						break;
					}
				} else if (status > 0) {
					String errText = null;
					switch (status) {
					case 1:
						errText = "Motion disabled. Please ensure it's safe to drive motors and use the control "
								+ "panel on the wall to enable motion control.";
						break;
					case 2:
						errText = "Axis B forward limit switch activated. Contact instrument scientist for further instructions.";
						break;
					case 4:
						errText = "Axis B Reverse limit switch activated. Contact instrument scientist for further instructions.";
						break;
					case 7:
						errText = "Motion disabled: E-stopped or Safety Trip or Isolation Switch off or Doors not locked.";
						break;
					case 8:
						errText = "Sample not within ±12mm of beam height. Use the the 'Move Sample Z to Beam' button in "
								+ "the footbar of this window to drive sample into the beam.";
						break;
					default:
						break;
					}
					if (errText != null) {
						err = "GALIL_STATUS = " + status + "\n" + errText;
						if (msgDevice != null) {
							String msg = ((IDynamicController) msgDevice).getControllerDataValue().getStringData();
							if (msg != null && msg.trim().length() > 0) {
								err += "\nGalil Status message: " + msg.trim();
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return err;
	}
	
	public static class DrumZHelper {
		
		private Button drumButton;
		
		public DrumZHelper(final Button drumButton) {
			this.drumButton = drumButton;
			drumButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					logger.warn("Drum Down button pressed.");
					Thread runThread = new Thread(new Runnable() {

						@Override
						public void run() {
							ControlHelper.concurrentDrive(System.getProperty(ControlHelper.DRUM_PATH), 
										Float.valueOf(System.getProperty(ControlHelper.DRUM_DOWN_VALUE)));
						}
					});
					runThread.start();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			if (getProxy().isConnected()) {
				initialise();
			}
			ISicsProxyListener proxyDrumZListener = new SicsProxyListenerAdapter() {

				@Override
				public void modelUpdated() {
					initialise();
				}
				
				@Override
				public void disconnect() {
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							drumButton.setEnabled(false);
						}
					});
				}
			};
			getProxy().addProxyListener(proxyDrumZListener);
		}
		
		private void initialise() {
			final ISicsController drumZController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.DRUM_PATH));	
			if (drumZController != null) {
				if (drumZController instanceof DynamicController) {
					try {
						final float value = Float.valueOf(((DynamicController) drumZController).getValue().toString());
						final float limit = Float.valueOf(System.getProperty(ControlHelper.DRUM_DOWN_VALUE));
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								if (value > limit + 1) {
									drumButton.setEnabled(true);
								} else {
									drumButton.setEnabled(false);
								}
							}
						});
					} catch (SicsModelException e) {
					}

					drumZController.addControllerListener(new DrumZControllerListener(drumButton));
				}
			}
		}
		
		class DrumZControllerListener implements ISicsControllerListener {

			private Button drumButton;
			private ControllerState state = ControllerState.IDLE;
			
			public DrumZControllerListener(final Button drumButton) {
				this.drumButton = drumButton;
			}
			
			@Override
			public void updateState(final ControllerState oldState, final ControllerState newState) {
				state = newState;
				
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (ControllerState.BUSY == newState) {
							drumButton.setEnabled(false);
						} else {
							drumButton.setEnabled(true);
						}
					}

				});

			}

			@Override
			public void updateValue(final Object oldValue, final Object newValue) {
				final float limit = Float.valueOf(System.getProperty(ControlHelper.DRUM_DOWN_VALUE));
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (Float.valueOf(String.valueOf(newValue)) > limit + 1) {
							if (state != ControllerState.BUSY) {
								drumButton.setEnabled(true);
							}
						} else {
							drumButton.setEnabled(false);
						}
					}
					
				});
			}

			@Override
			public void updateEnabled(boolean isEnabled) {
			}

			@Override
			public void updateTarget(Object oldValue, Object newValue) {
			}
			
		}

	}
	
	
	public static class SampleZHelper {
		
		public static final String MOVE_Z_UP 	= "Move Sample Stage Up ";
		public static final String MOVE_Z_BACK 	= "Move Sample Z to Beam ";
		
		private Button samzButton;
		private boolean isUp = false;
		
		public SampleZHelper(final Button samzButton) {
			this.samzButton = samzButton;
			samzButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					logger.warn("Sample Stage Up button pressed");
					Thread runThread = new Thread(new Runnable() {

						@Override
						public void run() {
							float target;
							if (isUp) {
								String value = Activator.getPreference(Activator.NAME_SZ_ALIGN);
								if (value == null || value.length() == 0) {
									value = System.getProperty(SZ_ZERO);
								}
								if (value == null || value.length() == 0) {
									target = 0;									
								} else {
									target = Float.valueOf(value);
								}
							} else {
								target = Float.valueOf(System.getProperty(ControlHelper.SZ_UP_VALUE));
							}
							ControlHelper.concurrentDrive(System.getProperty(ControlHelper.SZ_PATH), 
										target);
						}
					});
					runThread.start();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			if (getProxy().isConnected()) {
				initialise();
			}
			ISicsProxyListener proxySamZListener = new SicsProxyListenerAdapter() {

				@Override
				public void modelUpdated() {
					initialise();
				}
				
				@Override
				public void disconnect() {
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							samzButton.setEnabled(false);
						}
					});
				}
			};
			getProxy().addProxyListener(proxySamZListener);
		}
		
		private void initialise() {
			final ISicsController samZController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.SZ_PATH));	
			if (samZController != null) {
				if (samZController instanceof DynamicController) {
					try {
						final float value = Float.valueOf(((DynamicController) samZController).getValue().toString());
						final float limit = Float.valueOf(System.getProperty(ControlHelper.SZ_UP_VALUE)) / 2;
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								if (value >= limit) {
									isUp = true;
									samzButton.setImage(KoalaImage.DOWN32.getImage());
									samzButton.setText(MOVE_Z_BACK);
								} else {
									isUp = false;
									samzButton.setImage(KoalaImage.UP32.getImage());
									samzButton.setText(MOVE_Z_UP);
								}
							}
						});
					} catch (SicsModelException e) {
					}

					samZController.addControllerListener(new SampleZControllerListener(samzButton));
				}
			}
		}

		class SampleZControllerListener implements ISicsControllerListener {

			private Button samzButton;
			
			public SampleZControllerListener(final Button samzButton) {
				this.samzButton = samzButton;
			}
			
			@Override
			public void updateState(final ControllerState oldState, final ControllerState newState) {
				
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (ControllerState.BUSY == newState) {
							samzButton.setEnabled(false);
						} else {
							samzButton.setEnabled(true);
						}
					}

				});
			}

			@Override
			public void updateValue(final Object oldValue, final Object newValue) {
				final float limit = Float.valueOf(System.getProperty(ControlHelper.SZ_UP_VALUE)) / 2;
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (Float.valueOf(String.valueOf(newValue)) >= limit) {
							isUp = true;
							samzButton.setImage(KoalaImage.DOWN32.getImage());
							samzButton.setText(MOVE_Z_BACK);
						} else {
							isUp = false;
							samzButton.setImage(KoalaImage.UP32.getImage());
							samzButton.setText(MOVE_Z_UP);
						}
					}
					
				});
			}

			@Override
			public void updateEnabled(boolean isEnabled) {
			}

			@Override
			public void updateTarget(Object oldValue, Object newValue) {
			}
			
		}

	}
	
}
