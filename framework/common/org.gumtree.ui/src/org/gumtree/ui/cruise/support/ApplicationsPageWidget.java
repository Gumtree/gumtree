package org.gumtree.ui.cruise.support;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.gumtree.ui.service.applaunch.IAppLaunchDescriptor;
import org.gumtree.ui.service.applaunch.IAppLaunchRegistry;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationsPageWidget extends AbstractCruisePageWidget {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationsPageWidget.class);
	
	@Inject
	private IAppLaunchRegistry appLaunchRegistry;

	// TODO: can it be injected by e4 context?
	private ICommandService commandService;
	
	// TODO: can it be injected by e4 context?
	private IHandlerService handlerService;
	
	private int numberOfColumn;
	
	public ApplicationsPageWidget(Composite parent, int style) {
		super(parent, style);
		setNumberOfColumn(2);
	}

	public void render() {
		List<IAppLaunchDescriptor> descs = sort(getAppLaunchRegistry()
				.getAllAppLaunches(), on(IAppLaunchDescriptor.class).getLabel());
		
		GridLayoutFactory.swtDefaults().numColumns(getNumberOfColumn()).applyTo(this);
		for (IAppLaunchDescriptor desc : descs) {
			Composite appLaunchHolder = getWidgetFactory().createComposite(this);
			appLaunchHolder.setBackgroundMode(SWT.INHERIT_FORCE);
			GridLayoutFactory.swtDefaults().spacing(2, 2).applyTo(appLaunchHolder);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(appLaunchHolder);
			
			Label label = getWidgetFactory().createLabel(appLaunchHolder, "");
			label.setImage(desc.getIcon64().createImage());	// Any SWT resource leakage?
			label.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
			label.addMouseListener(new MouseListener(desc));
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(label);
			
			label = getWidgetFactory().createLabel(appLaunchHolder, "");
			label.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			label.setText(desc.getLabel());
			label.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
			label.addMouseListener(new MouseListener(desc));
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(label);
		}
	}
	
	@Override
	protected void disposeWidget() {
	}
	
	
	public int getNumberOfColumn() {
		return numberOfColumn;
	}

	public void setNumberOfColumn(int numberOfColumn) {
		this.numberOfColumn = numberOfColumn;
	}

	/*************************************************************************
	 * Helper utilities
	 *************************************************************************/
	
	private class MouseListener extends MouseAdapter {
		
		private IAppLaunchDescriptor desc;
		
		private MouseListener(IAppLaunchDescriptor desc) {
			this.desc = desc;
		}
		
		public void mouseDown(MouseEvent e) {
			try {
				if (desc.hasParameters()) {
					Command command = getCommandService().getCommand(desc.getCommandId());
					List<Parameterization> params = new ArrayList<Parameterization>(2); 
					for (Entry<String, String> entry : desc.getParameters().entrySet()) {
						params.add(new Parameterization(command.getParameter(entry.getKey()), entry.getValue()));
					}
					ParameterizedCommand parmCommand = new ParameterizedCommand(
							command, params.toArray(new Parameterization[params.size()]));
					getHandlerService().executeCommand(parmCommand, null);
				} else {
					getHandlerService().executeCommand(desc.getCommandId(), null);
				}
			} catch (Exception e1) {
				logger.error("Failed to execute " + desc.getCommandId(), e);
			}
		}
	}
	
	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	public IAppLaunchRegistry getAppLaunchRegistry() {
		return appLaunchRegistry;
	}
	
	public void setAppLaunchRegistry(IAppLaunchRegistry appLaunchRegistry){
		this.appLaunchRegistry = appLaunchRegistry;
	}
	
	public ICommandService getCommandService() {
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

}
