package org.gumtree.app.workbench.apps;

import java.text.DecimalFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.tile.TileColor;
import org.gumtree.widgets.swt.tile.TileDataFactory;
import org.gumtree.widgets.swt.tile.TileLayoutFactory;
import org.gumtree.widgets.swt.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResources;

@SuppressWarnings("restriction")
public class JVMStatisticsApp extends ExtendedComposite {

	private static final long BYTE = 1024;

	private static final long KILO_BYTE = 1024 * BYTE;

	private static final long MEGA_BYTE = 1024 * KILO_BYTE;

	private static final long GIGA_BYTE = 1024 * MEGA_BYTE;

	private static final long TERA_BYTE = 1024 * GIGA_BYTE;

	@Inject
	public JVMStatisticsApp(Composite parent, @Optional int style) {
		super(parent, style);
		setBackgroundMode(SWT.INHERIT_FORCE);
	}

	@PostConstruct
	public void render() {
		disposeChildren();
		int width = ((GridData) getLayoutData()).horizontalSpan;
		int height = ((GridData) getLayoutData()).verticalSpan;
		if (width == 2 && height == 2) {
			GridLayoutFactory.swtDefaults().applyTo(this);
			Label label = getWidgetFactory().createLabel(this, "Memory");
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.END)
					.grab(true, true).applyTo(label);
			String memory = formatByteString(Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().freeMemory());
			final Label memoryLabel = getWidgetFactory().createLabel(this, memory);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING)
					.grab(true, true).applyTo(memoryLabel);
			Job job = new Job("") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						@Override
						public void run() throws Exception {
							if (memoryLabel.isDisposed()) {
								return;
							}
							String memory = formatByteString(Runtime.getRuntime().totalMemory()
									- Runtime.getRuntime().freeMemory());
							memoryLabel.setText(memory);
						}
					});
					schedule(1000);
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		} else if (width == 4 && height == 4) {

		} else if (width == 8 && height == 4) {

		}
	}

	@Override
	protected void disposeWidget() {
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	private String formatByteString(long byteValue) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		if (byteValue < KILO_BYTE) {
			return byteValue + "B";
		} else if (byteValue < MEGA_BYTE) {
			return twoDForm.format(byteValue / (float) KILO_BYTE) + "KB";
		} else if (byteValue < GIGA_BYTE) {
			return twoDForm.format(byteValue / (float) MEGA_BYTE) + "MB";
		} else if (byteValue < TERA_BYTE) {
			return twoDForm.format(byteValue / (float) GIGA_BYTE) + "GB";
		}
		return "NaN";
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		TileLayoutFactory.create(8).applyTo(shell);
		shell.setBackground(UIResources.getSystemColor(SWT.COLOR_BLACK));
		shell.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));

		JVMStatisticsApp app = new JVMStatisticsApp(shell, SWT.NONE);
		app.setBackground(TileColor.CYAN.getColor());
		app.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		TileDataFactory.create().size(2, 2).applyTo(app);
		app.render();

		app = new JVMStatisticsApp(shell, SWT.NONE);
		app.setBackground(TileColor.CYAN.getColor());
		app.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		TileDataFactory.create().size(4, 4).applyTo(app);
		app.render();

		app = new JVMStatisticsApp(shell, SWT.NONE);
		app.setBackground(TileColor.CYAN.getColor());
		app.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		TileDataFactory.create().size(8, 4).applyTo(app);
		app.render();

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
