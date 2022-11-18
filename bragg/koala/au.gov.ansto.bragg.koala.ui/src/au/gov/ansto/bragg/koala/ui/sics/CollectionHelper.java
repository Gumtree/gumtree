package au.gov.ansto.bragg.koala.ui.sics;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.IDynamicController;
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

import au.gov.ansto.bragg.koala.ui.scan.KoalaInterruptionException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper.InstrumentPhase;

public class CollectionHelper {
	
	private static final int START_TIMEOUT = 50000;
	private static final int COLLECTION_TIMEOUT = 60000;
	private static final int CHECK_CYCLE = 50;
	private static final int READ_TIME = 240;
	private static final int ERASE_TIME = 60;
	
	private static final Logger logger = LoggerFactory.getLogger(CollectionHelper.class);
	private InstrumentPhase phase = InstrumentPhase.IDLE;
//	private int timeCost = -1;
	private int exposure;
	private boolean isBusy;
	private boolean isStarted;
	private boolean initialised;
	private IDynamicController stateController;
//	private IDynamicController gumtreeStatusController;
//	private IDynamicController gumtreeTimeController;
	private List<ICollectionListener> listeners;
	private static CollectionHelper instance;
	
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
		ERROR
	};
	
	protected CollectionHelper() {
//		controlHelper = ControlHelper.getInstance();
		listeners = new ArrayList<ICollectionListener>();
		if (ControlHelper.getProxy().isConnected()) {
			initControllers();
		}
		
		ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
			
			@Override
			public void connect() {
				if (!initialised) {
					initControllers();
				}
			}
		};
		ControlHelper.getProxy().addProxyListener(proxyListener);
	}
	
	public void initControllers() {
//		gumtreeStatusController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
//				System.getProperty(ControlHelper.GUMTREE_STATUS_PATH));
//		gumtreeTimeController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
//				System.getProperty(ControlHelper.GUMTREE_TIME_PATH));
		
		stateController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
				System.getProperty(ControlHelper.IMAGE_STATE_PATH));
		try {
			setState(stateController.getValue().toString().toUpperCase());
		} catch (SicsModelException e) {
			e.printStackTrace();
		}
		
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
		initialised = true;
	}
	
	private void setState(final String stateValue) {
		System.err.println(stateValue);
		ImageState phase = ImageState.ERROR;
		try {
			phase = ImageState.valueOf(stateValue);
		} catch (Exception e) {
		}
		if (!phase.equals(ImageState.IDLE)) {
			if (isBusy && !isStarted) {
				isStarted = true;
			}
		}
		switch (phase) {
		case EXPOSE_RUNNING:
			setCollectionPhase(InstrumentPhase.EXPOSE, exposure);
			break;
		case READ_RUNNING:
			setCollectionPhase(InstrumentPhase.READ, READ_TIME);
			break;
		case ERASE_RUNNING:
			setCollectionPhase(InstrumentPhase.ERASE, ERASE_TIME);
			break;
		case SHUTTER_CLOSE_RUNNING:
			setCollectionPhase(InstrumentPhase.SHUTTER_CLOSE, -1);
			break;
		case IDLE:
			if (isBusy) {
				isBusy = false;
			}
			setCollectionPhase(InstrumentPhase.IDLE, -1);
			break;
		case ERROR:
			this.phase = InstrumentPhase.ERROR;
			if (isBusy) {
				isBusy = false;
			}
			setCollectionPhase(InstrumentPhase.ERROR, -1);
			break;
		default:
			break;
		}
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
	
	public void collect(final int exposure) throws KoalaServerException, KoalaInterruptionException {
		if (isBusy) {
			throw new KoalaServerException("server busy with current collection");
		}
		logger.warn(String.format("start collecting for {} seconds", exposure));
		this.exposure = exposure;
		try {
			isStarted = false;
			isBusy = true;
			ControlHelper.getProxy().setServerStatus(ServerStatus.RUNNING_A_SCAN);
			ControlHelper.setValue(System.getProperty(ControlHelper.EXPOSURE_TIME_PATH), exposure);
			ControlHelper.getProxy().syncRun(String.format("hset /instrument/image/start 1"));
			int ct = 0;
			while (!isStarted && ct <= START_TIMEOUT) {
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
				handleError("timeout in starting the collection");
			}
			ct = 0;
			while (isBusy && ct <= (exposure + READ_TIME + ERASE_TIME) * 1000 + COLLECTION_TIMEOUT) {
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
						exposure + READ_TIME + ERASE_TIME + COLLECTION_TIMEOUT / 1000));
			} 
			if (InstrumentPhase.ERROR.equals(phase)) {
				handleError("error in collection");
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
				// TODO Auto-generated catch block
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
		if (abortController != null) {
			try {
				if (getPhase().equals(InstrumentPhase.EXPOSE)) {
					setCollectionPhase(InstrumentPhase.EXPOSE_ENDING, -1);
					abortController.setValue(0);
				}
			} catch (SicsException e) {
				// TODO Auto-generated catch block
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
}
