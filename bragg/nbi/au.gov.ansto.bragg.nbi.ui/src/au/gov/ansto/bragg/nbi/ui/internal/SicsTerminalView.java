/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.gumnix.sics.control.ServerStatus;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.ui.terminal.support.CommandLineTerminal;


/**
 * @author nxi
 * Created on 19/02/2009
 */
public class SicsTerminalView extends CommandLineTerminal {

	public static final String SICS_TELNET_ADAPTOR_ID = "org.gumtree.gumnix.sics.ui.serverCommunicationAdapter";
	private Composite parent = null;
	
	/**
	 * 
	 */
	public SicsTerminalView() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.internal.terminal.CommandLineTerminal#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		this.parent = parent;
		Thread thread = new Thread(){
			boolean isConnected = false;
			boolean isFirstConnection = true;
			@Override
			public void run() {
				try {
					while(!isDisposed()){
						ISicsProxy proxy = SicsCore.getDefaultProxy();
						boolean connectionStatus = proxy != null && proxy.isConnected() 
								&& SicsCore.getSicsController() != null 
								&& SicsCore.getSicsController().getServerStatus() != ServerStatus.UNKNOWN;
						if (connectionStatus != isConnected) {
							if (connectionStatus) {
								Display.getDefault().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										try {
											if (isFirstConnection) {
												selectCommunicationAdapter(SICS_TELNET_ADAPTOR_ID);
												isFirstConnection = false;
											}
											connect();
											isConnected = true;
										} catch (Exception e) {
										}
									}
								});
								int time = 0;
								while (!isConnected && time < 5000) {
									Thread.sleep(500);
									time += 500;
								}
							} else {
								Display.getDefault().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										try {
											disconnect();
											isConnected = false;
										} catch (Exception e) {
										}
									}
								});
								int time = 0;
								while (isConnected && time < 5000) {
									Thread.sleep(500);
									time += 500;
								}
							}
						}
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		
//		SafeRunner.run(new ISafeRunnable() {
//
//			@Override
//			public void run() throws Exception {
//				while(SicsCore.getSicsController() == null){
//					Thread.sleep(500);
//				}
//				selectCommunicationAdapter(SICS_TELNET_ADAPTOR_ID);
//				connect();					
//			}
//
//			@Override
//			public void handleException(Throwable exception) {
//				exception.printStackTrace();
//			}
//		});
	}

	private boolean isDisposed() {
		return parent.isDisposed();
	}
}
