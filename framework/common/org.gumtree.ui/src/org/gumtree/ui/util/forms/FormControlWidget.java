package org.gumtree.ui.util.forms;

import java.beans.Expression;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.core.object.IConfigurable;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.bean.AbstractModelObject;
import org.gumtree.util.collection.IParameters;
import org.gumtree.widgets.IWidget;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public abstract class FormControlWidget extends Composite implements IWidget, IConfigurable {

	private FormToolkit toolkit;
	
	private int originalStyle;
	
	private IParameters parameters;
	
	@XStreamOmitField
	private transient PropertyChangeSupport changeSupport;
	
	public FormControlWidget(Composite parent, int style) {
		super(parent, SWT.NONE);
		getToolkit().adapt(this);
		originalStyle = style;
		// Dispose listener
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				changeSupport = null;
				widgetDispose();
				if (toolkit != null) {
					toolkit.dispose();
					toolkit = null;
				}
				parameters = null;
			}
		});
	}

	protected int getOriginalStyle() {
		return originalStyle;
	}
	
	protected abstract void widgetDispose();
	
	protected FormToolkit getToolkit() {
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
		return toolkit;
	}
	
	public IParameters getParameters() {
		return parameters;
	}

	public void setParameters(IParameters parameters) {
		this.parameters = parameters;
	}
	
	private PropertyChangeSupport getChangeSupport() {
		if (changeSupport == null) {
			synchronized (AbstractModelObject.class) {
				if (changeSupport == null) {
					changeSupport = new PropertyChangeSupport(this);
				}
			}
		}
		return changeSupport;
	}
	
	protected void asyncUIExec(final String methodName,
			final Object... arguments) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				Expression expression = new Expression(FormControlWidget.this,
						methodName, arguments);
				expression.execute();
			}
		});
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		getChangeSupport().addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		getChangeSupport().addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		getChangeSupport().removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		getChangeSupport().removePropertyChangeListener(propertyName, listener);
	}
	
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		getChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
	}
	
}
