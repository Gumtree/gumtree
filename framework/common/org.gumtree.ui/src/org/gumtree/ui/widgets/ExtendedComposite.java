package org.gumtree.ui.widgets;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class ExtendedComposite extends Composite implements IWidget {

	private IWidgetFactory widgetFactory;
	
	private int originalStyle;
	
	public ExtendedComposite(Composite parent, int style) {
		super(parent, style);
		originalStyle = style;
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeWidget();
				if (widgetFactory != null) {
					widgetFactory.disposeObject();
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
	
	public static <T extends Composite> T launchSWTShell(Class<T> compositeClass) {
		return launchSWTShell(compositeClass, "", SWT.NONE);
	}
	
	public static <T extends Composite> T launchSWTShell(Class<T> compositeClass, String title) {
		return launchSWTShell(compositeClass, title, SWT.NONE);	
	}
	
	public static <T extends Composite> T launchSWTShell(final Class<T> compositeClass, final String title, final int style) {
		final BlockingQueue<T> queue = new ArrayBlockingQueue<T>(1);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(Display.getDefault());
				shell.setText(title);
				shell.setLayout(new FillLayout());
				shell.setSize(800, 640);

				try {
					queue.put(compositeClass.getConstructor(Composite.class, int.class)
							.newInstance(shell, style));
				} catch (Exception e) {
					e.printStackTrace();
				}

				shell.open();
			}
		});
		try {
			// We need to wait for above code to be executed by the SWT thread
			return queue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
