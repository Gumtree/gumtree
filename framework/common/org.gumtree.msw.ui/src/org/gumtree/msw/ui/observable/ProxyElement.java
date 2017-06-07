package org.gumtree.msw.ui.observable;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;

public class ProxyElement<TElement extends Element> {
	// fields
	private final List<IProxyElementListener<TElement>> listeners;
	private final List<IElementListener> targetListeners;
	private final ElementListener proxyListener;
	private TElement target;
	
	// construction
	public ProxyElement() {
		listeners = new ArrayList<IProxyElementListener<TElement>>();
		targetListeners = new ArrayList<IElementListener>();
		proxyListener = new ElementListener();
		target = null;
	}
	
	// properties
	public boolean hasTarget() {
		return target != null;
	}
	public TElement getTarget() {
		return target;
	}
	public void setTarget(TElement newTarget) {
		if (newTarget == this.target)
			return;
		
		if ((this.target != null) && this.target.isValid())
			this.target.removeElementListener(proxyListener);
	
		TElement oldTarget = this.target;
		this.target = newTarget;
		
		for (IProxyElementListener<TElement> listener : listeners)
			listener.onTargetChange(oldTarget, newTarget);
		
		if (this.target != null) {
			this.target.addElementListener(proxyListener);
			
			for (IDependencyProperty property : newTarget.getProperties()) {
				Object oldValue = oldTarget != null ? oldTarget.get(property) : null;
				Object newValue = newTarget.get(property);
				for (IElementListener listener : targetListeners)
					listener.onChangedProperty(property, oldValue, newValue);
			}
		}
	}
	
	// methods
	public Object get(IDependencyProperty property) {
		if (target == null)
			return null;
		
		return target.get(property);
	}
	public boolean validate(IDependencyProperty property, Object newValue) {
		if (target == null)
			return false;
		
		return target.validate(property, newValue);
	}
	public void set(IDependencyProperty property, Object newValue) {
		if (target == null)
			return;
		
		target.set(property, newValue);
	}

	// listeners
	public void addListener(IProxyElementListener<TElement> listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
		
		listener.onTargetChange(null, target);
	}
	public boolean removeListener(IProxyElementListener<TElement> listener) {
		return listeners.remove(listener);
	}
	public void addListener(IElementListener listener) {
		if (targetListeners.contains(listener))
			throw new Error("listener already exists");
		
		targetListeners.add(listener);
	}
	public boolean removeListener(IElementListener listener) {
		return targetListeners.remove(listener);
	}

	// helper
	private class ElementListener implements IElementListener {
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
			for (IElementListener listener : targetListeners)
				listener.onChangedProperty(property, oldValue, newValue);
		}
		@Override
		public void onDisposed() {
			setTarget(null);
		}
	}
}
