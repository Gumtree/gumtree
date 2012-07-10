package org.gumtree.gumnix.sics.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.editors.SicsEditorInput;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;

public class SicsControlLaunchAction extends Action {

	private IEditorInput editorInput = new SicsEditorInput(
			SicsCore.getSicsController());

	private IWorkbenchWindow window;
	
	public SicsControlLaunchAction() {
		this(null);
	}

	public SicsControlLaunchAction(IWorkbenchWindow window) {
		super();
		this.window = window; 
	}
	
	public SicsControlLaunchAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public void run() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					if (window == null) {
						window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					}
					IEditorPart editorPart = window.getActivePage()
							.openEditor(editorInput,
									SicsUIConstants.ID_EDITOR_SICS_CONTROL, true, IWorkbenchPage.MATCH_ID);
//					if(editorPart instanceof SicsMultiPageControlEditor) {
//						((SicsMultiPageControlEditor)editorPart).setActivePage(SicsControlPage.ID);
//					}

				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
