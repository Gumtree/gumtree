package org.gumtree.gumnix.sics.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsIOConsoleManager extends SicsProxyListenerAdapter {

	private static int TEXT_LIMIT = 1000;

	private static SicsIOConsoleManager singleton;

	private static Logger logger;

	private Map<String, ConsoleContent> consoleContentMap;

	private SicsIOConsoleManager() {
		super();
	}

	public static SicsIOConsoleManager getDefault() {
		if(singleton == null) {
			singleton = new SicsIOConsoleManager();
		}
		return singleton;
	}

	public void activate() {
		SicsCore.getDefaultProxy().addProxyListener(this);
	}

	public void stop() {
		SicsCore.getDefaultProxy().removeProxyListener(this);
//		for(MessageConsole console : getConsoleMap().values()) {
//			console.destroy();
//		}
		consoleContentMap = null;
	}

	public synchronized void messageReceived(final String message, String channelId) {
		ConsoleContent content = getConsoleContentMap().get(channelId);
		if(content == null) {
			content = addNewChannelConsole(channelId);
		}
		content.increaseBufferCount();
		if(content.getBufferCount() > 50) {
			content.getConsole().clearConsole();
			content.clearBufferCount();
		}
		final MessageConsoleStream fStream = content.getStream();
		final Display display = PlatformUI.getWorkbench().getDisplay();
		new Job("Proxy connected") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				display.asyncExec(new Runnable() {
					public void run() {
						// fStream.setColor(display.getSystemColor(SWT.COLOR_DARK_BLUE));
						if(message.length() > TEXT_LIMIT) {
							fStream.println(message.substring(0, TEXT_LIMIT) + "(..more)");
						} else {
							fStream.println(message);

						}
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}


	public synchronized void messageSent(final String message, String channelId) {
		ConsoleContent content = getConsoleContentMap().get(channelId);
		if(content == null) {
			content = addNewChannelConsole(channelId);
		}
		content.increaseBufferCount();
		if(content.getBufferCount() > 50) {
			content.getConsole().clearConsole();
			content.clearBufferCount();
		}
		final MessageConsoleStream fStream = content.getStream();
		final Display display = PlatformUI.getWorkbench().getDisplay();
		new Job("Proxy connected") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				display.asyncExec(new Runnable() {
					public void run() {
						// fStream.setColor(display.getSystemColor(SWT.COLOR_BLACK));
						fStream.print(">> ");
						// fStream.setColor(display.getSystemColor(SWT.COLOR_DARK_RED));
						fStream.println(message);
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	private synchronized ConsoleContent addNewChannelConsole(String channelId) {
		MessageConsole console = new MessageConsole("SICS IO (" + channelId + ")", null);
		ConsoleContent consoleContent = new ConsoleContent(console);
		getConsoleContentMap().put(channelId, consoleContent);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final MessageConsoleStream fStream = consoleContent.getStream();
		display.asyncExec(new Runnable() {
			public void run() {
				fStream.setColor(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
			}
		});
		return consoleContent;
	}

	private Map<String, ConsoleContent> getConsoleContentMap() {
		if(consoleContentMap == null) {
			consoleContentMap = new HashMap<String, ConsoleContent>();
		}
		return consoleContentMap;
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsIOConsoleManager.class);
		}
		return logger;
	}

}
