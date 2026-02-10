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

package org.gumtree.ui.terminal.support.telnet;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.terminal.ICommunicationConfigPart;

public class TelnetConfigPart implements ICommunicationConfigPart {

	private IConnectionContext connectionContext;

	private Composite parent;

	public void createControlPart(Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout(2, false));
		Label label = new Label(parent, SWT.NONE);
		label.setText("Host: ");
		final Text hostText = new Text(parent, SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(hostText, new Property(getConnectionContext(), "host"), null);

		label = new Label(parent, SWT.NONE);
		label.setText("Port: ");
		final Text portText = new Text(parent, SWT.BORDER);
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(portText, new Property(getConnectionContext(), "port"), null);

		// Databinding in Eclipse 3.3
		Realm.runWithDefault(DisplayRealm.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(hostText), BeanProperties.value(
						"host").observe(getConnectionContext()), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(portText), BeanProperties.value(
						"port").observe(getConnectionContext()), new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
	}

	public IConnectionContext getConnectionContext() {
		if (connectionContext == null) {
			connectionContext = new ConnectionContext("", 21);
		}
		return connectionContext;
	}

}
