/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.nbi.ui.widgets;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.CommandStatus;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.control.events.SicsControllerEvent;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.service.eventbus.IFilteredEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.ui.util.resource.UIResources;
import org.gumtree.util.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;

public class HMControlWidget extends FormControlWidget {
	
	private static final Logger logger = LoggerFactory.getLogger(HMControlWidget.class);
	
	private static final String PATH_HISTMEM = "/commands/histogram/histmem";
	
	private ISicsProxyListener proxyListener;
	
	private IDataAccessManager dam;
	
	private IFilteredEventHandler<SicsControllerEvent> eventHandler;
	
	private ICommandController commandController;
	
	private Map<URI, Object> controlMap;
	
	public HMControlWidget(Composite parent, int style) {
		super(parent, style);
		init();
	}

	private void bindProxy() {
		// Get command controller
		Job job = new Job("Get command controller") {
			protected IStatus run(IProgressMonitor monitor) {
				// Check if SICS is ready
				if (SicsCore.getSicsController() == null) {
					schedule(500);
					return Status.OK_STATUS;
				}
				// Get controller
				commandController = (ICommandController) SicsCore
						.getSicsController().findComponentController(
								PATH_HISTMEM);
				// Create UI
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						createUI();
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	private void unbindProxy() {
		commandController = null;
		// Disable UI
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				HMControlWidget.this.setEnabled(false);
			}
		});
	}
	
	protected void init() {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		controlMap = new LinkedHashMap<URI, Object>();
		
		/*********************************************************************
		 * Setup data access manager
		 *********************************************************************/
		dam = ServiceUtils.getService(IDataAccessManager.class);
		
		/*********************************************************************
		 * Setup event handler
		 *********************************************************************/
		eventHandler = new IFilteredEventHandler<SicsControllerEvent>() {
			public void handleEvent(SicsControllerEvent event) {
				Class<?> representation = String.class;
				if ("status".equals(event.getURI().getQuery())) {
					representation = ControllerStatus.class;
				}
				updateData(event.getURI(), dam.get(event.getURI(), representation));
			}
			public boolean isDispatchable(SicsControllerEvent event) {
				return controlMap.containsKey(event.getURI());
			}
		};
		PlatformUtils.getPlatformEventBus().subscribe(eventHandler);
		
		
		/*********************************************************************
		 * Setup SICS
		 *********************************************************************/
		// Connect now
		if (SicsCore.getDefaultProxy().isConnected()) {
			bindProxy();
		}
		
		// Setup to handle dynamic connection
		proxyListener = new SicsProxyListenerAdapter() {
			public void proxyConnected() {
				bindProxy();
			}
			public void proxyDisconnected() {
				unbindProxy();
			}
		};
		SicsCore.getDefaultProxy().addProxyListener(proxyListener);
	}

	private void createUI() {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		for (Control child : this.getChildren()) {
			child.dispose();
		}
		setEnabled(true);
		controlMap.clear();
		
		/*********************************************************************
		 * Setup UI
		 *********************************************************************/
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(this);
		
		Label label = getToolkit().createLabel(this, "Mode");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));
		GridDataFactory.swtDefaults().hint(45, SWT.DEFAULT).applyTo(label);
		
		label = getToolkit().createLabel(this, "Preset");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));
		GridDataFactory.swtDefaults().hint(45, SWT.DEFAULT).applyTo(label);
		
		final Button controlButton = getToolkit().createButton(this, "", SWT.PUSH);
		controlButton.setImage(InternalImage.START_32.getImage());
		GridDataFactory.swtDefaults().span(1, 2).applyTo(controlButton);
		controlMap.put(URI.create("sics://hdb" + PATH_HISTMEM + "/feedback/status"), controlButton);
		
		Button pauseButton = getToolkit().createButton(this, "", SWT.PUSH);
		pauseButton.setImage(InternalImage.PAUSE_32.getImage());
		GridDataFactory.swtDefaults().span(1, 2).applyTo(pauseButton);
		
		final ComboViewer comboViewer = new ComboViewer(this);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider());
		URI availableModeURI = URI.create("sics://hdb" + PATH_HISTMEM + "/mode?values");
		String[] availableModes = dam.get(availableModeURI, String.class).split(",");
		for (int i = 0; i < availableModes.length; i++) {
			availableModes[i] = availableModes[i].trim();
		}
		comboViewer.setInput(availableModes);
		GridDataFactory.swtDefaults().hint(45, SWT.DEFAULT).applyTo(comboViewer.getControl());
		controlMap.put(URI.create("sics://hdb" + PATH_HISTMEM + "/mode"), comboViewer);
		
		final Text presetText = getToolkit().createText(this, "");
		GridDataFactory.swtDefaults().hint(45, SWT.DEFAULT).applyTo(presetText);
		controlMap.put(URI.create("sics://hdb" + PATH_HISTMEM + "/preset"), presetText);
		
		/*********************************************************************
		 * Button logics
		 *********************************************************************/
		controlButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					if (controlButton.getData("status") != null && controlButton.getData("status").equals("idle")) {
						SicsCore.getSicsController().setValue(
								PATH_HISTMEM + "/cmd",
								ComponentData.createData("start"));
						SicsCore.getSicsController().setValue(
								PATH_HISTMEM + "/preset",
								ComponentData.createData(presetText.getText()));
						SicsCore.getSicsController().setValue(
								PATH_HISTMEM + "/mode",
								ComponentData.createData(
										(String) ((StructuredSelection) comboViewer.getSelection()).getFirstElement()));
						commandController.asyncExecute();
					} else {
						SicsCore.getSicsController().setValue(
								PATH_HISTMEM + "/cmd",
								ComponentData.createData("stop"));
						commandController.asyncExecute();
					}
				} catch (SicsIOException e) {
					logger.error("Failed to pause.", e);
				}
				
			}
		});
		
		pauseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (commandController != null) {
					try {
						SicsCore.getSicsController().setValue(
								PATH_HISTMEM + "/cmd",
								ComponentData.createData("pause"));
						commandController.asyncExecute();
					} catch (SicsIOException e) {
						logger.error("Failed to pause.", e);
					}
				}
			}
		});
		
		/*********************************************************************
		 * Update content
		 *********************************************************************/
		for (URI uri : controlMap.keySet()) {
			updateData(controlMap.get(uri), dam.get(uri, String.class));
		}

		/*********************************************************************
		 * Layout
		 *********************************************************************/
		this.layout(true, true);
	}
	
	private void updateData(final Object control, final Object data) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (control instanceof ComboViewer) {
					// Update Mode
					((ComboViewer) control).setSelection(new StructuredSelection(data));
				} else if (control instanceof Text) {
					// Update preset
					((Text) control).setText(data.toString());
				} else if (control instanceof Button) {
					// Command status
					if (CommandStatus.valueOf((String) data).equals(CommandStatus.IDLE)) {
						((Button) control).setImage(InternalImage.START_32.getImage());
						((Button) control).setData("status", "idle");
					} else {
						((Button) control).setImage(InternalImage.STOP_32.getImage());
						((Button) control).setData("status", "running");
					}
				}
			}			
		});
	}
	
	protected void widgetDispose() {
		if (proxyListener != null) {
			SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		if (eventHandler != null) {
			PlatformUtils.getPlatformEventBus().unsubscribe(eventHandler);
			eventHandler = null;
		}
		if (controlMap != null) {
			controlMap.clear();
			controlMap = null;
		}
		dam = null;	
		commandController = null;
	}

	public void afterParametersSet() {
	}

}
