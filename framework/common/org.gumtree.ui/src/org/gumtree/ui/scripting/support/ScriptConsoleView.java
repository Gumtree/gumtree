package org.gumtree.ui.scripting.support;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.scripting.IScriptConsole;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class ScriptConsoleView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(ScriptConsoleView.class);
	
	private EventHandler consoleEventHandler;
	
	private IScriptConsole console;
	
	public ScriptConsoleView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		// Create UI
		console = new ScriptConsole(parent, SWT.NONE);
		ContextInjectionFactory.inject(console, Activator.getDefault()
				.getEclipseContext());
		createActions();
	}

	/**
	 * Creates actions on the view
	 */
	private void createActions() {
		/*********************************************************************
		 * Interrupt
		 *********************************************************************/
		Action interruptAction =  new Action("Interrupt", IAction.AS_PUSH_BUTTON) {
			public void run() {
				boolean isInterrupt = MessageDialog.openConfirm(getViewSite().getShell(), "Interrupt Script",
						"Do you want interrupt your running script?");
				if (isInterrupt) {
					console.getScriptExecutor().interrupt();
				}
			}
		};
		interruptAction.setImageDescriptor(InternalImage.INTERRUPT.getDescriptor());
		getViewSite().getActionBars().getToolBarManager().add(interruptAction);
		
		/*********************************************************************
		 * Enable content assist (code completion)
		 *********************************************************************/
		if ((console.getOriginalStyle() & ICommandLineViewer.NO_INPUT_TEXT) == 0) {
			Action contentAssistAction = new Action("Enable Code Completion", IAction.AS_CHECK_BOX) {
				public void run() {
					console.setContentAssistEnabled(isChecked());
				}
			};
			contentAssistAction.setImageDescriptor(InternalImage.CONTENT_ASSIST.getDescriptor());
			console.setContentAssistEnabled(true);
			contentAssistAction.setChecked(true);
			getViewSite().getActionBars().getToolBarManager().add(contentAssistAction);
		}
		
		/*********************************************************************
		 * Scroll lock
		 *********************************************************************/
		final Action scrollLockAction = new Action("Scroll Lock", IAction.AS_CHECK_BOX) {
			public void run() {
				console.setScrollLocked(isChecked());
			}
		};
		consoleEventHandler = new EventHandler(
				IScriptConsole.EVENT_TOPIC_SCRIPT_CONSOLE_SCROLL_LOCK,
				IScriptConsole.EVENT_PROP_CONSOLE_ID, console.getId()) {
			@Override
			public void handleEvent(Event event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						scrollLockAction.setChecked(console.isScrollLocked());
					}
				});
			}
		}.activate();
		scrollLockAction.setImageDescriptor(InternalImage.SCROLL_LOCK.getDescriptor());
		getViewSite().getActionBars().getToolBarManager().add(scrollLockAction);

		/*********************************************************************
		 * Clear console text
		 *********************************************************************/
		Action clearAction = new Action("Clear Console", IAction.AS_PUSH_BUTTON) {
			public void run() {
				console.clearConsole();
			}
		};
		clearAction.setImageDescriptor(InternalImage.CLEAR.getDescriptor());
		getViewSite().getActionBars().getToolBarManager().add(clearAction);
		
		/*********************************************************************
		 * Separator
		 *********************************************************************/
		getViewSite().getActionBars().getToolBarManager().add(new Separator());
		
		/*********************************************************************
		 * Export console output
		 *********************************************************************/
		Action exportOutputAction = new ExportConsoleAction(console, getSite().getShell()); 
		getViewSite().getActionBars().getToolBarManager().add(exportOutputAction);
		
		/*********************************************************************
		 * Export command history
		 *********************************************************************/
		Action exportHistoryAction = new Action("Export command history", IAction.AS_PUSH_BUTTON) {
			public void run() {
				FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
				dialog.setText("Save command history");
		        String[] filterExt = { "*.txt" };
		        dialog.setFilterExtensions(filterExt);
				String selectedFile = dialog.open();
				if (selectedFile != null) {
					try {
						FileWriter writer = new FileWriter(selectedFile);
						String[] histories = console.getCommandHistory();
						for (String history : histories) {
							if (Platform.getOS().equals(Platform.OS_WIN32)) {
								writer.write(history + "\r\n");
							} else {
								writer.write(history + "\n");
							}
						}
						writer.flush();
						writer.close();
					} catch (IOException e) {
						String errorMessage = "Failed to write command history to " + selectedFile;
						logger.error(errorMessage, e);
						StatusManager.getManager().handle(
								new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										IStatus.OK, errorMessage, e), StatusManager.SHOW);
					}
				}
			}
		};
		exportHistoryAction.setImageDescriptor(InternalImage.EXPORT_HISTORY.getDescriptor());
		getViewSite().getActionBars().getToolBarManager().add(exportHistoryAction);
	}
	
	public void dispose() {
		if (consoleEventHandler != null) {
			consoleEventHandler.deactivate();
		}
		super.dispose();
	}
	
	@Override
	public void setFocus() {
		if (console != null) {
			console.setFocus();
		}
	}

}
