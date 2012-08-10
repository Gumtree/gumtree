package org.gumtree.ui.scripting.support;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.scripting.IScriptConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportConsoleAction extends Action {

	// Logger
	private static Logger logger = LoggerFactory
			.getLogger(ExportOutputAction.class);

	// Script console for text output
	private IScriptConsole console;

	// Parent shell for file selection dialog
	private Shell shell;

	public ExportConsoleAction(IScriptConsole console, Shell shell) {
		super("Export Console Output", IAction.AS_PUSH_BUTTON);
		this.console = console;
		this.shell = shell;
		setImageDescriptor(InternalImage.EXPORT_OUTPUT.getDescriptor());
	}

	public void run() {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setText("Save console output");
		String[] filterExt = { "*.txt" };
		dialog.setFilterExtensions(filterExt);
		String selectedFile = dialog.open();
		if (selectedFile != null) {
			try {
				FileWriter writer = new FileWriter(selectedFile);
				console.exportConsoleText(writer);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				String errorMessage = "Failed to write console output to "
						+ selectedFile;
				logger.error(errorMessage, e);
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								IStatus.OK, errorMessage, e),
						StatusManager.SHOW);
			}
		}
	}

}
