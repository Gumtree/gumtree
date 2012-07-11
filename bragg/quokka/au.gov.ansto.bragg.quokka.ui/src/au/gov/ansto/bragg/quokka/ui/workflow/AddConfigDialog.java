/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.ui.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfigTemplate;
import au.gov.ansto.bragg.quokka.experiment.util.ExperimentModelUtils;
import au.gov.ansto.bragg.quokka.ui.QuokkaUIConstants;
import au.gov.ansto.bragg.quokka.ui.internal.Activator;

public class AddConfigDialog extends MessageDialog {

	private static Logger logger = LoggerFactory.getLogger(AddConfigDialog.class);
	
	private List<InstrumentConfigTemplate> standardTemplates;
	
	private List<InstrumentConfigTemplate> allTemplates;
	
	private TableViewer tableViewer;
	
	private InstrumentConfigTemplate selectedConfig;
	
	private Button removeButton;
	
	private boolean isNewConfig = false;
	
	public AddConfigDialog(Shell parentShell, List<InstrumentConfigTemplate> standardTemplates) {
		super(parentShell, "Select instrument configuration", null, "Select a new instrument configuration: ",
				NONE, new String[] {
                IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		if (standardTemplates == null) {
			this.standardTemplates = new ArrayList<InstrumentConfigTemplate>();
		} else {
			this.standardTemplates = standardTemplates;
		}
		allTemplates = new ArrayList<InstrumentConfigTemplate>();
		loadConfigs();
	}
	
	protected Control createCustomArea(Composite parent) {
		Composite mainArea = new Composite(parent, SWT.NONE);
		mainArea.setLayout(new GridLayout(3, false));
		
		Button templateButton = new Button(mainArea, SWT.RADIO);
		templateButton.setText("From an existing configuration");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(templateButton);
		templateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tableViewer.getTable().setEnabled(true);
				isNewConfig = false;
			}
		});

		
		Label label = new Label(mainArea, SWT.NONE);
		label.setText("                  ");
		
		Button newButton = new Button(mainArea, SWT.RADIO);
		newButton.setText("New an empty configuration");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(newButton);
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tableViewer.getTable().setEnabled(false);
				isNewConfig = true;
			}
		});
		
		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL);
		Table table = tableViewer.getTable();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(800, SWT.DEFAULT).span(3, 1).applyTo(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof InstrumentConfigTemplate) {
					InstrumentConfigTemplate template = (InstrumentConfigTemplate) element;
					if (template.isStandard()) {
						return "[STANDARD] " + template.getName();
					} else {
						return template.getName() + " - " + template.getDescription();
					}
				}
				return super.getText(element);
			}
		});
		tableViewer.setInput(allTemplates.toArray(new InstrumentConfigTemplate[allTemplates.size()]));
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedConfig = (InstrumentConfigTemplate) ((IStructuredSelection) event.getSelection()).getFirstElement();
				// Enable remove button for non standard config
				if (selectedConfig != null) {
					removeButton.setEnabled(!selectedConfig.isStandard());
				}
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// Double click on selection is same as pressing OK (close the dialog)
				buttonPressed(OK);
			}			
		});
		
		return mainArea;
	}
	
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);	
		GridLayout layout = new GridLayout();
		layout.numColumns = 1; // this is incremented by createButton
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0; //convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = 0; //convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		
		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);
		return composite;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		removeButton = new Button(parent, SWT.PUSH);
		removeButton.setText("Remove user defined");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!selectedConfig.isStandard() && selectedConfig.getFile() != null) {
//					// Open confirmation dialog
//					boolean removeOK = MessageDialog.openConfirm(getParentShell(),
//							"Remove user defined config",
//							"Do you want to remove config "
//									+ selectedConfig.getName() + "?");
//					// Remove config file
//					if (removeOK) {
						System.out.println("Removal: " + selectedConfig.getFile().delete());
						loadConfigs();
						tableViewer.setInput(allTemplates.toArray(new InstrumentConfigTemplate[allTemplates.size()]));
						tableViewer.refresh();
						removeButton.setEnabled(false);
//					}
				}
			}
		});
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).applyTo(removeButton);
		super.createButtonsForButtonBar(parent);
	}
	 
	public boolean isNewConfig() {
		return isNewConfig;
	}
	
	public InstrumentConfigTemplate getSelectedConfig() {
		return selectedConfig;
	}
	
	// Loads user defined config from templates directory
	private void loadConfigs() {
		// Initialises
		allTemplates.clear();
		
		// Loads standard templates
		if (standardTemplates != null) {
			allTemplates.addAll(standardTemplates);
		}
		
		// Loads user defined
		File templatesFolder = Activator.getDefault().getStateLocation().append(QuokkaUIConstants.PATH_TEMPLATES).toFile();
		if (templatesFolder.exists()) {
			for (File templateFile : templatesFolder.listFiles()) {
				if (templateFile.getName().endsWith(".xml")) {
					try {
						FileReader reader = new FileReader(templateFile);
						Object result = ExperimentModelUtils.getXStream().fromXML(reader);
						if (result instanceof InstrumentConfigTemplate) {
							allTemplates.add((InstrumentConfigTemplate) result);
						}
						reader.close();
					} catch (FileNotFoundException e) {
						logger.warn("Failed to read config from " + templateFile.getName(), e);
					} catch (IOException e) {
						logger.warn("Failed to read config from " + templateFile.getName(), e);
					}
				}
			}
		}
	}
	
}
