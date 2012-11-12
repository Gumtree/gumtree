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

package au.gov.ansto.bragg.nbi.ui.widgets;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.control.ISicsMonitor;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;

public class ShutterStatusWidget extends SicsStatusWidget {

	private EventHandler eventHandler;
	
	private Set<Context> contexts;

	public ShutterStatusWidget(Composite parent, int style) {
		super(parent, style);
		contexts = new HashSet<Context>();
	}

	public void renderWidget() {
		setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.swtDefaults().applyTo(this);

		Label label = getToolkit().createLabel(this, "Secondary\n--",
				SWT.CENTER | SWT.WRAP | SWT.BORDER);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(label);
		Context context = new Context();
		context.path = "/instrument/status/secondary";
		context.label = label;
		context.originalForeground = label.getForeground();
		context.handler = new HdbEventHandler(context).activate();
		contexts.add(context);

		label = getToolkit().createLabel(this, "Sample\n--",
				SWT.CENTER | SWT.WRAP | SWT.BORDER);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(label);
		context = new Context();
		context.path = "/instrument/status/tertiary";
		context.label = label;
		context.originalForeground = label.getForeground();
		context.handler = new HdbEventHandler(context).activate();
		contexts.add(context);

		// TODO: test this code
		// [GUMTREE-141] Blinking
		eventHandler = new EventHandler(IDirectoryService.EVENT_TOPIC_BIND) {
			@Override
			public void handleEvent(Event event) {
				// TODO: use topic to filter
				if (event.getProperty(IDirectoryService.EVENT_PROP_NAME).equals("shutterStatusCheckFailed")) {
					if ((Boolean) event.getProperty(IDirectoryService.EVENT_PROP_OBJECT)) {
						final int[] counter = new int[] { 0 };
						final Map<Label, Color> originalColourMap = new HashMap<Label, Color>();
						Job job = new Job("") {
							protected IStatus run(IProgressMonitor monitor) {
								// Over 5 times ... exit
								if (isDisposed() || counter[0] > 5) {
									return Status.OK_STATUS;
								}
								SafeUIRunner.asyncExec(new SafeRunnable() {
									public void run() throws Exception {
										for (Context context : contexts) {
											if (!context.isActivated) {
												continue;
											}
											// Initialise and finalise
											if (counter[0] == 0) {
												originalColourMap.put(
														context.label,
														context.label
																.getBackground());
											} else if (counter[0] >= 5) {
												context.label
														.setBackground(originalColourMap
																.get(context.label));
											}
											// Blinking
											if (counter[0] % 2 == 0) {
												context.label.setBackground(null);
											} else {
												context.label
														.setBackground(originalColourMap
																.get(context.label));
											}
										}
										counter[0]++;
									}
								});
								// Repeat every 0.5 sec
								schedule(500);
								return Status.OK_STATUS;
							}
						};
						// Run now
						job.schedule();
					}
				}
				
			}
		};
	}

	protected void enableWidget() {
		if (contexts == null) {
			return;
		}
		for (final Context context : contexts) {
			getDataAccessManager().get(URI.create("sics://hdb" + context.path),
					String.class, new IDataHandler<String>() {
						@Override
						public void handleData(URI uri, String data) {
							updateLabel(context, data);
						}

						@Override
						public void handleError(URI uri, Exception exception) {
						}
					});
		}
		if (eventHandler != null) {
			eventHandler.activate();
		}
	}

	protected void disableWidget() {
		if (contexts == null) {
			return;
		}
		if (eventHandler != null) {
			eventHandler.deactivate();
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				for (final Context context : contexts) {
					// Set label text
					StringBuilder builder = new StringBuilder();
					if (context.path.endsWith("secondary")) {
						builder.append("Secondary\n");
					} else if (context.path.endsWith("tertiary")) {
						builder.append("Sample\n");
					}
					builder.append("--");
					context.label.setText(builder.toString());
					context.label.setBackground(null);
					context.label.setForeground(context.originalForeground);
					context.isActivated = false;
				}
			}
		});
	}

	private void disposeWidget() {
		if (eventHandler != null) {
			eventHandler.deactivate();
			eventHandler = null;
		}
		if (contexts != null) {
			for (Context context : contexts) {
				if (context.handler != null) {
					context.handler.deactivate();
				}
			}
			contexts.clear();
			contexts = null;
		}
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	class Context {
		String path;
		Label label;
		EventHandler handler;
		Color originalForeground;
		boolean isActivated;
	}

	class HdbEventHandler extends EventHandler {
		Context context;

		public HdbEventHandler(Context context) {
			super(SicsStatusWidget.EVENT_TOPIC_HNOTIFY + context.path);
			this.context = context;
		}

		@Override
		public void handleEvent(final Event event) {
			// TODO: message overflow protection??
			updateLabel(context,
					event.getProperty(SicsStatusWidget.EVENT_PROP_VALUE).toString());
		}
	}

	private void updateLabel(final Context context, final String data) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				// Set label text
				StringBuilder builder = new StringBuilder();
				if (context.path.endsWith("secondary")) {
					builder.append("Secondary\n");
				} else if (context.path.endsWith("tertiary")) {
					builder.append("Sample\n");
				}
				builder.append(data);
				context.label.setText(builder.toString());
				// Set colour based on status
				if (data.equalsIgnoreCase("Opened")
						| data.equalsIgnoreCase("OPEN")) {
					context.label.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_GREEN));
					context.label.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_BLACK));
					context.isActivated = true;
				} else if (data.equalsIgnoreCase("Closed")
						| data.equalsIgnoreCase("CLOSE")) {
					context.label.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_RED));
					context.label.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_WHITE));
					context.isActivated = true;
				} else {
					context.label.setBackground(null);
					context.label.setForeground(context.originalForeground);
					context.isActivated = false;
				}
			}
		});
	}

	@Override
	public void afterParametersSet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void widgetDispose() {
		disposeWidget();
	}

}
