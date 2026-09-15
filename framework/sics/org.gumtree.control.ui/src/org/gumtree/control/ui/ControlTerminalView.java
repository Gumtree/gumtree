/*******************************************************************************
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package org.gumtree.control.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.ui.viewer.model.DefaultControllerNode;
import org.gumtree.ui.terminal.ITerminalOutputBuffer.OutputStyle;
import org.gumtree.ui.terminal.support.CommandLineTerminal;
import org.gumtree.ui.terminal.support.TerminalText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author nxi
 * Created on 19/02/2009
 */
public class ControlTerminalView extends CommandLineTerminal {

	private static Logger logger = LoggerFactory.getLogger(ControlTerminalView.class);

	public static final String SICS_ZMQ_ADAPTOR_ID = "org.gumtree.control.ui.ZMQAdapter";
	private Composite parent = null;

	// Devices being monitored, keyed by controller path
	private Map<String, DeviceMonitor> monitors = new LinkedHashMap<String, DeviceMonitor>();

	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 *
	 */
	public ControlTerminalView() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.internal.terminal.CommandLineTerminal#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		this.parent = parent;
		// Devices can be dropped here from the control viewer, also before the
		// terminal is connected and the text display exists
		createDropTarget(parent);
		Thread thread = new Thread(){
			boolean isConnected = false;
			boolean isFirstConnection = true;
			@Override
			public void run() {
				try {
					while(!isDisposed()){
						ISicsProxy proxy = SicsManager.getSicsProxy();
						boolean connectionStatus = proxy != null && proxy.isConnected()
								&& proxy.getServerStatus() != ServerStatus.UNKNOWN;
						if (connectionStatus != isConnected) {
							if (connectionStatus) {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										try {
											if (isFirstConnection) {
												selectCommunicationAdapter(SICS_ZMQ_ADAPTOR_ID);
												isFirstConnection = false;
											}
											connect();
											isConnected = true;
										} catch (Exception e) {
										}
									}
								});
								int time = 0;
								while (!isConnected && time < 15000) {
									Thread.sleep(1500);
									time += 1500;
								}
							} else {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										try {
											disconnect();
											isConnected = false;
										} catch (Exception e) {
										}
									}
								});
								int time = 0;
								while (isConnected && time < 15000) {
									Thread.sleep(1500);
									time += 1500;
								}
							}
						}
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();

//		SafeRunner.run(new ISafeRunnable() {
//
//			@Override
//			public void run() throws Exception {
//				while(SicsCore.getSicsController() == null){
//					Thread.sleep(500);
//				}
//				selectCommunicationAdapter(SICS_TELNET_ADAPTOR_ID);
//				connect();
//			}
//
//			@Override
//			public void handleException(Throwable exception) {
//				exception.printStackTrace();
//			}
//		});
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.terminal.support.CommandLineTerminal#handleTextDisplayCreated(org.gumtree.ui.terminal.support.TerminalText)
	 */
	@Override
	protected void handleTextDisplayCreated(TerminalText textDisplay) {
		// The text display is recreated on every connection, so the drop target
		// and the context menu have to be attached again
		createDropTarget(textDisplay);
		createMonitorContextMenu(textDisplay);
	}

	private void createDropTarget(Control control) {
		DropTarget dropTarget = null;
		try {
			dropTarget = new DropTarget(control, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		} catch (SWTError error) {
			// The control already holds a drop target, nothing else can be done
			logger.warn("failed to create a drop target for the terminal view", error);
			return;
		}
		dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT || event.detail == DND.DROP_NONE) {
					event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY : DND.DROP_MOVE;
				}
			}
			public void dragOperationChanged(DropTargetEvent event) {
				dragEnter(event);
			}
			public void drop(DropTargetEvent event) {
				ISelection selection = null;
				if (event.data instanceof ISelection) {
					selection = (ISelection) event.data;
				} else {
					// Some platforms do not fill in the data of a local transfer
					selection = LocalSelectionTransfer.getTransfer().getSelection();
				}
				if (selection instanceof IStructuredSelection) {
					handleDrop((IStructuredSelection) selection);
				}
			}
		});
	}

	private void createMonitorContextMenu(TerminalText textDisplay) {
		MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				fillMonitorContextMenu(menuManager);
			}
		});
		Menu menu = manager.createContextMenu(textDisplay);
		textDisplay.setMenu(menu);
	}

	private void fillMonitorContextMenu(IMenuManager manager) {
		if (monitors.isEmpty()) {
			Action emptyAction = new Action("No monitored device") {
			};
			emptyAction.setEnabled(false);
			manager.add(emptyAction);
			return;
		}
		for (final DeviceMonitor monitor : new ArrayList<DeviceMonitor>(monitors.values())) {
			manager.add(new Action("Stop monitoring " + monitor.getLabel()) {
				public void run() {
					stopMonitoring(monitor.getPath());
				}
			});
		}
		manager.add(new Separator());
		manager.add(new Action("Stop monitoring all devices") {
			public void run() {
				stopAllMonitoring();
			}
		});
	}

	private void handleDrop(IStructuredSelection selection) {
		for (Object element : selection.toList()) {
			if (!(element instanceof DefaultControllerNode)) {
				continue;
			}
			ISicsController controller = ((DefaultControllerNode) element).getController();
			if (controller == null) {
				continue;
			}
			List<IDynamicController> targets = new ArrayList<IDynamicController>();
			if (controller instanceof IDynamicController) {
				targets.add((IDynamicController) controller);
			} else {
				// A group node monitors its immediate dynamic children
				for (ISicsController child : controller.getChildren()) {
					if (child instanceof IDynamicController) {
						targets.add((IDynamicController) child);
					}
				}
			}
			if (targets.isEmpty()) {
				printMessage(controller.getPath() + " has no value to monitor");
				continue;
			}
			for (IDynamicController target : targets) {
				startMonitoring(target);
			}
		}
	}

	private void startMonitoring(IDynamicController controller) {
		String path = controller.getPath();
		if (monitors.containsKey(path)) {
			printMessage(path + " is being monitored already");
			return;
		}
		DeviceMonitor monitor = new DeviceMonitor(controller);
		monitors.put(path, monitor);
		controller.addControllerListener(monitor);
		printMessage("Monitoring " + monitor.getLabel() + " (right click to stop)");
		// Show the current value straight away
		try {
			printMessage(monitor.getLabel() + " value = " + format(controller.getValue(), controller));
		} catch (Exception e) {
			// Value is not available yet, wait for the next update event
		}
	}

	private void stopMonitoring(String path) {
		DeviceMonitor monitor = monitors.remove(path);
		if (monitor != null) {
			monitor.getController().removeControllerListener(monitor);
			printMessage("Stopped monitoring " + monitor.getLabel());
		}
	}

	private void stopAllMonitoring() {
		for (DeviceMonitor monitor : new ArrayList<DeviceMonitor>(monitors.values())) {
			stopMonitoring(monitor.getPath());
		}
	}

	private String format(Object value, IDynamicController controller) {
		String text = String.valueOf(value);
		String units = controller.getUnits();
		if (units != null && units.trim().length() > 0) {
			text += " " + units;
		}
		return text;
	}

	private void printMessage(String message) {
		final String text = timeFormat.format(new Date()) + " " + message;
		Display display = Display.getDefault();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			public void run() {
				TerminalText textDisplay = getTextDisplay();
				if (textDisplay != null && !textDisplay.isDisposed()) {
					textDisplay.appendOutputLine(text, OutputStyle.NORMAL);
				}
			}
		});
	}

	private boolean isDisposed() {
		return parent.isDisposed();
	}

	@Override
	public void dispose() {
		stopAllMonitoring();
		super.dispose();
	}

	/**
	 * Prints every update event of a dropped device to the terminal text.
	 */
	private class DeviceMonitor implements ISicsControllerListener {

		private IDynamicController controller;

		private String label;

		private DeviceMonitor(IDynamicController controller) {
			this.controller = controller;
			String deviceId = controller.getDeviceId();
			label = deviceId != null && deviceId.trim().length() > 0 ? deviceId : controller.getPath();
		}

		private IDynamicController getController() {
			return controller;
		}

		private String getLabel() {
			return label;
		}

		private String getPath() {
			return controller.getPath();
		}

		@Override
		public void updateValue(Object oldValue, Object newValue) {
			printMessage(label + " value = " + format(newValue, controller));
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
			printMessage(label + " target = " + format(newValue, controller));
		}

		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
			printMessage(label + " state = " + newState);
		}

		@Override
		public void updateEnabled(boolean isEnabled) {
			printMessage(label + (isEnabled ? " enabled" : " disabled"));
		}
	}
}
