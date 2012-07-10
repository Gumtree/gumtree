package org.gumtree.gumnix.sics.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gumtree.gumnix.sics.control.IComponentController.ComponentStatus;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.internal.control.DevicePropertyImpl;
import org.gumtree.gumnix.sics.internal.control.ISicsPropertyListener;
import org.gumtree.gumnix.sics.internal.control.InternalPropertyCallback;
import org.gumtree.gumnix.sics.internal.control.PropertyEntry;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsData;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public class DeviceController extends ComponentController implements IDeviceController, ISicsPropertyListener {

	private Logger logger;

	private static int TIME_OUT = 5000;

	private static int WAIT_TIME = 50;

	private IDeviceProperty defaultProperty;

	private Map<String, PropertyEntry> propertyMap;

	private Set<IDeviceListener> deviceListeners;

	public DeviceController() {
		super();
	}

	public void setComponent(Component component) {
		if(component instanceof Device) {
			super.setComponent(component);
			initialiseParameterMap();
		} else {
			throw new Error("Component is not an instance of Device.");
		}
	}

	public Device getDevice() {
		return (Device)getComponent();
	}

	protected Map<String, PropertyEntry> getPropertyMap() {
		if(propertyMap == null) {
			propertyMap = new HashMap<String, PropertyEntry>();
		}
		return propertyMap;
	}

	public void setPropertyValue(Property property, String newValue) {
		PropertyEntry entry = getPropertyMap().get(SicsUtils.getPath(property));
		String path = SicsUtils.getPath(property);
		if(entry != null) {
			entry.setTargetValue(newValue);
			try {
				ISicsCallback callback = new SicsCallbackAdapter() {
					public void receiveReply(ISicsData data) {
						setCallbackCompleted(true);
					}
					public void receiveError(ISicsData data) {
						setStatus(ComponentStatus.ERROR);
						setCallbackCompleted(true);
					}
				};
				getProxy().send("hset " + path + " " + newValue, callback);
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized String getPropertyCurrentValue(Property property) {
		String propertyValue = null;
		boolean dataArrived = false;
		InternalPropertyCallback callback = new InternalPropertyCallback();
		getPropertyCurrentValue(property, callback);
		int counter = 0;
		String path = SicsUtils.getPath(property);
		while(!callback.isDataReady()) {
			try {
				Thread.sleep(WAIT_TIME);
				counter += WAIT_TIME;
				if(WAIT_TIME > TIME_OUT) {
					getLogger().info("Time out in reading property " + path);
					return null;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return callback.getValue();
	}

	public String getPropertyTargetValue(Property property) {
		PropertyEntry entry = getPropertyMap().get(SicsUtils.getPath(property));
		if(entry != null) {
			return entry.getTargetValue();
		}
		return null;
	}

	public void getPropertyCurrentValue(final Property property, final IDevicePropertyCallback callback) {
		String path = SicsUtils.getPath(property);
		final PropertyEntry entry = getPropertyMap().get(path);
		if(entry != null) {
			if(entry.getStatus().equals(IDeviceController.PropertyStatus.OUT_OF_SYNC)) {
				try {
					ISicsCallback proxyCallback = new SicsCallbackAdapter() {
						public void receiveReply(ISicsData data) {
							setCallbackCompleted(true);
							JSONObject object = data.getJSONObject();
							if(object == null) {
								getLogger().error("Channel reporting null json object");
								return;
							}
							String path = (String)object.keys().next();
							Double newValue;
							try {
								newValue = object.getDouble(path);
								entry.setCurrentValue(newValue.toString());
								if(callback != null) {
									callback.handleReply(property, newValue.toString());
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					};
					getProxy().send("hget " + path, proxyCallback);
				} catch (SicsIOException e) {
					e.printStackTrace();
				}
			} else {
				if(callback != null) {
					callback.handleReply(property, entry.getCurrentValue());
				}
			}
		}
	}

	public void updatePropertyEntry(String path, final String value) {
		final PropertyEntry entry = getPropertyMap().get(path);
		String oldValue = entry.getCurrentValue();
		if(entry != null) {
			entry.setCurrentValue(value);
			Set<IDeviceListener> deviceListeners = getDeviceListeners();
			// Avoid concurrent access
			synchronized (deviceListeners) {
				for (final IDeviceListener listener : deviceListeners) {
					Thread notifyer = new Thread(new Runnable() {
						public void run() {
							listener.propertyChanged(getDevice(), entry.getProperty(), value);
						}
					});
					notifyer.run();
				}
			}
		}
		setStatus(ComponentStatus.OK);
		// status update for drivable.....testing only
//		if(entry.getProperty().getDataType().equals(DataType.FLOAT_LITERAL)) {
//			if(entry.getProperty().getId().equalsIgnoreCase("position")) {
//				setStatus(ComponentStatus.RUNNING);
//			}
//		}
	}

	public IDeviceController.PropertyStatus getPropertyStatus(Property property) {
		PropertyEntry entry = getPropertyMap().get(SicsUtils.getPath(property));
		if(entry != null) {
			return entry.getStatus();
		}
		return null;
	}

	private void initialiseParameterMap() {
		for(Property property : (List<Property>)getDevice().getProperty()) {
			PropertyEntry entry = new PropertyEntry(property);
			getPropertyMap().put(entry.getPath(), entry);
		}
		// Add mock up property
		PropertyEntry defaultEntry = new PropertyEntry(getDefaultProperty(), SicsUtils.getPath(getDevice()), getDevice());
		getPropertyMap().put(defaultEntry.getPath(), defaultEntry);
	}

	private ISicsProxy getProxy() {
		return ISicsManager.INSTANCE.proxy();
	}

	private Set<IDeviceListener> getDeviceListeners() {
		if(deviceListeners == null) {
			deviceListeners = new HashSet<IDeviceListener>();
		}
		return deviceListeners;
	}

	public void addDeviceListener(IDeviceListener listener) {
		Set<IDeviceListener> listeners = getDeviceListeners();
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeDeviceListener(IDeviceListener listener) {
		Set<IDeviceListener> listeners = getDeviceListeners();
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	protected void fireChildrenStateChanged() {
		// do nothing
	}

	private Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(DeviceController.class);
		}
		return logger;
	}

	public Property getDefaultProperty() {
		if(defaultProperty == null) {
			defaultProperty = new DevicePropertyImpl(getDevice());
			defaultProperty.setPrivilege(getDevice().getPrivilege());
			defaultProperty.setDataType(getDevice().getDataType());
		}
		return defaultProperty;
	}

}
