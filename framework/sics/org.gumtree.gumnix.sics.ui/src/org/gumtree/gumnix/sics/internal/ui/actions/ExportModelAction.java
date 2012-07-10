package org.gumtree.gumnix.sics.internal.ui.actions;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.sdo.EDataObject;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.SICS;

public class ExportModelAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				try {
					SICS model = SicsCore.getSicsManager().service().getOnlineModel();
					if(model != null) {
						FileDialog dialog = new FileDialog(getWindow().getShell(), SWT.SAVE);
						String filename = dialog.open();
						if(filename == null) {
							return;
						}
//						EDataGraph dataGraph = SDOFactory.eINSTANCE.createEDataGraph();
//						dataGraph.setERootObject((EDataObject)model);
//						Resource resource = dataGraph.getResourceSet().createResource(URI.createFileURI(filename));
						Resource resource = SDOUtil.createResourceSet().createResource(URI.createFileURI(filename));
						// [TLA] 2007-09-18 Add to add model into the resource for XML export
						resource.getContents().add((EDataObject)model);
						resource.save(null);
					} else {
						MessageDialog.openError(window.getShell(), "Error in exporting model", "Online instrument model is not available.");
					}
				} catch (SicsIOException e) {
					MessageDialog.openError(window.getShell(), "Error in exporting model", "Online instrument model is not available.");
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	private IWorkbenchWindow getWindow() {
		return window;
	}

}
