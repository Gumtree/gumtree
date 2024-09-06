package au.gov.ansto.bragg.koala.ui.sics;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsInterruptException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.scan.KoalaInterruptionException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper.InstrumentPhase;

public class CollectionHelper {
	
	public static final String NAME_ERASURE_TIME = "gumtree.koala.erasureTime";
	public static final String NAME_READING_TIME = "gumtree.koala.readingTime";
	public static final String NAME_EXPIRATION_TIME = "gumtree.koala.collectionExp";
	public static final String NAME_TEMP_START = "gumtree.koala.tempStart";
	public static final String NAME_TEMP_END = "gumtree.koala.tempEnd";
	public static final String NAME_TEMP_MAX = "gumtree.koala.tempMax";
	public static final String NAME_TEMP_MIN = "gumtree.koala.tempMin";
	
	public static final int COLLECTION_RETRY = 3;
	private static final int START_TIMEOUT = 20;
	private static final int WAITING_FOR_ERROR_TIMEOUT = 20;
	private static final int COLLECTION_TIMEOUT = Integer.valueOf(System.getProperty(NAME_EXPIRATION_TIME, "600"));
	private static final int TIFFSAVE_TIMEOUT = 30;
	private static final int CHECK_CYCLE = 50; // millisecond
	private static final int FAIL_RETRY = 3;
	public static final int READ_TIME = Integer.valueOf(System.getProperty(NAME_READING_TIME, "280"));
	public static final int ERASE_TIME = Integer.valueOf(System.getProperty(NAME_ERASURE_TIME, "160"));
	
	private static final Logger logger = LoggerFactory.getLogger(CollectionHelper.class);
	
	public enum ImageState {
		IDLE,
		EXPOSE_STARTING,
		EXPOSE_STARTED,
		EXPOSE_RUNNING,
		EXPOSE_END,
		READ_STARTED,
		READ_RUNNING,
		READ_END,
		ERASE_STARTED,
		ERASE_RUNNING,
		ERASE_END,
		SHUTTER_CLOSE_STARTED,
		SHUTTER_CLOSE_RUNNING,
		SHUTTER_CLOSE_END,
		CLOSE_SHUTTER_STARTED,
		CLOSE_SHUTTER_RUNNING,
		CLOSE_SHUTTER_END,
		ERROR,
		UNKNOWN
	};
	
	public enum TiffStatus{
		IDLE,
		BUSY,
		FAIL
	}
		
	private InstrumentPhase phase = InstrumentPhase.IDLE;
//	private int timeCost = -1;
	private int exposure;
	private boolean isBusy;
	private boolean isStarted;
	private boolean isAborting;
	private IDynamicController stateController;
	private IDynamicController galilStatusController;
	private IDynamicController errorController;
	private IDynamicController tiffStatusController;
	private IDynamicController tiffErrorController;
//	private IDynamicController gumtreeStatusController;
//	private IDynamicController gumtreeTimeController;
	private List<ICollectionListener> listeners;
	private static CollectionHelper instance;
	private String errorMessage;
	private TempReporter reporter;
	private ImageState imageState;
	
	protected CollectionHelper() {
//		controlHelper = ControlHelper.getInstance();
		listeners = new ArrayList<ICollectionListener>();
		if (ControlHelper.getProxy().isConnected()) {
			initControllers();
		}
		
		ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
			
			@Override
			public void modelUpdated() {
				initControllers();
			}
			
			@Override
			public void disconnect() {
				setState(InstrumentPhase.ERROR.getText());
			}
			
		};
		ControlHelper.getProxy().addProxyListener(proxyListener);
		
		reporter = new TempReporter();
		Thread tempThread = new Thread(reporter);
		tempThread.start();
	}
	
	public void initControllers() {
//		gumtreeStatusController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
//				System.getProperty(ControlHelper.GUMTREE_STATUS_PATH));
//		gumtreeTimeController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
//				System.getProperty(ControlHelper.GUMTREE_TIME_PATH));
		
		stateController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
				System.getProperty(ControlHelper.IMAGE_STATE_PATH));
		errorController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
				System.getProperty(ControlHelper.IMAGE_ERROR_PATH));
		galilStatusController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
				System.getProperty(ControlHelper.GALIL_STATUS));
		
		if (stateController != null) {
			try {
				setState(stateController.getValue().toString().toUpperCase());
			} catch (SicsModelException e) {
				ControlHelper.experimentModel.publishErrorMessage("failed to initiate image collection controller");
			}
		} else {
			setState(ImageState.UNKNOWN.name());
			ControlHelper.experimentModel.publishErrorMessage("failed to load image collection model");
		}
		tiffStatusController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
				System.getProperty(ControlHelper.TIFF_STATE_PATH));
		tiffErrorController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
				System.getProperty(ControlHelper.TIFF_ERROR_PATH));
		
		if (stateController != null)
			stateController.addControllerListener(new ISicsControllerListener() {
			
			@Override
			public void updateValue(Object oldValue, Object newValue) {
				setState(newValue.toString().toUpperCase());
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
		});
		
	}
	
	private void setState(final String stateValue) {
		imageState = ImageState.IDLE;
		try {
			imageState = ImageState.valueOf(stateValue);
		} catch (Exception e) {
			imageState = ImageState.UNKNOWN;
		}
		if (!imageState.equals(ImageState.IDLE)) {
			if (isBusy && !isStarted) {
				isStarted = true;
			}
		}
		switch (imageState) {
		case EXPOSE_RUNNING:
			setCollectionPhase(InstrumentPhase.EXPOSE, exposure);
			reporter.start();
			break;
		case READ_RUNNING:
			setCollectionPhase(InstrumentPhase.READ, READ_TIME);
			reporter.end();
			break;
		case ERASE_RUNNING:
			setCollectionPhase(InstrumentPhase.ERASE, ERASE_TIME);
			break;
		case SHUTTER_CLOSE_RUNNING:
			setCollectionPhase(InstrumentPhase.SHUTTER_CLOSE, -1);
			break;
		case IDLE:
			if (reporter.isRunning()) {
				reporter.end();
			}
			if (isBusy) {
				try {
					if (!isAborting) {
						waitForTiff(FAIL_RETRY);
					}
					setCollectionPhase(InstrumentPhase.IDLE, -1);
				} catch (KoalaServerException e) {
					this.phase = InstrumentPhase.ERROR;
					this.errorMessage = "error in waiting for TIFF creation, " + e.getMessage();
					setCollectionPhase(InstrumentPhase.ERROR, -1);
				}
				isBusy = false;
			} else {
				this.errorMessage = null;
				setCollectionPhase(InstrumentPhase.IDLE, -1);
			}
			break;
		case ERROR:
			if (reporter.isRunning()) {
				reporter.end();
			}
			this.phase = InstrumentPhase.ERROR;
			try {
				this.errorMessage = errorController.getValue().toString();
			} catch (SicsModelException e) {
				this.errorMessage = e.getMessage();
			}
			if (isBusy) {
				isBusy = false;
			}
			setCollectionPhase(InstrumentPhase.ERROR, -1);
			break;
		default:
			break;
		}
	}
	
	private void waitForTiff(int retry) throws KoalaServerException {
		int ct = 0;
		while (ct <= TIFFSAVE_TIMEOUT * 1000) {
			try {
				String tiffStatus = tiffStatusController.getValue().toString();
				if (TiffStatus.IDLE.name().equalsIgnoreCase(tiffStatus) && ControlHelper.experimentModel.isTiffLabelled()) {
					break;
				} else if (TiffStatus.FAIL.name().equalsIgnoreCase(tiffStatus)) {
					if (retry > 0) {
						ControlHelper.getProxy().asyncRun("hset /instrument/save_tiff/commands/save 1", null);
						try {
							Thread.sleep(CHECK_CYCLE);
						} catch (Exception e) {
							throw new KoalaServerException("waiting interrupted");
						}
						tiffStatusController.refreshValue();
						waitForTiff(retry - 1);
						break;
					} else {
						String errorMsg = String.format("failed to save TIFF file in %d retries: %s", 
								FAIL_RETRY,
								tiffErrorController.getValue());
						throw new KoalaServerException(errorMsg);
					}
				} else {
					String err = getErrorMessage();
					if (err != null && err.length() > 0) {
						String galilStatus = galilStatusController.getValue().toString();
						throw new KoalaServerException(err + ", galil status = " + (galilStatus.length() == 0 ? "<EMPTY>" : galilStatus));
					}
				}
			} catch (SicsModelException e) {
				throw new KoalaServerException("invalid tiff status value, " + e.getMessage(), e);
			} catch (SicsException e) {
				throw new KoalaServerException("failed to send command to SICS, " + e.getMessage(), e);
			}
			try {
				Thread.sleep(CHECK_CYCLE);
				ct += CHECK_CYCLE;
			} catch (Exception e) {
				throw new KoalaServerException("waiting interrupted");
			}
		}
		if (ct >= TIFFSAVE_TIMEOUT * 1000) {
			String errMsg = "";
			try {
				errMsg = tiffErrorController.getValue().toString();
			} catch (Exception e) {
			}
			if (errMsg.length() > 0 && !"OK".equalsIgnoreCase(errMsg)) {
				throw new KoalaServerException(String.format("timeout in saving TIFF file, %s", 
						errMsg));
			} else {
				throw new KoalaServerException("timeout in saving TIFF file");
			}
		}
		isBusy = false;		
	}
	
	public void addCollectionListener(ICollectionListener listener) {
		listeners.add(listener);
	}
	
	public void removeCollectionListener(ICollectionListener listener) {
		listeners.remove(listener);
	}
	
	private void setCollectionPhase(final InstrumentPhase phase, final int timeCost) {
		this.phase = phase;
//		this.timeCost = timeCost;
//		ControlHelper.experimentModel.setPhase(phase, timeCost);
//		try {
//			gumtreeStatusController.setValue(phase.name());
//		} catch (SicsException e) {
//			e.printStackTrace();
//		}
//		try {
//			gumtreeTimeController.setValue(String.valueOf(timeCost));
//		} catch (SicsException e) {
//			e.printStackTrace();
//		}
		for (ICollectionListener listener : listeners) {
			listener.phaseChanged(phase, timeCost);
		}
	}
	
	private void fireStartedEvent() {
		for (ICollectionListener listener : listeners) {
			listener.collectionStarted();
		}
	}

	private void fireFinishedEvent() {
		for (ICollectionListener listener : listeners) {
			listener.collectionFinished();
		}
	}

	private void handleError(String message) throws KoalaServerException {
		setCollectionPhase(InstrumentPhase.ERROR, -1);
		throw new KoalaServerException(message);
	}
	
	private String getErrorMessage() {
		String errMsg = null;
		try {
			errMsg = errorController.getValue().toString();
		} catch (SicsModelException e) {
		}
		if (errMsg != null && errMsg.trim().length() > 0 && !"OK".equalsIgnoreCase(errMsg)) {
			return errMsg;
		} else {
			return "";
		}
	}
	
	public void collect(final int exposure, final int retry) throws KoalaServerException, KoalaInterruptionException {
		if (isBusy) {
			throw new KoalaServerException("server busy with current collection");
		}
		if (imageState != ImageState.IDLE && imageState != ImageState.ERROR) {
			logger.warn("waiting for state to become IDLE, currently it is " + imageState.name());
			int ct = 0;
			while (imageState != ImageState.IDLE && imageState != ImageState.ERROR && ct <= WAITING_FOR_ERROR_TIMEOUT * 1000) {
				try {
					Thread.sleep(CHECK_CYCLE);
					ct += CHECK_CYCLE;
				} catch (Exception e) {
					throw new KoalaServerException("waiting interrupted");
				}
			}
			logger.warn(String.format("waited for %s milliseconds", ct));
			if (imageState != ImageState.IDLE && imageState != ImageState.ERROR) {
				throw new KoalaServerException("timeout waiting for image collection service to get ready; " + getErrorMessage());
			}
		}
		logger.warn(String.format("start collecting for %d seconds", exposure));
		this.exposure = exposure;
		this.errorMessage = null;
		try {
			isStarted = false;
			isAborting = false;
			isBusy = true;
			ControlHelper.experimentModel.setTiffLabelled(false);
			ControlHelper.getProxy().setServerStatus(ServerStatus.RUNNING_A_SCAN);
			ControlHelper.setValue(System.getProperty(ControlHelper.EXPOSURE_TIME_PATH), exposure);
//			ControlHelper.getProxy().syncRun(String.format("hset /instrument/image/error_msg OK"));
			errorController.setValue("OK");
			ControlHelper.getProxy().syncRun(String.format("hset /instrument/image/start 1"));
			int ct = 0;
			while (!isStarted && ct <= START_TIMEOUT * 1000) {
				String err = getErrorMessage();
				if (err != null && err.length() > 0) {
					String galilState = galilStatusController.getValue().toString();
					throw new KoalaServerException(err + ", galil state = " + (galilState.length() == 0 ? "<EMPTY>" : galilState));
				}
				try {
					Thread.sleep(CHECK_CYCLE);
					ct += CHECK_CYCLE;
				} catch (Exception e) {
					throw new KoalaServerException("waiting interrupted");
				}
			}
			if (!isStarted) {
//				logger.error("collection failed to start after 50 seconds");
//				throw new KoalaServerException("timeout in starting the collection");
				if (retry > 0) {
					isBusy = false;
					collect(exposure, retry - 1);
					return;
				} else {
					String errMsg = getErrorMessage();
					if (errMsg.length() > 0) {
						handleError("error in starting the collection: " + errMsg);
					} else {
						handleError("timeout in starting the collection");
					}
				}
			}
			ct = 0;
			while (isBusy && ct <= (exposure + READ_TIME + ERASE_TIME + COLLECTION_TIMEOUT) * 1000) {
				try {
					Thread.sleep(CHECK_CYCLE);
					ct += CHECK_CYCLE;
				} catch (Exception e) {
					throw new KoalaServerException("waiting interrupted");
				}
			}
			if (isBusy) {
//				logger.error(String.format("collection cycle lasted for more than %d seconds", 
//						exposure + READ_TIME + ERASE_TIME + COLLECTION_TIMEOUT / 1000));
//				throw new KoalaServerException("collection timeout");
				handleError(String.format("collection cycle lasted for more than %d seconds", 
						exposure + READ_TIME + ERASE_TIME + COLLECTION_TIMEOUT) + "; " + getErrorMessage());
			} 
			if (InstrumentPhase.ERROR.equals(phase)) {
				handleError("error in collection: " + this.errorMessage != null ? this.errorMessage : "unknown");
			}
			if (ControlHelper.getProxy().isInterrupted()) {
				throw new KoalaInterruptionException("Experiment aborted.");
			}
			logger.warn("collection finished");
		} catch (SicsInterruptException e) {
			if (e instanceof SicsInterruptException) {
				throw new KoalaInterruptionException("user interrupted");
			} 
		} catch (KoalaInterruptionException e)  {
			logger.warn("collection finished with error: interrupted");
			throw e;
		} catch (KoalaServerException e) {
			logger.warn("collection finished with error: " + e.getMessage());
			throw e;
		} catch (Exception e) {
//			isStarted = false;
//			isBusy = false;
//			try {
//				ControlHelper.getProxy().syncRun("save");
//			} catch (SicsException e1) {
//				throw new KoalaServerException(e1);
//			}
			logger.warn("collection finished with error: " + e.getMessage());
			throw new KoalaServerException(e);
		} finally {
			ControlHelper.getProxy().setServerStatus(ServerStatus.EAGER_TO_EXECUTE);
			isBusy = false;
			isStarted = false;
		}
	}
	
	public void endExposure() throws KoalaServerException {
		IDynamicController expTimeController = (IDynamicController) SicsManager.getSicsModel()
				.findController(ControlHelper.EXPO_TIME_NAME);
		if (expTimeController != null) {
			try {
				expTimeController.setValue(0);
				setCollectionPhase(InstrumentPhase.EXPOSE_ENDING, -1);
			} catch (SicsException e) {
				throw new KoalaServerException("failed to end exposure: " + e.getMessage());
			}
		} else {
			throw new KoalaServerException(String.format("failed to end exposure: can't find %s node in model", 
					ControlHelper.EXPO_TIME_NAME));
		}
	}
	
	public void abort() throws KoalaServerException {
		IDynamicController abortController = (IDynamicController) SicsManager.getSicsModel()
				.findController(ControlHelper.ABORT_COLLECTION_NAME);
		IDynamicController expTimeController = (IDynamicController) SicsManager.getSicsModel()
				.findController(ControlHelper.EXPO_TIME_NAME);
		if (abortController != null) {
			try {
				if (getPhase().equals(InstrumentPhase.EXPOSE)) {
					expTimeController.setValue(0);
					setCollectionPhase(InstrumentPhase.EXPOSE_ENDING, -1);
				}
				isAborting = true;
				abortController.setValue(1);
				if (stateController != null) {
					try {
						String stateValue = stateController.getValue().toString().toUpperCase();
						ImageState state = ImageState.valueOf(stateValue);
						if (ImageState.IDLE.equals(state)) {
							if (reporter.isRunning()) {
								reporter.end();
							}
							if (isBusy) {
								setCollectionPhase(InstrumentPhase.IDLE, -1);
								isBusy = false;
							}
						}
					} catch (SicsModelException e) {
						setState(ImageState.ERROR.name());
					}
				}
			} catch (SicsException e) {
				throw new KoalaServerException("failed to abort collection: " + e.getMessage());
			}
		} else {
			throw new KoalaServerException(String.format("failed to abort collection: can't find %s node in model", 
					ControlHelper.ABORT_COLLECTION_NAME));
		}
	}
	
	public interface ICollectionListener {
		
		public void phaseChanged(final InstrumentPhase newPhase, int timeCost);
		public void collectionStarted();
		public void collectionFinished();
	}
	
	public static synchronized CollectionHelper getInstance() {
		if (instance == null) {
			instance = new CollectionHelper();
		}
		return instance;
	}
	
	public InstrumentPhase getPhase() {
		return phase;
	}

	public static int getErasureTime() {
		int t = ControlHelper.ERASURE_TIME;
		String eraTimeProp = Activator.getPreference(Activator.NAME_ERASURE_TIME);
		if (eraTimeProp != null) {
			try {
				t = Integer.valueOf(eraTimeProp);
			} catch (Exception e) {
			}
		}
		return t;
	}

	class TempReporter implements Runnable{

		static final int SLEEP_MILSEC = 250;
		static final float ERROR_VALUE = -9999f;
		
		boolean runningFlag;
//		boolean initFlag;
		boolean endFlag;
		boolean isStartSaved;
		boolean isEndSaved;
//		float startTemp;
//		float endTemp;
		float maxTemp = Float.MIN_VALUE;
		float minTemp = Float.MAX_VALUE;
		IDynamicController sensorController;
		IDynamicController startController;
		IDynamicController endController;
		IDynamicController maxController;
		IDynamicController minController;
		
		private void initController() {
			ISicsController controller = ControlHelper.getProxy().getSicsModel().findController(
					System.getProperty(ControlHelper.ENV_VALUE));
			if (controller != null) {
				sensorController = (IDynamicController) controller;
			}
			controller = ControlHelper.getProxy().getSicsModel().findController(
					System.getProperty(NAME_TEMP_START));
			if (controller != null) {
				startController = (IDynamicController) controller;
			}
			controller = ControlHelper.getProxy().getSicsModel().findController(
					System.getProperty(NAME_TEMP_END));
			if (controller != null) {
				endController = (IDynamicController) controller;
			}
			controller = ControlHelper.getProxy().getSicsModel().findController(
					System.getProperty(NAME_TEMP_MAX));
			if (controller != null) {
				maxController = (IDynamicController) controller;
			}
			controller = ControlHelper.getProxy().getSicsModel().findController(
					System.getProperty(NAME_TEMP_MIN));
			if (controller != null) {
				minController = (IDynamicController) controller;
			}
		}
		
		public TempReporter() {
			if (ControlHelper.getProxy().isConnected()) {
				initController();
			} else {
				ControlHelper.getProxy().addProxyListener(new SicsProxyListenerAdapter() {
					
					@Override
					public void modelUpdated() {
						initController();
					}
					
					@Override
					public void disconnect() {
						sensorController = null;
						startController = null;
						endController = null;
						maxController = null;
						minController = null;
					}
					
				});
			}
		}
		
		@Override
		public void run() {
			while (true) {
				if (runningFlag) {
					if (sensorController != null) {
						try {
							float value = sensorController.getControllerDataValue().getFloatData();
							if (!isStartSaved) {
								if (startController != null) {
									startController.setTargetValue(value);
									startController.asyncCommitTarget();
								}
							} 
							if (value > maxTemp) {
								maxTemp = value;
								if (maxController != null) {
									maxController.setTargetValue(value);
									maxController.asyncCommitTarget();
								}
							} 
							if (value < minTemp) {
								minTemp = value;
								if (minController != null) {
									minController.setTargetValue(value);
									minController.asyncCommitTarget();
								}
							}
							if (endFlag) {
								if (!isEndSaved) {
									if (endController != null) {
										endController.setTargetValue(value);
										endController.asyncCommitTarget();
										reset();
									}
								}
							}
						} catch (SicsException e) {
							logger.error("failed to report temperature: ", e);
						}
					} else {
						try {
							if (startController != null) {
								startController.setTargetValue(ERROR_VALUE);
								startController.asyncCommitTarget();
							}
							if (maxController != null) {
								maxController.setTargetValue(ERROR_VALUE);
								maxController.asyncCommitTarget();
							}
							if (minController != null) {
								minController.setTargetValue(ERROR_VALUE);
								minController.asyncCommitTarget();
							}
							if (endController != null) {
								endController.setTargetValue(ERROR_VALUE);
								endController.asyncCommitTarget();
								reset();
							}
						} catch (SicsException e) {
						}
						reset();
					}
				}
				try {
					Thread.sleep(SLEEP_MILSEC);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
		public void start() {
			runningFlag = true;
		}
		
		public void end() {
			endFlag = true;
		}
		
		public boolean isRunning() {
			return runningFlag;
		}
		
		private void reset() {
			runningFlag = false;
			endFlag = false;
			isStartSaved = false;
			isEndSaved = false;
			maxTemp = Float.MIN_VALUE;
			minTemp = Float.MAX_VALUE;
		}
	}
}
