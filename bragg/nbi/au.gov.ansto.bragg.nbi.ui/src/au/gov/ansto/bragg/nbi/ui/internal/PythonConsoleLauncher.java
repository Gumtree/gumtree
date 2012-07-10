package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.ICommandLineView;
import org.gumtree.ui.scripting.ScriptingUI;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;

public class PythonConsoleLauncher extends AbstractLauncher {

	public PythonConsoleLauncher() {
		// TODO Auto-generated constructor stub
	}

	public void launch() throws LauncherException {
		try {
			String secondaryId = ScriptingUI.getNextCommandLineViewCounter();
			// Create but not activate (no focus to allow setting engine)
			ICommandLineView view = (ICommandLineView) PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(ScriptingUI.ID_VIEW_COMMAND_LINE,
							secondaryId,
							IWorkbenchPage.VIEW_VISIBLE);
			// Set Python engine
			IScriptExecutor executor = new ScriptExecutor("jep");
			view.setEngineExecutor(executor);
			// Give it the focus
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(ScriptingUI.ID_VIEW_COMMAND_LINE,
							secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			throw new LauncherException("UI cannot launch the command line view.", e);
		}
	}
}
