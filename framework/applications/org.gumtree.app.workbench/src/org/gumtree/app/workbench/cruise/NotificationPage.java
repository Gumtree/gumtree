package org.gumtree.app.workbench.cruise;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.app.workbench.internal.Activator;
import org.gumtree.ui.cruise.ICruisePanelPage;

@SuppressWarnings("restriction")
public class NotificationPage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Notification";
	}

	@Override
	public Composite createNormalWidget(Composite parent) {
		NotificationPageWidget widget = new NotificationPageWidget(parent, SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}

	@Override
	public Composite createFullWidget(Composite parent) {
		NotificationPageWidget widget = new NotificationPageWidget(parent, SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}

}
