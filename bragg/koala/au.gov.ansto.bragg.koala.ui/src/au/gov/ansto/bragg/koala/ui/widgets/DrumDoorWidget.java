package au.gov.ansto.bragg.koala.ui.widgets;

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
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.ui.widgets.ExtendedWidgetComposite;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.widgets.swt.util.SafeUIRunner;

@SuppressWarnings("restriction")
public class DrumDoorWidget extends ExtendedWidgetComposite {

	private static final String PATH_DOOR_LEFT = "gumtree.path.drumDoorLeft";
	private static final String PATH_DOOR_RIGHT = "gumtree.path.drumDoorRight";
	
	private static final String TEXT_LABEL = "Drum Door\n";
	
	private IDataAccessManager dataAccessManager;

	private IDelayEventExecutor delayEventExecutor;

	private Set<Context> contexts;

	private ISicsProxyListener proxyListener;

	private float dataLeft = -1;
	private float dataRight = -1;
	
	public DrumDoorWidget(Composite parent, int style) {
		super(parent, style);
		contexts = new HashSet<Context>();
	}

	@Override
	protected void handleRender() {
		setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.swtDefaults().applyTo(this);

		Label label = getWidgetFactory().createLabel(this, TEXT_LABEL + "--",
				SWT.CENTER | SWT.WRAP | SWT.BORDER);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(label);
		Context context = new Context();
		context.pathLeft = System.getProperty(PATH_DOOR_LEFT);
		context.pathRight = System.getProperty(PATH_DOOR_RIGHT);
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
		
		proxyListener = new SicsProxyListenerAdapter() {
			
			@Override
			public void disconnect() {
				handleSicsDisconnect();
			}
			
			@Override
			public void connect() {
				handleSicsConnect();
			}

		};
		ISicsProxy proxy = SicsManager.getSicsProxy();
		proxy.addProxyListener(proxyListener);
		
		if (proxy.isConnected()) {
			handleSicsConnect();
		}
	}

	@Override
	protected void handleSicsConnect() {
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

	private void initiateLabel(Context context) {
		ISicsController controllerLeft = SicsManager.getSicsModel().findControllerByPath(context.pathLeft);
		ISicsController controllerRight = SicsManager.getSicsModel().findControllerByPath(context.pathRight);
		if (controllerLeft != null && controllerLeft instanceof IDynamicController &&
				controllerRight != null && controllerRight instanceof IDynamicController ) {
			try {
				dataLeft = Float.valueOf(String.valueOf(((IDynamicController) controllerLeft).getValue()));
				dataRight = Float.valueOf(String.valueOf(((IDynamicController) controllerRight).getValue()));
				context.isActivated = true;
				updateLabel(context);
			} catch (SicsModelException e) {
				e.printStackTrace();
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
					context.label.setText(TEXT_LABEL + "--");
					context.label.setBackground(null);
					context.label.setForeground(context.originalForeground);
					context.isActivated = false;
				}
			}
		});
	}
	
	private void createListener(Context context) {
		ISicsControllerListener listenerLeft = new SicsControllerListener(context, true);
		ISicsController controllerLeft = SicsManager.getSicsModel().findControllerByPath(context.pathLeft);
		if (controllerLeft != null) {
			controllerLeft.addControllerListener(listenerLeft);
		}
		ISicsControllerListener listenerRight = new SicsControllerListener(context, false);
		ISicsController controllerRight = SicsManager.getSicsModel().findControllerByPath(context.pathRight);
		if (controllerRight != null) {
			controllerRight.addControllerListener(listenerRight);
		}
		context.listenerLeft = listenerLeft;
		context.listenerRight = listenerRight;
	}
	
	private void deactiveListener(Context context) {
		if (context.listenerLeft != null) {
			ISicsController controllerLeft = SicsManager.getSicsModel().findControllerByPath(context.pathLeft);
			if (controllerLeft != null) {
				controllerLeft.removeControllerListener(context.listenerLeft);
			}
			context.listenerLeft = null;
			ISicsController controllerRight = SicsManager.getSicsModel().findControllerByPath(context.pathRight);
			if (controllerRight != null) {
				controllerRight.removeControllerListener(context.listenerRight);
			}
			context.listenerLeft = null;
			context.listenerRight = null;
		}
	}
	
	class SicsControllerListener implements ISicsControllerListener {
		
		Context context;
		boolean isLeft;
		
		public SicsControllerListener(Context context, boolean isLeft) {
			this.context = context;
			this.isLeft = isLeft;
		}
		
		@Override
		public void updateValue(Object oldValue, Object newValue) {
			if (isLeft) {
				dataLeft = Float.valueOf(String.valueOf(newValue));
			} else {
				dataRight = Float.valueOf(String.valueOf(newValue));
			}
			updateLabel(context);
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
		if (proxyListener != null) {
			SicsManager.getSicsProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		if (contexts != null) {
			for (Context context : contexts) {
				if (context.listenerLeft != null || context.listenerRight != null) {
					deactiveListener(context);
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
		String pathLeft;
		String pathRight;
		Label label;
		ISicsControllerListener listenerLeft;
		ISicsControllerListener listenerRight;
		Color originalForeground;
		boolean isActivated;
	}

	private void updateLabel(final Context context) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				
				if ((dataLeft == 0 && dataRight >=0) || (dataRight == 0 && dataLeft >= 0)) {
					context.label.setText(TEXT_LABEL + "OPEN");
					context.label.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_RED));
					context.label.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_WHITE));
					context.isActivated = true;
				} else if (dataLeft == 1 && dataRight == 1) {
					context.label.setText(TEXT_LABEL + "CLOSED");
					context.label.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_GREEN));
					context.label.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_BLACK));
					context.isActivated = true;					
				} else {
					context.label.setText(TEXT_LABEL + "--");
					context.label.setBackground(null);
					context.label.setForeground(context.originalForeground);
					context.isActivated = false;
				}				
			}
		});
	}

}
