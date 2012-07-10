package org.gumtree.ui.scripting;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptingUI {

	public static final String ID_VIEW_COMMAND_LINE = "org.gumtree.ui.commandLineView";

	private static Logger logger = LoggerFactory.getLogger(ScriptingUI.class);
	
	private static int counter = 0;
	
	public static synchronized String getNextCommandLineViewCounter() {
		return Integer.toString(counter++);
	}
	
	public static String launchNewCommandLineView(IScriptExecutor executor) {
		return launchNewCommandLineView(executor, SWT.NONE);
	}
	
	public static String launchNewCommandLineView(IScriptExecutor executor, int style) {
		return launchNewCommandLineView(executor, style, -1);
	}
	
	public static String launchNewCommandLineView(final IScriptExecutor executor, final int style, final int windowIndex) {
		final String secondaryId = getNextCommandLineViewCounter();
		SafeUIRunner.asyncExec(new ISafeRunnable() {
			public void run() throws Exception {
				// Register style in global directory
				ServiceUtils.getService(IDirectoryService.class).bind(ICommandLineView.DIR_KEY_STYLE, style);
				
				// Select window
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow window = null;
				if (windowIndex == -1 || windowIndex >= workbench.getWorkbenchWindowCount()) {
					window = workbench.getActiveWorkbenchWindow();
				} else {
					window = workbench.getWorkbenchWindows()[windowIndex];
				}
				
				// Create but not activate (no focus to allow setting engine)
				ICommandLineView view = (ICommandLineView) window.getActivePage()
						.showView(ScriptingUI.ID_VIEW_COMMAND_LINE,
								secondaryId,
								IWorkbenchPage.VIEW_VISIBLE);
				
				// Unbind style from registry
				ServiceUtils.getService(IDirectoryService.class).unbind(ICommandLineView.DIR_KEY_STYLE);
				
				// Set engine
				view.setEngineExecutor(executor);
				
				// Give it the focus
				window.getActivePage().showView(ScriptingUI.ID_VIEW_COMMAND_LINE,
								secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			}
			public void handleException(Throwable exception) {
				logger.error("Failed to launch command line view", exception);
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Failed to launch command line view", exception);
				// Show error in UI
				StatusManager.getManager().handle(status, StatusManager.SHOW);
			}
		});
		return secondaryId;
	}
	
	private ScriptingUI() {
		super();
	}
	
}
