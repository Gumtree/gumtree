package au.gov.ansto.bragg.nbi.workbench.internal;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.support.CruisePanel;

public class NBICruisePanel extends CruisePanel {

	@Inject
	public NBICruisePanel(Composite parent, @Optional int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
	}

}
