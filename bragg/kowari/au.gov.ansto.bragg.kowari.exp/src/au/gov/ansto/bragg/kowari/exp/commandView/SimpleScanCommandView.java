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
package au.gov.ansto.bragg.kowari.exp.commandView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import au.gov.ansto.bragg.kowari.exp.command.AbstractScanCommand;
import au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter;
import au.gov.ansto.bragg.kowari.exp.command.SimpleTableScanCommand;
import au.gov.ansto.bragg.kowari.exp.command.TableScanParameter;

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
		if (((SimpleTableScanCommand) command).getNumberOfMotor() == 4) {
//			GridLayoutFactory.swtDefaults().margins(0, 0).spacing(6, 4).numColumns(7).applyTo(parent);
			final Button check1 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().span(3, 1).indent(34, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check1);
			final Button check2 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check2);
			final Button check3 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check3);
			final Button check4 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).span(2, 1).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check4);

			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check1),
							BeanProperties.value("column1").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check2),
							BeanProperties.value("column2").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check3),
							BeanProperties.value("column3").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check4),
							BeanProperties.value("column4").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		} else if (((SimpleTableScanCommand) command).getNumberOfMotor() == 7) {
			final Button check1 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().span(3, 1).indent(34, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check1);
			final Button check2 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check2);
			final Button check3 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check3);
			final Button check4 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check4);
			final Button check5 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check5);
			final Button check6 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check6);
			final Button check7 = getToolkit().createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().indent(4, 0).span(2, 1).align(SWT.BEGINNING, SWT.BOTTOM).applyTo(check7);

			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check1),
							BeanProperties.value("column1").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check2),
							BeanProperties.value("column2").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check3),
							BeanProperties.value("column3").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check4),
							BeanProperties.value("column4").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check5),
							BeanProperties.value("column5").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check6),
							BeanProperties.value("column6").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(check7),
							BeanProperties.value("column7").observe(command),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
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
		if (command.getNumberOfMotor() == 4) {
			GridLayoutFactory.swtDefaults().margins(0, 0).spacing(4, 4).numColumns(7).applyTo(parent);
		} else if (command.getNumberOfMotor() == 7) {
			GridLayoutFactory.swtDefaults().margins(0, 0).spacing(4, 4).numColumns(10).applyTo(parent);
		}
//		Label blankLabel = getToolkit().createLabel(parent, "");
//		GridDataFactory.swtDefaults().span(3, 1).hint(26, SWT.DEFAULT).applyTo(blankLabel);
		Label sxLabel = getToolkit().createLabel(parent, "sx");
		GridDataFactory.swtDefaults().span(3, 1).indent(35, 0).applyTo(sxLabel);
		Label syLabel = getToolkit().createLabel(parent, "sy");
		GridDataFactory.swtDefaults().indent(4, 0).applyTo(syLabel);
		Label szLabel = getToolkit().createLabel(parent, "sz");
		GridDataFactory.swtDefaults().indent(4, 0).applyTo(szLabel);
		Label somLabel = getToolkit().createLabel(parent, "som");
		GridDataFactory.swtDefaults().indent(4, 0).applyTo(somLabel);
		if (command.getNumberOfMotor() == 7) {
			Label eomLabel = getToolkit().createLabel(parent, "ga");
			GridDataFactory.swtDefaults().indent(4, 0).applyTo(eomLabel);
			Label echiLabel = getToolkit().createLabel(parent, "gb");
			GridDataFactory.swtDefaults().indent(4, 0).applyTo(echiLabel);
			Label ephiLabel = getToolkit().createLabel(parent, "gc");
			GridDataFactory.swtDefaults().indent(4, 0).applyTo(ephiLabel);
		}
		Label timeLabel = getToolkit().createLabel(parent, "time(s)");
		GridDataFactory.swtDefaults().indent(4, 0).applyTo(timeLabel);
	}

}
