package org.gumtree.ui.scripting.support;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.scripting.ICommandLineView;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.CommandlLineViewerEvent;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineView extends ViewPart implements ICommandLineView {
	
	private static Logger logger = LoggerFactory.getLogger(CommandLineView.class);
	
	private ICommandLineViewer viewer;
	
	private IScriptExecutor executor;
	
	private IEventHandler<CommandlLineViewerEvent> viewerEventHandler;
	
	private Lock setEngineLock;
	
	public CommandLineView() {
		super();
		setEngineLock = new ReentrantLock();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		// Create UI
		viewer = new CommandLineViewer();
		Integer style = null;
		IDirectoryService directoryService = ServiceUtils.getServiceNow(IDirectoryService.class);
		if (directoryService != null) {
			style = directoryService.lookup(DIR_KEY_STYLE, Integer.class);
		} 
		if (style != null) {
			viewer.createPartControl(parent, style);
		}  else {
			viewer.createPartControl(parent);
		}
		createActions();
	}

	public void setEngineExecutor(IScriptExecutor executor) {
		setEngineLock.lock();
		if (isEngineSet()) {
			setEngineLock.unlock();
			return;
		}
		if (viewer != null) {
			this.executor = executor;
			// Assign engine to the UI
			viewer.setScriptExecutor(executor);
		}
		setEngineLock.unlock();
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
					viewer.getScriptExecutor().interrupt();
				}
			}
		};
		interruptAction.setImageDescriptor(InternalImage.INTERRUPT.getDescriptor());
		getViewSite().getActionBars().getToolBarManager().add(interruptAction);
		
		/*********************************************************************
		 * Enable content assist (code completion)
		 *********************************************************************/
		if ((viewer.getStyle() & ICommandLineViewer.NO_INPUT_TEXT) == 0) {
			Action contentAssistAction = new Action("Enable Code Completion", IAction.AS_CHECK_BOX) {
				public void run() {
					viewer.setContentAssistEnabled(isChecked());
				}
			};
			contentAssistAction.setImageDescriptor(InternalImage.CONTENT_ASSIST.getDescriptor());
			viewer.setContentAssistEnabled(true);
			contentAssistAction.setChecked(true);
			getViewSite().getActionBars().getToolBarManager().add(contentAssistAction);
		}
		
		/*********************************************************************
		 * Scroll lock
		 *********************************************************************/
		final Action scrollLockAction = new Action("Scroll Lock", IAction.AS_CHECK_BOX) {
			public void run() {
				viewer.setScrollLocked(isChecked());
			}
		};
		viewerEventHandler = new IEventHandler<CommandlLineViewerEvent>() {
			@Override
			public void handleEvent(CommandlLineViewerEvent event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						scrollLockAction.setChecked(viewer.isScrollLocked());
					}					
				});
			}
		};
		PlatformUtils.getPlatformEventBus().subscribe(viewer, viewerEventHandler);
		scrollLockAction.setImageDescriptor(InternalImage.SCROLL_LOCK.getDescriptor());
		getViewSite().getActionBars().getToolBarManager().add(scrollLockAction);

		/*********************************************************************
		 * Clear console text
		 *********************************************************************/
		Action clearAction = new Action("Clear Console", IAction.AS_PUSH_BUTTON) {
			public void run() {
				viewer.clearConsole();
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
		Action exportOutputAction = new ExportOutputAction(viewer, getSite().getShell()); 
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
						String[] histories = viewer.getCommandHistory();
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
	
	public boolean isEngineSet() {
		return executor != null;
	}
	
	public void dispose() {
		if (viewer != null && !viewer.isDisposed()) {
			// Shouldn't need to do this
			viewer.dispose();
		}
		if (viewerEventHandler != null) {
			PlatformUtils.getPlatformEventBus().unsubscribe(viewerEventHandler);
			viewerEventHandler = null; 
		}
		super.dispose();
	}
	
	@Override
	public void setFocus() {
		// If the engine is not ready at the time the UI is used, we set the default engine
		if (!isEngineSet()) {
			Thread setEngineExcutorThread = new Thread() {
				public void run() {
					if (!isEngineSet()) {
						// Create new scripting engine (default)
						setEngineExecutor(new ScriptExecutor());
					}
				}
			};
			setEngineExcutorThread.start();
		}
		if (viewer != null) {
			viewer.setFocus();
		}
	}

}
