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
package au.gov.ansto.bragg.kakadu.ui.plot;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author nxi
 * Created on 25/06/2008
 */
public class NewFunctionDialog extends InputDialog {

	public final static String FUNCTION_NAME = "NewName";
	public final static String FUNCTION_TEXT = 
		"amplitude*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma)+background";
	String functionName;
	String functionText;
	Text nameBlock;
	Text functionBlock;
	String dialogMessage;
	Button okButton;
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
//		super.createButtonsForButtonBar(parent);
		okButton = new Button(parent, SWT.PUSH);
		okButton.setText("OK");
		Button cancelButton = new Button(parent, SWT.PUSH);
		cancelButton.setText("Cancel");
		
		okButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				buttonPressed(1);
			}
			
		});
		
		cancelButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				buttonPressed(0);
			}
			
		});
	}

	/**
	 * @param parentShell
	 * @param dialogTitle
	 * @param dialogMessage
	 * @param initialValue
	 * @param validator
	 */
	public NewFunctionDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String initialValue, IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		this.dialogMessage = dialogMessage;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void buttonPressed(int buttonId) {
		// TODO Auto-generated method stub
		if (buttonId == 0)
			getShell().dispose();
		else {
			functionName = nameBlock.getText();
			functionText = functionBlock.getText();
			getShell().dispose();
		}
//		super.buttonPressed(buttonId);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 3;
		gridLayout.marginWidth = 3;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 3;
		gridLayout.marginLeft = 5;
		gridLayout.marginRight = 5;
		gridLayout.marginTop = 5;
		gridLayout.marginBottom = 5;
		parent.setLayout(gridLayout);
		
		Composite composite = new Composite(parent, SWT.NONE);
		gridLayout = new GridLayout ();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 3;
		gridLayout.marginWidth = 3;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 3;
		composite.setLayout (gridLayout);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.verticalIndent = 3;
		data.horizontalSpan = 2;
		composite.setLayoutData(data);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(dialogMessage);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.verticalIndent = 3;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
//		label.setEnabled(false);
		
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText("Function Name");
		data = new GridData ();
		data.verticalAlignment = GridData.BEGINNING;
		data.verticalIndent = 3;
		nameLabel.setLayoutData(data);
		
		nameBlock = new Text(composite, SWT.BORDER);
		nameBlock.setText(FUNCTION_NAME);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		nameBlock.setLayoutData(data);
		nameBlock.setTextLimit(16);
		
		Label functionLabel = new Label(composite, SWT.NONE);
		functionLabel.setText("Function Description");
		data = new GridData ();
		data.verticalAlignment = GridData.BEGINNING;
		data.verticalIndent = 3;
		functionLabel.setLayoutData(data);
		
		functionBlock = new Text(composite, SWT.BORDER);
		functionBlock.setText(FUNCTION_TEXT);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		functionBlock.setLayoutData(data);
		functionBlock.setSelection(0, functionBlock.getText().length());
		functionBlock.forceFocus();
		
		createButtonsForButtonBar(parent);
		return composite;
	}

	@Override
	protected void validateInput() {
		// TODO Auto-generated method stub
		super.validateInput();
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getFunctionText() {
		return functionText;
	}

	@Override
	protected Button getOkButton() {
		// TODO Auto-generated method stub
		return okButton;
	}

	
}
