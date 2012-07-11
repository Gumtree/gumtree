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
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.commands.SicsVariableCommand;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

public class HeaderInformationBlockTask extends CommandBlockTask {

	private SicsVariableCommand experimentTitle;
	private SicsVariableCommand sampleDescription;
	private SicsVariableCommand sampleTitle;
	private SicsVariableCommand sampleName;
	private SicsVariableCommand userCommand;
	private SicsVariableCommand userEmail;
	
	
	public void initialise() {
		super.initialise();
		if (getDataModel() != null && getDataModel().getCommands().length != 0){
			ISicsCommandElement[] commands = getDataModel().getCommands();
			experimentTitle = (SicsVariableCommand) commands[0];
			sampleDescription = (SicsVariableCommand) commands[1];
			sampleTitle = (SicsVariableCommand) commands[2];
			sampleName = (SicsVariableCommand) commands[3];
			userCommand = (SicsVariableCommand) commands[4];
			userEmail = (SicsVariableCommand) commands[5];
		}else{
			experimentTitle = new SicsVariableCommand();
			experimentTitle.setSicsVariable("title");
			experimentTitle.setQuoted(true);
			getDataModel().addCommand(experimentTitle);
			sampleDescription = new SicsVariableCommand();
			sampleDescription.setSicsVariable("sampledescription");
			sampleDescription.setQuoted(true);
			getDataModel().addCommand(sampleDescription);
			sampleTitle = new SicsVariableCommand();
			sampleTitle.setSicsVariable("sampletitle");
			sampleTitle.setQuoted(true);
			getDataModel().addCommand(sampleTitle);
			sampleName = new SicsVariableCommand();
			sampleName.setSicsVariable("samplename");
			sampleName.setQuoted(true);
			getDataModel().addCommand(sampleName);
			userCommand = new SicsVariableCommand();
			userCommand.setSicsVariable("user");
			userCommand.setQuoted(true);
			String userName = System.getProperty("gumtree.user.name");
			if (userName != null && userName.trim().length() > 0) {
				userCommand.setValue(userName);
			}
			getDataModel().addCommand(userCommand);
			userEmail = new SicsVariableCommand();
			userEmail.setSicsVariable("email");
			getDataModel().addCommand(userEmail);
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
			
			getToolkit().createLabel(parent, "Experiment title ");
			final Text experimentTitleText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(experimentTitleText);
//			getDataModel().getCommands();
//			checkbox.setSelection(false);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(checkbox);
			
			// Row 2: title
			getToolkit().createLabel(parent, "Sample description: ");
			final Text descriptionText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(descriptionText);

			// Row 2: title
			getToolkit().createLabel(parent, "Sample title: ");
			final Text sampleTitleText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleTitleText);

			// Row 2: title
			getToolkit().createLabel(parent, "Sample name: ");
			final Text sampleNameText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleNameText);

			// Row 1: user
			getToolkit().createLabel(parent, "User: ");
			final Text userText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(userText);

			// Row 2: title
			getToolkit().createLabel(parent, "Email: ");
			final Text emailText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(emailText);


			
			// Data binding
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeText(experimentTitleText, SWT.Modify),
							BeansObservables.observeValue(experimentTitle, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(descriptionText, SWT.Modify),
							BeansObservables.observeValue(sampleDescription, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(sampleTitleText, SWT.Modify),
							BeansObservables.observeValue(sampleTitle, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(sampleNameText, SWT.Modify),
							BeansObservables.observeValue(sampleName, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(userText, SWT.Modify),
							BeansObservables.observeValue(userCommand, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(emailText, SWT.Modify),
							BeansObservables.observeValue(userEmail, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
		
	}
	
}
