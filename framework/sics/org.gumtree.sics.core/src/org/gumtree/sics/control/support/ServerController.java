package org.gumtree.sics.control.support;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.gumtree.sics.control.IServerController;
import org.gumtree.sics.control.ServerStatus;
import org.gumtree.sics.core.ISicsMonitor;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.ISicsLogManager.LogType;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsCallbackAdapter;
import org.gumtree.sics.io.SicsEventBuilder;
import org.gumtree.sics.io.SicsEventHandler;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.SicsLogManager;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerController extends SicsController implements IServerController {

	private static Logger logger = LoggerFactory
			.getLogger(ServerController.class);

	private static String PATH = "/";
	private static String TERTIARY_PATH = "/instrument/status/tertiary";
	private static String SECONDARY_PATH = "/instrument/status/secondary";
	private ServerStatus serverStatus;

	private volatile boolean isInterrupted;

	private Object interruptFlagLock = new Object();

	private SicsEventHandler proxyEventHandler;
	
	private SicsEventHandler monitorEventHandler;

	private SicsEventHandler tertiaryEventHandler;
	
	private SicsEventHandler secondaryEventHandler;
	
	public ServerController() {
		super();
		this.serverStatus = ServerStatus.UNKNOWN;
		proxyEventHandler = new SicsEventHandler(ISicsProxy.EVENT_TOPIC_PROXY_STATE_ALL) {
			@Override
			public void handleSicsEvent(Event event) {
				String topic = getTopic(event); 
				if (topic.equals(ISicsProxy.EVENT_TOPIC_PROXY_STATE_CONNECTED)) {
					bindProxy();	
				} else if (topic.equals(ISicsProxy.EVENT_TOPIC_PROXY_STATE_DISCONNECTED)) {
					unbindProxy();
				}
			}
		};		
		monitorEventHandler = new SicsEventHandler(ISicsMonitor.EVENT_TOPIC_SERVER) {
			@Override
			public void handleSicsEvent(Event event) {
				serverStatus = ServerStatus.parseStatus(getString(event,
						ISicsMonitor.EVENT_PROP_STATUS));
				SicsLogManager.getInstance().log(LogType.STATUS, serverStatus.getText());
			}
		};
		tertiaryEventHandler = new SicsEventHandler(ISicsMonitor.EVENT_TOPIC_HNOTIFY
				+ TERTIARY_PATH) {
			@Override
			public void handleSicsEvent(Event event) {
				String newValue = getString(event,
						ISicsMonitor.EVENT_PROP_VALUE);
				SicsLogManager.getInstance().log(LogType.TERTIARY, newValue);
			}
		};
		secondaryEventHandler = new SicsEventHandler(ISicsMonitor.EVENT_TOPIC_HNOTIFY
				+ SECONDARY_PATH) {
			@Override
			public void handleSicsEvent(Event event) {
				String newValue = getString(event,
						ISicsMonitor.EVENT_PROP_VALUE);
				SicsLogManager.getInstance().log(LogType.SECONDARY, newValue);
			}
		};

	}

//	public IComponentController findDeviceController(String deviceId) {
//		Component component = SicsModelUtils.findComponentFromSingleProperty(
//				getModelProvider().getModel(), "sicsdev", deviceId);
//		if (component != null) {
//			return findComponentController(component);
//		}
//		return null;
//
//	}
//
//	public IComponentController findComponentController(String path) {
//		return findComponentController(null, path);
//	}
//
//	public IComponentController findComponentController(
//			IComponentController controller, String relativePath) {
//		String[] parts = relativePath.split("/");
//		if (parts.length <= 1) {
//			return null;
//		}
//		IComponentController[] childControllers = null;
//		if (controller == null) {
//			childControllers = getComponentControllers();
//		} else {
//			childControllers = controller.getChildControllers();
//		}
//		for (IComponentController childController : childControllers) {
//			if (childController.getComponent().getId().equals(parts[1])) {
//				relativePath = relativePath.substring(parts[1].length() + 1);
//				if (relativePath.length() == 0) {
//					return childController;
//				} else {
//					return findComponentController(childController,
//							relativePath);
//				}
//			}
//		}
//		return null;
//	}
//
//	public IComponentController findComponentController(Component component) {
//		return findComponentController(SicsModelUtils.getPath(component));
//	}
//
//	public IComponentController findParentController(
//			IComponentController controller) {
//		Component parentComponent = SicsModelUtils
//				.getComponentParent(controller.getComponent());
//		if (parentComponent != null) {
//			return findComponentController(parentComponent);
//		}
//		return null;
//	}
//
//	public IComponentController[] getComponentControllers() {
//		return getControllerList().toArray(
//				new IComponentController[getControllerList().size()]);
//	}
//
//	private List<IComponentController> getControllerList() {
//		if (controllers == null) {
//			controllers = new ArrayList<IComponentController>();
//			// create controllers
//			for (Component childComponent : (List<Component>) getModelProvider()
//					.getModel().getComponent()) {
//				// logger.debug("Creating top level child controller for " +
//				// childComponent.getId());
//				// Object controller =
//				// Platform.getAdapterManager().getAdapter(childComponent,
//				// IComponentController.class);
//				// IComponentController controller =
//				// ComponentControllerFactory1.createController(childComponent);
//				IComponentController controller = getControllerFactory()
//						.createComponentController(childComponent);
//				if (controller != null) {
//					controllers.add(controller);
//					// Listen to status changes
//					controller
//							.addComponentListener(new DirectComponentListener());
//				}
//			}
//			// initialise the controllers once the hierarchy has been
//			// constructed
//			for (IComponentController childController : controllers) {
//				initialiseComponent(childController);
//			}
//			// Also initialise custom sics objects
//			for (ISicsObjectController controller : getControllerFactory()
//					.createSicsObjectControllers()) {
//				customControllers.put(controller.getId(), controller);
//			}
//		}
//		return controllers;
//	}
//
//	public ISicsObjectController[] getSicsObjectControllers() {
//		return customControllers.values().toArray(
//				new ISicsObjectController[customControllers.size()]);
//	}
//
//	public ISicsObjectController getSicsObjectController(String id) {
//		return customControllers.get(id);
//	}
//
//	private void initialiseComponent(IComponentController controller) {
//		// getLogger().debug("Start initialising " + controller.getPath());
//		// recurrsively initialise child controllers first
//		for (IComponentController childController : controller
//				.getChildControllers()) {
//			initialiseComponent(childController);
//		}
//		// then initialise the given controller if it supports initialisation
//		if (controller instanceof ComponentController) {
//			((ComponentController) controller).activate();
//		}
//	}
//
//	public IComponentData getValue(String path) throws SicsIOException {
//		IComponentController controller = findComponentController(path);
//		if (controller instanceof IDynamicController) {
//			return ((IDynamicController) controller).getValue();
//		}
//		return null;
//	}
//
//	public void setValue(String path, IComponentData newData)
//			throws SicsIOException {
//		IComponentController controller = findComponentController(path);
//		if (controller instanceof IDynamicController) {
//			((IDynamicController) controller).setTargetValue(newData);
//			((IDynamicController) controller).commitTargetValue(null);
//		}
//	}
//
//	private class DirectComponentListener extends
//			ComponentControllerListenerAdapter {
//
//		public void componentStatusChanged(ControllerStatus newStatus) {
//			// trigger status update
//			boolean isError = false;
//			boolean isRunning = false;
//			// Sics controller status is based on it's direct component
//			// controllers' status
//			for (IComponentController controller : getComponentControllers()) {
//				if (controller.getStatus().equals(ControllerStatus.RUNNING)) {
//					isRunning = true;
//				} else if (controller.getStatus()
//						.equals(ControllerStatus.ERROR)) {
//					isError = true;
//				}
//			}
//			if (isError) {
//				setStatus(ControllerStatus.ERROR);
//			} else if (isRunning) {
//				setStatus(ControllerStatus.RUNNING);
//			} else {
//				setStatus(ControllerStatus.OK);
//			}
//		}
//
//	}

	// private IComponentControllerFactory getControllerFactory() {
	// if (controllerFactory == null) {
	// logger.info("Acquiring controller factory");
	// controllerFactory =
	// ServiceUtils.getServiceManager().getService(IComponentControllerFactory.class,
	// IServiceManager.NO_TIMEOUT);
	// logger.info("Found controller factory " + controllerFactory.toString());
	// }
	// return controllerFactory;
	// }

	public String getPath() {
		return PATH;
	}
	
	/*************************************************************************
	 * 
	 * Status and interrupt
	 * 
	 *************************************************************************/

	public ServerStatus getServerStatus() {
		return serverStatus;
	}

	protected void setServerStatus(ServerStatus serverStatus) {
		this.serverStatus = serverStatus;
		SicsLogManager.getInstance().log(LogType.STATUS, serverStatus.getText());
		if (getProxy() != null) {
			new SicsEventBuilder(EVENT_TOPIC_SERVER_STATUS_CHANGE, getProxy()
					.getId()).append(EVENT_PROP_CONTROLLER, this)
					.append(EVENT_PROP_SERVER_STATUS, serverStatus).post();
		}
	}

	public void interrupt() throws SicsIOException {
		// [GT-54]
		if (getProxy() != null && getProxy().isConnected()) {
			getProxy().send("INT1712 3", null);

			// // [GT-135] Use UDP to abort SICS
			// // Read from default system properties
			// String host = SystemProperties.SICS_HOST.getValue();
			// int port =
			// Integer.parseInt(SystemProperties.SICS_PORT.getValue()) - 1;
			//
			// // Read from current context if possible (better!)
			// ISicsConnectionContext context =
			// SicsCore.getDefaultProxy().getConnectionContext();
			// if (context != null) {
			// host = context.getHost();
			// port = context.getPort() - 1;
			// }
			//
			// // Send interrupt
			// try {
			// Socket socket = new Socket(host, port);
			// PrintStream output = new PrintStream(socket.getOutputStream());
			// output.println("INT1712 3");
			// output.flush();
			// socket.close();
			// } catch (UnknownHostException e) {
			// logger.error("Failed to connect to " + host + ":" + port +
			// " for interrupt.", e);
			// } catch (IOException e) {
			// logger.error("Failed to connect to " + host + ":" + port +
			// " for interrupt.", e);
			// }

			synchronized (interruptFlagLock) {
				isInterrupted = true;
			}

			new SicsEventBuilder(EVENT_TOPIC_SERVER_INTERRUPT, getProxy()
					.getId()).append(EVENT_PROP_CONTROLLER, this).post();
		}
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

	/*************************************************************************
	 * 
	 * Getters and setters
	 * 
	 *************************************************************************/

	@Override
	@Inject
	public void setProxy(ISicsProxy proxy) {
		super.setProxy(proxy);
		if (getProxy() != null) {
			// Bind to proxy if connected
			if (proxy.isConnected()) {
				bindProxy();
			}
			proxyEventHandler.setProxyId(proxy.getId()).activate();
			monitorEventHandler.setProxyId(proxy.getId()).activate();
			tertiaryEventHandler.setProxyId(getProxy().getId()).activate();
			secondaryEventHandler.setProxyId(getProxy().getId()).activate();
		}
	}

	protected void bindProxy() {
		try {
			getProxy().send("status", new SicsCallbackAdapter() {
				public void receiveReply(ISicsData data) {
					try {
						setServerStatus(ServerStatus.parseStatus(data
								.getString().split("=")[1].trim()));
					} catch (Exception e) {
					}
					setCallbackCompleted(true);
				}
			});
		} catch (SicsIOException e) {
			logger.error("Failed to update status", e);
		}
	}

	protected void unbindProxy() {
		setServerStatus(ServerStatus.UNKNOWN);
		clearInterrupt();
	}

	/*************************************************************************
	 * Object life cycle
	 *************************************************************************/
	
	@Override
	@PreDestroy
	public void disposeObject() {
		if (proxyEventHandler != null) {
			proxyEventHandler.deactivate();
			proxyEventHandler = null;
		}
		if (monitorEventHandler != null) {
			monitorEventHandler.deactivate();
			monitorEventHandler = null;
		}
		if (getProxy() != null) {
			unbindProxy();
		}
		super.disposeObject();
	}
	
	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

}
