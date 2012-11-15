package org.gumtree.gumnix.sics.widgets.swt;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.events.SicsControllerEvent;
import org.gumtree.gumnix.sics.core.SicsEvents;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.service.eventbus.IFilteredEventHandler;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.messaging.DelayEventHandler;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.widgets.swt.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResources;
import org.osgi.service.event.Event;

@SuppressWarnings("restriction")
public class DeviceStatusWidget extends ExtendedSicsComposite {

	private IDataAccessManager dataAccessManager;

	private IDelayEventExecutor delayEventExecutor;

	private List<DeviceContext> deviceContexts;

	private Set<LabelContext> labelContexts;

	private IFilteredEventHandler<SicsControllerEvent> eventHandler;

	public DeviceStatusWidget(Composite parent, int style) {
		super(parent, style);
		if (parent instanceof PGroup) {
			((PGroup) parent).setExpanded(false);
		}
		deviceContexts = new ArrayList<DeviceStatusWidget.DeviceContext>();
		labelContexts = new HashSet<DeviceStatusWidget.LabelContext>();
	}

	@Override
	protected void handleRender() {
		GridLayoutFactory.swtDefaults().numColumns(5).spacing(1, 1)
				.applyTo(this);

		for (DeviceContext deviceContext : deviceContexts) {
			if (deviceContext.isSeparator) {
				// Draw separator
				Label separator = getWidgetFactory().createLabel(this, "",
						SWT.SEPARATOR | SWT.HORIZONTAL);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
						.grab(true, false).span(5, 1).applyTo(separator);
			} else {
				// Part 1: icon
				Label label = getWidgetFactory().createLabel(this, "");
				if (deviceContext.icon != null) {
					label.setImage(deviceContext.icon);
				}
				// Part 2: label
				label = getWidgetFactory().createLabel(this,
						deviceContext.label + ": ");
				label.setFont(UIResources.getDefaultFont(SWT.BOLD));
				// Part 3: Value
				label = createDeviceLabel(this, deviceContext.path, "--",
						SWT.RIGHT);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
						.grab(true, false).applyTo(label);
				label.setFont(UIResources.getDefaultFont(SWT.BOLD));
				// Part 4: Separator
//				String labelSep = (deviceContext.unit == null) ? "" : " ";
//				label = getWidgetFactory().createLabel(this, labelSep);
				label = getWidgetFactory().createLabel(this, "");
				// Part 5: Unit
				label = createDeviceLabel(this, deviceContext.path + "?units",
						(deviceContext.unit == null) ? "" : deviceContext.unit,
						SWT.LEFT);
			}
		}

		eventHandler = new IFilteredEventHandler<SicsControllerEvent>() {
			public void handleEvent(SicsControllerEvent event) {
				Class<?> representation = String.class;
				if ("status".equals(event.getURI().getQuery())) {
					representation = ControllerStatus.class;
				}
				updateData(event.getURI(), getLabelContext(event.getURI()).label,
						getDataAccessManager().get(event.getURI(), representation));
			}
			public boolean isDispatchable(SicsControllerEvent event) {
				return getDeviceContext(event.getURI()) != null;
			}
		};
		PlatformUtils.getPlatformEventBus().subscribe(eventHandler);

	}

	private DeviceContext getDeviceContext(URI uri) {
		for (DeviceContext context : deviceContexts){
			if (context.path.equals(uri.getPath())) {
				return context;
			}
		}
		return null;
	}
	
	private LabelContext getLabelContext(URI uri) {
		for (LabelContext context : labelContexts){
			if (context.path.equals(uri.getPath())) {
				return context;
			}
		}
		return null;
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
	
	protected void updateWidgetData(URI uri, Object widget, Object data) {
		if (data instanceof ControllerStatus && widget instanceof Control) {
			ControllerStatus status = (ControllerStatus) data;
			Control control = (Control) widget;
			if (status.equals(ControllerStatus.OK)) {
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			} else if (status.equals(ControllerStatus.RUNNING)) {
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			} else if (status.equals(ControllerStatus.ERROR)) {
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			}
		}
	}
	
	@Override
	protected void handleSicsConnect() {
		if (labelContexts == null) {
			return;
		}
		for (final LabelContext labelContext : labelContexts) {
			getDataAccessManager().get(
					URI.create("sics://hdb" + labelContext.path), String.class,
					new IDataHandler<String>() {
						@Override
						public void handleData(URI uri, String data) {
							updateLabelText(labelContext.label, data);
						}
						@Override
						public void handleError(URI uri, Exception exception) {
						}
					});
		}
	}

	@Override
	protected void handleSicsDisconnect() {
		if (labelContexts == null) {
			return;
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				for (final LabelContext labelContext : labelContexts) {
					labelContext.label.setText(labelContext.defaultText);
				}
				DeviceStatusWidget.this.layout(true, true);
			}
		});
	}

	@Override
	protected void disposeWidget() {
		if (deviceContexts != null) {
			deviceContexts.clear();
			deviceContexts = null;
		}
		if (labelContexts != null) {
			for (LabelContext context : labelContexts) {
				if (context.handler != null) {
					context.handler.deactivate();
				}
			}
			labelContexts.clear();
			labelContexts = null;
		}
		dataAccessManager = null;
		delayEventExecutor = null;
		super.disposeWidget();
	}

	/*************************************************************************
	 * Public API
	 *************************************************************************/

	public DeviceStatusWidget addDevice(String path, String label) {
		return addDevice(path, label, null, null);
	}

	public DeviceStatusWidget addDevice(String path, String label, Image icon) {
		return addDevice(path, label, icon, null);
	}
	
	public DeviceStatusWidget addDevice(String path, String label, Image icon,
			String unit) {
		DeviceContext context = new DeviceContext();
		context.path = path;
		context.icon = icon;
		context.label = label;
		context.unit = unit;
		context.isSeparator = false;
		deviceContexts.add(context);
		return this;
	}

	public DeviceStatusWidget addSeparator() {
		DeviceContext context = new DeviceContext();
		context.isSeparator = true;
		deviceContexts.add(context);
		return this;
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

	private class DeviceContext {
		String path;
		Image icon;
		String label;
		String unit;
		boolean isSeparator;
	}

	private class LabelContext {
		String path;
		Label label;
		String defaultText;
		EventHandler handler;
	}

	private class HdbEventHandler extends DelayEventHandler {
		Label label;

		public HdbEventHandler(String path, Label label,
				IDelayEventExecutor delayEventExecutor) {
			super(SicsEvents.HNotify.TOPIC_HNOTIFY + path, delayEventExecutor);
			this.label = label;
		}

		@Override
		public void handleDelayEvent(Event event) {
			updateLabelText(label,
					event.getProperty(SicsEvents.HNotify.VALUE).toString());
		}
	}

	private Label createDeviceLabel(Composite parent, String path,
			String defaultText, int style) {
		Label label = getWidgetFactory()
				.createLabel(parent, defaultText, style);
		LabelContext context = new LabelContext();
		context.path = path;
		context.label = label;
		context.defaultText = defaultText;
		context.handler = new HdbEventHandler(path, label,
				getDelayEventExecutor()).activate();
		labelContexts.add(context);
		return label;
	}

	private void updateLabelText(final Label label, final String data) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				if (label != null && !label.isDisposed()) {
					Composite parent = getParent();
					if (parent instanceof PGroup) {
						if (!((PGroup) parent).getExpanded()) {
							((PGroup) parent).setExpanded(true);
						}
					}
					String text = data;
					try {
						double value = Double.valueOf(data);
						if (data.contains(".")) {
							text = String.format("%.2f", value);
						} 
					} catch (Exception e) {
					} 
					label.setText(text);
					// TODO: does it have any performance hit?
					label.getParent().layout(true, true);
				}
			}
		});
	}

}
