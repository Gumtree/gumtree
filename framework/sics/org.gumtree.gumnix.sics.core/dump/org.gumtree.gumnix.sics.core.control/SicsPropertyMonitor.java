package org.gumtree.gumnix.sics.internal.control;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsData;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Device;

public class SicsPropertyMonitor {

	private Logger logger;

	private static final String CMD_GLOBAL_NOTIFY = "hnotify / 1";

	private SicsInstrumentControl instrumentControl;

	private Map<String, ISicsPropertyListener> listenerMap;

	protected SicsPropertyMonitor(SicsInstrumentControl instrumentControl) {
		this.instrumentControl = instrumentControl;
		subscribeNotification();
	}

	private void subscribeNotification() {
		ISicsProxy proxy = getInstrumentControl().getManager().proxy();
		ISicsCallback callback = new SicsCallbackAdapter() {
			private boolean initialised = false;
			public void receiveReply(ISicsData data) {
				try {
					if(!initialised) {
						initialised = true;
						return;
					}
					JSONObject object = data.getJSONObject();
					String path = (String)object.keys().next();
					Double value = object.getDouble(path);
					// updates entry
					int endIndex = path.lastIndexOf('/');
					String devicePath = path.substring(0, endIndex);
					ISicsPropertyListener listener = getListenerMap().get(devicePath);
					// path can be a device path itself
					if(listener == null) {
						listener = getListenerMap().get(path);
					}
					if(listener != null) {
						listener.updatePropertyEntry(path, value.toString());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		try {
			proxy.send(CMD_GLOBAL_NOTIFY, callback);
		} catch (SicsIOException e) {
			e.printStackTrace();
		}
	}

	private SicsInstrumentControl getInstrumentControl() {
		return instrumentControl;
	}

	private Map<String, ISicsPropertyListener> getListenerMap() {
		if(listenerMap == null) {
			listenerMap = new HashMap<String, ISicsPropertyListener>();
		}
		return listenerMap;
	}

	protected void addListener(Device device, ISicsPropertyListener listener) {
		getListenerMap().put(SicsUtils.getPath(device), listener);
	}

	protected void removeListener(Device device) {
		getListenerMap().remove(SicsUtils.getPath(device));
	}

	private Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsPropertyMonitor.class);
		}
		return logger;
	}

}
