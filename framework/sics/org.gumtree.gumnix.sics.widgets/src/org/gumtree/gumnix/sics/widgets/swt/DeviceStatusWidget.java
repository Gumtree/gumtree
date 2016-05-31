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
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.events.SicsControllerEvent;
import org.gumtree.gumnix.sics.core.SicsCore;
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

	private static final int SICS_CONNECTION_TIMEOUT = 5000;
	
	private IDataAccessManager dataAccessManager;

	private IDelayEventExecutor delayEventExecutor;

	private List<DeviceContext> deviceContexts;

	private Set<LabelContext> labelContexts;

	private IFilteredEventHandler<SicsControllerEvent> eventHandler;
	
	private boolean isExpandingEnabled = true;

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
		GridLayoutFactory.swtDefaults().numColumns(8).spacing(1, 1)
				.applyTo(this);

		for (DeviceContext deviceContext : deviceContexts) {
			if (deviceContext.isSeparator) {
				// Draw separator
				Label separator = getWidgetFactory().createLabel(this, "",
						SWT.SEPARATOR | SWT.HORIZONTAL);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
						.grab(true, false).span(8, 1).applyTo(separator);
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
						SWT.RIGHT, deviceContext.converter, deviceContext.showSoftLimits);
//				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
//						.grab(true, false).applyTo(label);
				label.setFont(UIResources.getDefaultFont(SWT.BOLD));
				// Part 4: Separator
//				String labelSep = (deviceContext.unit == null) ? "" : " ";
//				label = getWidgetFactory().createLabel(this, labelSep);
				label = getWidgetFactory().createLabel(this, "");
				// Part 5: Unit
				label = createUnitsLabel(deviceContext);
//				label = createDeviceLabel(this, deviceContext.path + "?units",
//						(deviceContext.unit == null) ? "" : deviceContext.unit,
//						SWT.LEFT, UnitsConverter.getInstance());
//				label = getWidgetFactory().createLabel(this, 
//						(deviceContext.unit == null) ? "" : deviceContext.unit);
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

	private Label createUnitsLabel(DeviceContext deviceContext) {
		if (deviceContext.unit != null) {
			return getWidgetFactory().createLabel(this, deviceContext.unit, SWT.LEFT);
		} else {
			return createUnitsLabel(this, deviceContext.path + "?units", "", SWT.LEFT);
		}
	}

	private DeviceContext getDeviceContext(URI uri) {
		if (deviceContexts == null) {
			return null;
		}
		for (DeviceContext context : deviceContexts){
			if (!context.isSeparator && context.path.equals(uri.getPath())) {
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
			if (context.showSoftLimits) {
				if ((context.path + "/softlowerlim").equals(uri.getPath())){
					return context;
				}
				if ((context.path + "/softupperlim").equals(uri.getPath())){
					return context;
				}
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
		checkSicsConnection();
		try {
			for (final LabelContext labelContext : labelContexts) {
				getDataAccessManager().get(
						URI.create("sics://hdb" + labelContext.path), String.class,
						new IDataHandler<String>() {
							@Override
							public void handleData(URI uri, String data) {
								updateLabelText(labelContext.label, data, labelContext.converter);
							}
							@Override
							public void handleError(URI uri, Exception exception) {
								exception.printStackTrace();
							}
						});
				if (labelContext.showSoftLimits) {
					getDataAccessManager().get(
							URI.create("sics://hdb" + labelContext.path + "/softlowerlim"), String.class,
							new IDataHandler<String>() {
								@Override
								public void handleData(URI uri, String data) {
									updateLabelText(labelContext.lowerlimLabel, " (" + data + ",", null);
								}
								@Override
								public void handleError(URI uri, Exception exception) {
									exception.printStackTrace();
								}
							});
					getDataAccessManager().get(
							URI.create("sics://hdb" + labelContext.path + "/softupperlim"), String.class,
							new IDataHandler<String>() {
								@Override
								public void handleData(URI uri, String data) {
									updateLabelText(labelContext.upperlimLabel, data + ")", null);
								}
								@Override
								public void handleError(URI uri, Exception exception) {
									exception.printStackTrace();
								}
							});
					getDataAccessManager().get(
							URI.create("sics://hdb" + labelContext.path + "/softzero"), String.class,
							new IDataHandler<String>() {
								@Override
								public void handleData(URI uri, String data) {
									updateLabelText(labelContext.softzeroLabel, "(" + data + ")", null);
								}
								@Override
								public void handleError(URI uri, Exception exception) {
									exception.printStackTrace();
								}
							});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				if (context.lowerlimHandler != null) {
					context.lowerlimHandler.deactivate();
				}
				if (context.upperlimHandler != null) {
					context.upperlimHandler.deactivate();
				}
				if (context.softzeroHandler != null) {
					context.softzeroHandler.deactivate();
				}
			}
			labelContexts.clear();
			labelContexts = null;
		}
		if (eventHandler != null) {
			PlatformUtils.getPlatformEventBus().unsubscribe(eventHandler);
			eventHandler = null;
		}
		dataAccessManager = null;
		delayEventExecutor = null;
		super.disposeWidget();
	}

	/*************************************************************************
	 * Public API
	 *************************************************************************/

	public DeviceStatusWidget addDevice(String path, String label) {
		return addDevice(path, label, null, null, null);
	}

	public DeviceStatusWidget addDevice(String path, String label, Image icon) {
		return addDevice(path, label, icon, null, null);
	}
	
	public DeviceStatusWidget addDevice(String path, String label, Image icon,
			String unit) {
		return addDevice(path, label, icon, unit, null);
	}
	
	public DeviceStatusWidget addDevice(String path, String label, Image icon,
			String unit, LabelConverter converter) {
		return addDevice(path, label, icon, unit, null, false);
	}
	
	public DeviceStatusWidget addDevice(String path, String label, Image icon,
			String unit, LabelConverter converter, boolean showSoftLimits) {
		DeviceContext context = new DeviceContext();
		context.path = path;
		context.icon = icon;
		context.label = label;
		context.unit = unit;
		context.isSeparator = false;
		context.converter = converter;
		context.showSoftLimits = showSoftLimits;
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
		LabelConverter converter;
		boolean showSoftLimits;
	}

	private class LabelContext {
		String path;
		Label label;
		String defaultText;
		EventHandler handler;
		LabelConverter converter;
		boolean showSoftLimits;
		Label lowerlimLabel;
		EventHandler lowerlimHandler;
		Label upperlimLabel;
		EventHandler upperlimHandler;
		Label softzeroLabel;
		EventHandler softzeroHandler;
	}

	public interface LabelConverter {
		String convertValue(Object obj);
	}

	static class UnitsConverter implements LabelConverter{

		static UnitsConverter converter;
		
		@Override
		public String convertValue(Object obj) {
			if ("Angstrom".equalsIgnoreCase(String.valueOf(obj))){
				return "\u212B";
			} else if ("degree".equalsIgnoreCase(String.valueOf(obj))){
				return "deg";
			} else if ("degrees".equalsIgnoreCase(String.valueOf(obj))){
				return "deg";
			} else if ("count".equalsIgnoreCase(String.valueOf(obj))){
				return "";
			} else if ("seconds".equalsIgnoreCase(String.valueOf(obj))){
				return "s";
			} else if (obj == null) {
				return null;
			} else {
				return String.valueOf(obj);
			}
		}
		
		public static UnitsConverter getInstance() {
			if (converter == null) {
				converter = new UnitsConverter();
			}
			return converter;
		}
	}
	
	public static class PrecisionConverter implements LabelConverter {

		private int precision;
		
		public PrecisionConverter(int precision) {
			this.precision = precision;
		}
		
		@Override
		public String convertValue(Object obj) {
			String text = obj.toString();
			if (precision >= 0) {
				try {
					double value = Double.valueOf(text);
					if (text.contains(".")) {
						text = String.format("%." + precision + "f", value);
					} 
				} catch (Exception e) {
				} 
			} 
			return text;
		}

	}
	
	private class HdbEventHandler extends DelayEventHandler {
		Label label;
		LabelConverter converter;

		public HdbEventHandler(String path, Label label,
				IDelayEventExecutor delayEventExecutor) {
			super(SicsEvents.HNotify.TOPIC_HNOTIFY + path, delayEventExecutor);
			this.label = label;
		}

		public void setLabelConverter(LabelConverter converter) {
			this.converter = converter;
		}
		
		@Override
		public void handleDelayEvent(Event event) {
			updateLabelText(label,
					event.getProperty(SicsEvents.HNotify.VALUE).toString(), converter);
		}
	}
	
	private class LowerlimHdbEventHandler extends DelayEventHandler {
		Label label;
		LabelConverter converter;

		public LowerlimHdbEventHandler(String path, Label label,
				IDelayEventExecutor delayEventExecutor) {
			super(SicsEvents.HNotify.TOPIC_HNOTIFY + path, delayEventExecutor);
			this.label = label;
		}

		public void setLabelConverter(LabelConverter converter) {
			this.converter = converter;
		}
		
		@Override
		public void handleDelayEvent(Event event) {
			updateLabelText(label,
					" (" + event.getProperty(SicsEvents.HNotify.VALUE).toString() + ",", converter);
		}
	}

	private class UpperlimHdbEventHandler extends DelayEventHandler {
		Label label;
		LabelConverter converter;

		public UpperlimHdbEventHandler(String path, Label label,
				IDelayEventExecutor delayEventExecutor) {
			super(SicsEvents.HNotify.TOPIC_HNOTIFY + path, delayEventExecutor);
			this.label = label;
		}

		public void setLabelConverter(LabelConverter converter) {
			this.converter = converter;
		}
		
		@Override
		public void handleDelayEvent(Event event) {
			updateLabelText(label,
					event.getProperty(SicsEvents.HNotify.VALUE).toString() + ")", converter);
		}
	}
	
	private class SoftzeroHdbEventHandler extends DelayEventHandler {
		Label label;
		LabelConverter converter;

		public SoftzeroHdbEventHandler(String path, Label label,
				IDelayEventExecutor delayEventExecutor) {
			super(SicsEvents.HNotify.TOPIC_HNOTIFY + path, delayEventExecutor);
			this.label = label;
		}

		public void setLabelConverter(LabelConverter converter) {
			this.converter = converter;
		}
		
		@Override
		public void handleDelayEvent(Event event) {
			updateLabelText(label,
					"(" + event.getProperty(SicsEvents.HNotify.VALUE).toString() + ")", converter);
		}
	}
	
	private Label createUnitsLabel(Composite parent, String path,
			String defaultText, int style) {
		Label label = getWidgetFactory()
				.createLabel(parent, defaultText, style);
		LabelContext context = new LabelContext();
		context.path = path;
		context.label = label;
		context.showSoftLimits = false;
		context.defaultText = defaultText;
		HdbEventHandler handler = new HdbEventHandler(path, label,
				getDelayEventExecutor());
		handler.activate();
		context.handler = handler;
		labelContexts.add(context);
		return label;
	}

	private Label createDeviceLabel(Composite parent, String path,
			String defaultText, int style, LabelConverter converter, boolean showSoftLimits) {
		Label label = getWidgetFactory()
				.createLabel(parent, defaultText, style);
		LabelContext context = new LabelContext();
		context.path = path;
		context.label = label;
		context.showSoftLimits = showSoftLimits;
		if (showSoftLimits) {
			Label lowerlimLabel = getWidgetFactory().createLabel(parent, "");
			context.lowerlimLabel = lowerlimLabel;
			context.lowerlimHandler = new LowerlimHdbEventHandler(path + "/softlowerlim", lowerlimLabel,
					getDelayEventExecutor());
			context.lowerlimHandler.activate();
			Label upperlimLabel = getWidgetFactory().createLabel(parent, "");
			context.upperlimLabel = upperlimLabel;
			context.upperlimHandler = new UpperlimHdbEventHandler(path + "/softupperlim", upperlimLabel,
					getDelayEventExecutor());
			context.upperlimHandler.activate();
			Label softzeroLabel = getWidgetFactory().createLabel(parent, "");
			context.softzeroLabel = softzeroLabel;
			context.softzeroHandler = new SoftzeroHdbEventHandler(path + "/softzero", softzeroLabel,
					getDelayEventExecutor());
			context.softzeroHandler.activate();
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(context.label);
		} else {
			GridDataFactory.swtDefaults().span(4, 1).align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(context.label);
		}
		context.defaultText = defaultText;
		HdbEventHandler handler = new HdbEventHandler(path, label,
				getDelayEventExecutor());
		handler.setLabelConverter(converter);
		handler.activate();
		context.handler = handler;
		context.converter = converter;
		labelContexts.add(context);
		return label;
	}

	private void updateLabelText(final Label label, final String data, final LabelConverter converter) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				if (label != null && !label.isDisposed()) {
					Composite parent = getParent();
					if (parent instanceof PGroup) {
						if (isExpandingEnabled() && !((PGroup) parent).getExpanded()) {
							((PGroup) parent).setExpanded(true);
						}
					}
					String text = data;
					if (converter == null) {
						try {
							double value = Double.valueOf(data);
							if (data.contains(".")) {
								text = String.format("%.2f", value);
							} 
						} catch (Exception e) {
						} 
					} else {
						text = converter.convertValue(data);
					}
					label.setText(text);
					// TODO: does it have any performance hit?
					label.getParent().layout(true, true);
				}
			}
		});
	}

	/**
	 * @return the isExpandingEnabled
	 */
	public boolean isExpandingEnabled() {
		return isExpandingEnabled;
	}

	/**
	 * @param isExpandingEnabled the isExpandingEnabled to set
	 */
	public void setExpandingEnabled(boolean isExpandingEnabled) {
		this.isExpandingEnabled = isExpandingEnabled;
	}

	protected void checkSicsConnection() {
		int counter = 0;
		IComponentController[] controllers = SicsCore.getSicsController().getComponentControllers();
		if (counter <= SICS_CONNECTION_TIMEOUT && (controllers == null || controllers.length == 0)) {
			try {
				Thread.sleep(500);
				counter += 500;
			} catch (InterruptedException e) {
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}
}
