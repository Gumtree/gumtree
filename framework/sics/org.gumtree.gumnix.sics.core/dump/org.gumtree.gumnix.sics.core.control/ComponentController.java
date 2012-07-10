package org.gumtree.gumnix.sics.control;

import java.util.HashSet;
import java.util.Set;

import org.gumtree.gumnix.sics.core.SicsUtils;

import ch.psi.sics.hipadaba.Component;

public abstract class ComponentController implements IComponentController {

	private ComponentStatus status;

	private Set<IComponentListener> listeners;

	private Component component;

	private IComponentController parentController;

	private String path;

	public ComponentController() {
		super();
		setStatus(ComponentStatus.OK);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.IComponentController#getStatus()
	 */
	public ComponentStatus getStatus() {
		return status;
	}

	protected void setStatus(ComponentStatus status) {
		if(this.status != null && this.status.equals(status)) {
			return;
		}
		this.status = status;
		if(getParentController() instanceof ComponentController) {
			((ComponentController)getParentController()).fireChildrenStateChanged();
		}
		Set<IComponentListener> listeners = getListeners();
		// Avoid concurrent access
		synchronized (listeners) {
			for (final IComponentListener listener : listeners) {
				Thread notifyer = new Thread(new Runnable() {
					public void run() {
						listener.componentStatusChanged(getStatus());
					}
				});
				notifyer.run();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.IComponentController#getComponent()
	 */
	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		if(this.component == null) {
			this.component = component;
		} else {
			throw new Error("Component for this controller has already been set.");
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.IComponentController#getPath()
	 */
	public String getPath() {
		if(path == null) {
			path = SicsUtils.getPath(getComponent());
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.IComponentController#getParentController()
	 */
	public IComponentController getParentController() {
		return parentController;
	}

	protected void setParentController(IComponentController parentController) {
		if(this.parentController == null) {
			this.parentController = parentController;
		} else {
			throw new Error("Parent controller has already been set.");
		}
	}

	protected abstract void fireChildrenStateChanged();

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.control.IComponentController#addComponentListener(org.gumtree.gumnix.sics.control.IComponentListener)
	 */
	public void addComponentListener(IComponentListener listener) {
		Set<IComponentListener> listeners = getListeners();
		synchronized (listeners) {
			getListeners().add(listener);
		}
	}

	public void removeComponentListener(IComponentListener listener) {
		Set<IComponentListener> listeners = getListeners();
		synchronized (listeners) {
			getListeners().remove(listener);
		}
	}

	protected Set<IComponentListener> getListeners() {
		if(listeners == null) {
			listeners = new HashSet<IComponentListener>();
		}
		return listeners;
	}

}
