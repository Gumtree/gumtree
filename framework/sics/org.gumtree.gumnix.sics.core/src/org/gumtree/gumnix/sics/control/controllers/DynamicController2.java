package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.IHipadabaListener;
import org.gumtree.gumnix.sics.control.events.DynamicControllerCallbackAdapter;
import org.gumtree.gumnix.sics.control.events.IComponentControllerListener;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerCallback;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.control.events.ValueChangeEvent;
import org.gumtree.gumnix.sics.core.ISicsPersistenceManager;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;

/**
 * This class is purely experimental for testing the supplied value from SICS.
 * 
 * @author tla
 * 
 */
public class DynamicController2 extends ComponentController implements IDynamicController, IHipadabaListener {

	private static final Logger logger = LoggerFactory.getLogger(DynamicController2.class);
	
	private String errorMessage;
	
	private IComponentData cachedCurrentValue;
	
	private IComponentData cachedTargetValue;
	
	private boolean persistData = true;
	
	private ISicsPersistenceManager persistenceManager;
	
	public DynamicController2(Component component) {
		super(component);
	}

	/***************************************************************
	 * Initialisation
	 ***************************************************************/
	
	@Override
	public void preInitialise() {
		SicsCore.getSicsManager().monitor().addListener(getPath(), this);
		persistData = SicsCoreProperties.PERSIST_HDB_DATA.getBoolean();
		persistenceManager = ServiceUtils.getService(ISicsPersistenceManager.class);
		if (getComponent().getValue() != null) {
			cachedCurrentValue = new ComponentData(getComponent().getValue().getValue(), getComponent().getDataType());
		}
	}

	@Override
	public void postInitialise() {
	}

	@Override
	public void activate() {
		setTargetValue(cachedCurrentValue);
	}

	/***************************************************************
	 * Controller properties
	 ***************************************************************/

	public DataType getDataType() {
		return getComponent().getDataType();
	}

	public boolean isEnabled() {
		return true;
	}
	
	/***************************************************************
	 * Error handling
	 ***************************************************************/
	
	public void clearError() {
		if (getStatus().equals(ControllerStatus.ERROR)) {
			errorMessage = null;
			setStatus(ControllerStatus.OK);
		} else {
			errorMessage = null;
		}
	}

	public String getErrorMessage() {
		if (errorMessage == null) {
			errorMessage = "";
		}
		return errorMessage;
	}

	/***************************************************************
	 * Values control
	 ***************************************************************/

	public boolean commitTargetValue(final IDynamicControllerCallback callback)
			throws SicsIOException {
		if (cachedTargetValue == null) {
			return false;
		}
		// Reset status after failure
		clearError();
		// Send request
		SicsCore.getDefaultProxy().send("hset " + getPath() + " " + getTargetValue().getSicsString(), new SicsCallbackAdapter() {
			public void receiveReply(ISicsReplyData data) {
				setCallbackCompleted(true);
				if(callback != null) {
					callback.handleOperationCompleted((IDynamicController)getComponentController());
				}
			}
			public void receiveError(ISicsReplyData data) {
				setCallbackCompleted(true);
				// Set error status
				errorMessage = data.getString();
				setStatus(ControllerStatus.ERROR);
				if(callback != null) {
					callback.handleOperationError((IDynamicController)getComponentController(), data.getString());
				}
			}
		});
		return true;
	}

	// Sync
	public IComponentData getTargetValue() throws SicsIOException {
		return cachedTargetValue;
	}

	public void setTargetValue(IComponentData newValue) {
		cachedTargetValue = newValue;
	}

	// Async
	public void getTargetValue(IDynamicControllerCallback callback)
			throws SicsIOException {
		if (callback != null) {
			callback.handleGetValueCallback(this, cachedTargetValue);
		}
	}

	// Sync
	public IComponentData getValue() throws SicsIOException {
		return getValue(false);
	}

	// Sync
	public IComponentData getValue(boolean update) throws SicsIOException {
		if (update | cachedCurrentValue == null) {
			// Read live value
			final IComponentData[] tempData = new IComponentData[1];
			getValue(new DynamicControllerCallbackAdapter() {
				public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
					tempData[0] = value;
				}
			}, update);
			int waitTime = 10;
			int timeOut = 5000;	// 5 sec timeout
			int count = 0;
			while(count < timeOut) {
				if(tempData[0] != null) {
					break;
				}
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					throw new SicsIOException("Time out in reading component value for " + getPath(), e);
				}
				count += waitTime;
				if(count >= timeOut) {
					throw new SicsIOException("Time out in reading component value for " + getPath());
				}
			}
			// Update cache
			cachedCurrentValue = tempData[0];
			// Also update target
			setTargetValue(cachedCurrentValue);
		}
		return new ComponentData(cachedCurrentValue);
	}

	// Async
	public void getValue(IDynamicControllerCallback callback)
			throws SicsIOException {
		getValue(callback, false);
	}

	// Async
	public void getValue(IDynamicControllerCallback callback, boolean update)
			throws SicsIOException {
		if (callback != null) {
			callback.handleGetValueCallback(this, new ComponentData(cachedCurrentValue));
		}
	}
	
	/***************************************************************
	 * Listeners
	 ***************************************************************/
	
	public void valueUpdated(String newValue) {
		// Update cache
		cachedCurrentValue = new ComponentData(newValue, getComponent().getDataType());
		// Also update target
		setTargetValue(cachedCurrentValue);
		final IComponentData data = new ComponentData(cachedCurrentValue);
		
		// Persist to database (TODO: get database from OSGi or else where for cleaner architecture)
		if (persistData) {
			persistenceManager.store(new ComponentRecord(getPath(), data));	
		}
		
		// Notify listener for value change
		getListenerManager().asyncInvokeListeners(new SafeListenerRunnable<IComponentControllerListener>() {
			public void run(IComponentControllerListener listener) throws Exception {
				if(listener instanceof IDynamicControllerListener) {
					((IDynamicControllerListener)listener).valueChanged((IDynamicController)getComponentController(), data);
				}
			}			
		});
		
		// Send to event bus
		PlatformUtils.getPlatformEventBus().postEvent(new ValueChangeEvent(this, data));
	}
	
	public long getLastValueUpdated() {
		return -1;
	}
	
}
