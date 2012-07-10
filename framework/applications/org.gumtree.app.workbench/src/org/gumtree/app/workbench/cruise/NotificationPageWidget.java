package org.gumtree.app.workbench.cruise;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;

@SuppressWarnings("restriction")
public class NotificationPageWidget extends AbstractCruisePageWidget {

	@Inject
	private IEclipseContext eclipseContext;

	public NotificationPageWidget(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		setLayout(new FillLayout());
		WeatherCruiseWidget weatherCruiseWidget = new WeatherCruiseWidget(this,
				SWT.NONE);
		ContextInjectionFactory
				.inject(weatherCruiseWidget, getEclipseContext());
	}

	@Override
	protected void disposeWidget() {
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IEclipseContext getEclipseContext() {
		return eclipseContext;
	}

	public void setEclipseContext(IEclipseContext eclipseContext) {
		this.eclipseContext = eclipseContext;
	}

}
