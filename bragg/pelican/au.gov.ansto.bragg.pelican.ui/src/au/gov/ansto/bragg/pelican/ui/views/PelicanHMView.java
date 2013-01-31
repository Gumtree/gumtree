/**
 * 
 */
package au.gov.ansto.bragg.pelican.ui.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;

import au.gov.ansto.bragg.nbi.ui.widgets.HMImageDisplayWidget;

/**
 * @author nxi
 *
 */
public class PelicanHMView extends ViewPart {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		HMImageDisplayWidget widget = new HMImageDisplayWidget(parent, SWT.NONE);
//		String path = "http://localhost:60030/admin/openimageinformat.egi&#63;type=HISTOPERIOD_XYT&#38;open_format=DISLIN_PNG&#38;open_colour_table=RAIN&#38;open_plot_zero_pixels=AUTO&#38;open_annotations=ENABLE";
		String path = "http://localhost:60030/admin/openimageinformat.egi?type=HISTOPERIOD_XYT&open_format=DISLIN_PNG&open_colour_table=RAIN&open_plot_zero_pixels=AUTO&open_annotations=ENABLE";
		widget.setDataURI(path);
		IParameters para = new Parameters();
		para.put("login", "manager");
		para.put("password", "ansto");
		para.put("refreshDelay", 5000);
		widget.setParameters(para);
		GridDataFactory.fillDefaults().applyTo(widget);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
