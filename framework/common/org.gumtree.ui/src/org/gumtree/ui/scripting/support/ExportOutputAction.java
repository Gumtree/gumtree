/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

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
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExportOutputAction is a reusable action for exporting command line viewer
 * output into a text file.
 * 
 * @since 1.2
 */
public class ExportOutputAction extends Action {

	// Logger
	private static Logger logger = LoggerFactory
			.getLogger(ExportOutputAction.class);

	// Command line viewer for text output
	private ICommandLineViewer viewer;

	// Parent shell for file selection dialog
	private Shell shell;

	public ExportOutputAction(ICommandLineViewer viewer, Shell shell) {
		super("Export Console Output", IAction.AS_PUSH_BUTTON);
		this.viewer = viewer;
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
				viewer.exportConsoleText(writer);
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
