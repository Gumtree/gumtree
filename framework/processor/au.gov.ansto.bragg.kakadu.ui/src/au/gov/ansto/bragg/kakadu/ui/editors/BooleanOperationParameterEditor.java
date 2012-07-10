/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.editors;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.process.port.TunerPortListener;

public class BooleanOperationParameterEditor extends OperationParameterEditor implements SelectionListener {

	private Button checkButton;


	public BooleanOperationParameterEditor(
			OperationParameter operationParameter, Composite parentComposite) {
		super(operationParameter, parentComposite);
	}

	protected Control createEditor() {
		checkButton = new Button(parentComposite, SWT.CHECK);
		
		checkButton.addSelectionListener(this);
		return checkButton;
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		dataUpdated();
	}

	public void loadData() {
		checkButton.setSelection(operationParameter.getValue() != null 
				? new Boolean(operationParameter.getValue().toString()) : false);
	}

	protected Object getEditorData() {
		return new Boolean(checkButton.getSelection());
	}

	public void addApplyParameterListener(SelectionListener selectionListener) {
		//currently Apply button should be pressed to apply the parameter. 
	}

	@Override
	protected void initializeTunerListener() {
		listener = new TunerPortListener(operationParameter.getTuner()){

			@Override
			public void updateUIMax(final Object max) {
			}

			@Override
			public void updateUIMin(final Object min) {
				
			}

			@Override
			public void updateUIOptions(final List<?> options) {
				
			}

			@Override
			public void updateUIValue(final Object value) {
				if (value instanceof Boolean)
					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							setChangeListenerEnable(false);
							checkButton.setSelection((Boolean) value);
							operationParameter.updateValueFromServer();
//							operationParameter.setValue(value);
							operationParameter.setChanged(false);
							setChangeListenerEnable(true);
						}});				
			}
			
		};
	}
}
