package org.gumtree.ui.scripting.support;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.scripting.ScriptingUI;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;

public class Launcher extends AbstractLauncher {

	public Launcher() {
		super();
	}

	public void launch() throws LauncherException {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ScriptingUI.ID_VIEW_COMMAND_LINE, ScriptingUI.getNextCommandLineViewCounter(), IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			throw new LauncherException("Cannot launch command line view", e);
		}
	}

}
