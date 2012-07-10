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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.widgets.ExtendedComposite;

public abstract class AbstractSicsWidget extends ExtendedComposite {

	@Inject
	private IDataAccessManager dataAccessManager;

	@Inject
	private ISicsManager sicsManager;

	private ISicsProxyListener proxyListener;

	public AbstractSicsWidget(Composite parent, int style) {
		super(parent, style);

		// Dispose
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (proxyListener != null) {
					if (getSicsManager() != null) {
						getSicsManager().proxy().removeProxyListener(
								proxyListener);
					}
					proxyListener = null;
				}
				sicsManager = null;
				dataAccessManager = null;
			}
		});
	}

	@PostConstruct
	public void render() {
		// Render
		handleRender();

		if (getSicsManager() != null) {
			// Connect to SICS
			if (getSicsManager().proxy().isConnected()) {
				internalHandleSicsConnect();
			}

			// Setup to handle dynamic connection
			proxyListener = new SicsProxyListenerAdapter() {
				public void proxyConnected() {
					internalHandleSicsConnect();
				}

				public void proxyDisconnected() {
					internalHandleSicsDisconnect();
				}
			};
			getSicsManager().proxy().addProxyListener(proxyListener);
		}
	}

	protected void internalHandleSicsConnect() {
		// Wait for SICS controller ready
		Job job = new Job("Fetch SICS data") {
			protected IStatus run(IProgressMonitor monitor) {
				// Check if SICS is ready
				if (getSicsManager().control().getSicsController() == null) {
					schedule(500);
					return Status.OK_STATUS;
				}
				// Subclass to handle sics connect
				handleSicsConnect();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected void internalHandleSicsDisconnect() {
		// Subclass to handle sics disconnect
		handleSicsDisconnect();
	}

	protected abstract void handleSicsConnect();

	protected abstract void handleSicsDisconnect();

	protected abstract void handleRender();

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IDataAccessManager getDataAccessManager() {
		return dataAccessManager;
	}

	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		this.dataAccessManager = dataAccessManager;
	}

	public ISicsManager getSicsManager() {
		return sicsManager;
	}

	public void setSicsManager(ISicsManager sicsManager) {
		this.sicsManager = sicsManager;
	}

}
