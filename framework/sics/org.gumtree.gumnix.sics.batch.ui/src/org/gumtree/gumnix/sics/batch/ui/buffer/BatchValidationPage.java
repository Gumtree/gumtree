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

package org.gumtree.gumnix.sics.batch.ui.buffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.Socket;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.SicsConnectionContext;
import org.gumtree.ui.widgets.AutoScrollStyledText;
import org.gumtree.widgets.swt.forms.ExtendedFormComposite;

public class BatchValidationPage extends ExtendedFormComposite {

	private static final String KEY_PREV_DOC_LEN = "previousDocLength";
	
	private IBatchBufferManager manager;
	
	private UIContext context;
	
	private ExtendedConnectionContext connContext;
	
	public BatchValidationPage(Composite parent, int style) {
		super(parent, style);
	}
	
	@PostConstruct
	protected void render() {
		context = new UIContext();
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);
		
		// Queue
		context.queueViewer = new BatchBufferQueueViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		context.queueViewer.setManager(getManager());
		context.queueViewer.afterParametersSet();
		context.queueViewer.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IBatchBuffer buffer = (IBatchBuffer) ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (buffer != null) {
					context.editorText.setText(buffer.getContent());
				}
			}
		});
		GridDataFactory.fillDefaults().grab(false, true).applyTo(context.queueViewer);
		
		// Display
		createDisplayArea(this);

		// Sync
		Button syncButton = getToolkit().createButton(this, "Synchronise", SWT.PUSH);
		syncButton.setToolTipText("Click to synchronise the validation server with the real server.");
		syncButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runSync();
			}
		});
		GridDataFactory.fillDefaults().grab(false, false).applyTo(syncButton);

		// Validation
		Button validateButton = getToolkit().createButton(this, "Validate", SWT.PUSH);
		validateButton.setToolTipText("Click to validate the selected script.");
		validateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IBatchBuffer buffer = (IBatchBuffer) ((IStructuredSelection) context.queueViewer.getViewer()
						.getSelection()).getFirstElement();
				if (buffer != null) {
					runValidation(buffer);
				}
			}
		});
		GridDataFactory.fillDefaults().grab(false, false).applyTo(validateButton);
		
		// Login
		createLoginArea(this);
		getParent().layout(true, true);
	}
	
	private void createDisplayArea(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		getToolkit().adapt(sashForm);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 4).applyTo(sashForm);
		
		// Editor
		Group editorGroup = new Group(sashForm, SWT.NONE);
		editorGroup.setText("Buffer");
		getToolkit().adapt(editorGroup);
		editorGroup.setLayout(new FillLayout());
		editorGroup.setFont(JFaceResources.getBannerFont());
		context.editorText = new AutoScrollStyledText(editorGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		context.editorText.setFont(JFaceResources.getBannerFont());
		context.editorText.setData(KEY_PREV_DOC_LEN, 0);
		
		// Log
		Group logGroup = new Group(sashForm, SWT.NONE);
		logGroup.setText("Log");
		getToolkit().adapt(logGroup);
		logGroup.setLayout(new FillLayout());
		logGroup.setFont(JFaceResources.getBannerFont());
		context.logText = new AutoScrollStyledText(logGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		context.logText.setFont(JFaceResources.getBannerFont());
		context.logText.setData(KEY_PREV_DOC_LEN, 0);
		
		sashForm.setWeights(new int[] { 1, 1 });
	}
	
	private void createLoginArea(Composite parent) {
		Group loginGroup = new Group(parent, SWT.NONE);
		loginGroup.setText("Validation Server");
		getToolkit().adapt(loginGroup);
		GridLayoutFactory.swtDefaults().applyTo(loginGroup);
		GridDataFactory.swtDefaults().applyTo(loginGroup);

		getToolkit().createLabel(loginGroup, "Host: ");
		final Text hostText = getToolkit().createText(loginGroup, "");
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		getToolkit().createLabel(loginGroup, "Port: ");
		final Text portText = getToolkit().createText(loginGroup, "");
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		getToolkit().createLabel(loginGroup, "Login: ");
		final Text loginText = getToolkit().createText(loginGroup, "");
		loginText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		getToolkit().createLabel(loginGroup, "Password: ");
		final Text passwordText = getToolkit().createText(loginGroup, "", SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Realm.runWithDefault(SWTObservables.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(hostText,
						SWT.Modify), BeansObservables.observeValue(
						getConnectionContext(), "host"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(portText,
						SWT.Modify), BeansObservables.observeValue(
						getConnectionContext(), "port"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(loginText,
						SWT.Modify), BeansObservables.observeValue(
						getConnectionContext(), "login"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(
						passwordText, SWT.Modify), BeansObservables.observeValue(
						getConnectionContext(), "password"), new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
	}
	


	private ExtendedConnectionContext getConnectionContext() {
		if (connContext == null) {
			connContext = new ExtendedConnectionContext();
			connContext.setHost(SicsCoreProperties.VALIDATION_HOST.getValue());
			connContext.setPort(SicsCoreProperties.VALIDATION_PORT.getInt());
			connContext.setLogin(SicsCoreProperties.ROLE.getValue());
			connContext.setPassword(SicsCoreProperties.PASSWORD.getValue());
			ISicsConnectionContext currentContext = SicsCore.getDefaultProxy().getConnectionContext();
			if (currentContext != null) {
				connContext.setLogin(currentContext.getRole().getLoginId());
				connContext.setPassword(currentContext.getPassword());
			}
		}
		return connContext;
	}
	
	@Override
	protected void disposeWidget() {
		if (manager != null) {
			manager = null;
		}
		if (context != null) {
			context.dispose();
			context = null;
		}
	}
	
	/*************************************************************************
	 * Getters and setters
	 *************************************************************************/
	
	public IBatchBufferManager getManager() {
		return manager;
	}
	
	@Inject
	public void setManager(IBatchBufferManager manager) {
		this.manager = manager;
	}
	
	/*************************************************************************
	 * Operation
	 *************************************************************************/
	
	private void runValidation(IBatchBuffer buffer) {
		context.logText.setText("");
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
							while (replyMessage.startsWith("�")) {
								replyMessage = replyMessage.substring(2);
							}
							if (context.logText == null || context.logText.isDisposed()) {
								break;
							}
							final String message = replyMessage;
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											if (context.logText == null
													|| context.logText.isDisposed()) {
												return;
											}
											int fontColor = SWT.COLOR_BLUE;
											if (message.toLowerCase().startsWith("error")) {
												fontColor = SWT.COLOR_RED;
											}
											StyleRange styleRange = new StyleRange();
											styleRange.start = context.logText
													.getCharCount();
											styleRange.length = message
													.length() + 1;
											styleRange.fontStyle = SWT.BOLD;
											styleRange.foreground = PlatformUI
													.getWorkbench()
													.getDisplay()
													.getSystemColor(
															fontColor);
											context.logText.append(message + "\n");
											context.logText
													.setStyleRange(styleRange);
											// Auto scroll
											StyledTextContent doc = context.logText
													.getContent();
											int docLength = doc.getCharCount();
											if (docLength > 0) {
												context.logText
														.setCaretOffset(docLength);
												context.logText.showSelection();
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
			BufferedReader reader  = new BufferedReader(new StringReader(buffer.getContent()));
			String line = null;
			try {
				while((line = reader.readLine()) != null) {
					line = line.replace("{", "\\{").replace("}", "\\}").replace("\"", "\\\"");
					send("exe append " + line, outputStream);
				}
			} catch (Exception e) {
				// TODO
			}
			send("exe forcesave " + buffer.getName(), outputStream);
			send("exe enqueue " + buffer.getName(), outputStream);
			send("exe run", outputStream);
		} catch (IOException e) {
			context.logText.setText("Cannot connect to server");
		}
		// Append batch
		// Run and capture output
	}
	
	
	private void runSync() {
		context.logText.setText("");
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
							while (replyMessage.startsWith("�")) {
								replyMessage = replyMessage.substring(2);
							}
							if (context.logText == null || context.logText.isDisposed()) {
								break;
							}
							final String message = replyMessage;
							PlatformUI.getWorkbench().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											if (context.logText == null
													|| context.logText.isDisposed()) {
												return;
											}
											StyleRange styleRange = new StyleRange();
											styleRange.start = context.logText
													.getCharCount();
											styleRange.length = message
													.length() + 1;
											styleRange.fontStyle = SWT.BOLD;
											styleRange.foreground = PlatformUI
													.getWorkbench()
													.getDisplay()
													.getSystemColor(
															SWT.COLOR_BLUE);
											context.logText.append(message + "\n");
											context.logText
													.setStyleRange(styleRange);
											// Auto scroll
											StyledTextContent doc = context.logText
													.getContent();
											int docLength = doc.getCharCount();
											if (docLength > 0) {
												context.logText
														.setCaretOffset(docLength);
												context.logText.showSelection();
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
//			send("exe upload", outputStream);
			send("getlog value", outputStream);
			send("getlog command", outputStream);
			send("sync", outputStream);
			send("getlog kill", outputStream);
		} catch (IOException e) {
			context.logText.setText("Cannot connect to server");
		}
		// Append batch
		// Run and capture output
	}
	
	private void send(final String text, PrintStream outputStream) {
		if (outputStream != null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					StyleRange styleRange = new StyleRange();
					styleRange.start = context.logText.getCharCount();
					styleRange.length = 1;
					styleRange.fontStyle = SWT.BOLD;
					context.logText.append(">> ");
					context.logText.setStyleRange(styleRange);
					styleRange = new StyleRange();
					styleRange.start = context.logText.getCharCount();
					styleRange.length = text.length() + 1;
					styleRange.foreground = PlatformUI.getWorkbench()
							.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
					context.logText.append(text + "\n");
					context.logText.setStyleRange(styleRange);
				}
			});
			outputStream.println(text);
			outputStream.flush();
		}
	}
	
	/*************************************************************************
	 * Internal model
	 *************************************************************************/
	
	private class UIContext {
		private BatchBufferQueueViewer queueViewer;
		private AutoScrollStyledText editorText;
		private AutoScrollStyledText logText;
		private void dispose() {
			logText = null;
			editorText = null;
			queueViewer = null;
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
	
}
