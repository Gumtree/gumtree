/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.ui.workflow;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SaveConfigDialog extends MessageDialog {

	private final IObservableValue configDescription;

	public SaveConfigDialog(Shell parentShell, IObservableValue configDescription) {
		super(
			parentShell,
			"Save instrument configuration",
			null,
			"Please enter description:",
			NONE,
			new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
			0);
		this.configDescription = configDescription;
	}

	protected Control createCustomArea(Composite parent) {
		Composite mainArea = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(mainArea);
		mainArea.setLayout(new GridLayout(1, false));
				
		final Text descriptionText = new Text(mainArea, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(300, 100).applyTo(descriptionText);
				
		Realm.runWithDefault(
				SWTObservables.getRealm(Display.getDefault()),
				new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(
								SWTObservables.observeText(descriptionText, SWT.Modify),
								configDescription);
					}
				});
		return mainArea;
	}

}
