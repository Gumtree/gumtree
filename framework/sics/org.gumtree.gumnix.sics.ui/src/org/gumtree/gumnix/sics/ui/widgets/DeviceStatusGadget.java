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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.control.events.SicsControllerEvent;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.service.eventbus.IFilteredEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.util.PlatformUtils;

/**
 * @author Tony Lam
 * @since 1.4
 */
public class DeviceStatusGadget extends FormControlWidget {

	public static final int SHOW_ICON = 1 << 1;
	
	public static final int SHOW_UNIT = 1 << 2;
	
	// Listen to the SICS proxy connect/disconnect event
	private ISicsProxyListener proxyListener;
	
	// Source of the status
	private IDataAccessManager dam;
	
	// Listen to controller changes
	private IFilteredEventHandler<SicsControllerEvent> eventHandler;
	
	// List of URI for monitoring
	private List<URI> deviceURIs;
	
	// Containers for status update event
	private Map<URI, Context> contexts;
	
	// Label, font and colour provider
	private ILabelProvider labelProvider; 
	
	public DeviceStatusGadget(Composite parent, int style) {
		super(parent, style);
		getParent().layout(true, true);
	}
	
	private void bindProxy() {
		// Wait for SICS controller ready
		Job job = new Job("Fetch SICS data") {
			protected IStatus run(IProgressMonitor monitor) {
				// Check if SICS is ready
				if (SicsCore.getSicsController() == null) {
					schedule(500);
					return Status.OK_STATUS;
				}
				// Setup UI
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						if (isDisposed()) {
							return;
						}
						setupUI();
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	private void unbindProxy() {
		// Clear value on unbinding
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				for (Context context : contexts.values()) {
					// Context can be null for non-existing device
					if(context != null) {
						updateValue(context.uri, "--");
					}
				}
			}
		});
	}
	
	// Avoid calling it to getDeviceURIs for bean setting error
	public List<URI> getDeviceURIList() {
		if (deviceURIs == null) {
			deviceURIs = new ArrayList<URI>();
		}
		return deviceURIs;
	}

	protected Map<URI, Context> getContexts() {
		return contexts;
	}
	
	protected IDataAccessManager getDam() {
		if (dam == null) {
			dam = ServiceUtils.getService(IDataAccessManager.class);
		}
		return dam;
	}
	
	// Used by dashboard configuration
	public void setDeviceURIs(String uris) {
		String[] uriStrings = uris.split(",");
		getDeviceURIList().clear();
		for (String uriString : uriStrings) {
			getDeviceURIList().add(URI.create(uriString.trim()));
		}
	}
	
	public ILabelProvider getLabelProvider() {
		if (labelProvider == null) {
			labelProvider = new LabelProvider() {
				public String getText(Object element) {
					URI uri = (URI) element;
					return getDam().get(URI.create(uri.toString() + "?sicsdev"), String.class);
				}
			};
		}
		return labelProvider;
	}
	
	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}
	
	public void afterParametersSet() {
		contexts = new LinkedHashMap<URI, Context>();
		
		/*********************************************************************
		 * Setup event handler
		 *********************************************************************/
		eventHandler = new IFilteredEventHandler<SicsControllerEvent>() {
			public void handleEvent(final SicsControllerEvent event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						if (isDisposed()) {
							return;
						}
						updateValue(event.getURI(), getDam().get(event.getURI(), String.class));	
					}
				});
			}
			public boolean isDispatchable(SicsControllerEvent event) {
				return contexts.containsKey(event.getURI());
			}
		};
		PlatformUtils.getPlatformEventBus().subscribe(eventHandler);
		
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

	protected void setupUI() {
		/*********************************************************************
		 * Dispose old controls
		 *********************************************************************/
		for (Control child : this.getChildren()) {
			child.dispose();
		}
		
		/*********************************************************************
		 * Setup contexts
		 *********************************************************************/
		if (contexts != null) {
			contexts.clear();
		}
		
		/*********************************************************************
		 * Compute layout
		 *********************************************************************/
		int numColumn = 1;
		if ((getOriginalStyle() & SHOW_ICON) != 0) {
			numColumn++;
		}
		GridLayoutFactory.swtDefaults().numColumns(numColumn).margins(3, 3).spacing(3, 3).applyTo(this);
		
		/*********************************************************************
		 * Create UI
		 *********************************************************************/
		for (URI uri : getDeviceURIList()) {
			// Use empty uri as separator when non-empty
			if ((uri == null || uri.toString().length() == 0) && contexts.size() != 0) {
				Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
				GridDataFactory.fillDefaults().span(numColumn, 1).applyTo(separator);
				continue;
			}
			
			// Check if device exist
			if (SicsCore.getSicsController().findComponentController(
					uri.getPath()) == null) {
				continue;
			}
			
			// Create new context
			Context context = new Context();
			context.uri = uri;
			contexts.put(uri, context);
			
			// Icon
			if ((getOriginalStyle() & SHOW_ICON) != 0) {
				Label iconLabel = new Label(this, SWT.NONE);
				iconLabel.setForeground(getForeground());
				iconLabel.setBackground(getBackground());
				iconLabel.setImage(getLabelProvider().getImage(uri));
			}
			
			// Label
//			Label label = new Label(this, SWT.NONE);
//			label.setForeground(getForeground());
//			label.setBackground(getBackground());
//			label.setText(getLabelProvider().getText(uri) + ":");
//			GridDataFactory.fillDefaults().applyTo(label);
			
			// Display
			Label displayLabel = new Label(this, SWT.NONE);
			displayLabel.setForeground(getForeground());
			displayLabel.setBackground(getBackground());
			displayLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			GridDataFactory.fillDefaults().grab(true, false).applyTo(displayLabel);
			context.displayLabel = displayLabel;
			updateValue(uri, getDam().get(uri, String.class));			
		}
		
		/*********************************************************************
		 * Layout
		 *********************************************************************/
		this.getParent().layout(true, true);
	}
	
	protected void updateValue(URI uri, String data) {
		Context context = contexts.get(uri);
		
		// Set with unit
		if ((getOriginalStyle() & SHOW_UNIT) != 0) {
			String unit = null;
			try {
				unit = getDam().get(URI.create(uri.toString() + "?units"), String.class);
			} catch (Exception e) {
			}
			if (unit != null) {
				context.displayLabel.setText(getLabelProvider().getText(uri) + ": " + data + " " + unit);
				// fix bug: label not shown if units are null
				return;
			}
		}
		// Normal
		context.displayLabel.setText(getLabelProvider().getText(uri) + ": " + data);
	}
	
	protected void widgetDispose() {
		if (proxyListener != null) {
			SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		if (eventHandler != null) {
			PlatformUtils.getPlatformEventBus().unsubscribe(eventHandler);
			eventHandler = null;
		}
		if (deviceURIs != null) {
			deviceURIs.clear();
			deviceURIs = null;
		}
		if (contexts != null) {
			contexts.clear();
			contexts = null;
		}
		dam = null;
		super.dispose();
	}
	
	protected class Context {
		public URI uri;
		public Label displayLabel;
	}
}
