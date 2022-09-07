package au.gov.ansto.bragg.koala.ui.sics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
import org.gumtree.control.model.PropertyConstants.ControllerState;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.scan.ExperimentModel;
import au.gov.ansto.bragg.koala.ui.scan.KoalaInterruptionException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;

public class ControlHelper {

	public static final String SAMPLE_PHI = "gumtree.koala.samplephi";
	public static final String SAMPLE_CHI = "gumtree.koala.samplechi";
	public static final String ENV_VALUE = "gumtree.koala.environmentValue";
	public static final String ENV_SETPOINT = "gumtree.koala.environmentSetpoint";
	public static final String SX_PATH = "gumtree.koala.sx";
	public static final String SY_PATH = "gumtree.koala.sy";
	public static final String STEP_PATH = "gumtree.koala.currpoint";
	public static final String FILENAME_PATH = "gumtree.koala.filename";
	public static final String PHASE_PATH = "gumtree.koala.phase";
	public static final String GUMTREE_STATUS_PATH = "gumtree.path.gumtreestatus";
	public static final String GUMTREE_TIME_PATH = "gumtree.path.gumtreetime";
	public static final String GUMTREE_SAMPLE_NAME = "gumtree.koala.samplename";
	public static final String GUMTREE_COMMENTS = "gumtree.koala.comments";
	public static final String GUMTREE_USER_NAME = "gumtree.koala.username";
	
	private final static Color BUSY_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final static Color IDLE_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	
	public static String TEMP_DEVICE_NAME;
	public static String CHI_DEVICE_NAME;
	public static String PHI_DEVICE_NAME;
	
	public static int ERASURE_TIME = 10;
	public static String proposalFolder;
	public static ExperimentModel experimentModel;
	
	static {
		TEMP_DEVICE_NAME = System.getProperty(ENV_SETPOINT);
		CHI_DEVICE_NAME = System.getProperty(SAMPLE_CHI);
		PHI_DEVICE_NAME = System.getProperty(SAMPLE_PHI);
	}
	
	public ControlHelper() {
	}

	public static void driveTemperature(float value) 
			throws KoalaServerException, KoalaInterruptionException {
		syncDrive(TEMP_DEVICE_NAME, value);
	}

	public static void driveChi(float value) 
			throws KoalaServerException, KoalaInterruptionException {
		syncDrive(CHI_DEVICE_NAME, value);
	}

	public static void drivePhi(float value) 
			throws KoalaServerException, KoalaInterruptionException {
		syncDrive(PHI_DEVICE_NAME, value);
	}

	public static void scanPhi(float start, float inc, int numSteps, int erasure, int exposure) 
			throws KoalaServerException, KoalaInterruptionException {
		float pos;
		for (int i = 0; i < numSteps; i++) {
			pos = start + inc * i;
			syncDrive(PHI_DEVICE_NAME, pos);
			
		}
	}
	
	private static ISicsProxy getProxy() {
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
		final ISicsControllerListener listener = new ControllerListener(currentControl, targetControl);
		final ISicsController controller = SicsManager.getSicsModel().findControllerByPath(path);
		if (controller != null) {
			controller.addControllerListener(listener);
		}
		targetControl.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				controller.removeControllerListener(listener);
			}
		});
		getProxy().addProxyListener(new SicsProxyListenerAdapter() {
			
			@Override
			public void connect() {
				if (controller instanceof IDynamicController) {
					try {
						Object value = ((IDynamicController) controller).getValue();
						currentControl.setText(String.valueOf(value));
					} catch (SicsModelException e) {
						e.printStackTrace();
					}
				}
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

	public static void publishGumtreeStatus(String status) throws KoalaServerException {
		try {
			getProxy().syncRun(String.format("hset %s %s", 
					System.getProperty(ControlHelper.GUMTREE_STATUS_PATH), status));
		} catch (Exception e) {
			throw new KoalaServerException(e);
		}
	}
	
	public static void interrupt() throws KoalaServerException {
//		asyncExec("INT1712 3");
		getProxy().interrupt();
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
					throw new KoalaInterruptionException(e);
				} else {
					throw new KoalaServerException(e);
				}
			}
		} else {
			throw new KoalaServerException("device not driveable: " + deviceName);
		}
	}
	
	public static void syncCollect(int exposure, int erasure) 
			throws KoalaServerException, KoalaInterruptionException {
		try {
			getProxy().syncRun(String.format("collect %d %d", exposure, erasure));
		} catch (SicsException e) {
			try {
				getProxy().syncRun("save");
			} catch (SicsException e1) {
				throw new KoalaServerException(e1);
			}
			if (e instanceof SicsInterruptException) {
				throw new KoalaInterruptionException(e);
			} else {
				throw new KoalaServerException(e);
			}
		}
	}
	
	public static String getProposalFolder() {
		if (proposalFolder == null) {
			proposalFolder = Activator.getPreference(Activator.NAME_PROP_FOLDER);
		}
		return proposalFolder;
	}
}
