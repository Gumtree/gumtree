package au.gov.ansto.bragg.nbi.workbench;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.gumtree.app.workbench.support.SidebarWidget;
import org.gumtree.ui.missioncontrol.support.MissionControlWidget;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.util.UIResources;

@SuppressWarnings("restriction")
public class NBISidebarWidget extends ExtendedComposite {

	@Inject
	public NBISidebarWidget(Composite parent, @Optional int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0)
				.applyTo(this);
		createMainArea(this);
		createControlArea(this);
	}

	private void createMainArea(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setFont(UIResources.getDefaultFont(SWT.BOLD));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
		.grab(true, true).applyTo(tabFolder);
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Mission Control");
		MissionControlWidget widget = new MissionControlWidget(tabFolder, SWT.NONE);
		widget.render();
		tabItem.setControl(widget);
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Status");
		
		tabFolder.setSelection(0);
	}
	
	private void createControlArea(Composite parent) {
		NBIControlWidget controlWidget = new NBIControlWidget(this, SWT.NONE);
		controlWidget.setBackground(UIResources.getSystemColor(SWT.COLOR_BLACK));
		controlWidget.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		controlWidget.render();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(controlWidget);
	}
	
	@Override
	protected void disposeWidget() {
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		NBISidebarWidget widget = new NBISidebarWidget(shell, SWT.NONE);
		widget.render();

		shell.setSize(300, 800);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
