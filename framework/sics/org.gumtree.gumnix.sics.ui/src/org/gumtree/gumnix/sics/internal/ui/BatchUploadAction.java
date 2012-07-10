package org.gumtree.gumnix.sics.internal.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ICommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchUploadAction extends Action {

	private static Logger logger;

	private ICommunicationAdapter adapter;

	public BatchUploadAction(ICommunicationAdapter adapter) {
		super("Upload SICS Batch", Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/bold.gif"));
		Assert.isNotNull(adapter);
		this.adapter = adapter;
	}

	public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		FileDialog dialog = new FileDialog(shell);
		String filename = dialog.open();
		if(filename != null) {
			try {
				File file = new File(filename);
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				adapter.send("exe clear");
				adapter.send("exe upload");
				while((line = reader.readLine()) != null) {
					adapter.send("exe append " + line);
				}
				adapter.send("exe forcesave " + file.getName());
			} catch (CommunicationAdapterException e) {
				getLogger().error("Cannot communicate with adapter", e);
			} catch (FileNotFoundException e) {
				getLogger().error("Cannot find batch file", e);
			} catch (IOException e) {
				getLogger().error("Cannot read batch file", e);
			}
		}

	}

	private static Logger getLogger() {
		if(logger == null) {
			logger= LoggerFactory.getLogger(BatchUploadAction.class);
		}
		return logger;
	}

}
