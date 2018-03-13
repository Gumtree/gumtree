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

package org.gumtree.control.ui.batch.taskeditor;

import java.io.File;
import java.net.URI;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.part.FileEditorInput;
import org.gumtree.control.batch.IBatchScript;
import org.gumtree.control.batch.ResourceBasedBatchScript;
import org.gumtree.control.ui.batch.IBatchManager;
import org.gumtree.ui.util.swt.IDNDHandler;

public class DNDHandler implements IDNDHandler<IBatchManager> {

	private IBatchManager manager;
	
	@Override
	public void handleDrop(DropTargetEvent event) {
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			// 1. Handle dropping from file system
			String[] filenames = (String[]) event.data;
			for (String filename : filenames) {
				File file = new File(filename);
				addResourceBasedBatchBuffer(file.getName(), file.toURI());
			}
		} else if (EditorInputTransfer.getInstance().isSupportedType(event.currentDataType)
				&& event.data instanceof EditorInputData[]) {
			// 2. Handle dropping from remote system explorer
			EditorInputData[] inputDatas = ((EditorInputData[]) event.data);
			for (EditorInputData inputData : inputDatas) {
				IEditorInput input = inputData.input;
				if (input instanceof FileEditorInput) {
					IFile file = ((FileEditorInput) input).getFile();
					addResourceBasedBatchBuffer(file.getName(), file.getLocationURI());
				}
			}
		} else if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)
				&& event.data instanceof IStructuredSelection) {
			// 3. Handle dropping from project explorer
			for (Object object : ((IStructuredSelection) event.data).toList()) {
				if (object instanceof IFile) {
					IFile file = (IFile) object;
					addResourceBasedBatchBuffer(file.getName(), file.getLocationURI());
				}
			}
		}
	}

	private void addResourceBasedBatchBuffer(String name, URI uri) {
		IBatchScript buffer = new ResourceBasedBatchScript(name, uri);
		getHost().getBatchBufferQueue().add(buffer);
	}
	
	@Override
	public void handleDrag(DragDetectEvent event) {
	}

	@Override
	public IBatchManager getHost() {
		return manager;
	}

	@Inject
	@Override
	public void setHost(IBatchManager manager) {
		this.manager = manager;
	}
	
}
