package org.gumtree.sics.core.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.sics.core.ISicsModelProvider;
import org.gumtree.sics.io.ISicsCallback;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsCallbackAdapter;
import org.gumtree.sics.io.SicsEventHandler;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.util.Activator;
import org.gumtree.sics.util.SicsCoreProperties;
import org.gumtree.sics.util.SicsModelUtils;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.collection.CollectionUtils;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.SICS;

public class SicsModelProvider implements ISicsModelProvider {

	private static final String COMMAND_GET_MODEL = "getgumtreexml /";

	private static final Logger logger = LoggerFactory
			.getLogger(SicsModelProvider.class);

	private static int WAIT_TIME = 50;

	private volatile SICS model;

	private ISicsProxy proxy;

	private SicsEventHandler eventHandler;
	
	private long timeout = -1;
	
	public SicsModelProvider() {
		// Activate when proxy is set
		eventHandler = new SicsEventHandler(ISicsProxy.EVENT_TOPIC_PROXY_STATE_DISCONNECTED) {
			@Override
			public void handleSicsEvent(Event event) {
				// Clear model cache when proxy is disconnected
				model = null;
			}
		};
	}

	@Override
	public SICS getModel() throws SicsIOException {
		return getModel(false);
	}

	@Override
	public SICS getModel(boolean refresh) throws SicsIOException {
		if (!refresh && model != null) {
			// return cache
			return model;
		}
		if (getProxy() == null) {
			// don't bother to connect if proxy is unavailable
			return null;
		}
		synchronized (this) {
			if (model == null) {
				// load model no matter refresh or not as model is empty
				loadModel();
			} else if (refresh) {
				// model is not empty, but refresh has been requested
				loadModel();
			}
		}
		return model;
	}

	@Override
	public ISicsProxy getProxy() {
		return proxy;
	}

	@Override
	@Inject
	public void setProxy(ISicsProxy proxy) {
		this.proxy = proxy;
		if (proxy != null) {
			eventHandler.setProxyId(proxy.getId()).activate();
		}
	}

	private void loadModel() throws SicsIOException {
		ISicsCallback callback = new SicsCallbackAdapter() {
			public void receiveReply(ISicsData data) {
				String content = data.getString();
				// Send event to the bus if available
				EventAdmin eventAdmin = ServiceUtils
						.getService(EventAdmin.class);
				if (eventAdmin != null) {
					eventAdmin
							.postEvent(new Event(
									PlatformUtils.EVENT_TOPIC_RUNTIME_STARTUP_MESSAGE,
									CollectionUtils
											.createMap(
													PlatformUtils.EVENT_PROP_RUNTIME_STARTUP_MESSAGE,
													"Loading instrument model...")));
				}
				// Keeps a record of the model
				try {
					File file = Activator.getDefault().getStateLocation()
							.append("/hipadaba.xml").toFile();
					FileOutputStream outputStream = new FileOutputStream(file);
					outputStream.write(content.getBytes());
					outputStream.flush();
					outputStream.close();
				} catch (FileNotFoundException e1) {
					logger.error("Failed to save hdb model.", e1);
				} catch (IOException e) {
					logger.error("Failed to save hdb model.", e);
				}

				byte[] byteData = content.getBytes();
				try {
					model = SicsModelUtils.deserialiseSICSModel(byteData);
				} catch (IOException e) {
					logger.error("Failed to deserialise model.", e);
				}
				setCallbackCompleted(true);
			}

			public void receiveError(ISicsData data) {
				setError(true);
				setCallbackCompleted(true);
			}
		};
		getProxy().send(COMMAND_GET_MODEL, callback);
		int counter = 0;
		while (!callback.isCallbackCompleted()) {
			try {
				Thread.sleep(WAIT_TIME);
				counter += WAIT_TIME;
				// [GT-24] [Tony] [2008-06-24] We need to give more time for
				// SICS to generate the hipadaba model.
				if (counter > getTimeout()) {
					throw new SicsIOException(
							"Time out on loading instrument model.");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (callback.hasError()) {
			throw new SicsIOException("Cannot loading instrument model.");
		}
	}
	
	@Override
	public long getTimeout() {
		if (timeout < 0) {
			timeout = SicsCoreProperties.PROXY_TIMEOUT.getLong() * 5;
		}
		return timeout;
	}
	
	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	@PreDestroy
	public void disposeObject() {
		if (eventHandler != null) {
			eventHandler.deactivate();
			eventHandler = null;
		}
		model = null;
		proxy = null;
	}
	
}
