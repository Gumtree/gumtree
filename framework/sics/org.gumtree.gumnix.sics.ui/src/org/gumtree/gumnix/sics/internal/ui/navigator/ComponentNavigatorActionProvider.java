package org.gumtree.gumnix.sics.internal.ui.navigator;

import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.InternalImage;
import org.gumtree.gumnix.sics.internal.ui.actions.ComponentControlEditorOpenAction;
import org.gumtree.gumnix.sics.internal.ui.actions.SicsControlEditorOpenAction;
import org.gumtree.gumnix.sics.internal.ui.actions.SicsTerminalLaunchAction;
import org.gumtree.gumnix.sics.ui.SicsUIProperties;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.gumtree.gumnix.sics.ui.controlview.NodeSet;
import org.gumtree.gumnix.sics.ui.util.SicsControllerNode;
import org.gumtree.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentNavigatorActionProvider extends CommonActionProvider {

	private static final String ID_VIEW_TRANSACTION = "org.gumtree.gumnix.sics.ui.transaction.transactionView";
	
	private static Logger logger = LoggerFactory.getLogger(ComponentNavigatorActionProvider.class);
	
	private IAction sicsOpenAction;

	private IAction componentOpenAction;

	private IAction openSicsTerminalAction;
	
	private IAction openTransactionAction;

	private StructuredViewer viewer;

	public ComponentNavigatorActionProvider() {
		super();
	}

	public void fillActionBars(IActionBars actionBars) {
		IStructuredSelection selections = ((IStructuredSelection)viewer.getSelection());
		if(selections.size() == 1) {
			if(selections.getFirstElement() instanceof SicsControllerNode ) {
				actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, sicsOpenAction);
			} else {
				actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, componentOpenAction);
			}
		}
	}

	public void fillContextMenu(IMenuManager aMenu) {
		IStructuredSelection selections = ((IStructuredSelection)viewer.getSelection());
		if(selections.size() == 1) {
			if(selections.getFirstElement() instanceof SicsControllerNode ) {
				
				// Control menu
				IMenuManager menuManager = new MenuManager("Filtered SICS Control");
				// Construct user defined list
				String filterPath = SicsUIProperties.FILTER_PATH.getValue();
				if (!StringUtils.isEmpty(filterPath)) {
					try {
						IFileStore filterFolder = EFS.getStore(new URI(filterPath));
						Map<String, IAction> actions = new TreeMap<String, IAction>();
						for (IFileStore child : filterFolder.childStores(EFS.NONE, new NullProgressMonitor())) {
							// Load individual filter
							if (child.getName().endsWith(".xml")) {
								try {
									INodeSet nodeSet = NodeSet.read(child.openInputStream(EFS.NONE, new NullProgressMonitor()));
									IAction action = new SicsControlEditorOpenAction(nodeSet.getTitle(), viewer, nodeSet);
									// Store here for sorting
									actions.put(nodeSet.getTitle(), action);
								} catch (Exception e) {
									logger.info("Failed to load " + child.getName() + " as filter.", e);
								}
							}
						}
						// Register action
						for (IAction action : actions.values()) {
							menuManager.add(action);
						}
					} catch (Exception e) {
					}
				}
				// Load user defined list
				aMenu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, menuManager);
				
				// Default (full list)
				SicsControlEditorOpenAction openDefaultEditorAction = new SicsControlEditorOpenAction("Full SICS Control", viewer, null);
				aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openDefaultEditorAction);
				// Terminal menu
				aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openSicsTerminalAction);
				
				
				// Show transaction window menu
				// Don't show this action when the transaction view is missing
				if (PlatformUI.getWorkbench().getViewRegistry().find(ID_VIEW_TRANSACTION) != null) {
					aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openTransactionAction);
				}
			} else {
				aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, componentOpenAction);
			}
		}
	}

	public void init(ICommonActionExtensionSite site) {
		viewer = site.getStructuredViewer();
//		openAction = new ComponentViewOpenAction(viewer);
		componentOpenAction = new ComponentControlEditorOpenAction(viewer);
//		sicsOpenAction = new SicsControlEditorOpenAction(viewer);
		openSicsTerminalAction = new SicsTerminalLaunchAction();
		openTransactionAction = new Action("Open Transaction",
				Activator.getDefault().getImageRegistry().getDescriptor(InternalImage.TRANSACTION.name())) {
			public void run() {
				try {
					// Open transaction view
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ID_VIEW_TRANSACTION);
				} catch (PartInitException e) {
					logger.error("Failed to launch transaction view", e);
				}
			}
		};
	}

	public void dispose() {
		sicsOpenAction = null;
		openSicsTerminalAction = null;
		componentOpenAction = null;
		super.dispose();
	}

}
