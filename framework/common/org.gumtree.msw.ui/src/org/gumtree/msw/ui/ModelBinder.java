package org.gumtree.msw.ui;

import java.util.Objects;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
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
import org.gumtree.msw.ui.observable.IMSWObservableValue;
import org.gumtree.msw.ui.observable.IProxyElementListener;
import org.gumtree.msw.ui.observable.MSWObservables;
import org.gumtree.msw.ui.observable.ProxyElement;

public final class ModelBinder {
	// finals
	private static final String[] EMPTY_STRING_ARRAY = {};
	private static final String ENABLE_STATE_ID = "org.gumtree.msw.ui.ModelBinder.EnableStateId";

	// enabled binding
	public static <TElement extends Element>
	IModelBinding createEnabledBinding(DataBindingContext bindingContext, Control control, TElement element, DependencyProperty<TElement, Boolean> property) {
		return createEnabledBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialBooleanValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	IModelBinding createEnabledBinding(DataBindingContext bindingContext, Control control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, Boolean> converter) {
		return createEnabledBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter);
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	IModelBinding createEnabledBinding(DataBindingContext bindingContext, Control control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, Boolean> property) {
		return createEnabledBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialBooleanValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	IModelBinding createEnabledBinding(DataBindingContext bindingContext, final Control control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, Boolean> converter) {
		return new ComposedModelBinding(
			createEnabledBinding(
					bindingContext,
					control,
					MSWObservables.observe(proxyElement, property),
					converter),			
			appendEnabledStateForProxy(control, proxyElement));
	}
	private static <TModel>
	IModelBinding createEnabledBinding(DataBindingContext bindingContext, final Control control, final IMSWObservableValue observable, final IModelValueConverter<TModel, Boolean> converter) {
		final EnabledState newAdapter = new EnabledState(new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				control.setEnabled(state);
			}
		});

		Object oldAdapter = control.getData(ENABLE_STATE_ID);
		if (oldAdapter instanceof EnabledState)
			newAdapter.setNext((EnabledState)oldAdapter);
		
		control.setData(ENABLE_STATE_ID, newAdapter);
		
		final IChangeListener changeListener = new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				boolean value;
				try {
					value = converter.fromModelValue(converter.getModelValueType().cast(observable.getValue()));
				}
				catch (Exception e) {
					value = TrivialBooleanValueConverter.DEFAULT_VALUE;
				}
				newAdapter.setEnabled(value);
				enqueueEnabledBindingUpdate(control);
			}
		};

		// evaluate converter  
		changeListener.handleChange(null);
		observable.addChangeListener(changeListener);
		
		return new IModelBinding() {
			@Override
			public void dispose() {
				removeEnabledState(control, newAdapter);
				
				observable.dispose();
			}
		};
	}
	public static <TElement extends Element>
	IModelBinding createEnabledBinding(final Control control, ProxyElement<TElement> proxyElement) {
		return appendEnabledStateForProxy(control, proxyElement, new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				control.setEnabled(state);
			}
		});
	}
	
	// text binding
	public static <TElement extends Element>
	IModelBinding createTextBinding(DataBindingContext bindingContext, Text control, TElement element, DependencyProperty<TElement, String> property) {
		return createTextBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	IModelBinding createTextBinding(DataBindingContext bindingContext, Text control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		return createTextBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter,
				true); // react to traverse-return
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	IModelBinding createTextBinding(DataBindingContext bindingContext, Text control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, String> property) {
		return createTextBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	IModelBinding createTextBinding(DataBindingContext bindingContext, final Text control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		return new ComposedModelBinding(
				createTextBinding(
						bindingContext,
						control,
						MSWObservables.observe(proxyElement, property),
						converter,
						true), // react to traverse-return
				appendEnabledStateForProxy(control, proxyElement));
	}
	private static <TModel>
	IModelBinding createTextBinding(DataBindingContext bindingContext, final Text control, final IMSWObservableValue observable, final IModelValueConverter<TModel, String> converter, final boolean handleTraverseReturn) {
		ITextWrapper textControl = new ITextWrapper() {
			@Override
			public Control getControl() {
				return control;
			}
			@Override
			public boolean isEnabled() {
				return control.isEnabled();
			}
			@Override
			public void setEnabled(boolean value) {
				if (value) {
					control.setEnabled(true);
				}
				else {
					control.setText(TrivialStringValueConverter.DEFAULT_VALUE);
					control.setBackground(Resources.COLOR_DISABLED);
					control.setEnabled(false);
				}
			}
			@Override
			public String getText() {
				return control.getText();
			}
			@Override
			public void setText(String value) {
				control.setText(value);
			}
			@Override
			public void addModifyListener(ModifyListener listener) {
				control.addModifyListener(listener);
			}
			@Override
			public void removeModifyListener(ModifyListener listener) {
				control.removeModifyListener(listener);
			}
		};
		
		return createTextBinding(bindingContext, textControl, observable, converter, handleTraverseReturn, false); // don't update immediately
	}

	// combo binding
	public static <TElement extends Element>
	IModelBinding createComboBinding(DataBindingContext bindingContext, Combo control, TElement element, DependencyProperty<TElement, String> property) {
		return createComboBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	IModelBinding createComboBinding(DataBindingContext bindingContext, Combo control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		return createComboBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter);
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	IModelBinding createComboBinding(DataBindingContext bindingContext, Combo control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, String> property) {
		return createComboBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	IModelBinding createComboBinding(DataBindingContext bindingContext, final Combo control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		return new ComposedModelBinding(
				createComboBinding(
						bindingContext,
						control,
						MSWObservables.observe(proxyElement, property),
						converter),
				appendEnabledStateForProxy(control, proxyElement));
	}
	private static <TModel>
	IModelBinding createComboBinding(DataBindingContext bindingContext, final Combo control, final IMSWObservableValue observable, final IModelValueConverter<TModel, String> converter) {
		ITextWrapper textControl = new ITextWrapper() {
			@Override
			public Control getControl() {
				return control;
			}
			@Override
			public boolean isEnabled() {
				return control.isEnabled();
			}
			@Override
			public void setEnabled(boolean value) {
				if (value) {
					control.setEnabled(true);
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
			@Override
			public String getText() {
				return control.getText();
			}
			@Override
			public void setText(String value) {
				control.setText(value);
			}
			@Override
			public void addModifyListener(ModifyListener listener) {
				control.addModifyListener(listener);
			}
			@Override
			public void removeModifyListener(ModifyListener listener) {
				control.removeModifyListener(listener);
			}
		};
		
		boolean updateImmediately = (control.getStyle() & SWT.READ_ONLY) == SWT.READ_ONLY;
		return createTextBinding(bindingContext, textControl, observable, converter, true, updateImmediately); // handleTraverseReturn
	}
	
	// multi line
	public static <TElement extends Element>
	IModelBinding createMultiLineBinding(DataBindingContext bindingContext, Text control, TElement element, DependencyProperty<TElement, String> property) {
		return createMultiLineBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	IModelBinding createMultiLineBinding(DataBindingContext bindingContext, Text control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		return createTextBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter,
				false); // ignore traverse-return
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	IModelBinding createMultiLineBinding(DataBindingContext bindingContext, Text control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, String> property) {
		return createMultiLineBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialStringValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	IModelBinding createMultiLineBinding(DataBindingContext bindingContext, final Text control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, String> converter) {
		return new ComposedModelBinding(
				createTextBinding(
						bindingContext,
						control,
						MSWObservables.observe(proxyElement, property),
						converter,
						false), // ignore traverse-return
				appendEnabledStateForProxy(control, proxyElement));
	}
	
	// checked binding
	public static <TElement extends Element>
	IModelBinding createCheckedBinding(DataBindingContext bindingContext, Button control, TElement element, DependencyProperty<TElement, Boolean> property) {
		return createCheckedBinding(
				bindingContext,
				control,
				element,
				property,
				TrivialBooleanValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TModel>
	IModelBinding createCheckedBinding(DataBindingContext bindingContext, Button control, TElement element, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, Boolean> converter) {
		return createCheckedBinding(
				bindingContext,
				control,
				MSWObservables.observe(element, property),
				converter);
	}
	public static <TElement extends Element, TProxyElement extends TElement>
	IModelBinding createCheckedBinding(DataBindingContext bindingContext, Button control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, Boolean> property) {
		return createCheckedBinding(
				bindingContext,
				control,
				proxyElement,
				property,
				TrivialBooleanValueConverter.DEFAULT);
	}
	public static <TElement extends Element, TProxyElement extends TElement, TModel>
	IModelBinding createCheckedBinding(DataBindingContext bindingContext, final Button control, ProxyElement<TProxyElement> proxyElement, DependencyProperty<TElement, TModel> property, IModelValueConverter<TModel, Boolean> converter) {
		return new ComposedModelBinding(
				createCheckedBinding(
						bindingContext,
						control,
						MSWObservables.observe(proxyElement, property),
						converter),
				appendEnabledStateForProxy(control, proxyElement));
	}
	private static <TModel>
	IModelBinding createCheckedBinding(DataBindingContext bindingContext, final Button control, final IMSWObservableValue observable, final IModelValueConverter<TModel, Boolean> converter) {
		final Class<?> propertyType = observable.getProperty().getPropertyType();
		
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
		
		final ISWTObservableValue targetObservable = SWTObservables.observeSelection(control);
		final Binding binding = bindingContext.bindValue(
				targetObservable,
				observable,
				targetToModel,
				modelToTarget);
		
		final IModelBinding enabledStateBinding = appendEnabledStateListener(control, new IEnabledStateListener() {
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
		
		return new IModelBinding() {
			@Override
			public void dispose() {
				enabledStateBinding.dispose();
				binding.dispose();
				
				targetObservable.dispose();
				observable.dispose();
			}
		};
	}
	
	// wrapped text binding
	private static interface ITextWrapper {
		// properties
		Control getControl();
		// enabled
		boolean isEnabled();
		void setEnabled(boolean value);
		// text
		String getText();
		void setText(String value);
		
		// Modify Listener
		void addModifyListener(ModifyListener listener);
		void removeModifyListener(ModifyListener listener);
	}

	private static <TModel>
	IModelBinding createTextBinding(DataBindingContext bindingContext, final ITextWrapper wrapper, final IMSWObservableValue observable, final IModelValueConverter<TModel, String> converter, final boolean handleTraverseReturn, final boolean updateImmediately) {
		final Listener traverseListener = new Listener() {
	        @Override
	        public void handleEvent(Event event) {
	        	switch (event.detail) {
	        	case SWT.TRAVERSE_RETURN:
	        		if (handleTraverseReturn)
		        		try {
			        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
			        		
			        		String newValue = wrapper.getText();
			        		String oldValue = converter.fromModelValue(oldModelValue);
			        		if (!Objects.equals(newValue, oldValue)) {
				        		wrapper.setText(oldValue);
								observable.setValue(converter.toModelValue(newValue));
			        		}
						}
		        		catch (Exception e) {
						}
	        		break;
	        		
	        	case SWT.TRAVERSE_ESCAPE:
	        		try {
		        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
		        		
		        		wrapper.setText(converter.fromModelValue(oldModelValue));
					}
	        		catch (Exception e) {
					}
	        		break;
	        	}
	        }
	    };
	    final Listener focusOutListener = new Listener() {
	        @Override
	        public void handleEvent(Event event) {
        		try {
	        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
	        		
	        		String newValue = wrapper.getText();
	        		String oldValue = converter.fromModelValue(oldModelValue);
	        		if (!Objects.equals(newValue, oldValue)) {
		        		wrapper.setText(oldValue);
						observable.setValue(converter.toModelValue(newValue));
	        		}
				}
        		catch (Exception e) {
        			e.printStackTrace();
				}
	        }
	    };
	    final ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				if (!wrapper.isEnabled()) {
					if (!TrivialStringValueConverter.DEFAULT_VALUE.equals(wrapper.getText()))
						wrapper.setText(TrivialStringValueConverter.DEFAULT_VALUE);
					return;
				}
				if (updateImmediately)
					try {
		        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
		        		
		        		String newValue = wrapper.getText();
		        		String oldValue = converter.fromModelValue(oldModelValue);
		        		if (!Objects.equals(newValue, oldValue)) {
			        		wrapper.setText(oldValue);
							observable.setValue(converter.toModelValue(newValue));
		        		}
					}
					catch (Exception e) {
					}
				else
					try {
		        		TModel oldModelValue = converter.getModelValueType().cast(observable.getValue());
		        		
		        		String newValue = wrapper.getText();
		        		String oldValue = converter.fromModelValue(oldModelValue);
						if (Objects.equals(newValue, oldValue))
							wrapper.getControl().setBackground(Resources.COLOR_DEFAULT);
						else if (observable.validateValue(converter.toModelValue(newValue)))
							wrapper.getControl().setBackground(Resources.COLOR_EDITING);
						else
							wrapper.getControl().setBackground(Resources.COLOR_ERROR);
					}
					catch (Exception e) {
						wrapper.getControl().setBackground(Resources.COLOR_ERROR);
					}
			}
		};

		wrapper.getControl().addListener(SWT.Traverse, traverseListener);
		wrapper.getControl().addListener(SWT.FocusOut, focusOutListener);
		wrapper.addModifyListener(modifyListener);

		Class<?> propertyType = observable.getProperty().getPropertyType();
		
		UpdateValueStrategy targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER);
		targetToModel.setConverter(new PolicyNeverConverter(
				String.class,
				propertyType));
		
		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		modelToTarget.setConverter(new ModelToTargetConverter<TModel, String>(
				converter,
				TrivialStringValueConverter.DEFAULT_VALUE));
				
		final ISWTObservableValue targetObservable = SWTObservables.observeText(wrapper.getControl());
		final Binding binding = bindingContext.bindValue(
				targetObservable,
				observable,
				targetToModel,
				modelToTarget);
		
		final IModelBinding enabledStateBinding = appendEnabledStateListener(wrapper.getControl(), new IEnabledStateListener() {
			@Override
			public void onStateChanged(boolean state) {
				wrapper.setEnabled(state);
				if (state)
					binding.updateModelToTarget(); // will also update background color
			}
		});
		
		return new IModelBinding() {
			@Override
			public void dispose() {
				wrapper.getControl().removeListener(SWT.Traverse, traverseListener);
				wrapper.getControl().removeListener(SWT.FocusOut, focusOutListener);
				wrapper.removeModifyListener(modifyListener);
				
				enabledStateBinding.dispose();
				binding.dispose();
				
				targetObservable.dispose();
				observable.dispose();
			}
		};
	}
	
	// helpers
	private static <TElement extends Element>
	IModelBinding appendEnabledStateForProxy(final Control control, ProxyElement<TElement> proxyElement) {
		return appendEnabledStateForProxy(control, proxyElement, null);
	}
	private static <TElement extends Element>
	IModelBinding appendEnabledStateForProxy(final Control control, final ProxyElement<TElement> proxyElement, IEnabledStateListener listener) {
		try {
			final EnabledState newAdapter = new EnabledState(listener);

			Object oldAdapter = control.getData(ENABLE_STATE_ID);
			if (oldAdapter instanceof EnabledState)
				newAdapter.setNext((EnabledState)oldAdapter);
			
			newAdapter.setEnabled(proxyElement.hasTarget());
			control.setData(ENABLE_STATE_ID, newAdapter);
			
			final IProxyElementListener<TElement> proxyListener = new IProxyElementListener<TElement>() {
				@Override
				public void onTargetChange(TElement oldTarget, TElement newTarget) {
					newAdapter.setEnabled(newTarget != null);
					enqueueEnabledBindingUpdate(control);
				}
			};
			proxyElement.addListener(proxyListener);

			return new IModelBinding() {
				@Override
				public void dispose() {
					proxyElement.removeListener(proxyListener);
					removeEnabledState(control, newAdapter);
				}
			};
		}
		finally {
			enqueueEnabledBindingUpdate(control);
		}
	}
	private static
	IModelBinding appendEnabledStateListener(final Control control, IEnabledStateListener listener) {
		try {
			final EnabledState newAdapter = new EnabledState(listener);

			Object oldAdapter = control.getData(ENABLE_STATE_ID);
			if (oldAdapter instanceof EnabledState)
				newAdapter.setNext((EnabledState)oldAdapter);

			newAdapter.setEnabled(true);
			control.setData(ENABLE_STATE_ID, newAdapter);
			
			return new IModelBinding() {
				@Override
				public void dispose() {
					removeEnabledState(control, newAdapter);
				}
			};
		}
		finally {
			enqueueEnabledBindingUpdate(control);
		}
	}
	private static void removeEnabledState(final Control control, final EnabledState targetAdapter) {
		targetAdapter.dispose();
		Object adapter = control.getData(ENABLE_STATE_ID);
		if (adapter instanceof EnabledState) {
			if (adapter == targetAdapter) {
				control.setData(ENABLE_STATE_ID, ((EnabledState)adapter).getNext());
			}
			else {
				EnabledState headAdapter, nextAdapter = (EnabledState)adapter;
				do {
					headAdapter = nextAdapter;
					nextAdapter = headAdapter.getNext();
					
					if (nextAdapter == targetAdapter) {
						headAdapter.setNext(nextAdapter.getNext());
						break;
					}
				} while (nextAdapter != null);
			}
		}
	}
	private static void enqueueEnabledBindingUpdate(final Control control) {
		// bindings are called in the ui-thread but to avoid quick state changes
		// update target controls at the end when the final state is known
		control.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// "onStateChanged" is called when the state has actually changed
				Object enabledState = control.getData(ENABLE_STATE_ID);
				if (enabledState instanceof EnabledState)
					((EnabledState)enabledState).update();
			}
		});
	}
	
	// ModelBinding
	private static class ComposedModelBinding implements IModelBinding {
		// fields
		private final IModelBinding[] bindings;
		
		// construction
		public ComposedModelBinding(IModelBinding... bindings) {
			this.bindings = bindings;
		}
		
		// methods
		@Override
		public void dispose() {
			for (IModelBinding binding : bindings)
				binding.dispose();
		}
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
			if (value != null)
				return value;

			return DEFAULT_VALUE;
		}
		@Override
		public Boolean toModelValue(Boolean value) {
			if (value != null)
				return value;

			return DEFAULT_VALUE;
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
	
	// EnabledState (only when all conditions are fulfilled "state" becomes true)
	private static class EnabledState {
		// fields
		private boolean disposed;
		// optional listener (EnabledState is also used to attach listeners)
		private final IEnabledStateListener listener; 
		// chaining
		private boolean enabled;
		private Boolean lastState; // last state is buffered (may be null) so that listeners are only notified when state has actually changed
		private EnabledState next;
		
		// construction
		public EnabledState(IEnabledStateListener listener) {
			disposed = false;
			
			// optional listener
			this.listener = listener; // may be null

			// chaining
			enabled = false;
			lastState = null;
			next = null;
		}
		public void dispose() {
			disposed = true;
		}

		// properties
		public void setEnabled(boolean value) {
			enabled = value;
		}
		public EnabledState getNext() {
			return next;
		}
		public void setNext(EnabledState value) {
			next = value;
		}
		
		// methods
		public void update() {
			Boolean newState = null;
			for (EnabledState item = this; item != null; item = item.next)
				if (!item.disposed)
					if (newState == null)
						newState = item.enabled;
					else
						newState = newState && item.enabled;

			if ((newState != null) && !Objects.equals(lastState, newState)) {
				lastState = newState;
				onStateChanged(newState);
			}
		}
		private void onStateChanged(boolean state) {
			if (!disposed && (listener != null))
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
