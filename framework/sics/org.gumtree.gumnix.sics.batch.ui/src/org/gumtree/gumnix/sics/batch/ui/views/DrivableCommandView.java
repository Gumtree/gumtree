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

package org.gumtree.gumnix.sics.batch.ui.views;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.commands.DrivableCommand;
import org.gumtree.gumnix.sics.batch.ui.commands.DrivableParameter;
import org.gumtree.gumnix.sics.batch.ui.internal.InternalImage;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.gumtree.gumnix.sics.control.controllers.ComponentDataFormatException;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;

public class DrivableCommandView extends AbstractSicsCommandView<DrivableCommand> {
	
	private DataBindingContext bindingContext;
	
	private FieldDecoration errorDec;
	
	@Override
	public void createPartControl(Composite parent, DrivableCommand command) {
		bindingContext = new DataBindingContext();
		errorDec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).numColumns(2).applyTo(parent);
		
		final ComboViewer methodComboViewer = new ComboViewer(parent, SWT.READ_ONLY);
		methodComboViewer.setContentProvider(new ArrayContentProvider());
		methodComboViewer.setLabelProvider(new LabelProvider());
		methodComboViewer.setSorter(new ViewerSorter());
		methodComboViewer.setInput(command.getAvailableMethods());
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 2).hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(methodComboViewer.getCombo());
	
		Composite parametersArea = getToolkit().createComposite(parent);
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(parametersArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parametersArea);
		
		// Fill default
		for (DrivableParameter parameter : getCommand().getParameters()) {
			createParameterArea(parametersArea, parameter);	
		}
		// Set default if no default is available from the model
		if (getCommand().getParameters().length == 0) {
			createNewParameter(parametersArea);
		}
		
		/*********************************************************************
		 * Data binding
		 *********************************************************************/
		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				bindingContext.bindValue(
						ViewersObservables.observeSingleSelection(methodComboViewer),
						BeansObservables.observeValue(getCommand(), "method"),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
			}
		});
	}
	
	private void createNewParameter(Composite parent) {
		DrivableParameter parameter = new DrivableParameter();
		getCommand().addDrivableParameter(parameter);
		createParameterArea(parent, parameter);
	}
	
	private void createParameterArea(final Composite parent, final DrivableParameter parameter) {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		parent.setMenu(new Menu(parent));
		final Composite parameterArea = getToolkit().createComposite(parent);
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).numColumns(4).applyTo(parameterArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parameterArea);
		
		final ComboViewer drivableComboViewer = new ComboViewer(parameterArea, SWT.READ_ONLY);
		drivableComboViewer.setContentProvider(new ArrayContentProvider());
		drivableComboViewer.setLabelProvider(new LabelProvider());
		drivableComboViewer.setSorter(new ViewerSorter());
		drivableComboViewer.setInput(SicsBatchUIUtils.getSicsDrivableIds());
		GridDataFactory.swtDefaults().hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(drivableComboViewer.getCombo());
		
		final Text targetText = getToolkit().createText(parameterArea, "", SWT.BORDER);
		targetText.setToolTipText("Enter target value");
		GridDataFactory.swtDefaults().hint(WIDTH_PARAMETER, SWT.DEFAULT).applyTo(targetText);
		
		if (DrivableCommand.isDrivingMultipleAllowed()) {
			Button addButton = getToolkit().createButton(parameterArea, "", SWT.PUSH);
			addButton.setImage(InternalImage.ADD.getImage());
			addButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					createNewParameter(parent);
					fireRefresh();
				}
			});

			Button removeButton = getToolkit().createButton(parameterArea, "", SWT.PUSH);
			removeButton.setImage(InternalImage.REMOVE.getImage());
			removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Do not delete if there is not too much left
					if (getCommand().getParameters().length <= 1) {
						return;
					}
					parameterArea.dispose();
					getCommand().removeDrivableParameter(parameter);
					fireRefresh();
				}
			});
		}
		/*********************************************************************
		 * Validation
		 *********************************************************************/
		final ControlDecoration controlDec = new ControlDecoration(targetText, SWT.LEFT | SWT.BOTTOM);
		// Validate on target value change
		targetText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate(targetText, controlDec, drivableComboViewer);
			}
		});
		// Validate on device change
		drivableComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validate(targetText, controlDec, drivableComboViewer);
			}			
		});
		
		/*********************************************************************
		 * Data binding
		 *********************************************************************/
		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				bindingContext.bindValue(
						ViewersObservables.observeSingleSelection(drivableComboViewer),
						BeansObservables.observeValue(parameter, "deviceId"),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
				bindingContext.bindValue(
						SWTObservables.observeText(targetText, SWT.Modify),
						BeansObservables.observeValue(parameter, "target"),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
				
				/*********************************************************************
				 * Default selection
				 *********************************************************************/
				if (parameter.getDeviceId() == null) {
					if (drivableComboViewer.getCombo().getItemCount() > 0) {
						drivableComboViewer.setSelection(new StructuredSelection(
								drivableComboViewer.getElementAt(drivableComboViewer.getCombo().getItemCount() - 1)));
					}
				}
			}
		});
	}
	
	private void validate(Text targetText, ControlDecoration controlDec, ComboViewer drivableComboViewer) {
		// Initially assume no problem
		controlDec.hide();

		// Test 1: non-empty field
		if ((targetText.getText() == null) || targetText.getText().length() == 0) {
			controlDec.setImage(errorDec.getImage());
			controlDec.setDescriptionText("Target is empty");
			controlDec.show();
			return;
		}

		// Test 2: valid float number
		float value;
		try {
			value = Float.parseFloat(targetText.getText());
		} catch (NumberFormatException nfe) {
			controlDec.setImage(errorDec.getImage());
			controlDec.setDescriptionText("Invalid target value");
			controlDec.show();
			return;
		}

		// Test 3: check for limits
		Object selectedDevice = ((IStructuredSelection) drivableComboViewer.getSelection()).getFirstElement();
		if (selectedDevice == null) {
			return;
		}
		IComponentController device = SicsCore.getSicsController().findDeviceController((String) selectedDevice);
		if (device == null) {
			return;
		}
		IDynamicController upperlimController = (IDynamicController) device.getChildController("/softupperlim");
		IDynamicController lowerlimController = (IDynamicController) device.getChildController("/softlowerlim");
		if (upperlimController == null || lowerlimController == null) {
			return;
		}
		try {
			float upperlim = upperlimController.getValue().getFloatData();
			float lowerlim = lowerlimController.getValue().getFloatData();
			if (value > upperlim || value < lowerlim) {
				controlDec.setImage(errorDec.getImage());
				controlDec.setDescriptionText("Target is out of range (" + lowerlim + " to " + upperlim + ")");
				controlDec.show();
				return;
			}
		} catch (SicsIOException sioe) {
		} catch (ComponentDataFormatException cdfe) {
		}
	}
	
	@Override
	public void dispose() {
		if (bindingContext != null) {
			bindingContext.dispose();
			bindingContext = null;
		}
		errorDec = null;
		super.dispose();
	}
}
