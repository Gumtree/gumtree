/**
 * 
 */
package au.gov.ansto.bragg.pelican.workbench.internal;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;

import au.gov.ansto.bragg.nbi.ui.widgets.HMImageDisplayWidget;

/**
 * @author nxi
 *
 */
@SuppressWarnings("restriction")
public class PelicanHMView extends ViewPart {

	private static final String DAE_HOST_NAME = "gumtree.dae.host";
	private static final String DAE_PORT_NUMBER = "gumtree.dae.port";
	private static final String DAE_HM_REFRESHTIME = "gumtree.hm.refreshperiod";
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {
		final HMImageDisplayWidget widget = new HMImageDisplayWidget(parent, SWT.NONE);
//		String path = "http://localhost:60030/admin/openimageinformat.egi&#63;type=HISTOPERIOD_XYT&#38;open_format=DISLIN_PNG&#38;open_colour_table=RAIN&#38;open_plot_zero_pixels=AUTO&#38;open_annotations=ENABLE";
		String host = "das1-pelican.nbi.ansto.gov.au";
		try {
			host = System.getProperty(DAE_HOST_NAME);
		} catch (Exception e) {
		}
		String port = "8081";
		try {
			port = System.getProperty(DAE_PORT_NUMBER);
		} catch (Exception e) {
		}
		String path = "http://" + host + ":" + port + "/admin/openimageinformat.egi?open_format=DISLIN_PNG&open_colour_table=RAIN&open_plot_zero_pixels=AUTO";
		widget.setDataURI(path);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		IParameters para = new Parameters();
		para.put("login", "manager");
		para.put("password", "ansto");
		float period = 60;
		try {
			period = Float.valueOf(System.getProperty(DAE_HM_REFRESHTIME));
		} catch (Exception e) {
		}
		para.put("refreshDelay", period * 1000);
		widget.setParameters(para);
		widget.setRefreshDelay(period);
//		widget.setWidth(240);
//		widget.setHeight(120);
		widget.afterParametersSet();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(widget);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
