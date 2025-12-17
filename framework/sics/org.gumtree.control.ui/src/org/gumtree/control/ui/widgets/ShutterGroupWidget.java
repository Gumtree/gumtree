package org.gumtree.control.ui.widgets;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.widgets.swt.util.SafeUIRunner;

@SuppressWarnings("restriction")
public class ShutterGroupWidget extends ExtendedWidgetComposite {

	private static final String PROP_PATH_SECONDARY_SHUTTER = "gumtree.sics.path.secondaryshutter";
	private static final String PROP_PATH_TERTIARY_SHUTTER = "gumtree.sics.path.tertiaryshutter";
	
	private IDataAccessManager dataAccessManager;

	private IDelayEventExecutor delayEventExecutor;

	private Set<Context> contexts;

//	private ISicsProxyListener proxyListener;

	public ShutterGroupWidget(Composite parent, int style) {
		super(parent, style);
		contexts = new HashSet<Context>();
	}

	@Override
	protected void handleRender() {
		setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.swtDefaults().applyTo(this);

		Label label = getWidgetFactory().createLabel(this, "Secondary --",
				SWT.CENTER | SWT.WRAP | SWT.BORDER);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(label);
		Context context = new Context();
		context.path = System.getProperty(PROP_PATH_SECONDARY_SHUTTER);
		context.label = label;
		context.originalForeground = label.getForeground();
		contexts.add(context);

		label = getWidgetFactory().createLabel(this, "Tertiary --",
				SWT.CENTER | SWT.WRAP | SWT.BORDER);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(label);
		context = new Context();
		context.path = System.getProperty(PROP_PATH_TERTIARY_SHUTTER);
		context.label = label;
		context.originalForeground = label.getForeground();
		contexts.add(context);

		// TODO: test this code
		// [GUMTREE-141] Blinking
//		blindEventHandler = new EventHandler(IDirectoryService.EVENT_TOPIC_BIND) {
//			@Override
//			public void handleEvent(Event event) {
//				// TODO: use topic to filter
//				if (event.getProperty(IDirectoryService.EVENT_PROP_NAME)
//						.equals("shutterStatusCheckFailed")) {
//					if ((Boolean) event
//							.getProperty(IDirectoryService.EVENT_PROP_OBJECT)) {
//						final int[] counter = new int[] { 0 };
//						final Map<Label, Color> originalColourMap = new HashMap<Label, Color>();
//						Job job = new Job("") {
//							protected IStatus run(IProgressMonitor monitor) {
//								// Over 5 times ... exit
//								if (isDisposed() || counter[0] > 5) {
//									return Status.OK_STATUS;
//								}
//								SafeUIRunner.asyncExec(new SafeRunnable() {
//									public void run() throws Exception {
//										for (Context context : contexts) {
//											if (!context.isActivated) {
//												continue;
//											}
//											// Initialise and finalise
//											if (counter[0] == 0) {
//												originalColourMap
//														.put(context.label,
//																context.label
//																		.getBackground());
//											} else if (counter[0] >= 5) {
//												context.label
//														.setBackground(originalColourMap
//																.get(context.label));
//											}
//											// Blinking
//											if (counter[0] % 2 == 0) {
//												context.label
//														.setBackground(null);
//											} else {
//												context.label
//														.setBackground(originalColourMap
//																.get(context.label));
//											}
//										}
//										counter[0]++;
//									}
//								});
//								// Repeat every 0.5 sec
//								schedule(500);
//								return Status.OK_STATUS;
//							}
//						};
//						// Run now
//						job.schedule();
//					}
//				}
//
//			}
//		};
		
	}

	@Override
	protected void handleSicsConnect() {
	}

	@Override
	protected void handleModelUpdate() {
		if (contexts == null) {
			return;
		}
//		checkSicsConnection();
		try {
			for (final Context context : contexts) {
				initiateLabel(context);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void handleStatusUpdate(ServerStatus status) {
	}
	
	private void initiateLabel(Context context) {
		ISicsController controller = SicsManager.getSicsModel().findControllerByPath(context.path);
		if (controller != null && controller instanceof IDynamicController) {
			String data;
			try {
				data = String.valueOf(((IDynamicController) controller).getValue());
				updateLabel(context, data);
			} catch (SicsModelException e) {
			}
			createListener(context);
		}
	}

	@Override
	protected void handleSicsDisconnect() {
		if (contexts == null) {
			return;
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				for (final Context context : contexts) {
					// Set label text
					StringBuilder builder = new StringBuilder();
					if (context.path.contains("secondary")) {
						builder.append("Secondary -- ");
					} else if (context.path.contains("tertiary")) {
						builder.append("Tertiary -- ");
					}
//					builder.append("--");
					context.label.setText(builder.toString());
					context.label.setBackground(null);
					context.label.setForeground(context.originalForeground);
					context.isActivated = false;
				}
			}
		});
	}
	
	private void createListener(Context context) {
		ISicsControllerListener listener = new SicsControllerListener(context);
		ISicsController controller = SicsManager.getSicsModel().findControllerByPath(context.path);
		if (controller != null) {
			controller.addControllerListener(listener);
		}
		context.listener = listener;
	}
	
	private void deactiveListener(Context context) {
		if (context.listener != null) {
			ISicsController controller = SicsManager.getSicsModel().findControllerByPath(context.path);
			if (controller != null) {
				controller.removeControllerListener(context.listener);
			}
			context.listener = null;
		}
	}
	
	class SicsControllerListener implements ISicsControllerListener {
		
		Context context;
		
		public SicsControllerListener(Context context) {
			this.context = context;
		}
		
		@Override
		public void updateValue(Object oldValue, Object newValue) {
			updateLabel(context, String.valueOf(newValue));
		}
		
		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
		}
		
		@Override
		public void updateEnabled(boolean isEnabled) {
		}
	}
	
	@Override
	protected void disposeWidget() {
		if (contexts != null) {
			for (Context context : contexts) {
				if (context.listener != null) {
					deactiveListener(context);
				}
			}
			contexts.clear();
			contexts = null;
		}
		dataAccessManager = null;
		delayEventExecutor = null;
		super.disposeWidget();
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
		ISicsControllerListener listener;
		Color originalForeground;
		boolean isActivated;
	}

	private void updateLabel(final Context context, final String data) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				// Set label text
				StringBuilder builder = new StringBuilder();
				if (context.path.contains("secondary")) {
					builder.append("Secondary - ");
				} else if (context.path.contains("tertiary")) {
					builder.append("Tertiary - ");
				}
				String status;
				// Set colour based on status
				if (data.equalsIgnoreCase("Opened")
						| data.equalsIgnoreCase("OPEN")
						| data.equalsIgnoreCase("1")) {
					context.label.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_GREEN));
					context.label.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_BLACK));
					status = "OPEN";
					context.isActivated = true;
				} else if (data.equalsIgnoreCase("Closed")
						| data.equalsIgnoreCase("CLOSE")
						| data.equalsIgnoreCase("0")) {
					context.label.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_RED));
					context.label.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_WHITE));
					status = "CLOSED";
					context.isActivated = true;
				} else {
					context.label.setBackground(null);
					context.label.setForeground(context.originalForeground);
					status = "UNKNOWN";
					context.isActivated = false;
				}
				builder.append(status);
				context.label.setText(builder.toString());
			}
		});
	}

}
