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

package org.gumtree.gumnix.sics.batch.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.commands.SicsVariableCommand;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

public class HeaderInformationBlockTask extends CommandBlockTask {

	private SicsVariableCommand userCommand;

	private SicsVariableCommand titleCommand;

	public void initialise() {
		super.initialise();
		if (getDataModel() != null && getDataModel().getCommands().length == 2) {
			ISicsCommandElement[] commands = getDataModel().getCommands();
			userCommand = (SicsVariableCommand) commands[0];
			titleCommand = (SicsVariableCommand) commands[1];
		} else {
			userCommand = new SicsVariableCommand();
			userCommand.setSicsVariable("user");
			getDataModel().addCommand(userCommand);
			titleCommand = new SicsVariableCommand();
			titleCommand.setSicsVariable("title");
			getDataModel().addCommand(titleCommand);
		}
	}

	protected ITaskView createViewInstance() {
		return new HeaderInformationBlockTaskView();
	}

	private class HeaderInformationBlockTaskView extends AbstractTaskView {

		private DataBindingContext bindingContext;

		public void createPartControl(Composite parent) {
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);

			// Row 1: user
			Label label = getToolkit().createLabel(parent, "User: ");
			final Text userText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(userText);

			// Row 2: title
			label = getToolkit().createLabel(parent, "Title: ");
			final Text titleText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(titleText);

			// Data binding
			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					bindingContext = new DataBindingContext();
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(userText),
							BeanProperties.value("value").observe(userCommand), new UpdateValueStrategy(),
							new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(titleText),
							BeanProperties.value("value").observe(titleCommand), new UpdateValueStrategy(),
							new UpdateValueStrategy());
				}
			});

			// Add dispose listener to clean up data binding resources
			parent.addDisposeListener(e -> {
				if (bindingContext != null) {
					bindingContext.dispose();
				}
			});
		}

	}

}
