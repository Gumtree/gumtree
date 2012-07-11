/**
 * 
 */
package au.gov.ansto.bragg.quokka.ui.views;

import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView;

/**
 * @author nxi
 *
 */
public class QuokkaParametersControlView extends OperationParametersView {

	/**
	 * 
	 */
	public QuokkaParametersControlView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		setVisibility();
	}

	protected void setVisibility() {
		defaultParametersButton.setVisible(false);
		revertParametersButton.setVisible(false);
		applyParametersButton.setText("Apply");
	}

	
	
}
