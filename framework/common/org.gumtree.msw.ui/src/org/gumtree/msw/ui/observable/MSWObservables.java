package org.gumtree.msw.ui.observable;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;

final public class MSWObservables {
	// methods
	public static <TElement extends Element>
	IMSWObservableValue observe(TElement element, DependencyProperty<TElement, ?> property) {
		return new ObservableValue<TElement>(element, property);
	}
	public static <TElement extends Element>
	IMSWObservableValue observe(ProxyElement<? extends TElement> proxyElement, DependencyProperty<TElement, ?> property) {
		return new ObservableProxyValue<TElement>(proxyElement, property);
	}
	
	// helpers
	private static class ObservableValue<TElement extends Element> extends AbstractObservableValue implements IMSWObservableValue, IElementListener {
		// fields
		private final TElement element;
		private final IDependencyProperty property;
		
		// construction
		public ObservableValue(TElement element, IDependencyProperty property) {
			this.element = element;
			this.property = property;
			
			element.addElementListener(this);
		}
		@Override
		public synchronized void dispose() {
			element.removeElementListener(this);
			super.dispose();
		}

		// properties
		@Override
		public IDependencyProperty getProperty() {
			return property;
		}
		@Override
		public Object getValueType() {
			return property.getPropertyType();
		}
		// protected
		@Override
		protected Object doGetValue() {
			if (element.isValid())
				return element.get(property);
			else
				return null;
		}
		@Override
		public boolean validateValue(Object newValue) {
			if (element.isValid())
				return element.validate(property, newValue);
			else
				return false;
		}
		@Override
		protected void doSetValue(Object newValue) {
			if (element.isValid())
				element.set(property, newValue);
		}
		
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty p, Object oldValue, Object newValue) {
			if (property == p)
				fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
		@Override
		public void onDisposed() {
			// ignore (element.isValid() is always checked when element is accessed)
		}
	}
	private static class ObservableProxyValue<TElement extends Element> extends AbstractObservableValue implements IMSWObservableValue, IElementListener {
		// fields
		private final ProxyElement<? extends TElement> proxyElement;
		private final IDependencyProperty property;
		
		// construction
		public ObservableProxyValue(ProxyElement<? extends TElement> proxyElement, IDependencyProperty property) {
			this.proxyElement = proxyElement;
			this.property = property;
			
			proxyElement.addListener(this);
		}
		@Override
		public synchronized void dispose() {
			proxyElement.removeListener(this);
			super.dispose();
		}

		// properties
		@Override
		public IDependencyProperty getProperty() {
			return property;
		}
		@Override
		public Object getValueType() {
			return property.getPropertyType();
		}
		// protected
		@Override
		protected Object doGetValue() {
			return proxyElement.get(property);
		}
		@Override
		public boolean validateValue(Object newValue) {
			return proxyElement.validate(property, newValue);
		}
		@Override
		protected void doSetValue(Object newValue) {
			proxyElement.set(property, newValue);
		}
		
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty p, Object oldValue, Object newValue) {
			if (property == p)
				fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
		@Override
		public void onDisposed() {
			// ignore (proxy will take care of this)
		}
	}
}
