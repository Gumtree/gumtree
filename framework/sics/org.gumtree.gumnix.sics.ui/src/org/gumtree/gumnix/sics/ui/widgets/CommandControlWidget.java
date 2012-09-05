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

package org.gumtree.gumnix.sics.ui.widgets;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.dataaccess.SicsDataAccessUtils;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandControlWidget extends AbstractSicsDeviceWidget {

	private static final Logger logger = LoggerFactory.getLogger(CommandControlWidget.class);
	
	private URI commandURI;
	
	protected List<URI> deviceURIs = new ArrayList<URI>();
	
	public CommandControlWidget(Composite parent, int style) {
		super(parent, style);
	}
	
	protected void createUI() {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		GridLayoutFactory.swtDefaults().numColumns(3).spacing(5, 2).applyTo(this);
		
		/*********************************************************************
		 * Construct header
		 *********************************************************************/
		final ICommandController commandController = getDataAccessManager().get(commandURI, ICommandController.class);
		
		Composite headerComposite = getToolkit().createComposite(this);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(headerComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(headerComposite);
		
		Label label = getToolkit().createLabel(headerComposite, commandController.getId().toUpperCase());
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));
		
		Label statusLabel = getToolkit().createLabel(headerComposite, "", SWT.CENTER);
		statusLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusLabel);
		uriMap.put(SicsDataAccessUtils.createControllerStatusURI(commandController), statusLabel);
		
		Button runButton = getToolkit().createButton(headerComposite, "Run", SWT.PUSH);
		runButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					commandController.asyncExecute();
				} catch (SicsIOException e1) {
					logger.error("Failed to run command" + commandController.getDeviceId(), e1);
				}
			}
		});
		GridDataFactory.swtDefaults().hint(60, 20).applyTo(runButton);
		
		/*********************************************************************
		 * Construct arguments
		 *********************************************************************/
		for (IComponentController childController : commandController.getChildControllers()) {
			if (childController instanceof IDynamicController) {
				final IDynamicController controller = (IDynamicController) childController;
				
				Label argLabel = getToolkit().createLabel(this, "");
				argLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
				uriMap.put(SicsDataAccessUtils.createControllerIdURI(controller), argLabel);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(argLabel);
				
				URI controllerURI = SicsDataAccessUtils.createControllerURI(controller);
				Label currentLabel = getToolkit().createLabel(this, "", SWT.RIGHT);
				uriMap.put(controllerURI, currentLabel);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(80, SWT.DEFAULT).applyTo(currentLabel);
				
				final URI controllerTargetURI = SicsDataAccessUtils.createControllerTargetURI(controller);
				String valuesString = null;
				try {
					valuesString = getDataAccessManager().get(
							SicsDataAccessUtils.createControllerValuesURI(controller),
							String.class);
				} catch (Exception e) {
				}
				if (valuesString != null) {
					final ComboViewer comboViewer = new ComboViewer(this, SWT.NONE);
					comboViewer.setContentProvider(new ArrayContentProvider());
					comboViewer.setLabelProvider(new LabelProvider());
					List<String> values = new ArrayList<String>();
					for (String value : valuesString.split(",")) {
						values.add(value.trim());
					}
					comboViewer.setInput(values.toArray(new String[values.size()]));
					// Read from selection
					comboViewer.getCombo().addFocusListener(new FocusAdapter() {
						public void focusLost(FocusEvent e) {
							String selection = (String) ((IStructuredSelection) comboViewer
									.getSelection()).getFirstElement();
							if (selection != null) {
								String currentValue = getDataAccessManager()
										.get(controllerTargetURI, String.class);
								// Drive only on change in target
								if (!selection.equals(currentValue)) {
									driveDevice(controller, selection);
								}
							}
						}
					});
					// Read from custom text
					comboViewer.getCombo().addKeyListener(new KeyAdapter() {
						public void keyReleased(KeyEvent e) {
							if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
								driveDevice(controller, comboViewer.getCombo().getText());
							}
						}
					});
					uriMap.put(controllerTargetURI, comboViewer);
					GridDataFactory.fillDefaults().hint(80, SWT.DEFAULT).applyTo(comboViewer.getControl());
				} else {
					final Text targetText = getToolkit().createText(this, "");
					uriMap.put(SicsDataAccessUtils.createControllerTargetURI(controller), targetText);
					GridDataFactory.fillDefaults().hint(80, SWT.DEFAULT).applyTo(targetText);
					targetText.addKeyListener(new KeyAdapter() {
						public void keyReleased(KeyEvent e) {
							if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
								driveDevice(controller, targetText.getText());
							}
						}
					});
				}
			}
		}
		
		/*********************************************************************
		 * Fetch initial values
		 *********************************************************************/
		for (Entry<URI, Object> entry : uriMap.entrySet()) {
			URI uri = entry.getKey();
			Object widget = entry.getValue();
			Class<?> representation = String.class;
			if ("status".equals(uri.getQuery())) {
				representation = ControllerStatus.class;
			} 
			updateWidgetData(uri, widget, getDataAccessManager().get(uri, representation));
		}
		
		/*********************************************************************
		 * Finalise
		 *********************************************************************/
		getParent().layout(true, true);
	}

	private void driveDevice(IDynamicController controller, String target) {
		controller.setTargetValue(ComponentData.createData(target));
		try {
			controller.commitTargetValue(null);
		} catch (SicsIOException e1) {
			logger.error("Failed to drive." + e1);
		}
	}
	
	protected void initialise() {
	}

	protected void updateWidgetData(URI uri, Object widget, Object data) {
		// Status update
		if (data instanceof ControllerStatus && widget instanceof Label) {
			Label label = (Label) widget;
			ControllerStatus status = (ControllerStatus) data;
			if (status.equals(ControllerStatus.OK)) {
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				label.setText("");
			} else if (status.equals(ControllerStatus.RUNNING)) {
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				label.setText("RUNNING");
			} else if (status.equals(ControllerStatus.ERROR)) {
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				label.setText("ERROR");
			}
			return;
		}
		// Other cases
		if (widget instanceof Label) {
			((Label) widget).setText(data.toString());
			layout(true, true);
		} else if (widget instanceof Text) {
			((Text) widget).setText(data.toString());
			layout(true, true);
		} else if (widget instanceof ComboViewer) {
			((ComboViewer) widget).setSelection(new StructuredSelection(data));
			layout(true, true);
		}
	}
	
	public void setCommandURI(String commandURI) {
		this.commandURI = URI.create(commandURI);
	}

}
