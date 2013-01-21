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
package au.gov.ansto.bragg.nbi.ui.core.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * @author nxi
 * Created on 11/08/2009
 */
public class SimpleScanCommandView extends AbstractScanCommandView {

	/**
	 * 
	 */

	public SimpleScanCommandView(SimpleTableScanCommand command){
		super(command);
	}
	
	@Override
	protected void createPartControl(Composite parent,
			final AbstractScanCommand command) {
		super.createPartControl(parent, command);
		GridLayoutFactory.swtDefaults().applyTo(parent);
		createLabelArea(parent, (SimpleTableScanCommand) command);
//		Label titleLabel = getToolkit().createLabel(parent, command.getCommandName());
//		titleLabel.setFont(new Font(titleLabel.getFont().getDevice(), new FontData[]{new FontData("Courier New", 10, SWT.BOLD)}));
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 12).applyTo(titleLabel);
//		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.TOP).applyTo(titleLabel);
//		parameterComposite = getToolkit().createComposite(parent);
		int numberOfMotors = ((SimpleTableScanCommand) command).getNumberOfMotor();
		Label spaceLabel = getToolkit().createLabel(parent, "");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(spaceLabel);
		for (int i = 0; i < numberOfMotors; i++) {
			final Button check = getToolkit().createButton(parent, "", SWT.CHECK);
			final String columnName = "column" + i;
			GridDataFactory.swtDefaults().indent(4, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check);
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeSelection(check),
							BeansObservables.observeValue(command, columnName),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
		getToolkit().createLabel(parent, "");
		for (AbstractScanParameter parameter : command.getParameterList()) {
			((TableScanParameter) parameter).createParameterUI(parent, this, getToolkit());
		}
		
		final Button selectAll = getToolkit().createButton(parent, "Select/Deselect All", SWT.CHECK);
		GridDataFactory.swtDefaults().span(7, 1).indent(9, 2).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(selectAll);
		boolean isAllSelected = true;
		for (AbstractScanParameter parameter : command.getParameterList()) {
			if (!((TableScanParameter) parameter).getIsSelected()) {
				isAllSelected = false;
				break;
			}
		}
		selectAll.setSelection(isAllSelected);
		for (AbstractScanParameter parameter : command.getParameterList()) {
			parameter.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					Object parameter = evt.getSource();
					if (parameter instanceof TableScanParameter)
						if (evt.getPropertyName().equals("isSelected")) {
							if (!((Boolean) evt.getNewValue())) {
								selectAll.setSelection(false);
							} else {
								boolean isAllSelected = true;
								for (AbstractScanParameter parm : command.getParameterList()) {
									if (!((TableScanParameter) parm).getIsSelected()) {
										isAllSelected = false;
										break;
									}
								}
								selectAll.setSelection(isAllSelected);
							}
						}
				}
			});
		}
		selectAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = selectAll.getSelection();
				for (AbstractScanParameter parameter : command.getParameterList()) {
					((TableScanParameter) parameter).setIsSelected(isSelected);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createLabelArea(Composite parent, SimpleTableScanCommand command) {
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(4, 4).numColumns(command.getNumberOfMotor() + 3).applyTo(parent);
//		Label blankLabel = getToolkit().createLabel(parent, "");
//		GridDataFactory.swtDefaults().span(3, 1).hint(26, SWT.DEFAULT).applyTo(blankLabel);
		Label spaceLabel = getToolkit().createLabel(parent, "");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(spaceLabel);
		for (String pName : command.getPNames()) {
			Label pLabel = getToolkit().createLabel(parent, pName);
			GridDataFactory.swtDefaults().indent(4, 0).applyTo(pLabel);
		}
		Label presetLabel = getToolkit().createLabel(parent, "preset");
		GridDataFactory.swtDefaults().indent(4, 0).applyTo(presetLabel);
	}

}
