package org.gumtree.control.ui.navigator;

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
import org.gumtree.control.ui.editors.ControlModelEditorInput;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.control.ui.internal.SicsUIProperties;
import org.gumtree.control.ui.viewer.model.INodeSet;
import org.gumtree.control.ui.viewer.model.NodeSet;
import org.gumtree.control.ui.viewer.model.SicsModelNode;
import org.gumtree.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlNavigatorActionProvider extends CommonActionProvider {

	private static final String ID_VIEW_CONTROL_TERMINAL = "org.gumtree.control.ui.ControlTerminalView";

	private static Logger logger = LoggerFactory.getLogger(ControlNavigatorActionProvider.class);

	private IAction openDefaultEditorAction;

	private IAction openTerminalAction;

	private StructuredViewer viewer;

	public ControlNavigatorActionProvider() {
		super();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		IStructuredSelection selections = (IStructuredSelection) viewer.getSelection();
		if (selections.size() == 1 && selections.getFirstElement() instanceof SicsModelNode) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openDefaultEditorAction);
		}
	}

	@Override
	public void fillContextMenu(IMenuManager aMenu) {
		IStructuredSelection selections = (IStructuredSelection) viewer.getSelection();
		if (selections.size() == 1 && selections.getFirstElement() instanceof SicsModelNode) {

			// Filtered SICS Control submenu
			IMenuManager filteredMenu = new MenuManager("Filtered SICS Control");
			String filterPath = SicsUIProperties.FILTER_PATH.getValue();
			if (!StringUtils.isEmpty(filterPath)) {
				try {
					IFileStore filterFolder = EFS.getStore(new URI(filterPath));
					Map<String, IAction> actions = new TreeMap<String, IAction>();
					for (IFileStore child : filterFolder.childStores(EFS.NONE, new NullProgressMonitor())) {
						if (child.getName().endsWith(".xml")) {
							try {
								INodeSet nodeSet = NodeSet.read(child.openInputStream(EFS.NONE, new NullProgressMonitor()));
								IAction action = createOpenEditorAction(nodeSet.getTitle(), viewer, nodeSet);
								actions.put(nodeSet.getTitle(), action);
							} catch (Exception e) {
								logger.info("Failed to load " + child.getName() + " as filter.", e);
							}
						}
					}
					for (IAction action : actions.values()) {
						filteredMenu.add(action);
					}
				} catch (Exception e) {
				}
			}
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, filteredMenu);

			aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openDefaultEditorAction);
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openTerminalAction);
		}
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		viewer = site.getStructuredViewer();
		openDefaultEditorAction = createOpenEditorAction("Open New SICS Model", viewer, null);
		openTerminalAction = new Action("Show SICS Control Terminal",
				Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/terminal.png")) {
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(ID_VIEW_CONTROL_TERMINAL);
				} catch (PartInitException e) {
					logger.error("Failed to open control terminal view", e);
				}
			}
		};
	}

	@Override
	public void dispose() {
		openDefaultEditorAction = null;
		openTerminalAction = null;
		super.dispose();
	}

	private IAction createOpenEditorAction(final String name, final StructuredViewer structuredViewer,
			final INodeSet nodeSet) {
		return new Action(name,
				Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/server.gif")) {
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.openEditor(new ControlModelEditorInput(null, nodeSet),
									InstrumentContentProvider.ID_EDITOR);
				} catch (Exception e) {
					logger.error("Cannot open SICS control editor", e);
				}
			}
		};
	}

}
