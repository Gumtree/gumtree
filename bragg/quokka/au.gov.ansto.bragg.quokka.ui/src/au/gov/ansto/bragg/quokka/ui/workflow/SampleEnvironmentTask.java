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

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironmentPreset;
import au.gov.ansto.bragg.quokka.ui.internal.InternalImage;

public class SampleEnvironmentTask extends AbstractExperimentTask {

	@Override
	protected ITaskView createViewInstance() {
		return new SampleEnvironmentTaskView();
	}

	// Column data structure
	private enum Column {
		NUMBER(0), PRESET(1), WAIT_TIME(2);

		private int index;
		public int getIndex() {
			return index;
		}
		private Column(int index) {
			this.index = index;
		}
	}
	
	private class SampleEnvironmentTaskView extends AbstractTaskView {
		
		private Composite controlledEnvArea;
		
		private Font boldFont;
		
		private Font normalFont;
		
		private Button normalEnvButton;
		
		private Button controlledEnvButton;
		
		@Override
		public void createPartControl(Composite parent) {
			parent.setLayout(new GridLayout());
			boldFont = UIResources.getDefaultFont(SWT.BOLD);
			normalFont = UIResources.getDefaultFont();
			
			/*****************************************************************
			 * Normal Environment 
			 *****************************************************************/
			normalEnvButton = getToolkit().createButton(parent, "Normal Environment", SWT.RADIO);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(normalEnvButton);
			getToolkit().createLabel(parent, "", SWT.NONE);
			
			/*****************************************************************
			 * Controlled Environment
			 *****************************************************************/
			controlledEnvButton = getToolkit().createButton(parent, "Controlled Environment", SWT.RADIO);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(controlledEnvButton);
			controlledEnvButton.setEnabled(getExperiment().hasEnvironmentControllers());
			
			controlledEnvArea = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(controlledEnvArea);
			controlledEnvArea.setLayout(new GridLayout());
			
			/*****************************************************************
			 * Default settings
			 *****************************************************************/
			if (getExperiment().isControlledEnvironment()) {
				setNormalEnvEnable(false);
				setCustomEnvEnable(true);
				controlledEnvButton.setSelection(true);
				// Load existing sample environment
				for (SampleEnvironment sampleEnvironment : getExperiment().getSampleEnvironments()) {
					Composite composite = getToolkit().createComposite(controlledEnvArea);
					GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(composite);
					createEnvironmentElement(composite, sampleEnvironment);
				}
			} else {
				setNormalEnvEnable(true);
				setCustomEnvEnable(false);
				normalEnvButton.setSelection(true);
			}
			// Update UI
			refreshUI();
			
			/*****************************************************************
			 * Button logics
			 *****************************************************************/
			normalEnvButton.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					System.err.println("normal is selected");
					setNormalEnvEnable(true);
					setCustomEnvEnable(false);
//					if (normalEnvButton.getSelection()) {
						// Set model to normal environment mode
						getExperiment().setControlledAcquisition(false);
//					}
					// Update UI
					refreshUI();
				}
			});
//			normalEnvButton.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					System.err.println("normal is " + normalEnvButton.);
//					setNormalEnvEnable(true);
//					setCustomEnvEnable(false);
//					if (normalEnvButton.getSelection()) {
//						// Set model to normal environment mode
//						getExperiment().setControlledAcquisition(false);
//					}
//					// Update UI
//					refreshUI();
//				}
//			});
			controlledEnvButton.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void focusGained(FocusEvent e) {
					System.err.println("control is selected");
					setNormalEnvEnable(false);
					setCustomEnvEnable(true);
					getExperiment().setControlledAcquisition(true);
					// Add default
					if (getExperiment().getSampleEnvironments().size() == 0) {
						// Dispose on this new refresh
						for (Control control : controlledEnvArea.getChildren()) {
							control.dispose();
						}
						Composite composite = getToolkit().createComposite(controlledEnvArea);
						GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(composite);
						if (getExperiment().hasEnvironmentControllers()) {
							// Create new sample environment
							SampleEnvironment sampleEnvironment = new SampleEnvironment(getExperiment());
							sampleEnvironment.setControllerId(getExperiment().getSampleEnvControllerIds().get(0));
							getExperiment().getSampleEnvironments().add(sampleEnvironment);
							createEnvironmentElement(composite, sampleEnvironment);
						}
					}
					// Update UI
					refreshUI();
				}
			});
//			controlledEnvButton.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					System.err.println("control is " + normalEnvButton.getSelection());
//					setNormalEnvEnable(false);
//					setCustomEnvEnable(true);
//					getExperiment().setControlledAcquisition(true);
//					// Add default
//					if (controlledEnvButton.getSelection() && getExperiment().getSampleEnvironments().size() == 0) {
//						// Dispose on this new refresh
//						for (Control control : controlledEnvArea.getChildren()) {
//							control.dispose();
//						}
//						Composite composite = getToolkit().createComposite(controlledEnvArea);
//						GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(composite);
//						if (getExperiment().hasEnvironmentControllers()) {
//							// Create new sample environment
//							SampleEnvironment sampleEnvironment = new SampleEnvironment(getExperiment());
//							sampleEnvironment.setControllerId(getExperiment().getSampleEnvControllerIds().get(0));
//							getExperiment().getSampleEnvironments().add(sampleEnvironment);
//							createEnvironmentElement(composite, sampleEnvironment);
//						}
//					}
//					// Update UI
//					refreshUI();
//				}
//			});
		}
		
		// Creates block for each controller setting
		private void createEnvironmentElement(final Composite parent, final SampleEnvironment sampleEnvironment) {
			parent.setLayout(new GridLayout(4, false));
			
			/*****************************************************************
			 * Controller label
			 *****************************************************************/
			Label label = getToolkit().createLabel(parent, "Controller");
			label.setFont(boldFont);
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).span(3, 1).indent(SWT.DEFAULT, 7).applyTo(label);
			
			/*****************************************************************
			 * Presets area
			 *****************************************************************/
			// Need group to adapt form colour to tabfolder
			Group group = new Group(parent, SWT.NONE);
			group.setLayout(new FillLayout());
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).span(1, 3).applyTo(group);
			getToolkit().adapt(group);
			createPresetArea(group, sampleEnvironment);
			
			/*****************************************************************
			 * Controller selection combo 
			 *****************************************************************/
			final ComboViewer comboViewer = new ComboViewer(parent, SWT.DROP_DOWN);
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).span(2, 1).applyTo(comboViewer.getControl());
			comboViewer.setContentProvider(new ArrayContentProvider());
			comboViewer.setLabelProvider(new LabelProvider());
			List<String> controllerIds = getExperiment().getSampleEnvControllerIds();
			comboViewer.setInput(controllerIds.toArray(new String[controllerIds.size()]));
//			comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//				@Override
//				public void selectionChanged(SelectionChangedEvent event) {
//					// Update model
//					Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
//					sampleEnvironment.setControllerId((String) selection);
//					System.err.println("selection " + selection);
//				}
//			});
			comboViewer.getCombo().addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					sampleEnvironment.setControllerId(comboViewer.getCombo().getText());
					System.err.println("modify " + comboViewer.getCombo().getText());
				}
			});
			// Set default selection
			if (comboViewer.getCombo().getItemCount() > 0) {
				comboViewer.getCombo().select(0);
			}
			
			getToolkit().createLabel(parent, "    ");
			
			/*****************************************************************
			 * Add / remove buttons
			 *****************************************************************/
			Button addButton = getToolkit().createButton(parent, "", SWT.PUSH);
			addButton.setImage(InternalImage.ADD.getImage());
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(SWT.DEFAULT, 5).applyTo(addButton);
			
			Button removeButton = getToolkit().createButton(parent, "", SWT.PUSH);
			removeButton.setImage(InternalImage.DELETE.getImage());
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(SWT.DEFAULT, 5).applyTo(removeButton);
			
			addButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Do not render if no sample environment is available
					if (getExperiment().getSampleEnvControllerIds().size() <= 0) {
						return;
					}
					// Build UI
					Composite composite = getToolkit().createComposite(controlledEnvArea);
					GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(composite);
					SampleEnvironment sampleEnvironment = new SampleEnvironment(getExperiment());
					getExperiment().getSampleEnvironments().add(sampleEnvironment);
					sampleEnvironment.setControllerId(getExperiment().getSampleEnvControllerIds().get(0));
					createEnvironmentElement(composite, sampleEnvironment);
					controlledEnvArea.getParent().layout(true, true);
					// TODO: This only works in workflow view but not the automator
					if (controlledEnvArea.getParent().getParent().getParent() instanceof ScrolledForm) {
						ScrolledForm scrolledForm = (ScrolledForm) controlledEnvArea.getParent().getParent().getParent();
						ScrollBar verticalBar = scrolledForm.getVerticalBar();
						scrolledForm.reflow(true);
						// Update scrolling towards the end of form
						verticalBar.setSelection(verticalBar.getMaximum());
						verticalBar.notifyListeners(SWT.Selection, new Event());
					}

				}
			});
			
			removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Do not delete if only one element left 
					if (controlledEnvArea.getChildren().length <= 1) {
						return;
					}
					
					// Update the model
					getExperiment().getSampleEnvironments().remove(sampleEnvironment);
					
					// Find element to delete
					for (Control control : controlledEnvArea.getChildren()) {
						if (control == parent) {
							control.dispose();
							controlledEnvArea.getParent().layout(true, true);
							if (controlledEnvArea.getParent().getParent().getParent() instanceof ScrolledForm) {
								ScrolledForm scrolledForm = (ScrolledForm) controlledEnvArea.getParent().getParent().getParent();
								scrolledForm.reflow(true);
							}
							break;
						}
					}
				}
			});
		}
		
		// Create setting area
		private void createPresetArea(Composite parent, final SampleEnvironment sampleEnvironment) {
			parent.setLayout(new GridLayout());
			
			/*****************************************************************
			 * Table area
			 *****************************************************************/
			Composite tableArea = getToolkit().createComposite(parent);
			tableArea.setLayout(new GridLayout(1, false));
			GridDataFactory.fillDefaults().applyTo(tableArea);
			final TableViewer tableViewer = createPresetTable(tableArea, sampleEnvironment);
			
			/*****************************************************************
			 * Button area
			 *****************************************************************/
			/*
			Composite buttonArea = getToolkit().createComposite(tableArea);
			buttonArea.setLayout(new GridLayout());
			
			Button addButton = getToolkit().createButton(buttonArea, "", SWT.PUSH);
			addButton.setImage(InternalImage.ADD.getImage());
			
			Button removeButton = getToolkit().createButton(buttonArea, "", SWT.PUSH);
			removeButton.setImage(InternalImage.DELETE.getImage());
			
			Button upButton = getToolkit().createButton(buttonArea, "", SWT.PUSH);
			upButton.setImage(InternalImage.UP.getImage());
			
			Button downButton = getToolkit().createButton(buttonArea, "", SWT.PUSH);
			downButton.setImage(InternalImage.DOWN.getImage());
			*/
			/*****************************************************************
			 * Presets generation area 
			 *****************************************************************/
			Composite generationArea = getToolkit().createComposite(parent);
			generationArea.setLayout(new GridLayout(8, false));
			
			getToolkit().createLabel(generationArea, "From ");
			final Text startText = getToolkit().createText(generationArea, "");
			GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(startText);
			getToolkit().createLabel(generationArea, " to ");
			final Text endText = getToolkit().createText(generationArea, "");
			GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(endText);
			getToolkit().createLabel(generationArea, " in ");
			final Text stepsText = getToolkit().createText(generationArea, "");
			GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(stepsText);
			getToolkit().createLabel(generationArea, " steps");
			Button generateButton = getToolkit().createButton(generationArea, "Generate", SWT.PUSH);
			generateButton.setFont(boldFont);
			GridDataFactory.fillDefaults().span(1, 2).applyTo(generateButton);
			
			getToolkit().createLabel(generationArea, "Wait for ");
			final Text waitText = getToolkit().createText(generationArea, "");
			GridDataFactory.fillDefaults().span(5, 1).applyTo(waitText);
			getToolkit().createLabel(generationArea, " sec");
			
			/*****************************************************************
			 * Button logic
			 *****************************************************************/
			generateButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						float start = Float.parseFloat(startText.getText());
						float end = Float.parseFloat(endText.getText());
						int steps = Integer.parseInt(stepsText.getText());
						int waitTime = Integer.parseInt(waitText.getText());
						sampleEnvironment.resetPresets(start, end, steps, waitTime);
						tableViewer.setInput(sampleEnvironment.getPresets().toArray(SampleEnvironmentPreset.class));
						tableViewer.refresh();
					} catch (NumberFormatException nfe) {
						// Pop dialog for error
					}
				}
			});
		}
		
		// Creates the table (preset + wait time) for each controller
		private TableViewer createPresetTable(Composite parent, SampleEnvironment sampleEnvironment) {
			final TableViewer tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
			GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, false)
				.hint(SWT.DEFAULT, 300)
				.applyTo(tableViewer.getTable());
			
			final Table table = tableViewer.getTable();
			tableViewer.setContentProvider(new ArrayContentProvider());
			class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
				@Override
				public Image getColumnImage(Object element, int columnIndex) {
					return null;
				}
				@Override
				public String getColumnText(Object element, int columnIndex) {
					SampleEnvironmentPreset preset = (SampleEnvironmentPreset) element;
					if (columnIndex == Column.NUMBER.getIndex())
						return Integer.toString(preset.getNumber());
					else if (columnIndex == Column.PRESET.getIndex())
						return Float.toString(preset.getPreset());
					else if (columnIndex == Column.WAIT_TIME.getIndex())
						return Integer.toString(preset.getWaitTime());
					return "";
				}
			}
			tableViewer.setLabelProvider(new TableLabelProvider());
			// Generate default preset if necessary
//			if (sampleEnvironment.getPresets().size() == 0) {
//				SampleEnvironmentPreset preset = new SampleEnvironmentPreset();
//				preset.setPreset(100);
//				preset.setWaitTime(1);
//				sampleEnvironment.getPresets().add(preset);
//			}
			tableViewer.setInput(sampleEnvironment.getPresets().toArray());
			
			/*****************************************************************
			 * Table columns
			 *****************************************************************/
			TableColumn numberColumn = new TableColumn(table, SWT.LEFT);
			numberColumn.setText("");
			numberColumn.setWidth(30);
			numberColumn.setResizable(false);
			
			TableColumn presetColumn = new TableColumn(table, SWT.LEFT);
			presetColumn.setText("Preset");
			presetColumn.setWidth(150);
			presetColumn.setResizable(false);
			
			TableColumn waitTimeColumn = new TableColumn(table, SWT.LEFT);
			waitTimeColumn.setText("Wait (sec)");
			waitTimeColumn.setWidth(150);
			waitTimeColumn.setResizable(false);
			
			tableViewer.getTable().setHeaderVisible(true);
			tableViewer.getTable().setLinesVisible(true);
			
			/*****************************************************************
			 * Table cell editor
			 *****************************************************************/
			tableViewer.setColumnProperties(new String[] { Column.NUMBER.name(), Column.PRESET.name(), Column.WAIT_TIME.name() });

			TextCellEditor textCellEditor = new TextCellEditor(tableViewer.getTable());
			tableViewer.setCellEditors(new CellEditor[] {textCellEditor, textCellEditor, textCellEditor});
			
			class CellModifier implements ICellModifier {
				@Override
				public boolean canModify(Object element, String property) {
					if (property.equals(Column.PRESET.name()))
						return true;
					else if (property.equals(Column.WAIT_TIME.name()))
						return true;
					return false;
				}
				@Override
				public Object getValue(Object element, String property) {
					SampleEnvironmentPreset preset = (SampleEnvironmentPreset) element;
					if (property.equals(Column.NUMBER.name()))
						return Integer.toString(preset.getNumber());
					else if (property.equals(Column.PRESET.name()))
						return Float.toString(preset.getPreset());
					else if (property.equals(Column.WAIT_TIME.name()))
						return Integer.toString(preset.getWaitTime());
					return "";
				}
				@Override
				public void modify(Object element, String property, Object value) {
					SampleEnvironmentPreset preset = (SampleEnvironmentPreset) ((TableItem) element).getData();
					try {
						if (property.equals(Column.PRESET.name()))
							preset.setPreset(Float.parseFloat((String) value));
						else if (property.equals(Column.WAIT_TIME.name()))
							preset.setWaitTime(Integer.parseInt((String) value));
						
						tableViewer.refresh(preset);
					} catch (NumberFormatException nfe) {
					}
				}
			}
			tableViewer.setCellModifier(new CellModifier());
			
			return tableViewer;
		}
		
		public void dispose() {
			boldFont = null;
			normalFont = null;
			controlledEnvArea = null;
			super.dispose();
		}
		
		/*********************************************************************
		 * UI helper methods
		 *********************************************************************/
		private void refreshUI() {
			controlledEnvArea.getParent().layout(true, true);
			// Works for wizard viewer only
			if (controlledEnvArea.getParent().getParent().getParent() instanceof ScrolledForm) {
				ScrolledForm scrolledForm = (ScrolledForm) controlledEnvArea.getParent().getParent().getParent();
				scrolledForm.reflow(true);
			}
		}
		
		private void setNormalEnvEnable(boolean enabled) {
			if (enabled) {
				normalEnvButton.setFont(boldFont);
			} else {
				normalEnvButton.setFont(normalFont);
			}
		}
		
		private void setCustomEnvEnable(boolean enabled) {
			if (enabled) {
				controlledEnvButton.setFont(boldFont);
				controlledEnvArea.setVisible(true);
				
			} else {
				controlledEnvButton.setFont(normalFont);
				controlledEnvArea.setVisible(false);
			}
		}
	}
	
}
