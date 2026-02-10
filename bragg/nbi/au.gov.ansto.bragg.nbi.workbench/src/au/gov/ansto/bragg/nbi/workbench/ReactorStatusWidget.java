/**
 * 
 */
package au.gov.ansto.bragg.nbi.workbench;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.util.UIResources;
import org.w3c.dom.Element;

import au.gov.ansto.bragg.nbi.service.soap.NeutronSourceSOAPService;
import au.gov.ansto.bragg.nbi.service.soap.NeutronSourceSOAPService.ISoapEventListener;

/**
 * @author nxi
 *
 */
public class ReactorStatusWidget extends ExtendedComposite {

	private boolean isExpandingEnabled = true;
	private ISoapEventListener soapEventListener;
	
	private List<DeviceContext> devices;
	/**
	 * @param parent
	 * @param style
	 */
	public ReactorStatusWidget(Composite parent, int style) {
		super(parent, style);
		devices = new ArrayList<DeviceContext>();
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);
//		createWidgetArea();
	}

	public void createWidgetArea() {
		for (DeviceContext device : devices) {
			// Part 2: label
			Label label = getWidgetFactory().createLabel(this, device.title);
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
			// Part 3: Value
			label = getWidgetFactory().createLabel(this, "--");
			GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(true, false).applyTo(label);
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
			device.label = label;
			// Part 4: Separator
			//		String labelSep = (deviceContext.unit == null) ? "" : " ";
			//		label = getWidgetFactory().createLabel(this, labelSep);
			label = getWidgetFactory().createLabel(this, device.units);
		}
		NeutronSourceSOAPService.addEventListener(new ISoapEventListener() {
			
			@Override
			public void post(Element element) {
				if (!isDisposed()) {
					updateValue(element);
				}
			}
		});
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				NeutronSourceSOAPService.removeEventListener(soapEventListener);
			}
		});
	}


	private void updateValue(Element element) {
			try{
//				SOAPPart soapPart = message.getSOAPPart();
//				final Element ele = soapPart.getDocumentElement();
//				setValueLabel(ele);
				setValueLabel(element);
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {

						Composite parent = getParent();
						if (parent instanceof PGroup) {
							if (isExpandingEnabled() && !((PGroup) parent).getExpanded()) {
								((PGroup) parent).setExpanded(true);
							}
						}
					}
				});

			} catch (Exception e) {
				setValueLabel(null);
			}
	}
	
	private void setValueLabel(Element element) {
		if (element == null || isDisposed()) {
			return;
		}
		for (final DeviceContext device : devices) {
			String content = null;
			try{
				content = element.getElementsByTagName("ns1:" + device.id).item(0).getTextContent();
				try {
					double value = Double.valueOf(content);
					content = String.format("%.3f", value);
				} catch (Exception e) {
				}
			}catch (Exception ex) {
			}
			if (content != null) {
				final String text = content;
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (device.label != null && !device.label.isDisposed()) {
							device.label.setText(text);
						}
					}
				});
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.data.ui.viewers.ExtendedComposite#disposeWidget()
	 */
	@Override
	protected void disposeWidget() {
		NeutronSourceSOAPService.removeEventListener(soapEventListener);
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

	class DeviceContext {
		String id;
		String title;
		Label label;
		String units;
		boolean isSeparator;
	}
	
	public ReactorStatusWidget addDevice(String id, String title, String units){
		DeviceContext device = new DeviceContext();
		device.id = id;
		device.title = title;
		device.units = units;
		devices.add(device);
		return this;
	}
}
