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

package au.gov.ansto.bragg.wombat.exp.task;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.commands.SicsVariableCommand;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;

public class ChooseWavelengthTask extends AbstractWombatTask {

	private SicsVariableCommand setWavelength;
	private SicsVariableCommand setGe;
	private SicsVariableCommand setAutoCheck;
	
	
	public void initialise() {
		super.initialise();
		if (getDataModel() != null && getDataModel().getCommands().length != 0){
			ISicsCommandElement[] commands = getDataModel().getCommands();
			setWavelength = (SicsVariableCommand) commands[0];
			setGe = (SicsVariableCommand) commands[1];
			setAutoCheck = (SicsVariableCommand) commands[2];
		}else{
			setWavelength = new SicsVariableCommand();
			setWavelength.setSicsVariable("lambda");
			getDataModel().addCommand(setWavelength);
			setGe = new SicsVariableCommand();
			setGe.setSicsVariable("ge");
			getDataModel().addCommand(setGe);
			setAutoCheck = new SicsVariableCommand();
			setAutoCheck.setValue("0");
			setAutoCheck.setSicsVariable("autocheck");
			getDataModel().addCommand(setAutoCheck);
		}
	}

	protected ITaskView createViewInstance() {
		return new HeaderInformationBlockTaskView();
	}
		
	private class HeaderInformationBlockTaskView extends AbstractTaskView {

		public void createPartControl(Composite parent) {
//			
//		}
//		
//		
//		public void addHeaderUI(Composite parent) {
			GridLayoutFactory.swtDefaults().numColumns(3).applyTo(parent);
			
			getToolkit().createLabel(parent, "wavelength");
			getToolkit().createLabel(parent, "ge");
			getToolkit().createLabel(parent, "");
			final Text wavelengthText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(wavelengthText);
			GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(wavelengthText);
			// Row 2: title
			final Text geText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(geText);
			GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(geText);
			// Row 2: title
			final Button autoCheckButton = getToolkit().createButton(parent, "", SWT.CHECK);
			autoCheckButton.setText("auto check");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(autoCheckButton);
			
			// Data binding
			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(wavelengthText),
						BeanProperties.value("value").observe(setWavelength),
						new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(geText),
						BeanProperties.value("value").observe(setGe),
						new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.buttonSelection().observe(autoCheckButton),
						BeanProperties.value("value").observe(setAutoCheck),
						new UpdateValueStrategy(){

						/* (non-Javadoc)
						 * @see org.eclipse.core.databinding.UpdateValueStrategy#convert(java.lang.Object)
						 */
						@Override
						public Object convert(Object value) {
							if (value instanceof Boolean)
								if ((Boolean) value)
									return "1";
									else 
									return "0";
							return super.convert(value);
						}

					}, new UpdateValueStrategy(){

						/* (non-Javadoc)
						 * @see org.eclipse.core.databinding.UpdateValueStrategy#convert(java.lang.Object)
						 */
						@Override
						public Object convert(Object value) {
							if (value instanceof String)
								if ("1".equals(value))
									return true;
								else 
									return false;
							return super.convert(value);
						}

					});
				}
			});
		}
		
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Choose Wavelength";
	}

	@Override
	public ITask newThisTask() {
		// TODO Auto-generated method stub
		return new ChooseWavelengthTask();
	}
	
}