/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.koala.ui.scan.AbstractScanModel;

/**
 * @author nxi
 *
 */
public class ChemistryPanel extends AbstractExpPanel {

	/**
	 * @param parent
	 * @param style
	 */
	public ChemistryPanel(Composite parent, int style, MainPart part) {
		super(parent, style, part);
	}

	@Override
	protected AbstractScanModel getModel() {
		return mainPart.getChemistryModel();
	}

}
