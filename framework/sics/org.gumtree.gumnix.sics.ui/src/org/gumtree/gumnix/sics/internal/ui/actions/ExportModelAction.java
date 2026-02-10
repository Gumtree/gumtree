package org.gumtree.gumnix.sics.internal.ui.actions;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
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
						// Use EMF ResourceSet/XMIResourceFactory to save the model
//						ResourceSet resourceSet = new ResourceSetImpl();
//						resourceSet.getPackageRegistry().put(HipadabaPackageImpl.eNS_URI,
//								HipadabaPackageImpl.eINSTANCE);
//						Resource resource = resourceSet.createResource(URI
//								.createURI("all.hipadaba"));
//						resource.load(new FileInputStream(filename), null);

						ResourceSet resourceSet = new ResourceSetImpl();
						// register XMI resource factory for XML serialization
						resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("hipadaba", new XMIResourceFactoryImpl());
						resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
						// create resource and add model
						Resource resource = resourceSet.createResource(URI.createFileURI(filename));
						if (model instanceof EObject) {
							resource.getContents().add((EObject) model);
						} else {
							// fallback: cannot export non-EObject models
							MessageDialog.openError(window.getShell(), "Error in exporting model", "Online instrument model cannot be exported: unsupported model type.");
							return;
						}
						resource.save(null);
					} else {
						MessageDialog.openError(window.getShell(), "Error in exporting model", "Online instrument model is not available.");
					}
				} catch (SicsIOException e) {
					MessageDialog.openError(window.getShell(), "Error in exporting model", "Online instrument model is not available.");
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					MessageDialog.openError(window.getShell(), "Error in exporting model", "Failed to save model: " + e.getMessage());
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