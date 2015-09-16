package org.gumtree.msw.ui.observable;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementPropertyListener;

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
	private static class ObservableValue<TElement extends Element> extends AbstractObservableValue implements IMSWObservableValue, IElementPropertyListener {
		// fields
		private final TElement element;
		private final IDependencyProperty property;
		
		// construction
		public ObservableValue(TElement element, IDependencyProperty property) {
			this.element = element;
			this.property = property;
			
			element.addPropertyListener(this);
		}
		@Override
		public synchronized void dispose() {
			element.removePropertyListener(this);
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
			return element.get(property);
		}
		@Override
		protected void doSetValue(Object newValue) {
			element.set(property, newValue);
		}
		
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty p, Object oldValue, Object newValue) {
			if (property == p)
				fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
	}
	private static class ObservableProxyValue<TElement extends Element> extends AbstractObservableValue implements IMSWObservableValue, IElementPropertyListener {
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
		protected void doSetValue(Object newValue) {
			proxyElement.set(property, newValue);
		}
		
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty p, Object oldValue, Object newValue) {
			if (property == p)
				fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
	}
}
