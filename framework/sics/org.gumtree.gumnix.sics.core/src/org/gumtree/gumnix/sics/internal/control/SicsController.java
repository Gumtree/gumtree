package org.gumtree.gumnix.sics.internal.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.gumtree.core.service.IServiceManager;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.IComponentControllerFactory;
import org.gumtree.gumnix.sics.control.IHipadabaListener;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.ISicsControllerListener;
import org.gumtree.gumnix.sics.control.IStateMonitorListener;
import org.gumtree.gumnix.sics.control.ServerStatus;
import org.gumtree.gumnix.sics.control.controllers.ComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.controllers.ISicsObjectController;
import org.gumtree.gumnix.sics.control.events.ComponentControllerListenerAdapter;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsEvents;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.util.messaging.EventBuilder;
import org.gumtree.util.messaging.IListenerManager;
import org.gumtree.util.messaging.ListenerManager;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.SICS;

public class SicsController implements ISicsController {

	private static Logger logger = LoggerFactory.getLogger(SicsController.class);

	private ControllerStatus status;
	
	private ServerStatus serverStatus;
	
	private IListenerManager<ISicsControllerListener> listenerManager;
	
	private SICS model;
	
	private IComponentControllerFactory controllerFactory;

	private List<IComponentController> controllers;
	
	private Map<String, ISicsObjectController> customControllers;
	
	private volatile boolean isInterrupted;
	
	private Object interruptFlagLock = new Object();

	public SicsController(SICS model) {
		Assert.isNotNull(model);
		this.model = model;
		listenerManager = new ListenerManager<ISicsControllerListener>();
		customControllers = new HashMap<String, ISicsObjectController>();
		setStatus(ControllerStatus.OK);
		serverStatus = ServerStatus.UNKNOWN;
		
		// Manual update
		if (SicsCore.getDefaultProxy().isConnected()) {
			fetchServerStatus();
		}
		
		// Note: this is version of SICS proxy, we do not remove proxy listener
		// because there is no dispose feature for SicsController
		SicsCore.getDefaultProxy().addProxyListener(
				new SicsProxyListenerAdapter() {
					public void proxyConnected() {
						fetchServerStatus();
					}

					public void proxyDisconnected() {
						setServerStatus(ServerStatus.UNKNOWN);
					}
				});
		
		// Pushing update
		SicsCore.getSicsManager().monitor().addListener("/", new IHipadabaListener() {
			public void valueUpdated(String newValue) {
				setServerStatus(ServerStatus.parseStatus(newValue));
			}
		});
	}

	private void fetchServerStatus() {
		try {
			SicsCore.getDefaultProxy().send("status",
					new SicsCallbackAdapter() {
						public void receiveReply(ISicsReplyData data) {
							try {
								setServerStatus(ServerStatus.parseStatus(data
										.getString().split("=")[1].trim()));
							} catch (Exception e) {
							}
							;
							setCallbackCompleted(true);
						}
					});
		} catch (SicsIOException e) {
			logger.error("Failed to fetch server status", e);
		}
	}
	
	public IComponentController findDeviceController(String deviceId) {
		Component component = SicsUtils.findComponentFromSingleProperty(model, "sicsdev", deviceId);
		if(component != null) {
			return findComponentController(component);
		}
		return null;
		
	}
	
	public IComponentController findComponentController(String path) {
		return findComponentController(null, path);
	}

	public IComponentController findComponentController(IComponentController controller, String relativePath) {
		Assert.isNotNull(relativePath);
		String[] parts = relativePath.split("/");
		if(parts.length <= 1) {
			return null;
		}
		IComponentController[] childControllers = null;
		if(controller == null) {
			childControllers = getComponentControllers();
		} else {
			childControllers = controller.getChildControllers();
		}
		for(IComponentController childController : childControllers) {
			if(childController.getComponent().getId().equals(parts[1])) {
				relativePath = relativePath.substring(parts[1].length() + 1);
				if(relativePath.length() == 0) {
					return childController;
				} else {
					return findComponentController(childController, relativePath);
				}
			}
		}
		return null;
	}

	public IComponentController findComponentController(Component component) {
		return findComponentController(SicsUtils.getPath(component));
	}

	public IComponentController findParentController(IComponentController controller) {
		Component parentComponent = SicsUtils.getComponentParent(controller.getComponent());
		if (parentComponent != null) {
			return findComponentController(parentComponent);
		}
		return null;
	}
	
	public IComponentController[] getComponentControllers() {
		return getControllerList().toArray(new IComponentController[getControllerList().size()]);
	}

	private List<IComponentController> getControllerList() {
		if(controllers == null) {
			controllers = new ArrayList<IComponentController>();
			// create controllers
			for(Component childComponent : (List<Component>)getSICSModel().getComponent()) {
//				logger.debug("Creating top level child controller for " + childComponent.getId());
//				Object controller = Platform.getAdapterManager().getAdapter(childComponent, IComponentController.class);
//				IComponentController controller = ComponentControllerFactory1.createController(childComponent);
				IComponentController controller = getControllerFactory().createComponentController(childComponent);
				if(controller != null) {
					controllers.add(controller);
					// Listen to status changes
					controller.addComponentListener(new DirectComponentListener());
				}
			}
			// initialise the controllers once the hierarchy has been constructed
			for(IComponentController childController : controllers) {
				initialiseComponent(childController);
			}
			// Also initialise custom sics objects
			for (ISicsObjectController controller : getControllerFactory()
					.createSicsObjectControllers()) {
				customControllers.put(controller.getId(), controller);
			}
		}
		return controllers;
	}
	
	public ISicsObjectController[] getSicsObjectControllers() {
		return customControllers.values().toArray(
				new ISicsObjectController[customControllers.size()]);
	}
	
	public ISicsObjectController getSicsObjectController(String id) {
		return customControllers.get(id);
	}

	public void addControllerListener(ISicsControllerListener listener) {
		listenerManager.addListenerObject(listener);
	}
	
	public void removeControllerListener(ISicsControllerListener listener) {
		listenerManager.removeListenerObject(listener);
	}
	
	public ControllerStatus getStatus() {
		return status;
	}
	
	public ServerStatus getServerStatus() {
		return serverStatus;
	}
	
	private void setServerStatus(ServerStatus serverStatus) {
		this.serverStatus = serverStatus;
//		new EventBuilder(EVENT_TOPIC_SERVER_STATUS).append(EVENT_PROP_VALUE,
//				serverStatus).post();
		new EventBuilder(SicsEvents.Server.TOPIC_SERVER_STATUS).append(
				SicsEvents.Server.STATUS, serverStatus).post();
	}

	private IComponentControllerFactory getControllerFactory() {
		if (controllerFactory == null) {
			logger.info("Acquiring controller factory");
			controllerFactory = ServiceUtils.getServiceManager().getService(IComponentControllerFactory.class, IServiceManager.NO_TIMEOUT);
			logger.info("Found controller factory " + controllerFactory.toString());
		}
		return controllerFactory;
	}
	
	private void setStatus(final ControllerStatus newStatus) {
		status = newStatus;
		listenerManager.asyncInvokeListeners(new SafeListenerRunnable<ISicsControllerListener>() {
			public void run(ISicsControllerListener listener) throws Exception {
				listener.statusChanged(newStatus);
			}
		});
	}
	
	public SICS getSICSModel() {
		return model;
	}

	private void initialiseComponent(IComponentController controller) {
//		getLogger().debug("Start initialising " + controller.getPath());
		// recurrsively initialise child controllers first
		for(IComponentController childController : controller.getChildControllers()) {
			initialiseComponent(childController);
		}
		// then initialise the given controller if it supports initialisation
		if(controller instanceof ComponentController) {
			((ComponentController)controller).activate();
		}
	}

	public void addStateMonitor(String sicsObject, IStateMonitorListener listener) {
		SicsCore.getSicsManager().monitor().addStateMonitor(sicsObject, listener);
	}

	public void removeStateMonitor(String sicsObject, IStateMonitorListener listener) {
		SicsCore.getSicsManager().monitor().removeStateMonitor(sicsObject, listener);
	}

	public IComponentData getValue(String path) throws SicsIOException {
		IComponentController controller = findComponentController(path);
		if (controller instanceof IDynamicController) {
			return ((IDynamicController) controller).getValue();
		}
		return null;
	}
	
	public void setValue(String path, IComponentData newData) throws SicsIOException {
		IComponentController controller = findComponentController(path);
		if (controller instanceof IDynamicController) {
			((IDynamicController) controller).setTargetValue(newData);
			((IDynamicController) controller).commitTargetValue(null);
		}
	}
	
	public void interrupt() throws SicsIOException {
		// [GT-54]
		SicsCore.getDefaultProxy().send("INT1712 3", null);
		
//		// [GT-135] Use UDP to abort SICS
//		// Read from default system properties
//		String host = SystemProperties.SICS_HOST.getValue();
//		int port = Integer.parseInt(SystemProperties.SICS_PORT.getValue()) - 1;
//		
//		// Read from current context if possible (better!)
//		ISicsConnectionContext context = SicsCore.getDefaultProxy().getConnectionContext();
//		if (context != null) {
//			host = context.getHost();
//			port = context.getPort() - 1;
//		}
//		
//		// Send interrupt
//		try {
//			Socket socket = new Socket(host, port);
//			PrintStream output = new PrintStream(socket.getOutputStream());
//			output.println("INT1712 3");
//			output.flush();
//			socket.close();
//		} catch (UnknownHostException e) {
//			logger.error("Failed to connect to " + host + ":" + port + " for interrupt.", e);
//		} catch (IOException e) {
//			logger.error("Failed to connect to " + host + ":" + port + " for interrupt.", e);
//		}
		
		synchronized (interruptFlagLock) {
			isInterrupted = true;	
		}
		listenerManager.asyncInvokeListeners(new SafeListenerRunnable<ISicsControllerListener>() {
			public void run(ISicsControllerListener listener) throws Exception {
				listener.controllerInterrupted();
			}			
		});
	}
	
	public boolean isInterrupted() {
		synchronized (interruptFlagLock) {
			return isInterrupted;
		}
	}
	
	public void clearInterrupt() {
		synchronized (interruptFlagLock) {
			isInterrupted = false;	
		}
	}
	
	private class DirectComponentListener extends ComponentControllerListenerAdapter {
		
		public void componentStatusChanged(ControllerStatus newStatus) {
			// trigger status update
			boolean isError = false;
			boolean isRunning = false;
			// Sics controller status is based on it's direct component controllers' status
			for (IComponentController controller : getComponentControllers()) {
				if (controller.getStatus().equals(ControllerStatus.RUNNING)) {
					isRunning = true;
				} else if (controller.getStatus().equals(ControllerStatus.ERROR)) {
					isError = true;
				}
			}
			if (isError) {
				setStatus(ControllerStatus.ERROR);
			} else if (isRunning) {
				setStatus(ControllerStatus.RUNNING);
			} else {
				setStatus(ControllerStatus.OK);
			}
		}
		
	}

}
