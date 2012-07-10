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

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.events.SicsControllerEvent;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.service.eventbus.IFilteredEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.util.PlatformUtils;

public abstract class AbstractSicsDeviceWidget extends FormControlWidget {

	private IDataAccessManager dam;
	
	private ISicsProxyListener proxyListener;
	
	protected List<URI> deviceURIs = new ArrayList<URI>();
	
	protected Map<URI, Object> uriMap = new LinkedHashMap<URI, Object>();
	
	private IFilteredEventHandler<SicsControllerEvent> eventHandler;
	
	public AbstractSicsDeviceWidget(Composite parent, int style) {
		super(parent, style);
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (proxyListener != null) {
					SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
					proxyListener = null;
				}
				dam = null;
			}
		});
	}

	protected void bindProxy() {
		// Schedule to fetch initial value on binding
		Job job = new Job("Fetch SICS data") {
			protected IStatus run(IProgressMonitor monitor) {
				// Check if SICS is ready
				if (SicsCore.getSicsController() == null) {
					schedule(500);
					return Status.OK_STATUS;
				}
				// Initialise
				uriMap.clear();
				initialise();
				// Create UI
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						createUI();
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	protected void unbindProxy() {
		// Clear value and controllers on unbinding
//		for (Control control : uriMap.values()) {
//			updateData(control, "-- --");
//		}
		// Disable widget
//		SafeUIRunner.asyncExec(new SafeRunnable() {
//			public void run() throws Exception {
//				AbstractSicsDeviceWidget.this.setEnabled(false);
//			}
//		});
	}
	
	protected void afterWidgetParametersSet() {
		/*********************************************************************
		 * Setup event handler
		 *********************************************************************/
		eventHandler = new IFilteredEventHandler<SicsControllerEvent>() {
			public void handleEvent(SicsControllerEvent event) {
				Class<?> representation = String.class;
				if ("status".equals(event.getURI().getQuery())) {
					representation = ControllerStatus.class;
				} 
				updateData(event.getURI(), uriMap.get(event.getURI()),
						getDataAccessManager().get(event.getURI(), representation));
			}
			public boolean isDispatchable(SicsControllerEvent event) {
				return uriMap.containsKey(event.getURI());
			}
		};
		PlatformUtils.getPlatformEventBus().subscribe(eventHandler);
	}

	public void afterParametersSet() {
		/*********************************************************************
		 * set subclass
		 *********************************************************************/
		afterWidgetParametersSet();
		
		/*********************************************************************
		 * Setup SICS
		 *********************************************************************/
		// Connect now
		if (SicsCore.getDefaultProxy().isConnected()) {
			bindProxy();
		}
		
		// Setup to handle dynamic connection
		proxyListener = new SicsProxyListenerAdapter() {
			public void proxyConnected() {
				bindProxy();
			}
			public void proxyDisconnected() {
				unbindProxy();
			}
		};
		SicsCore.getDefaultProxy().addProxyListener(proxyListener);
	}
	
	private void updateData(final URI uri, final Object widget, final Object data) {
		if (isDisposed()) {
			return;
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				updateWidgetData(uri, widget, data);
			}
		});
	}
	
	protected abstract void initialise();
	
	protected abstract void createUI();
	
	protected abstract void updateWidgetData(URI uri, Object widget, Object data);
	
	protected void widgetDispose() {
		if (eventHandler != null) {
			PlatformUtils.getPlatformEventBus().unsubscribe(eventHandler);
			eventHandler = null;
		}
		if (deviceURIs != null) {
			deviceURIs.clear();
			deviceURIs = null;
		}
		if (uriMap != null) {
			uriMap.clear();
			uriMap = null;
		}
	}

	/*************************************************************************
	 * Getters and setters
	 *************************************************************************/
	
	public void setDeviceURIs(String uris) {
		String[] uriList = uris.split(",");
		deviceURIs = new ArrayList<URI>();
		for (String uri : uriList) {
			deviceURIs.add(URI.create(uri.trim()));
		}
	}
	
	protected IDataAccessManager getDataAccessManager() {
		if (dam == null) {
			dam = ServiceUtils.getService(IDataAccessManager.class);
		}
		return dam;
	}
	
}
