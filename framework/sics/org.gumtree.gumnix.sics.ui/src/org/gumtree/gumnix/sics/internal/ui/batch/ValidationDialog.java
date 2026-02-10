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

package org.gumtree.gumnix.sics.internal.ui.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.SicsConnectionContext;

public class ValidationDialog extends TitleAreaDialog {

	private static final String BATCH_NAME = "gumtreeBatchValidation";

	private Text hostText;

	private Text portText;

	private Text loginText;

	private Text passwordText;

	private StyledText outputText;

	private ExtendedConnectionContext context;

	private String[] commands;

	public ValidationDialog(Shell parentShell) {
		super(parentShell);
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setSize(600, 600);
	}

	protected Control createDialogArea(Composite parent) {
		Composite mainComposite = (Composite) super.createDialogArea(parent);
		Group configGroup = new Group(mainComposite, SWT.NONE);
		configGroup.setText("Validation Server");
		configGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		createConnnectionConfigArea(configGroup);

		Composite outputComposite = new Composite(mainComposite, SWT.NONE);
		outputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		createOutputArea(outputComposite);

		return mainComposite;
	}

	private void createConnnectionConfigArea(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Label label = new Label(parent, SWT.NONE);
		label.setText("Host: ");
		hostText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(hostText, new Property(getConnectionContext(), "host"), null);

		label = new Label(parent, SWT.NONE);
		label.setText("Port: ");
		portText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(portText, new Property(getConnectionContext(), "port"), null);

		label = new Label(parent, SWT.NONE);
		label.setText("Login: ");
		loginText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		loginText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(loginText, new Property(getConnectionContext(), "login"), null);

		Label passwordLabel = new Label(parent, SWT.NONE);
		passwordLabel.setText("Password: ");
		passwordText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
//		getBindingContext().bind(passwordText, new Property(getConnectionContext(), "password"), null);

		// Eclipse 3.3 databinding
		Realm.runWithDefault(DisplayRealm.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(
						WidgetProperties.text(SWT.Modify).observe(hostText),
						BeanProperties.value(ExtendedConnectionContext.class, "host").observe(getConnectionContext()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(
						WidgetProperties.text(SWT.Modify).observe(portText),
						BeanProperties.value(ExtendedConnectionContext.class, "port").observe(getConnectionContext()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(
						WidgetProperties.text(SWT.Modify).observe(loginText),
						BeanProperties.value(ExtendedConnectionContext.class, "login").observe(getConnectionContext()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(
						WidgetProperties.text(SWT.Modify).observe(passwordText),
						BeanProperties.value(ExtendedConnectionContext.class, "password").observe(getConnectionContext()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
	}

	private void createOutputArea(Composite parent) {
		parent.setLayout(new FillLayout());
		outputText = new StyledText(parent, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION
				| SWT.READ_ONLY);
	}

	private ExtendedConnectionContext getConnectionContext() {
		if (context == null) {
			context = new ExtendedConnectionContext();
			context.setHost(SicsCoreProperties.VALIDATION_HOST.getValue());
			context.setPort(SicsCoreProperties.VALIDATION_PORT.getInt());
			ISicsConnectionContext currentContext = SicsCore.getDefaultProxy().getConnectionContext();
			if (currentContext != null) {
				context.setLogin(currentContext.getRole().getLoginId());
				context.setPassword(currentContext.getPassword());
			}
		}
		return context;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Validate", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
	}

	protected void okPressed() {
		outputText.setText("");
		runValidation();
	}

	private void runValidation() {
		// Connect and login to SICS
		try {
			Socket socket = new Socket(getConnectionContext().getHost(),
					getConnectionContext().getPort());
			final BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			PrintStream outputStream = new PrintStream(socket.getOutputStream());
			Thread listenerThread = new Thread(new Runnable() {
				public void run() {
					try {
						String replyMessage;
						while ((replyMessage = inputStream.readLine()) != null) {
							// little hack to sics telnet bug
							while (replyMessage.startsWith("��")) {
								replyMessage = replyMessage.substring(2);
							}
							if (outputText == null || outputText.isDisposed()) {
								break;
							}
							final String message = replyMessage;
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											if (outputText == null
													|| outputText.isDisposed()) {
												return;
											}
											StyleRange styleRange = new StyleRange();
											styleRange.start = outputText
													.getCharCount();
											styleRange.length = message
													.length() + 1;
											styleRange.fontStyle = SWT.BOLD;
											styleRange.foreground = PlatformUI
													.getWorkbench()
													.getDisplay()
													.getSystemColor(
															SWT.COLOR_BLUE);
											outputText.append(message + "\n");
											outputText
													.setStyleRange(styleRange);
											// Auto scroll
											StyledTextContent doc = outputText
													.getContent();
											int docLength = doc.getCharCount();
											if (docLength > 0) {
												outputText
														.setCaretOffset(docLength);
												outputText.showSelection();
											}
										}
									});

						}
					} catch (IOException e) {
					}
				}
			});
			listenerThread.start();
			send(getConnectionContext().getLogin() + " "
					+ getConnectionContext().getPassword(), outputStream);
			send("exe clear", outputStream);
			send("exe upload", outputStream);
			for (String command : getCommands()) {
				send("exe append " + command, outputStream);
			}
			send("exe forcesave " + BATCH_NAME, outputStream);
			send("exe enqueue " + BATCH_NAME, outputStream);
			send("exe run", outputStream);
		} catch (IOException e) {
			setErrorMessage("Cannot connect to server");

		}
		// Append batch
		// Run and capture output
	}

	private void send(final String text, PrintStream outputStream) {
		if (outputStream != null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					StyleRange styleRange = new StyleRange();
					styleRange.start = outputText.getCharCount();
					styleRange.length = 1;
					styleRange.fontStyle = SWT.BOLD;
					outputText.append(">> ");
					outputText.setStyleRange(styleRange);
					styleRange = new StyleRange();
					styleRange.start = outputText.getCharCount();
					styleRange.length = text.length() + 1;
					styleRange.foreground = PlatformUI.getWorkbench()
							.getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
					outputText.append(text + "\n");
					outputText.setStyleRange(styleRange);
				}
			});
			outputStream.println(text);
			outputStream.flush();
		}
	}

	private String[] getCommands() {
		if (commands == null) {
			// Ensures not returning a null object
			commands = new String[] {};
		}
		return commands;
	}

	/**
	 * Extended connection context for handling sics role as string to make
	 * better data binding with JFace.
	 *
	 * @since 1.0
	 */
	private class ExtendedConnectionContext extends SicsConnectionContext {

		private String login;

		private ExtendedConnectionContext() {
			super();
		}

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

	}

}