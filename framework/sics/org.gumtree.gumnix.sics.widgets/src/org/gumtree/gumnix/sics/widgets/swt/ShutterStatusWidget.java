package org.gumtree.gumnix.sics.widgets.swt;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.core.SicsEvents;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.util.messaging.DelayEventHandler;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.widgets.swt.util.SafeUIRunner;
import org.osgi.service.event.Event;

@SuppressWarnings("restriction")
public class ShutterStatusWidget extends ExtendedSicsComposite {

	private IDataAccessManager dataAccessManager;

	private IDelayEventExecutor delayEventExecutor;

	private EventHandler blindEventHandler;

	private Set<Context> contexts;

	public ShutterStatusWidget(Composite parent, int style) {
		super(parent, style);
		contexts = new HashSet<Context>();
	}

	@Override
	protected void handleRender() {
		setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.swtDefaults().applyTo(this);

		Label label = getWidgetFactory().createLabel(this, "Secondary\n--",
				SWT.CENTER | SWT.WRAP | SWT.BORDER);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(label);
		Context context = new Context();
		context.path = "/instrument/status/secondary";
		context.label = label;
		context.originalForeground = label.getForeground();
		context.handler = new HdbEventHandler(context, getDelayEventExecutor())
				.activate();
		contexts.add(context);

		label = getWidgetFactory().createLabel(this, "Sample\n--",
				SWT.CENTER | SWT.WRAP | SWT.BORDER);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(label);
		context = new Context();
		context.path = "/instrument/status/tertiary";
		context.label = label;
		context.originalForeground = label.getForeground();
		context.handler = new HdbEventHandler(context, getDelayEventExecutor())
				.activate();
		contexts.add(context);

		// TODO: test this code
		// [GUMTREE-141] Blinking
		blindEventHandler = new EventHandler(IDirectoryService.EVENT_TOPIC_BIND) {
			@Override
			public void handleEvent(Event event) {
				// TODO: use topic to filter
				if (event.getProperty(IDirectoryService.EVENT_PROP_NAME)
						.equals("shutterStatusCheckFailed")) {
					if ((Boolean) event
							.getProperty(IDirectoryService.EVENT_PROP_OBJECT)) {
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
												originalColourMap
														.put(context.label,
																context.label
																		.getBackground());
											} else if (counter[0] >= 5) {
												context.label
														.setBackground(originalColourMap
																.get(context.label));
											}
											// Blinking
											if (counter[0] % 2 == 0) {
												context.label
														.setBackground(null);
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

	@Override
	protected void handleSicsConnect() {
		if (contexts == null) {
			return;
		}
		checkSicsConnection();
		try {
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
			if (blindEventHandler != null) {
				blindEventHandler.activate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void handleSicsDisconnect() {
		if (contexts == null) {
			return;
		}
		if (blindEventHandler != null) {
			blindEventHandler.deactivate();
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

	@Override
	protected void disposeWidget() {
		if (blindEventHandler != null) {
			blindEventHandler.deactivate();
			blindEventHandler = null;
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
		dataAccessManager = null;
		delayEventExecutor = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IDataAccessManager getDataAccessManager() {
		return dataAccessManager;
	}

	@Inject
	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		this.dataAccessManager = dataAccessManager;
	}

	public IDelayEventExecutor getDelayEventExecutor() {
		return delayEventExecutor;
	}

	@Inject
	@Optional
	public void setDelayEventExecutor(IDelayEventExecutor delayEventExecutor) {
		this.delayEventExecutor = delayEventExecutor;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	private class Context {
		String path;
		Label label;
		EventHandler handler;
		Color originalForeground;
		boolean isActivated;
	}

	private class HdbEventHandler extends DelayEventHandler {
		Context context;

		public HdbEventHandler(Context context,
				IDelayEventExecutor delayEventExecutor) {
			super(SicsEvents.HNotify.TOPIC_HNOTIFY + context.path,
					delayEventExecutor);
			this.context = context;
		}

		@Override
		public void handleDelayEvent(Event event) {
			updateLabel(context, event.getProperty(SicsEvents.HNotify.VALUE)
					.toString());
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

}
