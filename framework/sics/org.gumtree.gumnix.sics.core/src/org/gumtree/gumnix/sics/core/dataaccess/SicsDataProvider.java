package org.gumtree.gumnix.sics.core.dataaccess;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.events.ControllerStatusEvent;
import org.gumtree.gumnix.sics.control.events.SicsControllerEvent;
import org.gumtree.gumnix.sics.control.events.TargetChangeEvent;
import org.gumtree.gumnix.sics.control.events.ValueChangeEvent;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.IDataChangeListener;
import org.gumtree.service.dataaccess.InvalidResourceException;
import org.gumtree.service.dataaccess.providers.AbstractDataProvider;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.messaging.SafeListenerRunnable;

public class SicsDataProvider extends AbstractDataProvider<Object> {

	private static final String HOST_HDB = "hdb";
	
	private static final String HOST_PROXY = "proxy";
	
	private IEventHandler<SicsControllerEvent> sicsEventHandler;
	
	public SicsDataProvider() {
		sicsEventHandler = new IEventHandler<SicsControllerEvent>() {
			public void handleEvent(SicsControllerEvent event) {
				handleSicsEvent(event);
			}
		};
		PlatformUtils.getPlatformEventBus().subscribe(sicsEventHandler);
	}
	
	public <T> T get(URI uri, Class<T> representation,
			Map<String, Object> properties) throws DataAccessException {
		if (HOST_HDB.equals(uri.getHost())) {
			IComponentController controller = SicsCore.getSicsController().findComponentController(uri.getPath());
			if (controller != null) {
				Map<String, Object> props = new HashMap<String, Object>(2);
				props.put(SicsDataConverter.PROP_ATTRIBUTE, uri.getQuery());
				return convert(controller, representation, props);
			}
		} else if (HOST_PROXY.equals(uri.getHost())) {
			// TODO: get proxy information
		}
		throw new InvalidResourceException(uri.toString());
	}

	private void handleSicsEvent(SicsControllerEvent event) {
		if (event instanceof ValueChangeEvent) {
			final ValueChangeEvent changeEvent = (ValueChangeEvent) event;
			final URI uri = changeEvent.getURI();
			listenerManager.asyncInvokeListeners(new SafeListenerRunnable<IDataChangeListener>() {
				public void run(IDataChangeListener listener) throws Exception {
					// Matching URI
					if (listener.matchUri(uri)) {
						// Get representation
						Class<?> representation = listener.getRepresentation(uri);
						// Convert data
						Object data =  convert(changeEvent.getController(), representation, null);
						// Send data
						listener.handleDataChange(uri, representation, data);
					}
				}
			});
		} else if (event instanceof TargetChangeEvent) {
			final TargetChangeEvent changeEvent = (TargetChangeEvent) event;
			final URI uri = changeEvent.getURI();
			final Map<String, Object> props = new HashMap<String, Object>(2);
			props.put(SicsDataConverter.PROP_ATTRIBUTE, uri.getQuery());
			listenerManager.asyncInvokeListeners(new SafeListenerRunnable<IDataChangeListener>() {
				public void run(IDataChangeListener listener) throws Exception {
					// Matching URI
					if (listener.matchUri(uri)) {
						// Get representation
						Class<?> representation = listener.getRepresentation(uri);
						// Convert data
						Object data = convert(changeEvent.getController(), representation, props);
						// Send data
						listener.handleDataChange(uri, representation, data);
					}
				}
			});
		} else if (event instanceof ControllerStatusEvent) {
			final ControllerStatusEvent statusEvent = (ControllerStatusEvent) event;
			final URI uri = statusEvent.getURI();
			final Map<String, Object> props = new HashMap<String, Object>(2);
			props.put(SicsDataConverter.PROP_ATTRIBUTE, uri.getQuery());
			listenerManager.asyncInvokeListeners(new SafeListenerRunnable<IDataChangeListener>() {
				public void run(IDataChangeListener listener) throws Exception {
					// Matching URI
					if (listener.matchUri(uri)) {
						// Get representation
						Class<?> representation = listener.getRepresentation(uri);
						// Convert data
						Object data = convert(statusEvent.getController(), representation, props);
						// Send data
						listener.handleDataChange(uri, representation, data);
					}
				}
			});
		}
	}
	
}
