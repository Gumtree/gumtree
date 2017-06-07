package org.gumtree.msw.ui.observable;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.gumtree.msw.elements.IDependencyProperty;

public interface IMSWObservableValue extends IObservableValue {
	// properties
	public IDependencyProperty getProperty();
	
	// methods
	public boolean validateValue(Object newValue);
}
