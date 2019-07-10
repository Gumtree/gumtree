package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.IHipadabaListener;
import org.gumtree.gumnix.sics.control.events.DynamicControllerCallbackAdapter;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IComponentControllerListener;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerCallback;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.control.events.TargetChangeEvent;
import org.gumtree.gumnix.sics.control.events.ValueChangeEvent;
import org.gumtree.gumnix.sics.core.ISicsPersistenceManager;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.gumtree.util.string.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.Value;

public class DynamicController extends ComponentController implements IDynamicController, IHipadabaListener {

	private enum ValueStatus {
		IN_SYNC, OUT_OF_SYNC, UNAVAILABLE
	}

	private static final Logger logger = LoggerFactory.getLogger(DynamicController.class);
	
	private String cachedValue;
	
	private long lastValueUpdated = -1;

	private IComponentData targetValue;

	private ValueStatus valueStatus;

	// Only the drivable component has this controller
	private IDynamicController targetController;

	// Only the drivable component has this controller
	private IDynamicController softzeroController;
	
	private String errorMessage;
	
	private boolean persistData = true;
	
	private ISicsPersistenceManager persistenceManager;
	
	public DynamicController(Component component) {
		super(component);
		SicsCore.getSicsManager().monitor().addListener(getPath(), this);
		persistData = SicsCoreProperties.PERSIST_HDB_DATA.getBoolean();
		persistenceManager = ServiceUtils.getService(ISicsPersistenceManager.class);
		Value value = component.getValue();
		if (value != null && value.getValue() != null){
			valueStatus = ValueStatus.IN_SYNC;
			cachedValue = value.getValue();
			targetValue = new ComponentData(cachedValue, component.getDataType());
		}
	}

	public void preInitialise() {
	}

	public void postInitialise() {
	}
	
	public void activate() {
		if (valueStatus == null) {
			setValueStatus(ValueStatus.OUT_OF_SYNC);
		}
		IComponentController targetControllerObject = SicsCore.getSicsController().findComponentController(this, "/target");
		if(targetControllerObject instanceof IDynamicController) {
			targetController = (IDynamicController)targetControllerObject;
		}
		IComponentController softzeroControllerObject = SicsCore.getSicsController().findComponentController(this, "/softzero");
		if(softzeroControllerObject instanceof IDynamicController) {
			softzeroController = (IDynamicController)softzeroControllerObject;
			// Update value on softzero change (not tested)
			softzeroController.addComponentListener(new DynamicControllerListenerAdapter() {
				public void valueChanged(IDynamicController controller,
						IComponentData newValue) {
					try {
						getValue(new DynamicControllerCallbackAdapter() {}, true);
					} catch (SicsIOException e) {
						e.printStackTrace();
					}
				}
			});
		}
		// Read initial value if available
		if (getComponent().getValue() != null) {
//			String value = getComponent().getValue().getValue();
//			// Cache value
//			cachedValue = value;
//			// Mark status as value in sync
//			setValueStatus(ValueStatus.IN_SYNC);
//			valueUpdated(getComponent().getValue().getValue());
		}
	}

	// Can be null
	protected IDynamicController getTargetController() {
		return targetController;
	}

	// Can be null
	protected IDynamicController getSoftzeroController() {
		return softzeroController;
	}
	
	// Get cached value
	public IComponentData getValue() throws SicsIOException {
		return getValue(false);
	}

	// Synchronous get
	public IComponentData getValue(boolean update) throws SicsIOException {
		if(valueStatus.equals(ValueStatus.IN_SYNC) && !update) {
			return new ComponentData(cachedValue, getComponent().getDataType());
		}
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
			if(tempData[0] != null && targetValue != null) {
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
		return tempData[0];
	}

	public void getValue(IDynamicControllerCallback callback) throws SicsIOException {
		getValue(callback, false);
	}

	public void getValue(final IDynamicControllerCallback callback, boolean update) throws SicsIOException {
		if(valueStatus.equals(ValueStatus.IN_SYNC) && !update) {
			if(callback != null) {
				callback.handleGetValueCallback(this, new ComponentData(cachedValue, getComponent().getDataType()));
			}
		} else {
			SicsCore.getDefaultProxy().send("hget " + getPath(), new SicsCallbackAdapter() {
				public void receiveReply(ISicsReplyData data) {
					setCallbackCompleted(true);
					JSONObject object = data.getJSONObject();
					try {
						if(object == null || object.keys() == null) {
							return;
						}
						String key = (String)object.keys().next();
						String newValue = object.getString(key).trim();
						valueUpdated(newValue);
						if(callback != null) {
							callback.handleGetValueCallback((IDynamicController)getComponentController(), new ComponentData(newValue, getComponent().getDataType()));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void setTargetValue(IComponentData newValue) {
		targetValue = newValue;
		// Send to listeners
		getListenerManager().asyncInvokeListeners(new SafeListenerRunnable<IComponentControllerListener>() {
			public void run(IComponentControllerListener listener) throws Exception {
				if(listener instanceof IDynamicControllerListener) {
					((IDynamicControllerListener)listener).targetChanged((IDynamicController)getComponentController(), targetValue);
				}
			}			
		});
		// Send to event bus
		postEvent(new TargetChangeEvent(this, targetValue));
	}

	public void valueUpdated(final String newValue) {
		// Cache value
		cachedValue = newValue;
		// Update timestamp
		lastValueUpdated = System.currentTimeMillis();
		// Mark status as value in sync
		setValueStatus(ValueStatus.IN_SYNC);
		// Create data wrapper
		final IComponentData data = new ComponentData(newValue, getComponent().getDataType());
		// Persist to database (TODO: get database from OSGi or else where for cleaner architecture)
		if (persistData) {
			persistenceManager.store(new ComponentRecord(getPath(), data));	
		}
		
		// Update target value
		if(getTargetController() == null) {
			// Use value as target
			setTargetValue(ComponentData.createStringData(newValue));
			// Notify
			getListenerManager().asyncInvokeListeners(new SafeListenerRunnable<IComponentControllerListener>() {
				public void run(IComponentControllerListener listener) throws Exception {
					if(listener instanceof IDynamicControllerListener) {
						((IDynamicControllerListener)listener).targetChanged((IDynamicController)getComponentController(), data);
					}
				}			
			});
		} else {
			try {
				getTargetController().getValue(new DynamicControllerCallbackAdapter () {
					public void handleGetValueCallback(IDynamicController controller, final IComponentData value) {
						// [GUMTREE-354] Software zero adjustment and assume it is float
						if (StringUtils.isNumber(value.getSicsString()) && getSoftzeroController() != null) {
							try {
								getSoftzeroController().getValue(new DynamicControllerCallbackAdapter () {
									public void handleGetValueCallback(IDynamicController controller, IComponentData softzero) {
										IComponentData adjustedValue;
										try {
											adjustedValue = ComponentData.createData(value.getFloatData() - softzero.getFloatData());
											setTargetValue(adjustedValue);
										} catch (ComponentDataFormatException e) {
											e.printStackTrace();
											// No adjustment 
											setTargetValue(value);
										}
									}
								});
							} catch (SicsIOException e) {
								e.printStackTrace();
								// No adjustment
								setTargetValue(value);
							}
						} else {
							setTargetValue(value);
						}
					}
				});
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
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
		postEvent(new ValueChangeEvent(this, data));
	}

	public boolean isEnabled() {
		IDynamicController fixedNode = (IDynamicController) getChildController("/fixed");
		if (fixedNode != null) {
			try {
				return fixedNode.getValue().getFloatData() == -1.0f;
			} catch (ComponentDataFormatException e) {
				logger.error("Failed to retrieve enable status form " + getPath(), e);
			} catch (SicsIOException e) {
				logger.error("Failed to retrieve enable status form " + getPath(), e);
			}
		}
		return true;
	}
	
	private void setValueStatus(ValueStatus newStatus) {
		valueStatus = newStatus;
	}

	public IComponentData getTargetValue() throws SicsIOException {
		if (targetValue == null) {
			getTargetValue(new DynamicControllerCallbackAdapter() {});
			// Wait
			int waitTime = 10;
			int timeOut = 5000;	// 5 sec timeout
			int count = 0;
			while(count < timeOut) {
				if(targetValue != null) {
					break;
				}
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
				}
				count += waitTime;
				if(count >= timeOut) {
					break;
				}
			}
		}
		return targetValue;
	}
	
	public void getTargetValue(final IDynamicControllerCallback callback) {
		if (targetValue == null) {
			// Run on a separated thread
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						// Do a synchronized get first to ensure the target is loaded
						getValue(false);
						callback.handleGetValueCallback(DynamicController.this, targetValue);
					} catch (SicsIOException e) {
						logger.error("Failed to fetch target", e);
					}
				}
			});
			thread.run();
//			IComponentData data = getValue(true);
//			// try again
//			getValue(new DynamicControllerCallbackAdapter() {
//				public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
//					callback.handleGetValueCallback(DynamicController.this, targetValue);
//				}
//			}, true);
		} else {
			callback.handleGetValueCallback(this, targetValue);
		}
	}

	public String toString() {
		return "[DynamicController] : " + getPath();
	}

	public DataType getDataType() {
		return getComponent().getDataType();
	}

	public long getLastValueUpdated() {
		return lastValueUpdated;
	}
	
	public boolean commitTargetValue(final IDynamicControllerCallback callback) throws SicsIOException {
		if(getTargetValue() == null) {
			return false;
		}
		// Reset status after failure
		clearError();
		SicsCore.getDefaultProxy().send("hset " + getPath() + " " + getTargetValue().getSicsString(), new SicsCallbackAdapter() {
			public void receiveReply(ISicsReplyData data) {
				setCallbackCompleted(true);
				String msg = data.getString();
				if (msg.contains("ERROR:")) {
					errorMessage = msg;
					setStatus(ControllerStatus.ERROR);
					if(callback != null) {
						callback.handleOperationError((IDynamicController)getComponentController(), msg);
					}
				} else {
					if(callback != null) {
						callback.handleOperationCompleted((IDynamicController)getComponentController());
					}
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
	
	public String getErrorMessage() {
		if (errorMessage == null) {
			errorMessage = "";
		}
		return errorMessage;
	}

	public void clearError() {
		if (getStatus().equals(ControllerStatus.ERROR)) {
			errorMessage = null;
			setStatus(ControllerStatus.OK);
		} else {
			errorMessage = null;
		}
	}
	
}
