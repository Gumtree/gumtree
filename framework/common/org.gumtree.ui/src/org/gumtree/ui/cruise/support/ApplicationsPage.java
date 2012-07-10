package org.gumtree.ui.cruise.support;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.service.applaunch.IAppLaunchRegistry;

public class ApplicationsPage implements ICruisePanelPage {

	private IAppLaunchRegistry appLaunchRegistry;

	
	@Override
	public Composite createNormalWidget(Composite parent) {
		ApplicationsPageWidget widget = new ApplicationsPageWidget(
				parent, SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		widget.setNumberOfColumn(2);
		widget.render();
		return widget;
	}

	@Override
	public Composite createFullWidget(Composite parent) {
		ApplicationsPageWidget widget = new ApplicationsPageWidget(
				parent, SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		widget.setNumberOfColumn(6);
		widget.render();
		return widget;
	}

	
	@Override
	public String getName() {
		return "Applications";
	}
	
	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IAppLaunchRegistry getAppLaunchRegistry() {
		return appLaunchRegistry;
	}

	public void setAppLaunchRegistry(IAppLaunchRegistry appLaunchRegistry) {
		this.appLaunchRegistry = appLaunchRegistry;
	}

}
