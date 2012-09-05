package org.gumtree.app.workbench.support;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;
import static ch.lambdaj.collection.LambdaCollections.with;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.gumtree.app.workbench.internal.Activator;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.service.applaunch.IAppLaunchDescriptor;
import org.gumtree.ui.service.applaunch.IAppLaunchRegistry;
import org.gumtree.widgets.IWidget;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaunchPadDialog implements IWidget {

	private static final Logger logger = LoggerFactory.getLogger(LaunchPadDialog.class);
	
	private IAppLaunchRegistry appLaunchRegistry;
	
	private IHandlerService handlerService;
	
	private ICommandService commandService;
	
	private Shell shell;
	
	public LaunchPadDialog(Shell parent, int style) {
		shell = new Shell(parent, style | SWT.APPLICATION_MODAL);
		shell.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		shell.setBackgroundMode(SWT.INHERIT_FORCE);
		shell.setAlpha(240);
		
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(shell);
		
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, shell);
		Font font = resourceManager.createDefaultFont(16, SWT.BOLD);
		
		Composite mainArea = new Composite(shell, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(true).applyTo(mainArea);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainArea);
		
		List<IAppLaunchDescriptor> descs = sort(with(getAppLaunchRegistry()
				.getAllAppLaunches()), on(IAppLaunchDescriptor.class)
				.getLabel());
		for (IAppLaunchDescriptor desc : descs) {
			Composite appLaunchHolder = new Composite(mainArea, SWT.NONE);
			GridLayoutFactory.swtDefaults().spacing(2, 2).applyTo(appLaunchHolder);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(appLaunchHolder);
			
			Label label = new Label(appLaunchHolder, SWT.NONE);
			label.setImage(desc.getIcon64().createImage());	// Any SWT resource leakage?
			label.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
			label.addMouseListener(new MouseListener(desc));
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(label);
			
			label = new Label(appLaunchHolder, SWT.NONE);
			label.setText(desc.getLabel());
			label.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			label.setFont(font);
			label.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
			label.addMouseListener(new MouseListener(desc));
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(label);
			
		}

		Button button = new Button(shell, SWT.PUSH);
		button.setText("Close");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(button);
		
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}
	
	// TODO: use e4 context injection later
	private IAppLaunchRegistry getAppLaunchRegistry() {
		if (appLaunchRegistry == null) {
			appLaunchRegistry = ServiceUtils.getService(IAppLaunchRegistry.class);
		}
		return appLaunchRegistry;
	}
	
	private IHandlerService getHandlerService() {
		if (handlerService == null) {
			try {
				handlerService = (IHandlerService) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow()
						.getService(IHandlerService.class);
			} catch (Exception e) {
				logger.warn("Failed to obtain handler service from active workbench window", e);
			}
		}
		return handlerService;
	}
	
	private ICommandService getCommandService() {
		if (commandService == null) {
			try {
				commandService = (ICommandService) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow()
						.getService(ICommandService.class);
			} catch (Exception e) {
				logger.warn("Failed to obtain command service from active workbench window", e);
			}
		}
		return commandService;
	}
	
	public Shell getShell() {
		return shell;
	}
	
	private void dispose() {
		shell = null;
		appLaunchRegistry = null;
		handlerService = null;
		commandService = null;
	}
	
	private class MouseListener extends MouseAdapter {
	
		private IAppLaunchDescriptor desc;
		
		private MouseListener(IAppLaunchDescriptor desc) {
			this.desc = desc;
		}
		
		public void mouseDown(MouseEvent e) {
			shell.close(); 
			try {
				if (desc.hasParameters()) {
					Command command = getCommandService().getCommand(desc.getCommandId());
					List<Parameterization> params = new ArrayList<Parameterization>(2); 
					for (Entry<String, String> entry : desc.getParameters().entrySet()) {
						params.add(new Parameterization(command.getParameter(entry.getKey()), entry.getValue()));
					}
					ParameterizedCommand parmCommand = new ParameterizedCommand(command,
							params.toArray(new Parameterization[params.size()]));
					getHandlerService().executeCommand(parmCommand, null);
				} else {
					getHandlerService().executeCommand(desc.getCommandId(), null);
				}
			} catch (Exception e1) {
				logger.error("Failed to execute " + desc.getCommandId(), e);
			}
		}
	}
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		
		LaunchPadDialog dialog = new LaunchPadDialog(shell, SWT.APPLICATION_MODAL);
		dialog.getShell().setSize(500, 500);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
