package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.ui.util.jface.ITreeViewerColumn;

public class ControlView extends ViewPart {

	private Composite parent;

	private ControlViewer viewer;

	public ControlView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new FillLayout());
		viewer = new ControlViewer();
		viewer.createPartControl(parent, null);
		createActions();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void createActions() {
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(createRefreshAction());
		IMenuManager mgr = bars.getMenuManager();
		mgr.add(createColumnSelectionAction());
	}

	private Action createColumnSelectionAction() {
		Action action = new Action("Select columns") {
			public void run() {
				ListSelectionDialog dialog = new ListSelectionDialog(getSite()
						.getShell(), viewer.getAllColumns(),
						new ArrayContentProvider(), new LabelProvider(),
						"Select columns for display");
				dialog.setHelpAvailable(true);
				dialog.setInitialSelections(viewer.getVisibleColumns());
				dialog.open();
				if(dialog.getResult() != null) {
					final Object[] selecton = dialog.getResult();
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							for(ITreeViewerColumn column : viewer.getAllColumns()) {
								viewer.setColumnVisibility(column, false);
							}
							for(Object column : selecton) {
								viewer.setColumnVisibility((ITreeViewerColumn)column, true);
							}
							parent.update();
						}
					});
				}

			}
		};
		action.setImageDescriptor(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/full/obj16/layout_co.gif"));
		return action;
	}

	private Action createRefreshAction() {
		Action action = new Action("Refresh View") {
			public void run() {
				if(viewer != null) {
					viewer.refresh();
				}
			}
		};
		action.setImageDescriptor(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/full/elcl16/refresh_nav.gif"));
		return action;
	}

	public void dispose() {
		super.dispose();
		viewer.dispose();
		viewer = null;
		parent = null;
	}

}
