/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.batch.ui.buffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.part.FileEditorInput;
import org.gumtree.ui.util.swt.IDNDHandler;

public class DNDHandler implements IDNDHandler<IBatchBufferManager> {

	private IBatchBufferManager manager;
	
	@Override
	public void handleDrop(DropTargetEvent event) {
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			// 1. Handle dropping from file system
			String[] filenames = (String[]) event.data;
			String checkUnicode = "";
			for (String filename : filenames) {
				File file = new File(filename);
				addResourceBasedBatchBuffer(file.getName(), file.toURI());
				checkUnicode += checkResourceBasedBatchBuffer(file.getName(), file.toURI());
			}
			if (checkUnicode.length() > 0) {
				showWarningMsg(event.display.getActiveShell(), checkUnicode);
			}
		} else if (EditorInputTransfer.getInstance().isSupportedType(event.currentDataType)
				&& event.data instanceof EditorInputData[]) {
			// 2. Handle dropping from remote system explorer
			EditorInputData[] inputDatas = ((EditorInputData[]) event.data);
			String checkUnicode = "";
			for (EditorInputData inputData : inputDatas) {
				IEditorInput input = inputData.input;
				if (input instanceof FileEditorInput) {
					IFile file = ((FileEditorInput) input).getFile();
					addResourceBasedBatchBuffer(file.getName(), file.getLocationURI());
					checkUnicode += checkResourceBasedBatchBuffer(file.getName(), file.getLocationURI());
				}
			}
			if (checkUnicode.length() > 0) {
				showWarningMsg(event.display.getActiveShell(), checkUnicode);
			}
		} else if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)
				&& event.data instanceof IStructuredSelection) {
			// 3. Handle dropping from project explorer
			String checkUnicode = "";
			for (Object object : ((IStructuredSelection) event.data).toList()) {
				if (object instanceof IFile) {
					IFile file = (IFile) object;
					addResourceBasedBatchBuffer(file.getName(), file.getLocationURI());
					checkUnicode += checkResourceBasedBatchBuffer(file.getName(), file.getLocationURI());
				}
			}
			if (checkUnicode.length() > 0) {
				showWarningMsg(event.display.getActiveShell(), checkUnicode);
			}
		}
	}
	
	private void showWarningMsg(Shell shell, String msg) {
		msg += "The batch file has been queued. But please review your file and queue it again.\n";
		MessageDialog.openWarning(shell, "Found unexpected characters in the batch file", msg);
	}

	private void addResourceBasedBatchBuffer(String name, URI uri) {
		IBatchBuffer buffer = new ResourceBasedBatchBuffer(name, uri);
		getHost().getBatchBufferQueue().add(buffer);
	}
	
	private String checkResourceBasedBatchBuffer(String name, URI uri) {
		String res = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(uri)), StandardCharsets.ISO_8859_1));

            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                for (int colNum = 0; colNum < line.length(); colNum++) {
                    char ch = line.charAt(colNum);
                    if (ch > 127) {
                        res += String.format("Found Unicode char in file %s: %c (U+%04X) at line %d, column %d%n",
                        		name, ch, (int) ch, lineNum, colNum + 1);
                        res += String.format("line content (there may be invisible symbol): '%s'%n", line); 
                    }
                }
            }

        } catch (Exception e) {
        	if (reader != null) {
        		try {
					reader.close();
				} catch (IOException e1) {
				}
        	}
        	BatchBufferManager.logger.error("failed to check Unicode for " + name + ": " + uri);
		}
		return res;
	}
	
	@Override
	public void handleDrag(DragDetectEvent event) {
	}

	@Override
	public IBatchBufferManager getHost() {
		return manager;
	}

	@Inject
	@Override
	public void setHost(IBatchBufferManager manager) {
		this.manager = manager;
	}
	
}
