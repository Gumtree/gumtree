package org.gumtree.app.workbench.support;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.gumtree.ui.missioncontrol.IHubRegistry;
import org.gumtree.ui.missioncontrol.support.MissionControlWidget;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.util.UIResources;

@SuppressWarnings("restriction")
public class SidebarWidget extends ExtendedComposite {

	private IHubRegistry hubRegistry;
	
	@Inject
	public SidebarWidget(Composite parent, @Optional int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		setLayout(new FillLayout());
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setFont(UIResources.getDefaultFont(SWT.BOLD));
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Mission Control");
		
		MissionControlWidget widget = new MissionControlWidget(tabFolder, SWT.NONE);
		widget.setHubRegistry(getHubRegistry());
		widget.render();
		tabItem.setControl(widget);
		
		tabFolder.setSelection(0);
	}
	
	
	@Override
	protected void disposeWidget() {
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	public IHubRegistry getHubRegistry() {
		return hubRegistry;
	}

	@Inject
	public void setHubRegistry(IHubRegistry hubRegistry) {
		this.hubRegistry = hubRegistry;
	}
	
	/*************************************************************************
	 * Utilities
	 *************************************************************************/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		SidebarWidget sidebarWidget = new SidebarWidget(shell, SWT.NONE);
		sidebarWidget.render();

		shell.setSize(300, 800);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
