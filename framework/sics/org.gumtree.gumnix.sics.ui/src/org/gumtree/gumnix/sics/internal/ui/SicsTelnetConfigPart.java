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

package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.ui.terminal.support.telnet.TelnetConfigPart;

public class SicsTelnetConfigPart extends TelnetConfigPart {

	private SicsConnectionContext connectionContext;

	public SicsTelnetConfigPart() {
		super();
	}

	public SicsConnectionContext getConnectionContext() {
		if (connectionContext == null) {
			// Default
			connectionContext = new SicsConnectionContext("", 21);
			connectionContext
					.setHost(SicsCoreProperties.SERVER_HOST.getValue());
			connectionContext.setPort(SicsCoreProperties.TELNET_PORT.getInt());
			// Replace with current context if available
			ISicsConnectionContext currentContext = SicsCore.getDefaultProxy()
					.getConnectionContext();
			if (currentContext != null) {
				connectionContext.setHost(currentContext.getHost());
				connectionContext.setLogin(currentContext.getRole()
						.getLoginId());
				connectionContext.setPassword(currentContext.getPassword());
			}
		}
		return connectionContext;
	}

	public void createControlPart(Composite parent) {
		super.createControlPart(parent);

		Label label = new Label(parent, SWT.NONE);
		label.setText("Login: ");
		final Text loginText = new Text(parent, SWT.BORDER);
		loginText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(loginText, new Property(getConnectionContext(), "login"), null);

		label = new Label(parent, SWT.NONE);
		label.setText("Password: ");
		final Text passwordText = new Text(parent, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
//		getBindingContext().bind(passwordText, new Property(getConnectionContext(), "password"), null);

		// Eclipse 3.3 databinding
		Realm.runWithDefault(SWTObservables.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(loginText,
						SWT.Modify), BeansObservables.observeValue(
						getConnectionContext(), "login"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext
						.bindValue(SWTObservables.observeText(passwordText,
								SWT.Modify), BeansObservables.observeValue(
								getConnectionContext(), "password"), new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});

	}
}
