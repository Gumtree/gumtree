package org.gumtree.gumnix.sics.internal.core;

//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.collection.CollectionUtils;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.SICS;

public class SicsModelLoader {

	private static final String COMMAND_GET_MODEL = "getgumtreexml /";

	private static final Logger logger = LoggerFactory.getLogger(SicsModelLoader.class);
	
	private static int WAIT_TIME = 50;

	private volatile SICS model;

	private ISicsManager manager;

	protected SicsModelLoader(ISicsManager manager) {
		this.manager = manager;
	}

	public SICS getModel() throws SicsIOException {
		if(model == null) {
			synchronized (this) {
				if(model == null) {
					loadModel();
				}
			}
		}
		return model;
	}

	private ISicsManager getManager() {
		return manager;
	}

	private void loadModel() throws SicsIOException {
		ISicsCallback callback = new SicsCallbackAdapter() {
			public void receiveReply(ISicsReplyData data) {
				String content = data.getString();
				EventAdmin eventAdmin = ServiceUtils.getService(EventAdmin.class);
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
					File file = Activator.getDefault().getStateLocation().append("/hipadaba.xml").toFile();
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
					model = SicsUtils.deserialiseSICSModel(byteData);
				} catch (IOException e) {
					logger.error("Failed to deserialise model.", e);
				}
				setCallbackCompleted(true);
			}
			public void receiveError(ISicsReplyData data) {
				setError(true);
				setCallbackCompleted(true);
			}
		};
		getManager().proxy().send(COMMAND_GET_MODEL, callback);
		int counter = 0;
		while (!callback.isCallbackCompleted()) {
			try {
				Thread.sleep(WAIT_TIME);
				counter += WAIT_TIME;
				// [GT-24] [Tony] [2008-06-24] We need to give more time for
				// SICS to generate the hipadaba model.
				if (counter > (SicsCoreProperties.PROXY_TIMEOUT.getLong() * 5)) {
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
}
