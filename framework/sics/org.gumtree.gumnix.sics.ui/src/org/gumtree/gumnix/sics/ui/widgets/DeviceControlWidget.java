package org.gumtree.gumnix.sics.ui.widgets;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.dataaccess.SicsDataAccessUtils;
import org.gumtree.gumnix.sics.internal.ui.InternalImage;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * How does this widget works?
 * 
 * This widget is a SWT custom custom with connectivity to SICS and Data Access
 * Manager. It is also configurable via the IConfigurable interface.
 * 
 * Initially this widget is empty (init() is not set as deviceURIs is not available).
 * Once the deviceURIs is set, user need to all postSetup() to initialse the connectivity.
 * When SICS is connected and ready, it will retrieve dynamic controllers and create UI.
 * 
 */
public class DeviceControlWidget extends AbstractSicsDeviceWidget  {

	private static final Logger logger = LoggerFactory.getLogger(DeviceControlWidget.class);
		
	private static final String KEY_UNBINDABLE = "unbindable";
	
	private List<URI> lockedDeviceURIs = new ArrayList<URI>();
		
	private Map<URI, Context> contexts = new LinkedHashMap<URI, Context>();
	
	private List<String> customLabels = new ArrayList<String>();
	
	private boolean showLock = true;
	
	private boolean showMin = true;
	
	private boolean showMax = true;
	
	private boolean showTarget = true;
	
	private boolean showUnit = true;
	
	public DeviceControlWidget(Composite parent, int style) {
		super(parent, style);
	}
	
	protected void unbindProxy() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				// Clear value and controllers on unbinding
				for (Object widget : uriMap.values()) {
					if (widget instanceof Control) {
						if (Boolean.TRUE.equals(((Control) widget).getData(KEY_UNBINDABLE))) {
							updateWidgetData(null, widget, "");
						}
					}
				}
			}
		});
		super.unbindProxy();
	}
	
	protected void initialise() {
		contexts.clear();
		// Find controllers for drive
		for (URI uri : deviceURIs) {
			Context context = new Context();
			contexts.put(uri, context);
			try {
				context.controller = getDataAccessManager().get(uri,
						IDynamicController.class);
				if (context.controller != null) {
					context.shouldRender = true;
				}
			} catch (Exception e) {
				// Does not render missing controllers
			}
		}
	}
	
	protected void createUI() {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		for (Control child : this.getChildren()) {
			child.dispose();
		}
		setEnabled(true);
		
		/*********************************************************************
		 * Create UI
		 *********************************************************************/
		int numColumns = 7;
		if (!isShowMin()) {
			numColumns--;
		}
		if (!isShowMax()) {
			numColumns--;
		}
		if (!isShowTarget()) {
			numColumns--;
		}
		if (!isShowUnit()) {
			numColumns--;
		}
		if (!isShowLock()) {
			numColumns--;
		}
		GridLayoutFactory.swtDefaults().numColumns(numColumns).margins(0, 0).spacing(10, 2).applyTo(this);
		
		// Header
		Label label = getToolkit().createLabel(this, "Device");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
//		GridDataFactory.swtDefaults().hint(40, SWT.DEFAULT).applyTo(label);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
		if (isShowMin()) {
			label = getToolkit().createLabel(this, "Min");
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
//			GridDataFactory.swtDefaults().hint(30, SWT.DEFAULT).applyTo(label);
			GridDataFactory.swtDefaults().applyTo(label);
		}
		if (isShowMax()) {
			label = getToolkit().createLabel(this, "Max");
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
//			GridDataFactory.swtDefaults().hint(30, SWT.DEFAULT).applyTo(label);
			GridDataFactory.swtDefaults().applyTo(label);
		}
		label = getToolkit().createLabel(this, "Current");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
//		GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(label);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
		if (isShowTarget()) {
			label = getToolkit().createLabel(this, "Target");
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
//			GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(label);
			GridDataFactory.swtDefaults().applyTo(label);
		}
		if (isShowUnit()) {
			label = getToolkit().createLabel(this, "Unit");
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
//			GridDataFactory.swtDefaults().hint(40, SWT.DEFAULT).applyTo(label);
			GridDataFactory.swtDefaults().applyTo(label);
		}
		if (isShowLock()) {
			label = getToolkit().createLabel(this, "");
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
//			GridDataFactory.swtDefaults().hint(30, SWT.DEFAULT).applyTo(label);
			GridDataFactory.swtDefaults().applyTo(label);
		}
		
		label = getToolkit().createLabel(this, "", SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).span(numColumns, 1).applyTo(label);
		
		// Setup context
		int index = 0;
		for (URI uri : deviceURIs) {
			final Context context = contexts.get(uri);
			if (!context.shouldRender) {
				continue;
			}
			
			// Device name
			URI attributeURI = SicsDataAccessUtils.createControllerLabelURI(uri);
			URI statusURI = SicsDataAccessUtils.createControllerStatusURI(uri);
			Label deviceLabel = createLabel(this, attributeURI, 30, SWT.FILL);
			deviceLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			if (index < customLabels.size() && customLabels.get(index).length() > 0) {
				// Use custom label
				deviceLabel.setText(customLabels.get(index));
			} else {
				// Use SICS device name
				uriMap.put(attributeURI, deviceLabel);
			}
			index++;
			uriMap.put(statusURI, deviceLabel);
			
			// Min
			if (isShowMin()) {
				attributeURI = URI.create(uri.toString() + "/softlowerlim");
				label = createLabel(this, attributeURI, 30, SWT.FILL);
				label.setData(KEY_UNBINDABLE, true);
				uriMap.put(attributeURI, label);
			}
			
			// Max
			if (isShowMax()) {
				attributeURI = URI.create(uri.toString() + "/softupperlim");
				label = createLabel(this, attributeURI, 30, SWT.FILL);
				label.setData(KEY_UNBINDABLE, true);
				uriMap.put(attributeURI, label);
			}
			
			// Current
			attributeURI = uri;
			Label currentLabel = createLabel(this, attributeURI, 50, SWT.END);
			currentLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			currentLabel.setData(KEY_UNBINDABLE, true);
			uriMap.put(attributeURI, currentLabel);
			
			// Target
			if (isShowTarget()) {
				context.targetText = getToolkit().createText(this, "", SWT.BORDER);
				context.targetText.setData(KEY_UNBINDABLE, true);
				GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(50, SWT.DEFAULT).applyTo(context.targetText);
//				GridDataFactory.fillDefaults().applyTo(context.targetText);
				context.targetUri = URI.create(uri.toString() + "?target");
				uriMap.put(context.targetUri, context.targetText);
				context.targetText.addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent e) {
						if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
							driveDevice(context);
						}
					}
				});
			}
			
			// Unit
			if (isShowUnit()) {
				attributeURI = URI.create(uri.toString() + "?units");
				uriMap.put(attributeURI, createLabel(this, attributeURI, 40, SWT.FILL));
			}
			
			// Lock
			if (!showLock) {
				continue;
			}
			context.lockButton = getToolkit().createButton(this, "", SWT.PUSH);
			context.lockButton.setImage(InternalImage.UNLOCK.getImage());
			if (lockedDeviceURIs.contains(uri)) {
				context.lockButton.setImage(InternalImage.LOCK.getImage());
				context.lockButton.setData("lock", true);
				context.targetText.setEnabled(false);
			} else {
				context.lockButton.setImage(InternalImage.UNLOCK.getImage());
				context.lockButton.setData("lock", false);
				context.targetText.setEnabled(true);
			}
			context.lockButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (!(Boolean) context.lockButton.getData("lock")) {
						context.lockButton.setImage(InternalImage.LOCK.getImage());
						context.lockButton.setData("lock", true);
						context.targetText.setEnabled(false);
					} else {
						context.lockButton.setImage(InternalImage.UNLOCK.getImage());
						context.lockButton.setData("lock", false);
						context.targetText.setEnabled(true);
					}
				}
			});
			
		}
		
		// Fetch initial value
		for (URI uri : uriMap.keySet()) {
			try {
				if ("status".equals(uri.getQuery())) {
					updateWidgetData(uri, uriMap.get(uri),
							getDataAccessManager().get(uri, ControllerStatus.class));
				} else {
					updateWidgetData(uri, uriMap.get(uri),
							getDataAccessManager().get(uri, String.class));
				}
			} catch (Exception e) {
				// Nothing wrong sometimes
			}
		}

		this.getParent().layout(true, true);
	}
	
	private Label createLabel(Composite parent, URI uri, int width, int alignment) {
		Label label = getToolkit().createLabel(this, "");
//		GridDataFactory.swtDefaults().hint(width, SWT.DEFAULT).applyTo(label);
		GridDataFactory.swtDefaults().align(alignment, SWT.CENTER).applyTo(label);
//		uriMap.put(uri, label);
		return label;
	}
	
	private void driveDevice(Context context) {
		IDynamicController controller = context.controller;
		if (controller != null) {
			controller.setTargetValue(ComponentData.createData(context.targetText.getText()));
			try {
				controller.commitTargetValue(null);
			} catch (SicsIOException e1) {
				logger.error("Failed to drive." + e1);
			}
		}
	}
	
	protected void widgetDispose() {
		if (contexts != null) {
			contexts.clear();
			contexts = null;
		}
		if (lockedDeviceURIs != null) {
			lockedDeviceURIs.clear();
			lockedDeviceURIs = null;
		}
		if (customLabels != null) {
			customLabels.clear();
			customLabels = null;
		}
	}


	@Override
	protected void updateWidgetData(URI uri, Object widget, Object data) {
		if (data instanceof String) {
			if (widget instanceof Label) {
				Label label = (Label) widget; 
				label.setText((String) data);
				label.getParent().layout(new Control[] { label });
			} else if (widget instanceof Text) {
				((Text) widget).setText((String) data);
			}
		} else if (data instanceof ControllerStatus && widget instanceof Control) {
			ControllerStatus status = (ControllerStatus) data;
			Control control = (Control) widget;
			if (status.equals(ControllerStatus.OK)) {
				control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			} else if (status.equals(ControllerStatus.RUNNING)) {
				control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			} else if (status.equals(ControllerStatus.ERROR)) {
				control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			}
		}
	}
	
	public void setDeviceURIs(String uris) {
		String[] uriList = uris.split(",");
		deviceURIs = new ArrayList<URI>();
		for (String uri : uriList) {
			deviceURIs.add(URI.create(uri.trim()));
		}
	}
	
	public void setLockedDeviceURIs(String uris) {
		String[] uriList = uris.split(",");
		lockedDeviceURIs = new ArrayList<URI>();
		for (String uri : uriList) {
			lockedDeviceURIs.add(URI.create(uri.trim()));
		}
	}
	
	public boolean isShowLock() {
		return showLock;
	}
	
	public void setShowLock(boolean showLock) {
		this.showLock = showLock;
	}
	
	public boolean isShowMin() {
		return showMin;
	}

	public void setShowMin(boolean showMin) {
		this.showMin = showMin;
	}

	public boolean isShowMax() {
		return showMax;
	}

	public void setShowMax(boolean showMax) {
		this.showMax = showMax;
	}

	public boolean isShowTarget() {
		return showTarget;
	}

	public void setShowTarget(boolean showTarget) {
		this.showTarget = showTarget;
	}

	public boolean isShowUnit() {
		return showUnit;
	}

	public void setShowUnit(boolean showUnit) {
		this.showUnit = showUnit;
	}
	
	public void setLabels(String labels) {
		String[] labelList = labels.split(",");
		customLabels = new ArrayList<String>();
		for (String label : labelList) {
			customLabels.add(label.trim());
		}
	}
	
	private class Context {
		private boolean shouldRender;
		private Text targetText;
		private Button lockButton;
		private IDynamicController controller;
		private URI targetUri;
	}
	
}
