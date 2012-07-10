package org.gumtree.ui.terminal.support;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ICommandLineTerminal;

public final class TerminalUI {

	/**
	 * Unique identifer for the command line terminal view.
	 */
	public static final String ID_VIEW_COMMAND_LINE_TERMINAL = "org.gumtree.ui.terminal.commandLineTerminal";

	/**
	 * Unique identifer for the default (telnet) communication adapter.
	 */
	public static final String ID_DEFAULT_ADAPTER = "org.gumtree.ui.terminal.telnetAdapter";

	public static ICommandLineTerminal openNewTerminalView() throws PartInitException {
		return openNewTerminalView(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}
	
	public static ICommandLineTerminal openNewTerminalView(IWorkbenchWindow window)
			throws PartInitException {
		String secondaryId = Integer.toString(CommandLineTerminal
				.getViewActivationCount());
		if (window == null) {
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		IViewPart part = window.getActivePage().showView(
						TerminalUI.ID_VIEW_COMMAND_LINE_TERMINAL, secondaryId,
						IWorkbenchPage.VIEW_ACTIVATE);
		if (part != null && part instanceof ICommandLineTerminal) {
			// increase count
			CommandLineTerminal.addViewActivationCount();
			return (ICommandLineTerminal) part;
		}
		return null;
	}

	public static ICommandLineTerminal openNewTerminalView(String adapterId,
			boolean autoConnect) throws PartInitException,
			CommunicationAdapterException {
		return openNewTerminalView(adapterId, autoConnect, null);
	}
			
	public static ICommandLineTerminal openNewTerminalView(String adapterId,
			boolean autoConnect, IWorkbenchWindow window) throws PartInitException,
			CommunicationAdapterException {
		ICommandLineTerminal terminal = openNewTerminalView(window);
		terminal.selectCommunicationAdapter(adapterId);
		if (autoConnect) {
			terminal.connect();
		}
		return terminal;
	}

	/**
	 * Private constructor to block instance creation.
	 */
	private TerminalUI() {
		super();
	}
}
