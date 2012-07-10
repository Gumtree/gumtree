package org.gumtree.ui.terminal.support;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.util.workbench.ViewLaunchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalLaunchAction extends ViewLaunchAction {

	private static Logger logger;

	private IWorkbenchWindow window;
	
	public TerminalLaunchAction() {
		super();
	}

	public TerminalLaunchAction(String text, ImageDescriptor image) {
		super(text, image);
	}
	
	public TerminalLaunchAction(String text, ImageDescriptor image, IWorkbenchWindow window) {
		super(text, image);
		this.window = window;
	}
	
	public void run() {
		if(getViewId() == null) {
			return;
		}
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					TerminalUI.openNewTerminalView(getAdapterId(), isAutoConnect(), window);
				} catch (CommunicationAdapterException e) {
					getLogger().error("Cannot launch terminal", e);
				} catch (PartInitException e) {
					getLogger().error("Cannot launch terminal", e);
				}
			}
		});
	}

	@Override
	public String getViewId() {
		return TerminalUI.ID_VIEW_COMMAND_LINE_TERMINAL;
	}

	protected String getAdapterId() {
		return TerminalUI.ID_DEFAULT_ADAPTER;
	}

	protected boolean isAutoConnect() {
		return false;
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(TerminalLaunchAction.class);
		}
		return logger;
	}
}
