package org.gumtree.gumnix.sics.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;
import org.gumtree.ui.terminal.support.TerminalLaunchAction;

public class SicsTerminalLaunchAction extends TerminalLaunchAction implements IWorkbenchWindowActionDelegate {
	
	public SicsTerminalLaunchAction() {
		this(null);
	}
	
	public SicsTerminalLaunchAction(IWorkbenchWindow window) {
		super("SICS Telnet Terminal", Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/telnet.gif"), window);
	}
	
	protected String getAdapterId() {
		return SicsUIConstants.ID_SICS_CONSOLE_ADAPTER;
	}

	protected boolean isAutoConnect() {
		return true;
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
