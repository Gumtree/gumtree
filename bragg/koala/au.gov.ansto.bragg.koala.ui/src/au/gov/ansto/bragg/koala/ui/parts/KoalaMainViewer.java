/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * @author nxi
 *
 */
public class KoalaMainViewer extends Composite {

	private HeaderPart headerPart;
	private MainPart mainPart;
	private FooterPart footerPart;
	/**
	 * @param parent
	 * @param style
	 */
	public KoalaMainViewer(Composite parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(this);

		headerPart = new HeaderPart(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(getHeaderPart());
		
		mainPart = new MainPart(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainPart);
		
		footerPart = new FooterPart(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(getFooterPart());
		
		mainPart.showProposalPanel();
	}

	public MainPart getMainPart() {
		return mainPart;
	}

	public HeaderPart getHeaderPart() {
		return headerPart;
	}

	public FooterPart getFooterPart() {
		return footerPart;
	}

}
