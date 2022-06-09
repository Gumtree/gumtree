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

package org.gumtree.control.ui.login;

import java.net.URI;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.nebula.widgets.pgroup.SimpleGroupStrategy;
import org.eclipse.nebula.widgets.pgroup.TwisteToggleRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.core.SicsRole;
import org.gumtree.control.ui.internal.SicsUIProperties;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentSpecificLoginDialog extends BaseLoginDialog {

	private static final Logger logger = LoggerFactory.getLogger(InstrumentSpecificLoginDialog.class);
	
	private Composite mainComposite;

	private Button userRoleButton;

	private Button spyRoleButton;

	private Button managerRoleButton;

	private Text passwordText;

	private Text hostText;

	private Text portText;

	public InstrumentSpecificLoginDialog(Shell parentShell, ILoginHandler handler) {
		super(new Shell(parentShell, SWT.ON_TOP), handler);
		Assert.isNotNull(handler);
//		Assert.isNotNull(profile);
//		setInstrumentProfile(profile);
		// use command line option to overwrite settings
//		handleCommandLineOptions();
	}

	protected Control createDialogArea(Composite parent) {
		mainComposite = (Composite) super.createDialogArea(parent);
		Composite imageArea = new Composite(mainComposite, SWT.NONE);
		imageArea.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		createImage(imageArea);

		Composite optionArea = new Composite(mainComposite, SWT.NONE);
		optionArea
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		createHiddenOptions(optionArea);

		Composite loginArea = new Composite(mainComposite, SWT.NONE);
		loginArea
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		createLoginBar(loginArea);

		// Bind GUI for Eclipse 3.3
		Realm.runWithDefault(SWTObservables.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(hostText,
						SWT.Modify), BeansObservables.observeValue(
						getConnectionContext(), "serverAddress"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(portText,
						SWT.Modify), BeansObservables.observeValue(
						getConnectionContext(), "publisherAddress"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(passwordText,
						SWT.Modify), BeansObservables.observeValue(
						getConnectionContext(), "password"), new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});

		passwordText.setFocus();

		getShell().setText("Login");

		setTitle("Welcome to the " + SicsCoreProperties.INSTRUMENT_NAME.getValue() + " instrument server!");

		if(getInitialErrorMessage() != null) {
			setErrorMessage(getInitialErrorMessage());
		}

		return mainComposite;
	}

	private void createImage(Composite parent) {
		parent.setLayout(new FillLayout());
		Label label = new Label(parent, SWT.NONE);
		try {
			IDataAccessManager dam = ServiceUtils
					.getService(IDataAccessManager.class);
			ImageDescriptor imgDesc = dam.get(
					URI.create(SicsUIProperties.INSTRUMENT_LOGIN_IMAGE
							.getValue()), ImageDescriptor.class);
			label.setImage(imgDesc.createImage());
		} catch (Exception e) {
			logger.error("Failed to load instrument image from "
					+ SicsUIProperties.INSTRUMENT_LOGIN_IMAGE.getValue() + ".");
		}
	}

	private void createLoginBar(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Group roleGroup = new Group(parent, SWT.NONE);
		roleGroup.setText("Role");
		GridData roleGroupData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		roleGroupData.horizontalSpan = 2;
		roleGroup.setLayoutData(roleGroupData);
		roleGroup.setLayout(new FillLayout(SWT.HORIZONTAL));

		userRoleButton = new Button(roleGroup, SWT.RADIO);
		userRoleButton.setText("User");
		userRoleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getConnectionContext().setRole(SicsRole.USER);
			}
		});

		spyRoleButton = new Button(roleGroup, SWT.RADIO);
		spyRoleButton.setText("Spy");
		spyRoleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getConnectionContext().setRole(SicsRole.SPY);
			}
		});

		managerRoleButton = new Button(roleGroup, SWT.RADIO);
		managerRoleButton.setText("Manager");
		managerRoleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getConnectionContext().setRole(SicsRole.MANAGER);
			}
		});

		// Select role based on current context
		if(getConnectionContext().getRole().equals(SicsRole.SPY)) {
			spyRoleButton.setSelection(true);
		} else if(getConnectionContext().getRole().equals(SicsRole.MANAGER)) {
			managerRoleButton.setSelection(true);
		} else {
			userRoleButton.setSelection(true);
			getConnectionContext().setRole(SicsRole.USER);
		}
		getLogger().debug("Set role in UI: " + getConnectionContext().getRole());

		Label passwordLabel = new Label(parent, SWT.NONE);
		passwordLabel.setText("Password: ");

		passwordText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
//		getBindingContext().bind(passwordText, new Property(getConnectionContext(), "password"), null);
	}

	private void createHiddenOptions(Composite parent) {
		parent.setLayout(new FillLayout());
		AbstractGroupStrategy groupStrategy = new SimpleGroupStrategy();
		PGroup hiddenOptionGroup = new PGroup(parent, SWT.NONE);
		hiddenOptionGroup.setStrategy(groupStrategy);
		hiddenOptionGroup.setToggleRenderer(new TwisteToggleRenderer());
		hiddenOptionGroup.setText("Server Connection Options");
		hiddenOptionGroup.setLayout(new GridLayout(2, false));
		hiddenOptionGroup.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (mainComposite != null & !mainComposite.isDisposed()) {
					mainComposite.layout(true);
				}
			}
		});

		Label label = new Label(hiddenOptionGroup, SWT.NONE);
		label.setText("Host: ");

		hostText = new Text(hiddenOptionGroup, SWT.SINGLE
				| SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(hostText, new Property(getConnectionContext(), "host"), null);

		label = new Label(hiddenOptionGroup, SWT.NONE);
		label.setText("Port: ");

		portText = new Text(hiddenOptionGroup, SWT.SINGLE
				| SWT.BORDER);
		portText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {

			}
		});
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(portText, new Property(getConnectionContext(), "port"), null);

		// Hide only when other parts have been constructed
		hiddenOptionGroup.setExpanded(false);
	}

}
