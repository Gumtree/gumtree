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
package au.gov.ansto.bragg.kakadu.ui.views.mask;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.vis.mask.AbstractMask;

import au.gov.ansto.bragg.kakadu.ui.region.RegionParameter;

/**
 * 
 * @author Danil Klimontov (dak)
 */
public abstract class AbstractMaskEditor extends Composite {

	private Button applyButton;
	private Button revertButton;
	private AbstractMask region;
	private Text nameText;
	private Button inclusiveRadioButton;
	private Button exclusiveRadioButton;
	private Label nameLabel;
	
	protected static Color defaultTextColor;
	protected static Color errorTextColor;

	protected boolean isChanged = false;
	protected boolean isValid = true;
	protected RegionParameter regionParameter;
	
	protected ModifyListener textModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			isValid = isValuesValid();
			isChanged = true;
			updateButtons();
		}
	};
	protected SelectionListener radioSelectionListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			isChanged = true;
			updateButtons();
		}
	};

	public AbstractMaskEditor(Composite parent, int style) {
		super(parent, style);
		initialise();
	}

	protected void initialise() {
		if (defaultTextColor == null) {
			defaultTextColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
			errorTextColor = getDisplay().getSystemColor(SWT.COLOR_RED);
		}
		
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 3;
		setLayout (gridLayout);

		nameLabel = new Label (this, SWT.NONE);
		nameLabel.setText ("Name");
		
		nameText = new Text (this, SWT.BORDER);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		nameText.setLayoutData (data);

		Label label2 = new Label (this, SWT.NONE);
//		label2.setText ("Mode");
		
		inclusiveRadioButton = new Button (this, SWT.RADIO);
		inclusiveRadioButton.setText ("Inclusive");
		
		exclusiveRadioButton = new Button (this, SWT.RADIO);
		exclusiveRadioButton.setText ("Exclusive");
		
		Composite pointEditorComposite = createPointEditor();
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		pointEditorComposite.setLayoutData (data);

//		Label separator = new Label (this, SWT.SEPARATOR | SWT.HORIZONTAL);
		
//		data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
//		data.horizontalSpan = 3;
//		data.grabExcessHorizontalSpace = true;
//		separator.setLayoutData (data);

		Composite buttonComposite = new Composite(this, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData (data);
		GridLayout buttonCompositeGridLayout = new GridLayout();
		buttonCompositeGridLayout.numColumns = 3;
		buttonCompositeGridLayout.marginWidth = 0;
		buttonCompositeGridLayout.marginHeight = 0;
		buttonCompositeGridLayout.verticalSpacing = 3;
		buttonCompositeGridLayout.horizontalSpacing = 3;
		buttonComposite.setLayout(buttonCompositeGridLayout);
		
		Label box = new Label(buttonComposite, SWT.NONE);
		data = new GridData ();
		data.grabExcessHorizontalSpace = true;
		box.setLayoutData (data);
		
		applyButton = new Button(buttonComposite, SWT.PUSH);
		applyButton.setText("Apply");
		data = new GridData ();
		applyButton.setLayoutData (data);
		applyButton.setEnabled(false);
		
		revertButton = new Button(buttonComposite, SWT.PUSH);
		revertButton.setText("Revert");
		data = new GridData ();
		revertButton.setLayoutData (data);
		revertButton.setEnabled(false);
		
		setEditorEnabled(false);
		setRegion((AbstractMask) null);
		
		initListeners();
	}

	protected void initListeners() {
		nameText.addModifyListener(textModifyListener);
		inclusiveRadioButton.addSelectionListener(radioSelectionListener);
		
		revertButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				setRegion(region);
			}
		});
		
		applyButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				applyChanges();
			}
		});
	}

	protected abstract void applyChanges();

	protected boolean isInclusive() {
		return inclusiveRadioButton.getSelection();
	}

	protected String getMaskName() {
		return nameText.getText();
	}

//	protected int getId() {
//		return region.getId();
//	}
	
	public void setRegion(AbstractMask region) {
		this.region = region;
		if (region == null) {
			clearEditor();
		} else {
			nameText.setText(region.getName());
			boolean inclusive = region.isInclusive();
			inclusiveRadioButton.setSelection(inclusive);
			exclusiveRadioButton.setSelection(!inclusive);
			setPoints(region);
		}
		
		setEditorEnabled(region != null);
		
		isChanged = false;
		isValid = true;
		
		updateButtons();
	}


	protected void setEditorEnabled(boolean enabled) {
		nameLabel.setEnabled(enabled);
		nameText.setEnabled(enabled);
		inclusiveRadioButton.setEnabled(enabled);
		exclusiveRadioButton.setEnabled(enabled);
	}

	protected void updateButtons() {
		applyButton.setEnabled(isValid & isChanged);
		revertButton.setEnabled(isChanged);
	}

	protected void clearEditor() {
		nameText.setText("<mask not selected>");
		inclusiveRadioButton.setSelection(false);
		exclusiveRadioButton.setSelection(false);
		
		clearPoints();
	}

	protected boolean isValuesValid() {
		return true;
	}
	
	/**
	 * Gets changed flag for the editor.
	 * @return true if the editor's values were changed or false otherwise.
	 */
	public boolean isChanged() {
		return isChanged;
	}

	protected void showErrorMessage(String message) {
		MessageDialog.openError(
			getShell(),
			"Mask Editor",
			message);
	}


	protected abstract void clearPoints();

	protected abstract void setPoints(AbstractMask region);
	
	protected abstract Composite createPointEditor();

	public void setRegionParameter(RegionParameter regionParameter) {
		this.regionParameter = regionParameter;
	}
	
	public AbstractMask getMask() {
		return region;
	}
}
