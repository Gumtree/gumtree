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

package au.gov.ansto.bragg.kowari.exp.task;

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

//	private SicsVariableCommand experimentTitle;
	private SicsVariableCommand sampleName;
	private SicsVariableCommand sampleDescription;
//	private SicsVariableCommand userCommand;
	
	
	public void initialise() {
		super.initialise();
		if (getDataModel() != null && getDataModel().getCommands().length == 2){
			ISicsCommandElement[] commands = getDataModel().getCommands();
//			experimentTitle = (SicsVariableCommand) commands[0];
			sampleName = (SicsVariableCommand) commands[0];
			sampleDescription = (SicsVariableCommand) commands[1];
//			userCommand = (SicsVariableCommand) commands[3];
		}else{
//			experimentTitle = new SicsVariableCommand();
//			experimentTitle.setSicsVariable("title");
//			getDataModel().addCommand(experimentTitle);
			sampleName = new SicsVariableCommand();
			sampleName.setSicsVariable("samplename");
			sampleName.setQuoted(true);
			getDataModel().addCommand(sampleName);
			sampleDescription = new SicsVariableCommand();
			sampleDescription.setSicsVariable("sampledescription");
			sampleDescription.setQuoted(true);
			getDataModel().addCommand(sampleDescription);
//			userCommand = new SicsVariableCommand();
//			userCommand.setSicsVariable("user");
//			getDataModel().addCommand(userCommand);
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
			
//			getToolkit().createLabel(parent, "Experiment title ");
//			final Text experimentTitleText = getToolkit().createText(parent, "");
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(experimentTitleText);
//			getDataModel().getCommands();
//			checkbox.setSelection(false);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(checkbox);
			
			// Row 2: title
			getToolkit().createLabel(parent, "Sample name: ");
			final Text sampleNameText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleNameText);

			// Row 2: title
			getToolkit().createLabel(parent, "Sample description: ");
			final Text descriptionText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(descriptionText);

			// Row 1: user
//			label = getToolkit().createLabel(parent, "User: ");
//			final Text userText = getToolkit().createText(parent, "");
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(userText);
			
			
			// Data binding
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
//					bindingContext.bindValue(SWTObservables.observeText(experimentTitleText, SWT.Modify),
//							BeansObservables.observeValue(experimentTitle, "value"),
//							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(sampleNameText, SWT.Modify),
							BeansObservables.observeValue(sampleName, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(descriptionText, SWT.Modify),
							BeansObservables.observeValue(sampleDescription, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
		
	}
	
}
