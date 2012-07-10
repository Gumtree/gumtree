package org.gumtree.gumnix.sics.control.controllers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.gumtree.core.service.IServiceManager;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.IComponentControllerFactory;
import org.gumtree.gumnix.sics.control.events.ControllerStatusEvent;
import org.gumtree.gumnix.sics.control.events.IComponentControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.service.eventbus.IEvent;
import org.gumtree.service.eventbus.IEventBus;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.messaging.IListenerManager;
import org.gumtree.util.messaging.ListenerManager;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;

public abstract class ComponentController implements IComponentController {

	private Component component;

//	private Set<IComponentControllerListener> listeners;

	private ControllerStatus status;

	private volatile IComponentController parentController;

	private List<IComponentController> childControllers;

	private IListenerManager<IComponentControllerListener> listenerManager;

	private IComponentController instance;

	private String path;

	private String deviceId;
	
	private Logger logger;

	private IComponentControllerFactory controllerFactory;
	
	public ComponentController(Component component) {
		Assert.isNotNull(component);
		this.component = component;
		instance = this;
		status = ControllerStatus.OK;
		childControllers = new ArrayList<IComponentController>();
		listenerManager = new ListenerManager<IComponentControllerListener>();
		preInitialise();
		createChildControllers();
		postInitialise();
	}

	// Initialise before the controller is ready
	public abstract void preInitialise();
	
	// Initialise after the controller is ready
	public abstract void postInitialise();

	// Activate
	public abstract void activate();
	
	private void createChildControllers() {
		for(Component childComponent : (List<Component>)getComponent().getComponent()) {
//			getLogger().debug("Creating child controller for " + childComponent.getId());
//			IComponentController controller = ComponentControllerFactory1.createController(childComponent);
//			Object controller = Platform.getAdapterManager().getAdapter(childComponent, IComponentController.class);
			IComponentController controller = getControllerFactory().createComponentController(childComponent);
			if(controller != null) {
				childControllers.add(controller);
			}
		}
	}

	public IComponentController[] getChildControllers() {
		return childControllers.toArray(new IComponentController[childControllers.size()]);
	}

	public Component getComponent() {
		return component;
	}

	public String getId() {
		return getComponent().getId();
	}
	
	public String getPath() {
		if(path == null) {
			path = SicsUtils.getPath(getComponent());
		}
		return path;
	}

	public String getDeviceId() {
		if(deviceId == null) {
			deviceId = SicsUtils.getPropertyFirstValue(getComponent(), "sicsdev");
		}
		return deviceId;
	}
	
	public ControllerStatus getStatus() {
		return status;
	}

	protected IComponentController getComponentController() {
		return instance;
	}

	public IComponentController getChildController(String relativePath) {
		return SicsCore.getSicsController().findComponentController(this, relativePath);
	}
	
	public void setStatus(final ControllerStatus status) {
//		System.out.println(getPath() + " : " + status.name());
		
//		synchronized (this.status) {
//			if(this.status != null && this.status.equals(status)) {
//				return;
//			}
			this.status = status;

//			System.out.println("Set " + getPath() + " : " + status.name());
			
			// Notify listeners
			getListenerManager().asyncInvokeListeners(new SafeListenerRunnable<IComponentControllerListener>() {
				public void run(IComponentControllerListener listener)
						throws Exception {
					listener.componentStatusChanged(status);
				}
			});
			// Send to event bus
			postEvent(new ControllerStatusEvent(this, status));
			
			// Cascade status
			if (getParentController() != null) {
				getParentController().refreshStatus();
			}
//		}
	}

	private IComponentControllerFactory getControllerFactory() {
		if (controllerFactory == null) {
			controllerFactory = ServiceUtils.getServiceManager().getService(IComponentControllerFactory.class, IServiceManager.NO_TIMEOUT);
		}
		return controllerFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.controllers.IComponentController#refreshStatus()
	 */
	public void refreshStatus() {
		boolean isError = false;
		boolean isRunning = false;
		for (IComponentController controller : childControllers) {
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
	
	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.IComponentController#addComponentListener(org.gumtree.gumnix.sics.control.IComponentListener)
	 */
	public void addComponentListener(IComponentControllerListener listener) {
		getListenerManager().addListenerObject(listener);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.IComponentController#removeComponentListener(org.gumtree.gumnix.sics.control.IComponentListener)
	 */
	public void removeComponentListener(IComponentControllerListener listener) {
		getListenerManager().removeListenerObject(listener);
	}

	protected IListenerManager<IComponentControllerListener> getListenerManager() {
		return listenerManager;
	}

	protected IComponentController getParentController() {
		if (parentController == null) {
			Component parentComponent = SicsUtils.getComponentParent(getComponent());
			if (parentComponent != null) {
				parentController = SicsCore.getSicsController().findComponentController(parentComponent);
			}
		}
		return parentController;
	}
	
	protected void postEvent(IEvent event) {
		try {
			IEventBus eventBus = PlatformUtils.getPlatformEventBus();
			eventBus.postEvent(event);
		} catch (RuntimeException e) {
			// Event bus may not be available during shutdown
			getLogger().warn("Event bus is no longer available in the system.");
		} catch (Exception e) {
			// Event bus may not be available during shutdown
			getLogger().error("Failed to post event.", e);
		}
	}
	
	protected Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger("Component Controller (" + getPath() + ")");
		}
		return logger;
	}

	public boolean equals(Object obj) {
		if(obj instanceof IComponentController) {
			return getPath().equals(((IComponentController)obj).getPath());
		}
		return false;
	}

}
