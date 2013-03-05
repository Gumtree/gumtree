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

package org.gumtree.gumnix.sics.ui.widgets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.control.IHipadabaListener;
import org.gumtree.gumnix.sics.control.ServerStatus;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.gumnix.sics.widgets.swt.ExtendedSicsComposite;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HMVetoGadget extends ExtendedSicsComposite {

	private static final String SICS_COMMAND_VETO_ON = "histmem veto on";
	private static final String SICS_COMMAND_VETO_OFF = "histmem veto off";
	private static Logger logger = LoggerFactory.getLogger(HMVetoGadget.class);
	
	private Label status;
	
	private Label button;
	
	private Cursor handCursor;
	
	private Image pauseImage;
	
	private Image continueImage;
	
//	private HttpClient client;
	
	private ISicsProxyListener proxyListener;
	
	private IHipadabaListener statusListener;
	
//	private static String host = "http://das1-test.nbi.ansto.gov.au";
	
//	private static String host = "http://" + System.getProperty("gumtree.dae.host");
			
//	private static String port = "8081";
	
//	private static String port = System.getProperty("gumtree.dae.port");
	
//	private boolean isVetoed = false;
	
//	private boolean isAuth = false;
	
//	private boolean isRequested = false;
	
	public HMVetoGadget(Composite parent, int style) {
		super(parent, style);
	}

	protected void handleSicsConnect() {
		// Wait for SICS controller ready
		Job job = new Job("Fetch SICS data") {
			protected IStatus run(IProgressMonitor monitor) {
				// Check if SICS is ready
				if (SicsCore.getSicsController() == null) {
					schedule(500);
					return Status.OK_STATUS;
				}
				// Bind
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						SicsCore.getSicsManager().monitor().addListener("/", statusListener);
						// Fetch
						updateUI();
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	protected void handleSicsDisconnect() {
		if (statusListener != null) {
			SicsCore.getSicsManager().monitor().removeListener("/", statusListener);
		}
		updateUI();
	}
	
	public void handleRender() {
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(this);
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, this);
		status = getWidgetFactory().createLabel(this, "", SWT.CENTER);
		status.setText("Click to Pause Counting");
		status.setFont(UIResources.getDefaultFont(SWT.BOLD));
		status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory.swtDefaults().align(SWT.CENTER, GridData.VERTICAL_ALIGN_CENTER)
				.indent(SWT.DEFAULT, 8).grab(true, false).applyTo(status);
		button = getWidgetFactory().createLabel(this, "", SWT.CENTER);
		pauseImage = resourceManager.createImage("icons/button_blue_pause.png");
		continueImage = resourceManager.createImage("icons/StepForwardNormalBlue16.png");
		button.setImage(pauseImage);
		button.setToolTipText("Button is enabled only when the server is in counting status.");
//		button.setText("PAUSED");
		GridDataFactory.swtDefaults().align(SWT.LEFT, GridData.VERTICAL_ALIGN_CENTER)
				.hint(50, 32).grab(false, false).applyTo(button);
		button.setEnabled(false);
		FontData[] fontData = button.getFont().getFontData();
		fontData[0].setHeight(16);
		button.setFont(new Font(button.getDisplay(), fontData));
		handCursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
		button.setCursor(handCursor);
		button.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				try {
					// Action: interrupt SICS
//					runVeto(!isVetoed);
//					isRequested = !isRequested;
					ServerStatus serverStatus = SicsCore.getSicsController().getServerStatus();
//					if (isRequested) {
//						if (!isVetoed) {
//							runVeto(true);
//							isVetoed = true;
//						} else {
//							button.setImage(continueImage);
////							status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
//							status.setText("Counting Paused");
//						}
//					} else {
//						if (isVetoed) {
//							runVeto(false);
//							isVetoed = false;
//						} else {
//							button.setImage(pauseImage);
////							status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
//							status.setText("Click to Pause Counting");
//						}
//					}
					switch (serverStatus) {
					case PAUSED:
						runVeto(false);
						break;
					case COUNTING:
						runVeto(true);
						break;
					default:
						break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
//		this.layout(true, true);
		// Set UI status
		updateUI();
		// Register proxy listener
		if (SicsCore.getDefaultProxy().isConnected()) {
			handleSicsConnect();
		}
		
		// Setup to handle dynamic connection
		proxyListener = new SicsProxyListenerAdapter() {
			public void proxyConnected() {
				handleSicsConnect();
			}
			public void proxyDisconnected() {
				handleSicsDisconnect();
			}
		};
		
		// Setup status listener
		statusListener = new IHipadabaListener() {
			public void valueUpdated(String newValue) {
				updateUI();
			}
		};
		
		SicsCore.getDefaultProxy().addProxyListener(proxyListener);
	}

	public void widgetDispose() {
		if (proxyListener != null) {
			SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		if (handCursor != null) {
			handCursor.dispose();
			handCursor = null;
		}
		if (pauseImage != null && !pauseImage.isDisposed()){
			pauseImage.dispose();
			pauseImage = null;
		}
		if (continueImage != null && !continueImage.isDisposed()){
			continueImage.dispose();
			continueImage = null;
		}			
		if (status != null && !status.isDisposed()){
			status.dispose();
			status = null;
		}
		if (button != null && !button.isDisposed()){
			button.dispose();
			button = null;
		}
	}
	
	private void updateUI() {
		if (button != null && !button.isDisposed()) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					if (isDisposed() || button == null) {
						return;
					}
					if (SicsCore.getSicsController() == null || !SicsCore.getDefaultProxy().isConnected()) {
						button.setEnabled(false);
						return;
					}
					ServerStatus serverStatus = SicsCore.getSicsController().getServerStatus();
//					if (isRequested) {
//						if (!isVetoed) {
//							if (serverStatus.equals(ServerStatus.COUNTING)) {
//								System.err.println("veto triggered");
//								runVeto(true);
//								isVetoed = true;
//							}
//						} else {
//							status.setText("Click to Pause Counting");
//							button.setImage(pauseImage);
//							isVetoed = false;
//						}
//					} else {
//						button.setImage(pauseImage);
//						status.setText("Click to Pause Counting");
//						isVetoed = false;
//					}
//					System.err.println(serverStatus);
//					if (serverStatus.equals(ServerStatus.PAUSED) || serverStatus.equals(ServerStatus.COUNTING)) {
//						button.setEnabled(true);
//					} else {
//						button.setEnabled(false);
//						if (isVetoed) {
//							button.setImage(pauseImage);
//							status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
//							status.setText("Click to Pause Counting");
//							isVetoed = false;
//							isRequested = false;
//						}
//					}
					switch (serverStatus) {
					case PAUSED:
						button.setEnabled(true);
						button.setImage(continueImage);
						status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
						status.setText("Counting Paused");
						break;
					case COUNTING:
						button.setEnabled(true);
						button.setImage(pauseImage);
						status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
						status.setText("Click to Pause Counting");
						break;
					default:
						button.setEnabled(false);
						button.setImage(pauseImage);
						status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
						status.setText("Click to Pause Counting");
						break;
					}
					layout(true, true);
				}
			});
		}
	}

	private void runVeto(boolean vetoFlag) throws Exception {
//		String vetoEgi = null;
//		if (vetoFlag) {
//			vetoEgi = "/admin/guienablesoftveto.egi";
//		} else {
//			vetoEgi = "/admin/guidisablesoftveto.egi";
//		}
//		String seizeControl = "/admin/seizereleasecontrolconfig.egi";
//		getLink(seizeControl);
//		getLink(vetoEgi);
//		getLink(seizeControl);
//		if (vetoFlag) {
//			button.setImage(continueImage);
//			status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
//			status.setText("Counting Paused");
//			((SicsMonitor) SicsCore.getSicsManager().monitor()).notifyListener("/", "Paused");
//			System.err.println("Counting Paused");
//		} else {
//			button.setImage(pauseImage);
//			status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
//			status.setText("Click to Pause Counting");
//			((SicsMonitor) SicsCore.getSicsManager().monitor()).notifyListener("/", "Counting");
//		}
		if (vetoFlag) {
			SicsCore.getDefaultProxy().send(SICS_COMMAND_VETO_ON, null, ISicsProxy.CHANNEL_STATUS);
		} else {
			SicsCore.getDefaultProxy().send(SICS_COMMAND_VETO_OFF, null, ISicsProxy.CHANNEL_STATUS);
		}
	}
	
//	private void getLink(String path) throws Exception {
//		GetMethod getMethod = new GetMethod(host + ":" + port);
//		if (isAuth) {
//			getMethod.setDoAuthentication(false);
//		} else {
//			getMethod.setDoAuthentication(true);
//		}
//		getMethod.setPath(path);
//		int statusCode = getClient().executeMethod(getMethod);
//		if (statusCode != HttpStatus.SC_OK) {
//			logger.error("HTTP GET failed: " + getMethod.getStatusLine());
//			getMethod.releaseConnection();
//			throw new Exception("Cannot get file");
//		}
//		getMethod.releaseConnection();
//	}
	
//	private HttpClient getClient() {
//		if (client == null) {
//			synchronized (HMVetoGadget.class) {
//				if (client == null) {
//					client = new HttpClient();
//					
//					// Set proxy if available
////					if (getProxyHost() != null && getProxyPort() != null) {
////						client.getHostConfiguration().setProxy(getProxyHost(), getProxyPort());
////					}
//					
//					// Set credentials if login information supplied
//					client.getParams().setAuthenticationPreemptive(true);
//					Credentials defaultcreds = new UsernamePasswordCredentials("manager", "ansto");
//					client.getState().setCredentials(AuthScope.ANY, defaultcreds);
//				}
//			}
//		}
//		return client;
//	}

	protected void disposeWidget() {
		super.disposeWidget();
		widgetDispose();
	}

}
