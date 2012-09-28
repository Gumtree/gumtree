package org.gumtree.widgets.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.gumtree.widgets.IWidget;

public abstract class ExtendedComposite extends Composite implements IWidget {

	private IWidgetFactory widgetFactory;

	private int originalStyle;

	public ExtendedComposite(Composite parent, int style) {
		super(parent, style);
		originalStyle = style;
		setBackgroundMode(SWT.INHERIT_FORCE);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeWidget();
				if (widgetFactory != null) {
					widgetFactory.dispose();
					widgetFactory = null;
				}
			}
		});
	}

	protected abstract void disposeWidget();

	public IWidgetFactory getWidgetFactory() {
		if (widgetFactory == null) {
			// Use the widget factory from parent if necessary
			Composite parent = getParent();
			while (parent != null) {
				if (parent instanceof ExtendedComposite) {
					return ((ExtendedComposite) parent).getWidgetFactory();
				} else {
					parent = parent.getParent();
				}
			}
			// Otherwise provide the default SWT factory
			widgetFactory = new SwtWidgetFactory();
		}
		return widgetFactory;
	}

	public void setWidgetFactory(IWidgetFactory widgetFactory) {
		this.widgetFactory = widgetFactory;
	}

	public int getOriginalStyle() {
		return originalStyle;
	}

	public void disposeChildren() {
		if (!isDisposed()) {
			for (Control child : getChildren()) {
				child.dispose();
			}
		}
	}

}
