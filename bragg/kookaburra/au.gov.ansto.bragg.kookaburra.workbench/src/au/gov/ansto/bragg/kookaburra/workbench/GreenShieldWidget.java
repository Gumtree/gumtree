package au.gov.ansto.bragg.kookaburra.workbench;

import java.net.URI;

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
import org.gumtree.gumnix.sics.core.SicsEvents;
import org.gumtree.gumnix.sics.widgets.swt.ExtendedSicsComposite;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.util.messaging.DelayEventHandler;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.widgets.swt.util.SafeUIRunner;
import org.osgi.service.event.Event;

@SuppressWarnings("restriction")
public class GreenShieldWidget extends ExtendedSicsComposite {

	private final static String DEVICE_TITLE = "Green Polyshield - ";
	
	private IDataAccessManager dataAccessManager;

	private IDelayEventExecutor delayEventExecutor;

	private EventHandler blindEventHandler;

	private Context context;

	public GreenShieldWidget(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void handleRender() {
		setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.swtDefaults().applyTo(this);

		Label label = getWidgetFactory().createLabel(this, DEVICE_TITLE,
				SWT.CENTER | SWT.WRAP | SWT.BORDER);
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(label);
		context = new Context();
		context.path = "/instrument/GreenPolyShield/greenpolyshield";
		context.label = label;
		context.originalForeground = label.getForeground();
		context.handler = new HdbEventHandler(context, getDelayEventExecutor())
				.activate();

	}

	@Override
	protected void handleSicsConnect() {
		if (context == null) {
			return;
		}
		checkSicsConnection();
		try {
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
			if (blindEventHandler != null) {
				blindEventHandler.activate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void handleSicsDisconnect() {
		if (context == null) {
			return;
		}
		if (blindEventHandler != null) {
			blindEventHandler.deactivate();
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
					// Set label text
					context.label.setText(DEVICE_TITLE);
					context.label.setBackground(null);
					context.label.setForeground(context.originalForeground);
					context.isActivated = false;
			}
		});
	}

	@Override
	protected void disposeWidget() {
		if (blindEventHandler != null) {
			blindEventHandler.deactivate();
			blindEventHandler = null;
		}
		if (context != null) {
			if (context.handler != null) {
				context.handler.deactivate();
			}
			context = null;
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
				String text = DEVICE_TITLE + data.toUpperCase();
				context.label.setText(text);
				// Set colour based on status
				if (data.equalsIgnoreCase("in")) {
					context.label.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_GREEN));
					context.label.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_BLACK));
					context.isActivated = true;
				} else if (data.equalsIgnoreCase("out")) {
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
