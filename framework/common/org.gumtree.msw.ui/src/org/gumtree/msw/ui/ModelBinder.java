package org.gumtree.msw.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.ui.observable.IMSWObservableValue;
import org.gumtree.msw.ui.observable.IProxyElementListener;
import org.gumtree.msw.ui.observable.MSWObservables;
import org.gumtree.msw.ui.observable.ProxyElement;

public final class ModelBinder {
	// finals
	private static final String[] EMPTY_STRING_ARRAY = {};
	private static Map<Control, EnabledState> enabledBindings = new HashMap<>();

	// enabled binding
	public static <TElement extends Element>
	void createEnabledBinding(DataBindingContext bindingContext, Control control, TElement element, DependencyProperty<TElement, Boolean> property) {
		createEnabledBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialBooleanValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	void createEnabledBinding(DataBindingContext bindingContext, Control control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, Boolean> converter) {
		createEnabledBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter);
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	void createEnabledBinding(DataBindingContext bindingContext, Control control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, Boolean> property) {
		createEnabledBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialBooleanValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	void createEnabledBinding(DataBindingContext bindingContext, final Control control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, Boolean> converter) {
		createEnabledBinding(
				bindingContext,
				control,
				MSWObservables.observe(proxyElement, property),
				converter);
		
		appendEnabledStateForProxy(control, proxyElement);
	}
	private static <TModel>
	void createEnabledBinding(DataBindingContext bindingContext, final Control control, final IMSWObservableValue observable, final IModelValueConverter<TModel, Boolean> converter) {
		final EnabledState adapter = new EnabledState(new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				control.setEnabled(state);
			}
		});
		adapter.setNext(enabledBindings.put(control, adapter));
		
		IChangeListener changeListener = new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				boolean value;
				try {
					value = converter.fromModelValue(converter.getModelValueType().cast(observable.getValue()));
				}
				catch (Exception e) {
					value = TrivialBooleanValueConverter.DEFAULT_VALUE;
				}
				adapter.setEnabled(value);
				enqueueEnabledBindingUpdate(control);
			}
		};

		// evaluate converter  
		changeListener.handleChange(null);
		observable.addChangeListener(changeListener);
	}
	public static <TElement extends Element>
	void createEnabledBinding(final Control control, ProxyElement<TElement> proxyElement) {
		appendEnabledStateForProxy(control, proxyElement, new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				control.setEnabled(state);
			}
		});
	}
	
	// text binding
	public static <TElement extends Element>
	void createTextBinding(DataBindingContext bindingContext, Text control, TElement element, DependencyProperty<TElement, String> property) {
		createTextBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	void createTextBinding(DataBindingContext bindingContext, Text control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		createTextBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter);
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	void createTextBinding(DataBindingContext bindingContext, Text control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, String> property) {
		createTextBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	void createTextBinding(DataBindingContext bindingContext, final Text control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		createTextBinding(
				bindingContext,
				control,
				MSWObservables.observe(proxyElement, property),
				converter);

		appendEnabledStateForProxy(control, proxyElement);
	}
	private static <TModel>
	void createTextBinding(DataBindingContext bindingContext, final Text control, final IMSWObservableValue observable, final IModelValueConverter<TModel, String> converter) {
		control.addListener(SWT.Traverse, new Listener() {
	        @Override
	        public void handleEvent(Event event) {
	        	switch (event.detail) {
	        	case SWT.TRAVERSE_RETURN:
	        		try {
		        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
		        		
		        		String newValue = control.getText();
		        		String oldValue = converter.fromModelValue(oldModelValue);
		        		if (!Objects.equals(newValue, oldValue)) {
			        		control.setText(oldValue);
							observable.setValue(converter.toModelValue(newValue));
		        		}
					}
	        		catch (Exception e) {
					}
	        		break;
	        		
	        	case SWT.TRAVERSE_ESCAPE:
	        		try {
		        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
		        		
		        		control.setText(converter.fromModelValue(oldModelValue));
					}
	        		catch (Exception e) {
					}
	        		break;
	        	}
	        }
	    });
		control.addListener(SWT.FocusOut, new Listener() {
	        @Override
	        public void handleEvent(Event event) {
        		try {
	        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
	        		
	        		String newValue = control.getText();
	        		String oldValue = converter.fromModelValue(oldModelValue);
	        		if (!Objects.equals(newValue, oldValue)) {
		        		control.setText(oldValue);
						observable.setValue(converter.toModelValue(newValue));
	        		}
				}
        		catch (Exception e) {
				}
	        }
	    });
		control.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				if (!control.isEnabled()) {
					if (!TrivialStringValueConverter.DEFAULT_VALUE.equals(control.getText()))
						control.setText(TrivialStringValueConverter.DEFAULT_VALUE);
					return;
				}
				try {
	        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
	        		
	        		String newValue = control.getText();
	        		String oldValue = converter.fromModelValue(oldModelValue);
					if (Objects.equals(newValue, oldValue))
						control.setBackground(Resources.COLOR_DEFAULT);
					else {
						// check if value can be parsed
						converter.toModelValue(newValue);
						control.setBackground(Resources.COLOR_EDITING);
					}
				}
				catch (Exception e) {
					control.setBackground(Resources.COLOR_ERROR);
				}
			}
		});
		
		IDependencyProperty property = observable.getProperty();
		Class<?> propertyType = property.getPropertyType();
		
		UpdateValueStrategy targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER);
		targetToModel.setConverter(new PolicyNeverConverter(String.class, propertyType));
		
		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		modelToTarget.setConverter(new ModelToTargetConverter<TModel, String>(
				converter,
				TrivialStringValueConverter.DEFAULT_VALUE));
		
		final Binding binding = bindingContext.bindValue(
				SWTObservables.observeText(control, SWT.NONE),
				observable,
				targetToModel,
				modelToTarget);
		
		appendEnabledStateListener(control, new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				if (state) {
					control.setEnabled(true);
					binding.updateModelToTarget(); // will also update background color
				}
				else {
					control.setText(TrivialStringValueConverter.DEFAULT_VALUE);
					control.setBackground(Resources.COLOR_DISABLED);
					control.setEnabled(false);
				}
			}
		});
	}

	// multi line
	public static <TElement extends Element>
	void createMultiLineBinding(DataBindingContext bindingContext, Text control, TElement element, DependencyProperty<TElement, String> property) {
		createMultiLineBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	void createMultiLineBinding(DataBindingContext bindingContext, Text control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		createMultiLineBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter);
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	void createMultiLineBinding(DataBindingContext bindingContext, Text control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, String> property) {
		createMultiLineBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	void createMultiLineBinding(DataBindingContext bindingContext, final Text control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		createMultiLineBinding(
				bindingContext,
				control,
				MSWObservables.observe(proxyElement, property),
				converter);

		appendEnabledStateForProxy(control, proxyElement);
	}
	private static <TModel>
	void createMultiLineBinding(DataBindingContext bindingContext, final Text control, final IMSWObservableValue observable, final IModelValueConverter<TModel, String> converter) {
		control.addListener(SWT.Traverse, new Listener() {
	        @Override
	        public void handleEvent(Event event) {
	        	switch (event.detail) {	        		
	        	case SWT.TRAVERSE_ESCAPE:
	        		try {
		        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
		        		
		        		control.setText(converter.fromModelValue(oldModelValue));
					}
	        		catch (Exception e) {
					}
	        		break;
	        	}
	        }
	    });
		control.addListener(SWT.FocusOut, new Listener() {
	        @Override
	        public void handleEvent(Event event) {
        		try {
	        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
	        		
	        		String newValue = control.getText();
	        		String oldValue = converter.fromModelValue(oldModelValue);
	        		if (!Objects.equals(newValue, oldValue)) {
		        		control.setText(oldValue);
						observable.setValue(converter.toModelValue(newValue));
	        		}
				}
        		catch (Exception e) {
				}
	        }
	    });
		control.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				if (!control.isEnabled()) {
					if (!TrivialStringValueConverter.DEFAULT_VALUE.equals(control.getText()))
						control.setText(TrivialStringValueConverter.DEFAULT_VALUE);
					return;
				}
				try {
	        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
	        		
	        		String newValue = control.getText();
	        		String oldValue = converter.fromModelValue(oldModelValue);
					if (Objects.equals(newValue, oldValue))
						control.setBackground(Resources.COLOR_DEFAULT);
					else {
						// check if value can be parsed
						converter.toModelValue(newValue);
						control.setBackground(Resources.COLOR_EDITING);
					}
				}
				catch (Exception e) {
					control.setBackground(Resources.COLOR_ERROR);
				}
			}
		});
		
		IDependencyProperty property = observable.getProperty();
		Class<?> propertyType = property.getPropertyType();
		
		UpdateValueStrategy targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER);
		targetToModel.setConverter(new PolicyNeverConverter(String.class, propertyType));
		
		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		modelToTarget.setConverter(new ModelToTargetConverter<TModel, String>(
				converter,
				TrivialStringValueConverter.DEFAULT_VALUE));
		
		final Binding binding = bindingContext.bindValue(
				SWTObservables.observeText(control, SWT.NONE),
				observable,
				targetToModel,
				modelToTarget);
		
		appendEnabledStateListener(control, new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				if (state) {
					control.setEnabled(true);
					binding.updateModelToTarget(); // will also update background color
				}
				else {
					control.setText(TrivialStringValueConverter.DEFAULT_VALUE);
					control.setBackground(Resources.COLOR_DISABLED);
					control.setEnabled(false);
				}
			}
		});
	}
	
	// checked binding
	public static <TElement extends Element>
	void createCheckedBinding(DataBindingContext bindingContext, Button control, TElement element, DependencyProperty<TElement, Boolean> property) {
		createCheckedBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialBooleanValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	void createCheckedBinding(DataBindingContext bindingContext, Button control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, Boolean> converter) {
		createCheckedBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter);
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	void createCheckedBinding(DataBindingContext bindingContext, Button control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, Boolean> property) {
		createCheckedBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialBooleanValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	void createCheckedBinding(DataBindingContext bindingContext, final Button control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, Boolean> converter) {
		createCheckedBinding(
				bindingContext,
				control,
				MSWObservables.observe(proxyElement, property),
				converter);
		
		appendEnabledStateForProxy(control, proxyElement);
	}
	private static <TModel>
	void createCheckedBinding(DataBindingContext bindingContext, final Button control, final IMSWObservableValue observable, final IModelValueConverter<TModel, Boolean> converter) {
		IDependencyProperty property = observable.getProperty();
		final Class<?> propertyType = property.getPropertyType();
		
		UpdateValueStrategy targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);		
		targetToModel.setConverter(new IConverter() {
			@Override
			public Object getFromType() {
				return Boolean.class;
			}
			@Override
			public Object getToType() {
				return propertyType;
			}
			@Override
			public Object convert(Object fromObject) {
				if (fromObject instanceof Boolean)
	        		try {
						return converter.toModelValue((Boolean)fromObject);
					}
					catch (Exception e) {
					}
				
				return null;
			}
		});

		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		modelToTarget.setConverter(new IConverter() {
			@Override
			public Object getFromType() {
				return propertyType;
			}
			@Override
			public Object getToType() {
				return Boolean.class;
			}
			@Override
			public Object convert(Object fromObject) {
				try {
					return converter.fromModelValue(converter.getModelValueType().cast(fromObject));
				}
				catch (Exception e) {
					return TrivialBooleanValueConverter.DEFAULT_VALUE;
				}
			}
		});
		
		final Binding binding = bindingContext.bindValue(
				SWTObservables.observeSelection(control),
				observable,
				targetToModel,
				modelToTarget);
		
		appendEnabledStateListener(control, new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				if (state) {
					control.setEnabled(true);
					binding.updateModelToTarget();			
				}
				else {
					control.setSelection(false);
					control.setEnabled(false);
				}
			}
		});
	}
	
	// combo binding
	public static <TElement extends Element>
	void createComboBinding(DataBindingContext bindingContext, Combo control, TElement element, DependencyProperty<TElement, String> property) {
		createComboBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	void createComboBinding(DataBindingContext bindingContext, Combo control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		createComboBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter);
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	void createComboBinding(DataBindingContext bindingContext, Combo control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, String> property) {
		createComboBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	void createComboBinding(DataBindingContext bindingContext, final Combo control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		createComboBinding(
				bindingContext,
				control,
				MSWObservables.observe(proxyElement, property),
				converter);

		appendEnabledStateForProxy(control, proxyElement);
	}
	private static <TModel>
	void createComboBinding(DataBindingContext bindingContext, final Combo control, final IMSWObservableValue observable, final IModelValueConverter<TModel, String> converter) {
		IDependencyProperty property = observable.getProperty();
		final Class<?> propertyType = property.getPropertyType();
		
		UpdateValueStrategy targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);		
		targetToModel.setConverter(new IConverter() {
			@Override
			public Object getFromType() {
				return String.class;
			}
			@Override
			public Object getToType() {
				return propertyType;
			}
			@Override
			public Object convert(Object fromObject) {
				if (fromObject instanceof String)
	        		try {
						return converter.toModelValue((String)fromObject);
					}
					catch (Exception e) {
					}
				
				return null;
			}
		});

		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		modelToTarget.setConverter(new IConverter() {
			@Override
			public Object getFromType() {
				return propertyType;
			}
			@Override
			public Object getToType() {
				return String.class;
			}
			@Override
			public Object convert(Object fromObject) {
				try {
					return converter.fromModelValue(converter.getModelValueType().cast(fromObject));
				}
				catch (Exception e) {
					return TrivialStringValueConverter.DEFAULT_VALUE;
				}
			}
		});
		
		final Binding binding = bindingContext.bindValue(
				SWTObservables.observeSelection(control),
				observable,
				targetToModel,
				modelToTarget);
		
		appendEnabledStateListener(control, new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				if (state) {
					control.setEnabled(true);
					binding.updateModelToTarget();
				}
				else {
					control.setEnabled(true);
					
					// clear selection (workaround for READ_ONLY)
					if (control.getItemCount() != 0) {
						String[] items = control.getItems();
						control.setItems(EMPTY_STRING_ARRAY);
						control.setText(TrivialStringValueConverter.DEFAULT_VALUE);
						control.setItems(items);
					}
					else
						control.setText(TrivialStringValueConverter.DEFAULT_VALUE);
						
					control.setEnabled(false);
				}
			}
		});
	}
	
	// helpers
	private static <TElement extends Element>
	void appendEnabledStateForProxy(final Control control, ProxyElement<TElement> proxyElement) {
		appendEnabledStateForProxy(control, proxyElement, null);
	}
	private static <TElement extends Element>
	void appendEnabledStateForProxy(final Control control, ProxyElement<TElement> proxyElement, IEnabledStateListener listener) {
		final EnabledState adapter = new EnabledState(listener);
		adapter.setNext(enabledBindings.put(control, adapter));
		adapter.setEnabled(proxyElement.hasTarget());
		enqueueEnabledBindingUpdate(control);
		
		proxyElement.addListener(new IProxyElementListener<TElement>() {
			@Override
			public void onTargetChange(TElement oldTarget, TElement newTarget) {
				adapter.setEnabled(newTarget != null);
				enqueueEnabledBindingUpdate(control);
			}
		});	
	}
	private static void appendEnabledStateListener(Control control, IEnabledStateListener listener) {
		final EnabledState adapter = new EnabledState(listener);
		adapter.setNext(enabledBindings.put(control, adapter));
		adapter.setEnabled(true);
		enqueueEnabledBindingUpdate(control);
	}
	private static void enqueueEnabledBindingUpdate(final Control control) {
		// bindings are called in the ui-thread but to avoid quick state changes
		// update target controls at the end when the final state is known
		control.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// "onStateChanged" is called when the state has actually changed
				enabledBindings.get(control).update(); 
			}
		});
	}
	
	// IConverter
	private static class PolicyNeverConverter implements IConverter {
		// fields
		private final Class<?> fromType;
		private final Class<?> toType;
		
		// construction
		public PolicyNeverConverter(Class<?> fromType, Class<?> toType) {
			this.fromType = fromType;
			this.toType = toType;
		}

		// properties
		@Override
		public Object getFromType() {
			return fromType;
		}
		@Override
		public Object getToType() {
			return toType;
		}
		@Override
		public Object convert(Object fromObject) {
			return null; // not supported (POLICY_NEVER is used)
		}
	}
	private static class ModelToTargetConverter<TModel, TTarget> implements IConverter {
		// fields
		private final IModelValueConverter<TModel, TTarget> converter;
		private final TTarget defaultValue;
		
		// construction
		public ModelToTargetConverter(IModelValueConverter<TModel, TTarget> converter, TTarget defaultValue) {
			this.converter = converter;
			this.defaultValue = defaultValue;
		}
		
		// methods
		@Override
		public Object getFromType() {
			return converter.getModelValueType();
		}
		@Override
		public Object getToType() {
			return converter.getTargetValueType();
		}
		@Override
		public Object convert(Object fromObject) {
			try {
				return converter.fromModelValue(
						converter.getModelValueType().cast(fromObject));
			}
			catch (Exception e) {
				return defaultValue;
			}
		}
	}
	
	// IModelValueConverter
	private static class TrivialBooleanValueConverter implements IModelValueConverter<Boolean, Boolean> {
		// finals
		public static final TrivialBooleanValueConverter DEFAULT = new TrivialBooleanValueConverter();
		public static final Boolean DEFAULT_VALUE = Boolean.FALSE;

		// properties
		@Override
		public Class<Boolean> getModelValueType() {
			return Boolean.class;
		}
		@Override
		public Class<Boolean> getTargetValueType() {
			return Boolean.class;
		}
		
		// methods
		@Override
		public Boolean fromModelValue(Boolean value) {
			return value;
		}
		@Override
		public Boolean toModelValue(Boolean value) {
			return value;
		}
	}
	private static class TrivialStringValueConverter implements IModelValueConverter<String, String> {
		// finals
		public static final TrivialStringValueConverter DEFAULT = new TrivialStringValueConverter();
		public static final String DEFAULT_VALUE = "";

		// properties
		@Override
		public Class<String> getModelValueType() {
			return String.class;
		}
		@Override
		public Class<String> getTargetValueType() {
			return String.class;
		}
		
		// methods
		@Override
		public String fromModelValue(String value) {
			if (value != null)
				return value;

			return DEFAULT_VALUE;
		}
		@Override
		public String toModelValue(String value) {
			if (value != null)
				return value;

			return DEFAULT_VALUE;
		}
	}
	
	// EnabledState
	private static class EnabledState {
		// fields
		private final IEnabledStateListener listener;
		// chaining
		private boolean enabled;
		private Boolean lastState;
		private EnabledState next;
		
		// construction
		public EnabledState(IEnabledStateListener listener) {
			this.listener = listener; // may be null

			enabled = false;
			lastState = null;
			next = null;
		}
		
		// properties
		public void setEnabled(boolean value) {
			enabled = value;
		}
		public void setNext(EnabledState value) {
			next = value;
		}
		public boolean getState() {
			return enabled && ((next == null) || next.getState());
		}
		
		// methods
		public void update() {
			boolean newState = getState();
			if (!Objects.equals(lastState, newState)) {
				lastState = newState;
				onStateChanged(newState);
			}
		}
		private void onStateChanged(boolean state) {
			if (listener != null)
				listener.onStateChanged(state);
			if (next != null)
				next.onStateChanged(state);
		}
	}
	private static interface IEnabledStateListener {
		// methods
		public void onStateChanged(boolean state);
	}
}
