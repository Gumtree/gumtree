package au.gov.ansto.bragg.nbi.workbench;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.util.UIResources;

import au.gov.ansto.bragg.nbi.workbench.internal.InternalImage;

public class NBIControlWidget extends ExtendedComposite {

	private ISicsManager sicsManager;

	public NBIControlWidget(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true)
				.margins(0, 0).applyTo(this);
		createInterruptArea(this);
		createControlArea(this);
	}

	private void createInterruptArea(Composite parent) {
		Composite composite = getWidgetFactory().createComposite(parent);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);

		Label interruptButton = getWidgetFactory().createLabel(composite,
				InternalImage.INTERRUPT_72.getImage());
		interruptButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		interruptButton.setToolTipText("Interrupt");
		interruptButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
			}
		});
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, true).applyTo(interruptButton);

		Label label = getWidgetFactory().createLabel(composite, "INTERRUPT");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).applyTo(label);
	}

	private void createControlArea(Composite parent) {
		Composite composite = getWidgetFactory().createComposite(parent);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);

		Label connectionButton = getWidgetFactory().createLabel(composite,
				InternalImage.CONNECT_32.getImage());
		connectionButton
				.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		connectionButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
			}
		});

		Label label = getWidgetFactory().createLabel(composite, "Connect SICS");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));

		Label startButton = getWidgetFactory().createLabel(composite,
				InternalImage.START_32.getImage());
		startButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		startButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
			}
		});

		label = getWidgetFactory().createLabel(composite, "Start HM");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));

		Label stopButton = getWidgetFactory().createLabel(composite,
				InternalImage.STOP_32.getImage());
		stopButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		stopButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
			}
		});

		label = getWidgetFactory().createLabel(composite, "Stop HM");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));
	}

	@Override
	protected void disposeWidget() {
		sicsManager = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public ISicsManager getSicsManager() {
		return sicsManager;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		NBIControlWidget widget = new NBIControlWidget(shell, SWT.NONE);
		widget.setBackground(UIResources.getSystemColor(SWT.COLOR_BLACK));
		widget.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		widget.render();

		shell.setSize(300, 170);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
