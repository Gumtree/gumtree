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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * 
 * @author Danil Klimontov (dak)
 */
public class TextOperationParameterEditor extends OperationParameterEditor implements ModifyListener {
	private Text parameterText;


	public TextOperationParameterEditor(
			OperationParameter operationParameter, Composite parentComposite) {
		super(operationParameter, parentComposite);
	}

	protected Control createEditor() {
		parameterText = new Text(parentComposite, SWT.BORDER);

		//to register data modifications
		parameterText.addModifyListener(this);
		
		return parameterText;
	}

	public void modifyText(ModifyEvent e) {
		dataUpdated();
	}

	public void loadData() {
//		if (operationParameter.getValue() != null) {
//			parameterText.setText(operationParameter.getValue().toString());
//		}		
		String currentText = parameterText.getText();
		String operationValue = operationParameter.getValue() != null ? 
				operationParameter.getValue().toString() : "";
		if (!operationValue.trim().equals(currentText.trim()))
			parameterText.setText(operationValue);
	}

	protected Object getEditorData() {
		return parameterText.getText() != null ? parameterText.getText() : "";
	}
	
	public void addApplyParameterListener(SelectionListener selectionListener) {
		parameterText.addSelectionListener(selectionListener);
	}

	@Override
	protected void initializeTunerListener() {
		// TODO Auto-generated method stub
		listener = new TunerPortListener(operationParameter.getTuner()){

			@Override
			public void updateUIMax(final Object max) {
				// TODO Auto-generated method stub
			}

			@Override
			public void updateUIMin(final Object min) {
				// TODO Auto-generated method stub
			}

			@Override
			public void updateUIOptions(final List<?> options) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void updateUIValue(final Object value) {
				// TODO Auto-generated method stub
				if (value instanceof String)
					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							// TODO Auto-generated method stub
							setChangeListenerEnable(false);
							parameterText.setText(value.toString());
							operationParameter.setChanged(false);
							setChangeListenerEnable(true);
						}});
			}
			
		};
	}
}
