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

package org.gumtree.gumnix.sics.ui.workflow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.SicsConnectionContext;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;

public class BatchValidatorTask extends AbstractTask {

	private static final String BATCH_NAME = "gumtreeBatchValidation";

	private StyledText outputText;

	private ExtendedConnectionContext context;

	private String[] commands;

	@Override
	protected Object createModelInstance() {
		return null;
	}

	protected ITaskView createViewInstance() {
		return new BatchValidatorTaskView();
	}

	@Override
	protected Object run(Object input) throws WorkflowException {
		/*********************************************************************
		 * Clear text
		 *********************************************************************/
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (outputText != null && !outputText.isDisposed()) {
					outputText.setText("");
				}
			}
		});

		/*********************************************************************
		 * Connect and login to SICS
		 *********************************************************************/
		try (Socket socket = new Socket(getConnectionContext().getHost(), getConnectionContext().getPort());
				BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintStream outputStream = new PrintStream(socket.getOutputStream(), true)) { // Enable auto-flush

			// Set a read timeout. If no data is received for this duration,
			// we assume the batch is complete and the listener thread will terminate.
			socket.setSoTimeout(10000); // 10 seconds

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
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								public void run() {
									if (outputText == null || outputText.isDisposed()) {
										return;
									}
									StyleRange styleRange = new StyleRange();
									styleRange.start = outputText.getCharCount();
									styleRange.length = message.length() + 1;
									styleRange.fontStyle = SWT.BOLD;
									styleRange.foreground = PlatformUI.getWorkbench().getDisplay()
											.getSystemColor(SWT.COLOR_BLUE);
									outputText.append(message + "\n");
									outputText.setStyleRange(styleRange);
									// Auto scroll
									StyledTextContent doc = outputText.getContent();
									int docLength = doc.getCharCount();
									if (docLength > 0) {
										outputText.setCaretOffset(docLength);
										outputText.showSelection();
									}
								}
							});
						}
					} catch (SocketTimeoutException e) {
						// This is our signal that the batch is likely finished.
						final String message = "--> Batch validation finished (timeout waiting for more output).";
						SafeUIRunner.asyncExec(new SafeRunnable() {
							public void run() throws Exception {
								if (outputText != null && !outputText.isDisposed()) {
									outputText.append(message + "\n");
								}
							}
						});
					} catch (IOException e) {
						// If the socket is closed by the main thread's try-with-resources,
						// an exception is expected. Otherwise, it's an error.
						if (!socket.isClosed()) {
							final String errorMessage = "--> Network error: " + e.getMessage();
							SafeUIRunner.asyncExec(new SafeRunnable() {
								public void run() throws Exception {
									if (outputText != null && !outputText.isDisposed()) {
										StyleRange styleRange = new StyleRange();
										styleRange.start = outputText.getCharCount();
										styleRange.length = errorMessage.length() + 1;
										styleRange.foreground = PlatformUI.getWorkbench().getDisplay()
												.getSystemColor(SWT.COLOR_DARK_RED);
										outputText.append(errorMessage + "\n");
										outputText.setStyleRange(styleRange);
									}
								}
							});
						}
					}
				}
			});

			listenerThread.start();

			send(getConnectionContext().getLogin() + " " + getConnectionContext().getPassword(), outputStream);
			send("exe clear", outputStream);
			send("exe upload", outputStream);
			for (String command : getCommands()) {
				send("exe append " + command, outputStream);
			}
			send("exe forcesave " + BATCH_NAME, outputStream);
			send("exe enqueue " + BATCH_NAME, outputStream);
			send("exe run", outputStream);

			// Wait for the listener thread to finish (either by closing stream or timeout)
			listenerThread.join();

		} catch (IOException e) {
			throw new WorkflowException("Failed to connect or communicate with validation server: " + e.getMessage(),
					e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new WorkflowException("Batch validation task was interrupted.", e);
		}
		return null;
	}

	private String[] getCommands() {
		if (commands == null) {
			// Ensures not returning a null object
			commands = new String[] { "wait 1", "cl 123" };
		}
		return commands;
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

	private void send(final String text, PrintStream outputStream) {
		if (outputStream != null) {
			// Send to IO (auto-flush is enabled on PrintStream)
			outputStream.println(text);
			// Update UI
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					if (outputText == null || outputText.isDisposed()) {
						return;
					}
					StyleRange styleRange = new StyleRange();
					styleRange.start = outputText.getCharCount();
					styleRange.length = 1;
					styleRange.fontStyle = SWT.BOLD;
					outputText.append(">> ");
					outputText.setStyleRange(styleRange);
					styleRange = new StyleRange();
					styleRange.start = outputText.getCharCount();
					styleRange.length = text.length() + 1;
					styleRange.foreground = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
					outputText.append(text + "\n");
					outputText.setStyleRange(styleRange);
				}
			});
		}
	}

	private class BatchValidatorTaskView extends AbstractTaskView {

		private Text hostText;
		private Text portText;
		private Text loginText;
		private Text passwordText;
		private DataBindingContext bindingContext;

		public void createPartControl(Composite parent) {
			parent.setLayout(new GridLayout());
			Group configGroup = new Group(parent, SWT.NONE);
			configGroup.setText("Validation Server");
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(configGroup);
			getToolkit().adapt(configGroup);

			createConnnectionConfigArea(configGroup);

			createOutputArea(parent);

			// Add dispose listener to clean up data binding resources
			parent.addDisposeListener(e -> {
				if (bindingContext != null) {
					bindingContext.dispose();
				}
			});
		}

		private void createConnnectionConfigArea(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			getToolkit().createLabel(parent, "Host: ");
			hostText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(250, SWT.DEFAULT).applyTo(hostText);

			getToolkit().createLabel(parent, "Port: ");
			portText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(250, SWT.DEFAULT).applyTo(portText);

			getToolkit().createLabel(parent, "Login: ");
			loginText = getToolkit().createText(parent, "");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(250, SWT.DEFAULT).applyTo(loginText);

			getToolkit().createLabel(parent, "Password: ");
			passwordText = getToolkit().createText(parent, "", SWT.PASSWORD);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(250, SWT.DEFAULT)
					.applyTo(passwordText);

			// Data binding with modern JFace API
			Realm.runWithDefault(DisplayRealm.getRealm(PlatformUI.getWorkbench().getDisplay()), new Runnable() {
				public void run() {
					bindingContext = new DataBindingContext();

					// Bind host
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(hostText),
							BeanProperties.value("host").observe(getConnectionContext()), new UpdateValueStrategy(),
							new UpdateValueStrategy());

					// Bind port
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(portText),
							BeanProperties.value("port").observe(getConnectionContext()), new UpdateValueStrategy(),
							new UpdateValueStrategy());

					// Bind login
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(loginText),
							BeanProperties.value("login").observe(getConnectionContext()), new UpdateValueStrategy(),
							new UpdateValueStrategy());

					// Bind password
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(passwordText),
							BeanProperties.value("password").observe(getConnectionContext()), new UpdateValueStrategy(),
							new UpdateValueStrategy());
				}
			});
		}

		private void createOutputArea(Composite parent) {
			parent.setLayout(new GridLayout());
			outputText = new StyledText(parent,
					SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.READ_ONLY);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(SWT.DEFAULT, 300)
					.applyTo(outputText);
		}

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

	public Class<?>[] getInputTypes() {
		return null;
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}

}
