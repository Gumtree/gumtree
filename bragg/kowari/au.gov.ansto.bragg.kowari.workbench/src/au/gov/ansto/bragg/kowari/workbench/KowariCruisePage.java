package au.gov.ansto.bragg.kowari.workbench;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

public class KowariCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Kowari";
	}

	@Override
	public Composite create(Composite parent) {
		return new KowariCruisePageWidget(parent, SWT.NONE);
	}

}
