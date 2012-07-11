package au.gov.ansto.bragg.wombat.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.gumtree.gumnix.sics.ui.util.SicsControllerNode;

public class InstrumentActionProvider extends CommonActionProvider {

	private IAction openHMBorwserAction;

	private IAction openPCLBrowserAction;

	private StructuredViewer viewer;

	public InstrumentActionProvider() {
	}

	public void fillContextMenu(IMenuManager aMenu) {
		IStructuredSelection selections = ((IStructuredSelection)viewer.getSelection());
		if(selections.size() == 1) {
			if(selections.getFirstElement() instanceof SicsControllerNode ) {
				aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openHMBorwserAction);
				aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openPCLBrowserAction);
			}
		}
	}

	public void init(ICommonActionExtensionSite site) {
		viewer = site.getStructuredViewer();
		openHMBorwserAction = new OpenHMBorwserAction();
		openPCLBrowserAction = new OpenPLCBorwserAction();
	}


	public void dispose() {
		openHMBorwserAction = null;
		openPCLBrowserAction = null;
	}

}
