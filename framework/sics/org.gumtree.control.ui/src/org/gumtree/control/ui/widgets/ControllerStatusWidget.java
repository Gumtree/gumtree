package org.gumtree.control.ui.widgets;

import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
import org.gumtree.widgets.swt.util.UIResources;

@SuppressWarnings("restriction")
public class ControllerStatusWidget extends ExtendedWidgetComposite {

//	private static final int SICS_CONNECTION_TIMEOUT = 5000;
	
	private IDataAccessManager dataAccessManager;

	private IDelayEventExecutor delayEventExecutor;

	private List<DeviceContext> deviceContexts;

	private Set<LabelContext> labelContexts;

//	private IFilteredEventHandler<SicsControllerEvent> eventHandler;
//	private ISicsProxyListener proxyListener;
	
	private boolean isExpandingEnabled = true;

	public ControllerStatusWidget(Composite parent, int style) {
		super(parent, style);
		if (parent instanceof PGroup) {
			((PGroup) parent).setExpanded(false);
		}
		deviceContexts = new ArrayList<ControllerStatusWidget.DeviceContext>();
		labelContexts = new HashSet<ControllerStatusWidget.LabelContext>();
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
				deviceContext.labelWidget = label;
				label.setFont(UIResources.getDefaultFont(SWT.BOLD));
				// Part 3: Value
				label = createDeviceLabel(this, deviceContext.path, "--",
						SWT.RIGHT, deviceContext.converter, deviceContext.showSoftLimits, deviceContext.colorConverter);
				label.setToolTipText("--");
//				GridDataFactory.swtDefaults().hint(120, SWT.DEFAULT).applyTo(label);
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

//		proxyListener = new SicsProxyListenerAdapter() {
//			
//			@Override
//			public void modelUpdated() {
//				handleModelUpdate();
//			}
//			
//			@Override
//			public void disconnect() {
//				handleSicsDisconnect();
//			}
//
//		};
//		ISicsProxy proxy = SicsManager.getSicsProxy();
//		proxy.addProxyListener(proxyListener);
//		
//		if (proxy.isModelAvailable()) {
//			handleSicsConnect();
//		}
	}
	
	private Label createUnitsLabel(DeviceContext deviceContext) {
		if (deviceContext.unit != null) {
			return getWidgetFactory().createLabel(this, deviceContext.unit, SWT.LEFT);
		} else {
			return createUnitsLabel(this, deviceContext.path + "?units", "", SWT.LEFT);
		}
	}

//	private DeviceContext getDeviceContext(String path) {
//		if (deviceContexts == null) {
//			return null;
//		}
//		for (DeviceContext context : deviceContexts){
//			if (!context.isSeparator && context.path.equals(path)) {
//				return context;
//			}
//		}
//		return null;
//	}
//	
//	private LabelContext getLabelContext(String path) {
//		for (LabelContext context : labelContexts){
//			if (context.path.equals(path)) {
//				return context;
//			}
//			if (context.showSoftLimits) {
//				if ((context.path + "/softlowerlim").equals(path)){
//					return context;
//				}
//				if ((context.path + "/softupperlim").equals(path)){
//					return context;
//				}
//			}
//		}
//		return null;
//	}

	protected void updateWidgetState(final Control widget, final ControllerState state, final Color currentColor) {
		if (widget != null && !widget.isDisposed()) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (currentColor == null) {
						if (state.equals(ControllerState.IDLE)) {
							widget.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
						} else if (state.equals(ControllerState.BUSY)) {
							widget.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else if (state.equals(ControllerState.ERROR)) {
							widget.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						}
					} else {
						widget.setForeground(currentColor);
					}
				}
			});
		}
	}

	@Override
	protected void handleSicsConnect() {
		if (labelContexts == null) {
			return;
		}
//		checkSicsConnection();
		try {
			for (final LabelContext labelContext : labelContexts) {
				reloadLabel(labelContext.path, 
						labelContext.label, labelContext.converter, labelContext.colorConverter);
				if (labelContext.showSoftLimits) {
					reloadLabel(labelContext.path + "/softlowerlim", 
						labelContext.lowerlimLabel, null, labelContext.colorConverter);
					reloadLabel(labelContext.path + "/softupperlim", 
							labelContext.upperlimLabel, null, labelContext.colorConverter);
					reloadLabel(labelContext.path + "/softzero", 
							labelContext.softzeroLabel, null, labelContext.colorConverter);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void handleStatusUpdate(ServerStatus status) {
	}
	
	@Override
	protected void handleModelUpdate() {
		if (labelContexts == null) {
			return;
		}
//		checkSicsConnection();
		try {
			for (final LabelContext labelContext : labelContexts) {
				if (labelContext.listener != null) {
					labelContext.listener.updateEnabled(false);
				}
				labelContext.listener = initiateLabel(labelContext.path, 
						labelContext.label, labelContext.converter, labelContext.colorConverter);
				if (labelContext.showSoftLimits) {
					if (labelContext.lowerlimListener != null) {
						labelContext.lowerlimListener.updateEnabled(false);
					}
					if (labelContext.upperlimListener != null) {
						labelContext.upperlimListener.updateEnabled(false);
					}
					if (labelContext.softzeroListener != null) {
						labelContext.softzeroListener.updateEnabled(false);
					}
					labelContext.lowerlimListener = initiateLabel(labelContext.path + "/softlowerlim", 
						labelContext.lowerlimLabel, null, labelContext.colorConverter);
					labelContext.upperlimListener = initiateLabel(labelContext.path + "/softupperlim", 
							labelContext.upperlimLabel, null, labelContext.colorConverter);
					labelContext.softzeroListener = initiateLabel(labelContext.path + "/softzero", 
							labelContext.softzeroLabel, null, labelContext.colorConverter);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reloadLabel(String path, Label label, LabelConverter converter, ColorConverter colorConverter) {
		ISicsController controller = SicsManager.getSicsModel().findControllerByPath(path);
		if (controller != null && controller instanceof IDynamicController) {
			String data;
			try {
				data = String.valueOf(((IDynamicController) controller).getValue());
				updateLabelText(label, data, converter);
			} catch (SicsModelException e) {
			}
		}
	}
	
	private ISicsControllerListener initiateLabel(String path, Label label, LabelConverter converter, ColorConverter colorConverter) {
		ISicsController controller = SicsManager.getSicsModel().findControllerByPath(path);
		if (controller != null && controller instanceof IDynamicController) {
			String data;
			try {
				data = String.valueOf(((IDynamicController) controller).getValue());
				updateLabelText(label, data, converter);
			} catch (SicsModelException e) {
			}
			return createListener(path, label, converter, colorConverter);
		}
		return null;
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
				ControllerStatusWidget.this.layout(true, true);
			}
		});
	}

	protected void sortDevices() {
		Collections.sort(deviceContexts);
	}

	@Override
	protected void disposeWidget() {
		if (deviceContexts != null) {
			deviceContexts.clear();
			deviceContexts = null;
		}
		if (labelContexts != null) {
			for (LabelContext context : labelContexts) {
				if (context.listener != null) {
					deactiveListener(context.listener, context.path);
				}
				if (context.lowerlimListener != null) {
					deactiveListener(context.lowerlimListener, context.path + "/softlowerlim");
				}
				if (context.upperlimListener != null) {
					deactiveListener(context.lowerlimListener, context.path + "/softupperlim");
				}
				if (context.softzeroListener != null) {
					deactiveListener(context.softzeroListener, context.path + "/softzero");
				}
			}
			labelContexts.clear();
			labelContexts = null;
		}
//		if (proxyListener != null) {
//			SicsManager.getSicsProxy().removeProxyListener(proxyListener);
//			proxyListener = null;
//		}
		dataAccessManager = null;
		delayEventExecutor = null;
		super.disposeWidget();
	}

	/*************************************************************************
	 * Public API
	 *************************************************************************/

	public ControllerStatusWidget addDevice(String path, String label) {
		return addDevice(path, label, null, null, null);
	}

	public ControllerStatusWidget addDevice(String path, String label, Image icon) {
		return addDevice(path, label, icon, null, null);
	}
	
	public ControllerStatusWidget addDevice(String path, String label, Image icon,
			String unit) {
		return addDevice(path, label, icon, unit, null);
	}
	
	public ControllerStatusWidget addDevice(String path, String label, Image icon,
			String unit, LabelConverter converter) {
		return addDevice(path, label, icon, unit, converter, false);
	}
	
	public ControllerStatusWidget addDevice(String path, String label, Image icon,
			String unit, LabelConverter converter, boolean showSoftLimits) {
		return addDevice(path, label, icon, unit, converter, false, null);
	}
	
	public ControllerStatusWidget addDevice(String path, String label, Image icon,
			String unit, LabelConverter converter, boolean showSoftLimits, ColorConverter colorConverter) {
		DeviceContext context = new DeviceContext();
		context.path = path;
		context.icon = icon;
		context.label = label;
		context.unit = unit;
		context.isSeparator = false;
		context.converter = converter;
		context.showSoftLimits = showSoftLimits;
		context.colorConverter = colorConverter;
		deviceContexts.add(context);
		return this;
	}

	public String getDeviceData(String path) {
		for (LabelContext context : labelContexts) {
			if (context.path != null && context.path.equals(path)) {
				return context.data;
			}
		}
		return null;
	}
	
	public ControllerStatusWidget addSeparator() {
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

	private class DeviceContext implements Comparable<DeviceContext> {
		String path;
		Image icon;
		String label;
		Label labelWidget;
		String unit;
		boolean isSeparator;
		LabelConverter converter;
		ColorConverter colorConverter;
		boolean showSoftLimits;

		@Override
		public int compareTo(DeviceContext arg0) {
			if (label == null) {
				return -1;
			}
			return label.compareTo(arg0.label);
		}
	}

	private class LabelContext {
		String path;
		Label label;
		String data;
		String defaultText;
		ISicsControllerListener listener;
		LabelConverter converter;
		boolean showSoftLimits;
		Label lowerlimLabel;
		ISicsControllerListener lowerlimListener;
		Label upperlimLabel;
		ISicsControllerListener upperlimListener;
		Label softzeroLabel;
		ISicsControllerListener softzeroListener;
		ColorConverter colorConverter;
	}

	public interface LabelConverter {
		String convertValue(Object obj);
	}

	public interface ColorConverter {
		Color convertColor(Object obj);
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
	
	
	class SicsControllerListener implements ISicsControllerListener {
		
		Label widget;
		LabelConverter converter;
		ColorConverter colorConverter;
		Color currentColor;
		boolean isEnabled;
		
		public SicsControllerListener(Label widget, LabelConverter converter, ColorConverter colorConverter) {
			this.widget = widget;
			this.converter = converter;
			this.colorConverter = colorConverter;
			this.isEnabled = true;
		}
		
		@Override
		public void updateValue(Object oldValue, Object newValue) {
			if (isEnabled) {
				updateLabelText(widget, String.valueOf(newValue), converter);
				if (colorConverter != null) {
					currentColor = colorConverter.convertColor(newValue);
					updateWidgetState(widget, null, currentColor);
				}
			}
		}
		
		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
			if (isEnabled) {
				updateWidgetState(widget, newState, currentColor);
			}
		}
		
		@Override
		public void updateEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}
		
		public void dispose() {
			this.widget = null;
			this.converter = null;
			this.colorConverter = null;
			this.isEnabled = false;
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
		labelContexts.add(context);
		return label;
	}

	private ISicsControllerListener createListener(String path, Label label, LabelConverter converter, ColorConverter colorConverter) {
		ISicsControllerListener listener = new SicsControllerListener(label, converter, colorConverter);
		ISicsController controller = SicsManager.getSicsModel().findControllerByPath(path);
		if (controller != null) {
			controller.addControllerListener(listener);
		}
		return listener;
	}
	
	private void deactiveListener(ISicsControllerListener listener, String path) {
		if (listener != null) {
			ISicsController controller = SicsManager.getSicsModel().findControllerByPath(path);
			if (controller != null) {
				controller.removeControllerListener(listener);
			}	
		}
	}
	
	private Label createDeviceLabel(Composite parent, String path,
			String defaultText, int style, LabelConverter converter, boolean showSoftLimits, 
			ColorConverter colorConverter) {
		Label label = getWidgetFactory()
				.createLabel(parent, defaultText, style);
		LabelContext context = new LabelContext();
		context.path = path;
		context.label = label;
		context.showSoftLimits = showSoftLimits;
		context.converter = converter;
		context.colorConverter = colorConverter;
		if (showSoftLimits) {
			Label lowerlimLabel = getWidgetFactory().createLabel(parent, "");
			context.lowerlimLabel = lowerlimLabel;
			Label upperlimLabel = getWidgetFactory().createLabel(parent, "");
			context.upperlimLabel = upperlimLabel;
			Label softzeroLabel = getWidgetFactory().createLabel(parent, "");
			context.softzeroLabel = softzeroLabel;
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(context.label);
		} else {
			GridDataFactory.swtDefaults().span(4, 1).align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(context.label);
		}
		context.defaultText = defaultText;
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
							if (text != null && text.length() > 24) {
								text = text.substring(0, 24);
							}
						} 
					} else {
						text = converter.convertValue(data);
					}
					label.setText(text);
					label.setToolTipText(data);
					// TODO: does it have any performance hit?
//					label.getParent().layout(true, true);
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

//	protected void checkSicsConnection() {
//		int counter = 0;
//		ISicsController[] controllers = SicsManager.getSicsModel().getSicsControllers();
//		if (counter <= SICS_CONNECTION_TIMEOUT && (controllers == null || controllers.length == 0)) {
//			try {
//				Thread.sleep(500);
//				counter += 500;
//			} catch (InterruptedException e) {
//			}
//		}
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//		}
//	}
	
	public void setDeviceTitle(String path, String newTitle) {
		for (final DeviceContext device : deviceContexts) {
			if (device.path.equals(path)) {
				device.label = newTitle;
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						if (device.labelWidget != null && !device.labelWidget.isDisposed()) {
							device.labelWidget.setText(device.label);
							device.labelWidget.pack();
						}
					}
				});
			}
		}
	}

	protected void disposeAllDevices() {
		if (deviceContexts == null) {
			return;
		}
		try {
			for (final LabelContext labelContext : labelContexts) {
				if (labelContext.listener != null) {
					((SicsControllerListener) labelContext.listener).dispose();
				}
				if (labelContext.showSoftLimits) {
					if (labelContext.lowerlimListener != null) {
						((SicsControllerListener) labelContext.lowerlimListener).dispose();
					}
					if (labelContext.upperlimListener != null) {
						((SicsControllerListener) labelContext.upperlimListener).dispose();
					}
					if (labelContext.softzeroListener != null) {
						((SicsControllerListener) labelContext.softzeroListener).dispose();
					}
				}
			}
			labelContexts.clear();
			SafeUIRunner.asyncExec(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					for (Control child : getChildren()) {
						child.dispose();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
