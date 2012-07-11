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

package au.gov.ansto.bragg.echidna.exp.task;

import org.eclipse.core.databinding.DataBindingContext;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.commands.SicsVariableCommand;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

public class HeaderInformationBlockTask extends CommandBlockTask {

	private SicsVariableCommand userCommand;
	private SicsVariableCommand secondCollimator;
	private SicsVariableCommand sampleCommand;
	
	private SicsVariableCommand titleCommand;
	
	public void initialise() {
		super.initialise();
		if (getDataModel() != null && getDataModel().getCommands().length == 4){
			ISicsCommandElement[] commands = getDataModel().getCommands();
			secondCollimator = (SicsVariableCommand) commands[0];
			titleCommand = (SicsVariableCommand) commands[1];
			sampleCommand = (SicsVariableCommand) commands[2];
			userCommand = (SicsVariableCommand) commands[3];
		}else{
			secondCollimator = new SicsVariableCommand();
			secondCollimator.setSicsVariable("drive sc");
			secondCollimator.setValue("0");
			getDataModel().addCommand(secondCollimator);
			titleCommand = new SicsVariableCommand();
			titleCommand.setSicsVariable("title");
			titleCommand.setQuoted(true);
			getDataModel().addCommand(titleCommand);
			sampleCommand = new SicsVariableCommand();
			sampleCommand.setSicsVariable("sampledescription");
			sampleCommand.setQuoted(true);
			getDataModel().addCommand(sampleCommand);
			userCommand = new SicsVariableCommand();
			userCommand.setSicsVariable("user");
			userCommand.setQuoted(true);
			getDataModel().addCommand(userCommand);
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
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);
			
			Label label = getToolkit().createLabel(parent, "Secondary collimator inserted? ");
			final Button checkbox = getToolkit().createButton(parent, "", SWT.CHECK);
//			getDataModel().getCommands();
//			checkbox.setSelection(false);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(checkbox);
			
			// Row 2: title
			label = getToolkit().createLabel(parent, "Proposal number: ");
			final Text titleText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(titleText);
			AbstractEchidnaScanTask.addValidator(titleText, AbstractEchidnaScanTask.notEquationMarkValidator);
			
			// Row 2: title
			label = getToolkit().createLabel(parent, "Sample description: ");
			final Text sampleText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleText);
			AbstractEchidnaScanTask.addValidator(sampleText, AbstractEchidnaScanTask.notEquationMarkValidator);

			// Row 1: user
			label = getToolkit().createLabel(parent, "User: ");
			final Text userText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(userText);
			AbstractEchidnaScanTask.addValidator(userText, AbstractEchidnaScanTask.notEquationMarkValidator);
			
			
			// Data binding
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeSelection(checkbox),
							BeansObservables.observeValue(secondCollimator, "value"),
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
					bindingContext.bindValue(SWTObservables.observeText(titleText, SWT.Modify),
							BeansObservables.observeValue(titleCommand, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(sampleText, SWT.Modify),
							BeansObservables.observeValue(sampleCommand, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(userText, SWT.Modify),
							BeansObservables.observeValue(userCommand, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
		
	}
	
}
